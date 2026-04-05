#!/usr/bin/env python3
"""Hôte natif Chrome/Brave pour lancer ffmpeg depuis une extension."""

from __future__ import annotations

import json
import os
import struct
import subprocess
import sys
from datetime import datetime
from pathlib import Path
from urllib.parse import urlparse

DEFAULT_OUTPUT_DIR = Path.home() / "Videos"


def read_native_message() -> dict:
    raw_length = sys.stdin.buffer.read(4)
    if len(raw_length) == 0:
        sys.exit(0)
    if len(raw_length) != 4:
        raise RuntimeError("Message natif incomplet.")

    message_length = struct.unpack("=I", raw_length)[0]
    message = sys.stdin.buffer.read(message_length).decode("utf-8")
    return json.loads(message)


def send_native_message(payload: dict) -> None:
    encoded = json.dumps(payload).encode("utf-8")
    sys.stdout.buffer.write(struct.pack("=I", len(encoded)))
    sys.stdout.buffer.write(encoded)
    sys.stdout.buffer.flush()


def build_output_path(url: str, output_dir: Path) -> Path:
    parsed = urlparse(url)
    safe_host = parsed.netloc.replace(":", "_") or "video"
    timestamp = datetime.utcnow().strftime("%Y%m%d-%H%M%S")
    filename = f"{safe_host}-{timestamp}.mp4"
    return output_dir / filename


def validate_url(url: str) -> None:
    parsed = urlparse(url)
    if parsed.scheme not in {"http", "https"}:
        raise ValueError("L'URL doit être en http(s).")


def launch_ffmpeg(video_url: str, output_dir: Path) -> subprocess.Popen:
    output_dir.mkdir(parents=True, exist_ok=True)
    output_path = build_output_path(video_url, output_dir)

    cmd = [
        "ffmpeg",
        "-y",
        "-i",
        video_url,
        "-c",
        "copy",
        str(output_path),
    ]

    return subprocess.Popen(
        cmd,
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
        start_new_session=True,
        env=os.environ.copy(),
    )


def main() -> None:
    try:
        request = read_native_message()

        if request.get("action") != "download":
            raise ValueError("Action non supportée.")

        url = (request.get("url") or "").strip()
        if not url:
            raise ValueError("URL absente.")

        validate_url(url)

        output_dir_raw = (request.get("outputDir") or "").strip()
        output_dir = Path(output_dir_raw).expanduser() if output_dir_raw else DEFAULT_OUTPUT_DIR

        launch_ffmpeg(url, output_dir)
        send_native_message(
            {
                "ok": True,
                "message": f"ffmpeg lancé. Dossier de sortie: {output_dir}",
            }
        )
    except Exception as error:  # pylint: disable=broad-except
        send_native_message({"ok": False, "message": str(error)})


if __name__ == "__main__":
    main()
