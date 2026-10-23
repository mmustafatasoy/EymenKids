#!/usr/bin/env python3
"""Regression tests for the metadata-only GitHub Releases catalog reconciler."""
from sync_release_catalog import add_release_videos
from index_release_videos import sort_key

REPO = "mmustafatasoy/EymenKids"
HEX = "a" * 64

def asset(name, tag, digest=HEX):
    return {
        "name": name, "state": "uploaded", "size": 12345,
        "digest": "sha256:" + digest,
        "browser_download_url":
            f"https://github.com/{REPO}/releases/download/{tag}/{name}",
    }

tag = "kids-videos-8"
first = asset("niloya-s01e01-tospik.mp4", tag)
second = asset("niloya-s01e02-tospik-2.mp4", tag, digest="b"*64)
extra = asset("yeni-seri-s02e04-ozel-bolum.mp4", tag, digest="c"*64)
image = {
    "name": "niloya-s01e02-tospik-2.webp", "state": "uploaded",
    "browser_download_url":
        f"https://github.com/{REPO}/releases/download/{tag}/niloya-s01e02-tospik-2.webp",
}
release = {"tag_name": tag, "assets": [second, extra, first, image]}
items = {
    "cu-cu-cu": {
        "id": "cu-cu-cu", "series_id": "cu-cu-cu",
        "season": 1, "episode": 1, "title": "Çu Çu Çu"
    }
}
assert add_release_videos(items, release) == 3
assert len(items) == 4
assert items["niloya-s01e02-tospik-2"]["season"] == 1
assert items["niloya-s01e02-tospik-2"]["episode"] == 2
assert items["niloya-s01e02-tospik-2"]["thumbnail_url"].endswith(".webp")
assert items["yeni-seri-s02e04-ozel-bolum"]["series_id"] == "yeni-seri"
assert items["yeni-seri-s02e04-ozel-bolum"]["season"] == 2
assert items["yeni-seri-s02e04-ozel-bolum"]["episode"] == 4
assert add_release_videos(items, release) == 0, "repeated sync must be idempotent"
items["niloya-s01e01-tospik"]["title"] = "Özel düzenlenmiş Türkçe başlık"
assert add_release_videos(items, release) == 0
assert items["niloya-s01e01-tospik"]["title"] == "Özel düzenlenmiş Türkçe başlık"
bad = asset("test-s01e01-bad.mp4", tag)
bad["browser_download_url"] = "https://example.com/fake.mp4"
assert add_release_videos(items, {"tag_name": tag, "assets": [bad]}) == 0
assert "test-s01e01-bad" not in items
sorted_entries = sorted(
    [items["niloya-s01e02-tospik-2"], items["niloya-s01e01-tospik"]],
    key=sort_key,
)
assert [e["episode"] for e in sorted_entries] == [1, 2]
print("PASS: release metadata, series sorting, CDN URL trust, idempotent sync.")
