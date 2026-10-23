#!/usr/bin/env python3
import importlib.util
from pathlib import Path

source=Path(__file__).with_name("index_release_videos.py")
spec=importlib.util.spec_from_file_location("index_release_videos", source)
module=importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)

one=module.decode_order("niloya-s01e01-tospik.mp4")
two=module.decode_order("niloya-s01e02-ucurtma.mp4")
three=module.decode_order("niloya-s02e01-kar-yagiyor.mp4")
single=module.decode_order("cu-cu-cu.mp4")

assert one["series_id"]=="niloya"
assert one["season"]==1 and one["episode"]==1
assert one["episode_title"]=="Tospik"
assert single["series_id"]=="cu-cu-cu"
assert single["season"]==1 and single["episode"]==1
assert [e["id"] for e in sorted([three,two,one],key=module.sort_key)]==[
    "niloya-s01e01-tospik", "niloya-s01e02-ucurtma", "niloya-s02e01-kar-yagiyor"
]
assert module.EPISODE_FILE.fullmatch("niloya-s01e01-tospik") is not None
assert module.SAFE_FILE.fullmatch("niloya-s01e01-tospik.mp4") is not None
print("PASS: grouped releases, season/episode order, standalone video support.")
