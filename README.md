# 宇聊 Android APK (Capacitor Hybrid)

## 📱 项目信息

- **应用名**：宇聊
- **Bundle ID**：`com.qjyes.yuliao`
- **架构**：Capacitor 6.x Hybrid (原生壳 + 加载 https://chat.qjyes.com/h5)
- **API 支持**：Android 7.0+ (API 24)

## 🔌 21 个插件

| 插件 | 用途 |
|------|------|
| local-notifications | 本地通知（锁屏也响） |
| push-notifications | FCM 远程推送 |
| haptics | 震动反馈 |
| camera | 拍照 + 相册 |
| geolocation | GPS 定位 |
| filesystem | 文件读写 |
| preferences | 本地存储 |
| share | 系统分享面板 |
| device | 设备信息 |
| network | 网络状态监听 |
| clipboard | 剪贴板 |
| browser | 系统浏览器 |
| dialog | 系统对话框 |
| toast | Toast 提示 |
| status-bar | 状态栏样式 |
| splash-screen | 启动屏 |
| keyboard | 键盘控制 |
| app | 生命周期 |

## 🚀 编译

**自动**：push 代码到 main → GitHub Actions 自动编译

**手动**：Actions → Build Android APK → Run workflow

## 📥 下载

编译完成后：Actions 页面 → 最新运行 → Artifacts → `yuliao-android-apk`

## 📱 安装

1. APK 传到 Android 手机（微信/QQ/网盘/浏览器下）
2. 打开 APK 文件
3. 允许「安装未知来源应用」
4. 一键装完

## 🔐 权限说明

- 相机：拍照发送、扫码
- 麦克风：语音消息、语音通话
- 位置：分享位置、附近的人
- 存储：保存图片视频
- 通知：接收消息推送
- 震动：新消息震动
