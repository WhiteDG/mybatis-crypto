# 论坛爬虫（Python）

这个脚本会根据你输入的论坛 URL，抓取同域名页面并提取帖子信息，保存为 JSON。
同时会把报错信息保存到本地 JSON 文件。

## 1. 安装依赖

```bash
pip install requests beautifulsoup4
```

## 2. 运行方式

```bash
python3 forum_crawler.py "https://example-forum.com" \
  --output output/forum_posts.json \
  --error-output output/crawl_errors.json \
  --max-pages 30
```

## 3. 输出说明

- `output/forum_posts.json`
  - `seed_url`: 输入的论坛地址
  - `visited_pages`: 抓取页面数
  - `post_count`: 提取到的帖子数
  - `posts`: 帖子列表（url、标题、作者、时间、内容预览、标签）

- `output/crawl_errors.json`
  - `seed_url`: 输入的论坛地址
  - `error_count`: 报错数量
  - `errors`: 报错列表（url、error）

## 4. 说明

- 这是通用论坛爬虫，使用启发式规则提取内容，不同论坛结构可能需要微调。
- 请先确认目标网站允许爬取（遵守 robots 和站点条款）。
