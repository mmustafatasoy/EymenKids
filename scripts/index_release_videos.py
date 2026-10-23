#!/usr/bin/env python3
"""Index owned/licensed GitHub Release MP4 videos into an ordered season/episode catalog."""
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
EPISODE_FILE = re.compile(
    r"(?P<series>[a-z0-9][a-z0-9-]*)-s(?P<season>\d{2})e(?P<episode>\d{2})-(?P<slug>[a-z0-9][a-z0-9-]*)"
)
SERIES_NAMES = {
    "niloya": "Niloya", "cu-cu-cu": "Çu Çu Çu", "ari-viz-viz": "Arı Vız Vız",
    "kral-sakir": "Kral Şakir", "rafadan-tayfa": "Rafadan Tayfa",
    "bakkal-amca": "Bakkal Amca", "elifin-dusleri": "Elif'in Düşleri",
    "tavsan-momo": "Tavşan Momo", "mini-mini": "Mini Mini Bir Kuş",
    "parmak-ailesi": "Parmak Ailesi", "yaz-sarkilari": "Yaz Şarkıları"
}
EPISODE_TITLES = {"tospik": "Tospik", "ucurtma": "Uçurtma", "kar-yagiyor": "Kar Yağıyor"}

def run_command(*args):
    return subprocess.run(args, check=True, capture_output=True, text=True).stdout

def display_title(identifier):
    return EPISODE_TITLES.get(identifier, SERIES_NAMES.get(identifier, identifier.replace("-", " ").title()))

def decode_order(file_name):
    video_id = file_name[:-4]
    match = EPISODE_FILE.fullmatch(video_id)
    if match:
        series = match.group("series")
        season, episode = int(match.group("season")), int(match.group("episode"))
        if season < 1 or episode < 1:
            raise ValueError("Season/episode numbering starts at 01")
        return {
            "id": video_id,
            "series_id": series,
            "series_title": SERIES_NAMES.get(series, display_title(series)),
            "season": season,
            "episode": episode,
            "episode_title": display_title(match.group("slug")),
            "title": display_title(match.group("slug")),
        }
    return {
        "id": video_id,
        "series_id": video_id,
        "series_title": SERIES_NAMES.get(video_id, display_title(video_id)),
        "season": 1,
        "episode": 1,
        "episode_title": display_title(video_id),
        "title": display_title(video_id),
    }

def sort_key(item):
    return (
        item.get("series_id", item["id"]),
        int(item.get("season", 1)),
        int(item.get("episode", 1)),
        item["id"],
    )

def quality_of(video):
    try:
        raw = run_command("ffprobe", "-v", "error", "-select_streams", "v:0",
                          "-show_entries", "stream=height", "-of", "json", str(video))
        height = int(json.loads(raw)["streams"][0]["height"])
        return "1080p" if height >= 1080 else "720p HD" if height >= 720 else "480p" if height >= 480 else "MP4"
    except (FileNotFoundError, OSError, ValueError, IndexError, KeyError, subprocess.CalledProcessError):
        return "MP4"

def main():
    tag = os.environ.get("RELEASE_TAG", "")
    repo = os.environ.get("GITHUB_REPOSITORY", "")
    if repo != REPO:
        raise RuntimeError("Repository mismatch")
    if not SAFE_TAG.fullmatch(tag):
        print("Not a video release:", tag)
        return
    release = json.loads(run_command("gh", "api", f"repos/{repo}/releases/tags/{quote(tag, safe='')}"))
    if release.get("draft"):
        raise RuntimeError("Release is still a draft")
    assets = sorted((a for a in release.get("assets", []) if SAFE_FILE.fullmatch(a.get("name", ""))),
                    key=lambda a: a["name"])
    if not assets:
        print("No MP4 assets in this release")
        return

    current = json.loads(CATALOG.read_text(encoding="utf-8"))
    if current.get("schema_version") != 1:
        raise RuntimeError("Unexpected catalog version")
    items = {row["id"]: row for row in current["videos"]}

    with tempfile.TemporaryDirectory() as folder:
        for asset in assets:
            name = asset["name"]
            meta = decode_order(name)
            run_command("gh", "release", "download", tag, "--repo", repo,
                        "--pattern", name, "--dir", folder)
            video_file = Path(folder) / name
            size = video_file.stat().st_size
            if size <= 0 or size >= 2 * 1024 * 1024 * 1024:
                raise RuntimeError("Invalid video file size: " + name)
            with video_file.open("rb") as stream:
                sha = hashlib.file_digest(stream, "sha256").hexdigest()
            meta.update({
                "description": meta["series_title"] + " • " + (
                    "Sezon " + str(meta["season"]) + ", Bölüm " + str(meta["episode"])),
                "quality": quality_of(video_file),
                "size_bytes": size,
                "sha256": sha,
                "url": f"https://github.com/{repo}/releases/download/{quote(tag, safe='')}/{quote(name, safe='')}",
            })
            items[meta["id"]] = meta
            print(f"Episode: {meta['series_id']} S{meta['season']:02d}E{meta['episode']:02d} - {meta['title']}")
    catalog = {"schema_version": 1, "videos": sorted(items.values(), key=sort_key)}
    CATALOG.write_text(json.dumps(catalog, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print("Video list ready:", len(catalog["videos"]))

if __name__ == "__main__":
    main()
