#!/usr/bin/env python3
"""Automatically index authorized MP4 assets published in Eymen Kids Releases."""
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
from urllib.parse import quote

REPO = "mmustafatasoy/EymenKids"
CATALOG = Path("video-catalog/catalog.json")
SAFE_TAG = re.compile(r"kids-videos-[a-z0-9][a-z0-9._-]{0,80}")
SAFE_FILE = re.compile(r"([a-z0-9][a-z0-9-]{0,63})\.mp4")
SPECIAL_NAMES = {
    "cu-cu-cu": "Çu Çu Çu",
    "bakkal-amca": "Bakkal Amca",
    "ari-viz-viz": "Arı Vız Vız",
}

def run_command(*args):
    return subprocess.run(args, check=True, capture_output=True, text=True).stdout

def quality_of(video):
    try:
        raw = run_command("ffprobe", "-v", "error", "-select_streams", "v:0",
                          "-show_entries", "stream=height", "-of", "json", str(video))
        height = int(json.loads(raw)["streams"][0]["height"])
        return ("1080p" if height >= 1080 else "720p HD" if height >= 720
                else "480p" if height >= 480 else "MP4")
    except (FileNotFoundError, OSError, ValueError, IndexError, KeyError,
            subprocess.CalledProcessError):
        return "MP4"

def main():
    tag = os.environ.get("RELEASE_TAG", "")
    repo = os.environ.get("GITHUB_REPOSITORY", "")
    if repo != REPO:
        raise RuntimeError("Repository mismatch; refused to index media")
    if not SAFE_TAG.fullmatch(tag):
        print(f"Skipping non-video release {tag!r}")
        return
    release = json.loads(run_command("gh", "api",
        f"repos/{repo}/releases/tags/{quote(tag, safe='')}"))
    if release.get("draft"):
        raise RuntimeError("Release must be published first")
    assets = [a for a in release.get("assets", [])
              if SAFE_FILE.fullmatch(a.get("name", ""))]
    if not assets:
        print("No valid MP4 assets attached to release; catalog unchanged")
        return

    current = json.loads(CATALOG.read_text(encoding="utf-8"))
    if current.get("schema_version") != 1:
        raise RuntimeError("Unexpected catalog schema")
    items = {row["id"]: row for row in current["videos"]}

    with tempfile.TemporaryDirectory() as folder:
        for asset in assets:
            name = asset["name"]
            video_id = SAFE_FILE.fullmatch(name).group(1)
            run_command("gh", "release", "download", tag, "--repo", repo,
                        "--pattern", name, "--dir", folder)
            binary_file = Path(folder) / name
            size = binary_file.stat().st_size
            if size <= 0 or size >= 2 * 1024 * 1024 * 1024:
                raise RuntimeError(f"Invalid MP4 size: {name}")
            with binary_file.open("rb") as stream:
                sha = hashlib.file_digest(stream, "sha256").hexdigest()
            title = SPECIAL_NAMES.get(video_id,
                                      video_id.replace("-", " ").title())
            items[video_id] = {
                "id": video_id,
                "title": title,
                "description": "Eymen Kids video arşivi",
                "quality": quality_of(binary_file),
                "size_bytes": size,
                "sha256": sha,
                "url": f"https://github.com/{repo}/releases/download/"
                       f"{quote(tag, safe='')}/{quote(name, safe='')}",
            }
            print(f"Ready: {name}, {size} bytes, SHA256={sha}")
    output = {"schema_version": 1, "videos": list(items.values())}
    CATALOG.write_text(
        json.dumps(output, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8")
    print(f"Catalog updated: {len(assets)} release MP4(s), {len(items)} total")

if __name__ == "__main__":
    main()
