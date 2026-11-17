# deep-islet

深屿 —— 一个浮在所有应用最上层的桌面悬浮小窗。

基于 Kotlin + `WindowManager` + `TYPE_APPLICATION_OVERLAY` + 透明 WebView。
素材和页面沿用 `index.html`（自带图片，单文件，无外部依赖）。

## 用法

1. 安装 APK
2. 打开「深屿」，第一次会跳到系统设置，给一项权限：「显示在其他应用上层」
3. 回到 app，再点一次「让他出来」
4. 他会浮在屏幕上，按住可以拖

## 编译

推到 `main` 分支，GitHub Actions 自动构建，产物在 Actions 的 Artifacts 里（`islet-debug-apk`）。

也可以手动触发：Actions → Build APK → Run workflow。

## 结构

```
app/src/main/
  AndroidManifest.xml
  java/com/deepislet/pet/MainActivity.kt    # 入口，负责申请权限
  java/com/deepislet/pet/OverlayService.kt  # 悬浮窗本体
  assets/pet/index.html                     # 桌宠页面（含素材，942KB）
```
