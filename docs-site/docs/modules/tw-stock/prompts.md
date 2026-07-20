---
id: prompts
title: Prompt 範本
sidebar_label: Prompt 範本
---

# Prompt 範本

**tw-stock 是全 repo 唯一提供 `@Prompt` 的模組**，其餘七個模組都只有 `@Tool`。

`prompt/TWStockPrompt.kt` 有 5 個繁體中文投資分析範本：

- 股息投資策略
- 外資投資分析
- 投資篩選
- 市場熱點監控
- 個股趨勢分析

## 參數格式

`@PromptArg` 的 description 採用 `key:中文說明` 的列舉格式：

```kotlin
description = "high_yield:高收益／高殖利率, growth:成長／增長, timing:時機／入場時點"
```

新增 prompt 參數時沿用這個格式。
