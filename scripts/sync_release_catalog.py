#!/usr/bin/env python3
"""Idempotent, metadata-only fallback sync of published GitHub Release videos.

The normal release-published event may not run for every upload. This job
reconciles published video assets by GitHub's verified sha256 digest/byte size,
without downloading all the MP4s again.
"""
import json
import os
from pathlib import Path
import re
import subprocess

from index_release_videos import (
    CATALOG, REPO, SAFE_TAG, SAFE_FILE, decode_order, sort_key,
)

SHA = re.compile(r"sha256:([0-9a-f]{64})")
MAX_VIDEO_BYTES = 2 * 1024 * 1024 * 1024 - 1


def get_releases():
    releases = []
    for page in range(1, 51):
        result = subprocess.run(
            ["gh", "api", f"repos/{REPO}/releases?per_page=100&page={page}"],
            check=True, capture_output=True, text=True,
        )
        page_items = json.loads(result.stdout)
        if not isinstance(page_items, list):
            raise RuntimeError("GitHub Releases API did not return a list")
        if not page_items:
            break
        releases.extend(page_items)
        if len(page_items) < 100:
            break
    return sorted(
        (r for r in releases
         if not r.get("draft") and SAFE_TAG.fullmatch(r.get("tag_name", ""))),
        key=lambda r: (r.get("published_at") or "", r.get("tag_name", "")),
    )


def add_release_videos(existing, release):
    """Merge one published video release. Preserve manually edited titles."""
    changed = 0
    images = {
        a.get("name"): a.get("browser_download_url")
        for a in release.get("assets", [])
        if a.get("state") == "uploaded" and
        a.get("name", "").endswith((".webp", ".jpg", ".jpeg"))
    }
    for a in release.get("assets", []):
        name = a.get("name", "")
        if a.get("state") != "uploaded" or not SAFE_FILE.fullmatch(name):
            continue
        match = SHA.fullmatch(a.get("digest", ""))
        size = a.get("size")
        if match is None or not isinstance(size, int) or not 0 < size <= MAX_VIDEO_BYTES:
            print("Skipping asset without trusted digest or size:", name)
            continue
        url = a.get("browser_download_url", "")
        tag = release.get("tag_name", "")
        if not url.startswith(f"https://github.com/{REPO}/releases/download/{tag}/"):
            print("Skipping untrusted URL:", name)
            continue
        metadata = decode_order(name)
        video_id = metadata["id"]
        previous = existing.get(video_id)
        thumbnail = next((images[k] for k in (
            video_id + ".webp", video_id + ".jpg", video_id + ".jpeg"
        ) if k in images), None)

        if previous and (
            previous.get("sha256") == match.group(1)
            and previous.get("size_bytes") == size
            and previous.get("url") == url
        ):
            # Don't override customized names or the actual quality label.
            if thumbnail and previous.get("thumbnail_url") != thumbnail:
                previous["thumbnail_url"] = thumbnail
                changed += 1
            continue
        metadata.update({
            "description": (
                f"{metadata['series_title']} • "
                f"{metadata['season']}. Sezon, {metadata['episode']}. Bölüm"
            ),
            "quality": "MP4",
            "size_bytes": size,
            "sha256": match.group(1),
            "url": url,
        })
        if thumbnail:
            metadata["thumbnail_url"] = thumbnail
        existing[video_id] = metadata
        changed += 1
        print(f"New/update: {video_id} -> {metadata['series_id']} / "
              f"S{metadata['season']:02d} E{metadata['episode']:02d}")
    return changed


def main():
    if os.environ.get("GITHUB_REPOSITORY") != REPO:
        raise RuntimeError("Repository mismatch")
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    if catalog.get("schema_version") != 1:
        raise RuntimeError("Unknown catalog format")
    existing = {v["id"]: v for v in catalog["videos"]}
    changes = sum(add_release_videos(existing, release) for release in get_releases())
    final = {
        "schema_version": 1,
        "videos": sorted(existing.values(), key=sort_key),
    }
    output = json.dumps(final, indent=2, ensure_ascii=False) + "\n"
    if output != CATALOG.read_text(encoding="utf-8"):
        CATALOG.write_text(output, encoding="utf-8")
    print(f"Reconciled {len(existing)} videos, {changes} new/updated assets")


if __name__ == "__main__":
    main()
