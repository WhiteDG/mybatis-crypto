#!/usr/bin/env python3
"""Simple forum crawler.

Given a forum URL, this script crawls pages within the same domain,
extracts post-like content heuristically, and stores data as JSON.
It also writes crawl errors to a local JSON file.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import time
from collections import deque
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Iterable
from urllib.parse import urljoin, urlparse, urldefrag

import requests
from bs4 import BeautifulSoup


USER_AGENT = (
    "Mozilla/5.0 (compatible; ForumCrawler/1.0; +https://github.com/heizhuangzhuang)"
)


@dataclass
class CrawlError:
    url: str
    error: str


@dataclass
class PostRecord:
    url: str
    title: str
    author: str | None
    published_time: str | None
    content_preview: str
    tags: list[str]


def normalize_url(base: str, href: str) -> str:
    absolute = urljoin(base, href)
    clean, _ = urldefrag(absolute)
    return clean


def is_same_domain(seed_url: str, target_url: str) -> bool:
    return urlparse(seed_url).netloc == urlparse(target_url).netloc


def likely_forum_link(url: str) -> bool:
    lowered = url.lower()
    keywords = ["thread", "topic", "post", "forum", "discussion", "view"]
    return any(k in lowered for k in keywords)


def extract_post(soup: BeautifulSoup, page_url: str) -> PostRecord | None:
    title_tag = soup.find("h1") or soup.find("title")
    title = title_tag.get_text(" ", strip=True) if title_tag else ""

    if not title:
        return None

    author = None
    author_selectors = [
        "[class*=author]",
        "[rel=author]",
        "meta[name=author]",
    ]
    for selector in author_selectors:
        elem = soup.select_one(selector)
        if elem:
            author = elem.get("content") if elem.name == "meta" else elem.get_text(" ", strip=True)
            if author:
                break

    published_time = None
    time_elem = soup.select_one("time") or soup.select_one("meta[property='article:published_time']")
    if time_elem:
        published_time = time_elem.get("datetime") or time_elem.get("content") or time_elem.get_text(" ", strip=True)

    content_container = (
        soup.select_one("article")
        or soup.select_one("[class*=content]")
        or soup.select_one("[class*=post]")
        or soup.body
    )
    if not content_container:
        return None

    paragraphs = [p.get_text(" ", strip=True) for p in content_container.find_all(["p", "div"])[:30]]
    merged = "\n".join([p for p in paragraphs if p])
    preview = re.sub(r"\s+", " ", merged).strip()[:1000]

    if len(preview) < 40:
        return None

    tags = [t.get_text(" ", strip=True) for t in soup.select("a[rel=tag], .tag, [class*=tag]")[:10]]
    tags = [t for t in tags if t]

    return PostRecord(
        url=page_url,
        title=title,
        author=author,
        published_time=published_time,
        content_preview=preview,
        tags=tags,
    )


def crawl_forum(seed_url: str, max_pages: int, timeout: int, sleep: float) -> tuple[list[PostRecord], list[CrawlError], set[str]]:
    headers = {"User-Agent": USER_AGENT}
    queue: deque[str] = deque([seed_url])
    visited: set[str] = set()
    discovered: set[str] = set([seed_url])
    posts: list[PostRecord] = []
    errors: list[CrawlError] = []

    while queue and len(visited) < max_pages:
        current = queue.popleft()
        if current in visited:
            continue

        try:
            response = requests.get(current, headers=headers, timeout=timeout)
            response.raise_for_status()
            content_type = response.headers.get("Content-Type", "")
            if "text/html" not in content_type:
                visited.add(current)
                continue
        except Exception as exc:  # noqa: BLE001
            errors.append(CrawlError(url=current, error=str(exc)))
            visited.add(current)
            continue

        soup = BeautifulSoup(response.text, "html.parser")
        post = extract_post(soup, current)
        if post and likely_forum_link(current):
            posts.append(post)

        for link in soup.find_all("a", href=True):
            target = normalize_url(current, link["href"])
            if target in discovered:
                continue
            if not is_same_domain(seed_url, target):
                continue
            if urlparse(target).scheme not in {"http", "https"}:
                continue

            discovered.add(target)
            queue.append(target)

        visited.add(current)
        if sleep > 0:
            time.sleep(sleep)

    return posts, errors, visited


def save_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Crawl a forum and export post data as JSON.")
    parser.add_argument("url", help="Forum entry URL")
    parser.add_argument("--output", default="output/forum_posts.json", help="JSON output path")
    parser.add_argument("--error-output", default="output/crawl_errors.json", help="Error JSON path")
    parser.add_argument("--max-pages", type=int, default=30, help="Maximum number of pages to crawl")
    parser.add_argument("--timeout", type=int, default=10, help="Request timeout in seconds")
    parser.add_argument("--sleep", type=float, default=0.2, help="Sleep between requests in seconds")
    return parser


def main(argv: Iterable[str] | None = None) -> int:
    parser = build_arg_parser()
    args = parser.parse_args(argv)

    posts, errors, visited = crawl_forum(
        seed_url=args.url,
        max_pages=args.max_pages,
        timeout=args.timeout,
        sleep=args.sleep,
    )

    post_payload = {
        "seed_url": args.url,
        "visited_pages": len(visited),
        "post_count": len(posts),
        "posts": [asdict(p) for p in posts],
    }
    error_payload = {
        "seed_url": args.url,
        "error_count": len(errors),
        "errors": [asdict(e) for e in errors],
    }

    save_json(Path(args.output), post_payload)
    save_json(Path(args.error_output), error_payload)

    print(f"Crawl completed. visited={len(visited)} posts={len(posts)} errors={len(errors)}")
    print(f"Posts JSON: {args.output}")
    print(f"Error JSON: {args.error_output}")

    return 0


if __name__ == "__main__":
    sys.exit(main())
