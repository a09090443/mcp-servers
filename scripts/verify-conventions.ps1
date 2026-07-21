#!/usr/bin/env pwsh
<#
.SYNOPSIS
    驗證此 repo 的建置與程式碼慣例（harness 原則 3：機械化強制）。

.DESCRIPTION
    對照 CLAUDE.md 記載、且目前現狀全數通過的六條不變量做斷言。
    任一條違規即以非零 exit code 結束，錯誤訊息內含修復指引。

    此腳本目前僅供「本地執行」，尚未接進 CI。日後接 CI 時，
    請把它包成 Gradle 任務或改寫為 bash，於 build 階段呼叫。

    不變量清單（全部 DERIVE 自現有一致慣例，非發明）：
      1. 模組間無跨模組 project(":...") 相依
      2. 模組 build.gradle.kts 不出現 kotlin{} / allOpen{}（會覆寫慣例）
      3. 模組 build.gradle.kts 不寫死版本號（一律用 libs catalog 別名）
      4. 模組原始碼不得有 println / System.out / System.err（STDIO 協議鐵則）
      5. application.properties 不含金鑰（全部走 System.getenv()）
      6. bin/ 目錄不得入庫（Eclipse/VS Code 編譯輸出）

.EXAMPLE
    pwsh scripts/verify-conventions.ps1
#>

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

$script:failures = 0

function Write-Pass([string]$name) {
    Write-Host ("  [PASS] " + $name) -ForegroundColor Green
}

function Write-Fail([string]$name, [string[]]$details, [string]$fix) {
    Write-Host ("  [FAIL] " + $name) -ForegroundColor Red
    foreach ($d in $details) { Write-Host ("         " + $d) -ForegroundColor Red }
    Write-Host ("         → 修復：" + $fix) -ForegroundColor Yellow
    $script:failures++
}

# 從 settings.gradle.kts 動態解析模組清單（新增模組自動涵蓋）
function Get-ModuleDirs {
    $settings = Join-Path $root 'settings.gradle.kts'
    $content = Get-Content $settings -Raw
    if ($content -notmatch '(?s)include\s*\((.*?)\)') {
        throw "無法從 settings.gradle.kts 解析 include(...) 區塊"
    }
    $block = $Matches[1]
    $names = [regex]::Matches($block, '"([^"]+)"') | ForEach-Object { $_.Groups[1].Value }
    $dirs = @()
    foreach ($n in $names) {
        $p = Join-Path $root $n
        if (Test-Path $p) { $dirs += $p } else { Write-Warning "模組目錄不存在：$n" }
    }
    return $dirs
}

# 依模組收集符合 glob 的檔案（回傳 FileInfo）
function Get-ModuleFiles([string[]]$moduleDirs, [string]$relativeGlob) {
    $files = @()
    foreach ($m in $moduleDirs) {
        $target = Join-Path $m $relativeGlob
        $files += Get-ChildItem -Path $target -File -Recurse -ErrorAction SilentlyContinue
    }
    return $files
}

# 對一組檔案做 regex 掃描，回傳 "相對路徑:行號: 內容" 命中清單
function Find-Violations([System.IO.FileInfo[]]$files, [string]$pattern) {
    $hits = @()
    foreach ($f in $files) {
        $matches = Select-String -Path $f.FullName -Pattern $pattern -AllMatches -ErrorAction SilentlyContinue
        foreach ($mt in $matches) {
            $rel = $mt.Path.Substring($root.Length).TrimStart('\', '/')
            $hits += ("{0}:{1}: {2}" -f $rel, $mt.LineNumber, $mt.Line.Trim())
        }
    }
    return $hits
}

Write-Host "驗證建置與程式碼慣例 @ $root" -ForegroundColor Cyan
Write-Host ""

$modules = Get-ModuleDirs
$buildFiles = @()
foreach ($m in $modules) {
    $bf = Join-Path $m 'build.gradle.kts'
    if (Test-Path $bf) { $buildFiles += Get-Item $bf }
}
$mainKt = Get-ModuleFiles $modules 'src\main'  | Where-Object { $_.Extension -eq '.kt' }
$propFiles = Get-ModuleFiles $modules 'src\main\resources' | Where-Object { $_.Name -eq 'application.properties' }

# --- 1. 無跨模組 project(":...") 相依 ---
$v = Find-Violations $buildFiles 'project\(\s*["'':]'
if ($v.Count -eq 0) { Write-Pass "1. 模組間無跨模組 project(:...) 相依" }
else { Write-Fail "1. 模組間無跨模組 project(:...) 相依" $v "各模組執行期應彼此獨立；移除跨模組相依，改用 catalog 套件或抽出共用邏輯到 buildSrc。" }

# --- 2. build 檔不覆寫慣例（kotlin{} / allOpen{}） ---
$v = Find-Violations $buildFiles '(^|\s)(kotlin|allOpen)\s*\{'
if ($v.Count -eq 0) { Write-Pass "2. 模組 build.gradle.kts 未覆寫 kotlin{} / allOpen{}" }
else { Write-Fail "2. 模組 build.gradle.kts 未覆寫 kotlin{} / allOpen{}" $v "共用設定只改 buildSrc 的 mcp-server.conventions.gradle.kts；模組端重複宣告會覆寫慣例、造成版本漂移。" }

# --- 3. build 檔不寫死版本號 ---
$v = Find-Violations $buildFiles '["'']\d+\.\d+[\d.]*["'']'
if ($v.Count -eq 0) { Write-Pass "3. 模組 build.gradle.kts 無寫死版本號" }
else { Write-Fail "3. 模組 build.gradle.kts 無寫死版本號" $v "版本一律集中在 gradle/libs.versions.toml，build 檔改用 libs.* catalog 別名。" }

# --- 4. 原始碼無 println / System.out / System.err ---
$v = Find-Violations $mainKt '\bprintln\s*\(|System\.(out|err)\b'
if ($v.Count -eq 0) { Write-Pass "4. 原始碼無 println / System.out / System.err" }
else { Write-Fail "4. 原始碼無 println / System.out / System.err" $v "STDIO transport 下 stdout 專屬 MCP 協議；診斷輸出改用 Quarkus Log 或 java.util.logging。" }

# --- 5. application.properties 不含金鑰 ---
$v = Find-Violations $propFiles '(?i)(api[._-]?key|secret|password|token|credential)\s*='
if ($v.Count -eq 0) { Write-Pass "5. application.properties 不含金鑰" }
else { Write-Fail "5. application.properties 不含金鑰" $v "金鑰不入設定檔；執行期以 System.getenv() 讀取，缺少時拋例外。" }

# --- 6. bin/ 不入庫 ---
Push-Location $root
try {
    $tracked = git ls-files --error-unmatch . 2>$null | Where-Object { $_ -match '(^|/)bin/' }
} finally {
    Pop-Location
}
if (-not $tracked -or $tracked.Count -eq 0) { Write-Pass "6. bin/ 目錄未入庫" }
else { Write-Fail "6. bin/ 目錄未入庫" $tracked "bin/ 是 IDE 編譯輸出（src 的過期副本）；git rm --cached 移除並確認 .gitignore 已涵蓋。" }

Write-Host ""
if ($script:failures -eq 0) {
    Write-Host "全部通過：6 條不變量。" -ForegroundColor Green
    exit 0
} else {
    Write-Host ("有 {0} 條不變量違規。" -f $script:failures) -ForegroundColor Red
    exit 1
}
