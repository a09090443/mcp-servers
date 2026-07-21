---
paths:
  - "*/src/**"
  - "buildSrc/**"
  - "gradle/libs.versions.toml"
  - "settings.gradle.kts"
---

# 文件同步規則

**只要動到任一模組的程式碼或建置設定，收尾前務必回頭確認文件系統是否需要同步更新。** 這個 repo 的資訊分散在五層文件，程式改了卻漏改文件，會讓後續的人（與 agent）依據過期描述做事。

## 文件系統的五層

| 層級 | 位置 | 內容 | 讀者 |
|---|---|---|---|
| 根總覽 | `README.md` | 八個模組功能總覽 | 人 |
| 模組 README | `<模組>/README.md` | 該模組的工具清單、設定、用法 | 人 |
| Agent 全域指南 | `CLAUDE.md` | 專案結構、共用慣例、根層級目錄樹 | agent |
| Agent 模組規則 | `.claude/rules/<模組>.md` | 該模組的環境變數、進入點、陷阱、原始碼檔案樹 | agent |
| 文件網站 | `docs-site/docs/**` | Docusaurus，集中呈現上述資訊的可瀏覽版 | 人 |

## 收尾前的對照檢查

依「改了什麼」判斷要同步哪幾層，**不確定就實際打開對應檔案比對，不要憑印象跳過**：

| 這類變動 | 要一併確認的文件 |
|---|---|
| 新增／刪除／改名 `@Tool`、`@Prompt` | 該模組 `README.md`、`.claude/rules/<模組>.md` 的工具數量、`docs-site` 對應模組頁的工具清單 |
| 新增／刪除／移動原始碼檔案 | `.claude/rules/<模組>.md` 的「檔案結構」樹、`docs-site` 對應模組頁的目錄結構（`CLAUDE.md` 只列到模組目錄，通常不受影響） |
| 改環境變數名稱／新增必填變數 | 該模組 `README.md`、`.claude/rules/<模組>.md`「設定」段、`docs-site` 對應模組頁與 `getting-started` |
| 改回傳格式、序列化方式、既有陷阱 | `.claude/rules/<模組>.md`、`docs-site` 對應模組頁；跨模組的共通慣例才動 `CLAUDE.md`「架構要點」 |
| 改建置慣例、版本 catalog、technlogy stack | `CLAUDE.md`「共用技術棧／建置慣例」、`docs-site/docs/development.md`、`architecture.md` |
| 新增模組 | 全部五層都要建立或更新（另見 `CLAUDE.md` 的新增模組步驟） |

## 原則

- **文件與程式在同一個 commit 或同一系列提交內同步**，不要留「之後再補文件」的技術債。
- 同一份事實出現在多層時，改一處就要巡過其他層（例如工具數量同時寫在 README、rule 檔、docs-site 三處）。
- 若判斷某層確實不需更新，是正常結果——重點是**每次都要主動確認過**，而不是預設不用改。
- 拿不準是否需要動 `docs-site` 時，可用 `docusaurus` skill 或直接檢視 `docs-site/docs/` 下對應檔案。
