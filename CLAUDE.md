# MindTrace 项目说明

## 开发环境

### Java 环境

Java 通过 Homebrew 安装，构建时需要设置 JAVA_HOME：

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

### 构建命令

```bash
# 构建 Debug APK
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && ./gradlew assembleDebug

# 构建 Release APK
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home && ./gradlew assembleRelease
```

### Android SDK

SDK 路径配置在 `local.properties`：
```
sdk.dir=/opt/homebrew/share/android-commandlinetools
```

## 项目结构

- `app/src/main/java/com/mindtrace/diary/` - 主代码目录
  - `domain/model/` - 数据模型
  - `domain/usecase/` - 业务逻辑
  - `data/repository/` - 数据仓库实现
  - `ui/screens/` - UI 页面
  - `ui/components/` - UI 组件
  - `core/` - 核心工具类
