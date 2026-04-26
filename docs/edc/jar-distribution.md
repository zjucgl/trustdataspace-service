# EDC JAR 分发 SOP（方案 B / OSS）

> 关联：[issue #16](https://github.com/zjucgl/trustdataspace-service/issues/16) / [issue #15 Meta](https://github.com/zjucgl/trustdataspace-service/issues/15)

平台不预编译 EDC jar，统一从阿里云 OSS 拉取。本文档描述：

1. OSS 目录结构 / 命名约定
2. 一次性手工构建 + 上传步骤
3. 部署脚本如何拉取
4. 升级 EDC 版本时的操作

---

## 1. OSS 目录结构

Bucket：`dataspace2026`（与平台主存储复用）

```
oss://dataspace2026/edc-jars/
├── v0.10.1/                            ← 当前默认
│   ├── controlplane.jar
│   ├── dataplane.jar
│   ├── identity-hub.jar
│   ├── sts.jar
│   └── extensions/
│       ├── data-plane-aws-s3.jar
│       ├── data-plane-jdbc.jar
│       └── data-plane-sftp.jar
├── v0.10.2/                            ← 升级后保留旧版本目录至少 30 天
└── v0.11.0/
```

**访问方式：OSS pre-signed URL**

bucket `dataspace2026` 启用了"阻止公共访问"，无法直接 `https://...path` 公开访问。平台后端在生成部署 zip 时为每个 jar 路径用 AK/SK 签发一个 24h 有效的下载 URL，嵌入到 deploy.sh 里的 `wget` 命令。

URL 形式（每次生成 zip 时签）：

```
https://dataspace2026.oss-cn-hangzhou.aliyuncs.com/edc-jars/<version>/<filename>?Expires=...&OSSAccessKeyId=...&Signature=...
```

含义：
- 不依赖 OSS bucket policy / object ACL
- zip 下载后 24h 内可执行 deploy.sh；超时需重新下载 zip
- jar 内容完全私有，外部无法枚举或匿名访问

---

## 2. 一次性构建步骤

### 2.1 准备构建环境

```bash
# JDK 21（EDC v0.10.x 要求）
sudo apt install -y openjdk-21-jdk
# Gradle 8.5+
sdk install gradle 8.5
# 阿里云 ossutil
wget https://gosspublic.alicdn.com/ossutil/1.7.18/ossutil64
sudo mv ossutil64 /usr/local/bin/ossutil && chmod +x /usr/local/bin/ossutil
ossutil config -e oss-cn-hangzhou.aliyuncs.com -i <AK> -k <SK>
```

### 2.2 构建核心 launcher

EDC 不直接发布 fat jar，需要从 launcher 模块构建。推荐基于 [MinimumViableDataspace](https://github.com/eclipse-edc/MinimumViableDataspace) 改造：

```bash
git clone https://github.com/eclipse-edc/MinimumViableDataspace.git edc-build
cd edc-build && git checkout v0.10.1
```

`launchers/` 目录下应该有 4 个子模块（按 MVD 命名可能略有出入）：

| MVD 子模块 | 输出 fat jar |
|-------------|----------|
| `connector` | controlplane.jar |
| `dataplane` | dataplane.jar |
| `identity-hub` | identity-hub.jar |
| `sts` | sts.jar |

构建：

```bash
./gradlew shadowJar
# 输出在 launchers/<mod>/build/libs/<mod>-all.jar
```

把 4 个 fat jar 重命名归档：

```bash
mkdir -p ../jars-v0.10.1/extensions
cp launchers/connector/build/libs/connector-all.jar ../jars-v0.10.1/controlplane.jar
cp launchers/dataplane/build/libs/dataplane-all.jar ../jars-v0.10.1/dataplane.jar
cp launchers/identity-hub/build/libs/identity-hub-all.jar ../jars-v0.10.1/identity-hub.jar
cp launchers/sts/build/libs/sts-all.jar ../jars-v0.10.1/sts.jar
```

### 2.3 构建 dataplane 扩展

EDC 扩展 jar 需要单独打包成 module shadow jar。在 dataplane launcher 的 build.gradle 里追加扩展依赖时：

```groovy
// data-plane-aws-s3 module
dependencies {
    implementation("org.eclipse.edc:data-plane-aws-s3:${edcVersion}")
}
```

每个扩展独立 shadowJar 后产出：

```bash
cp launchers/data-plane-aws-s3/build/libs/*-all.jar ../jars-v0.10.1/extensions/data-plane-aws-s3.jar
cp launchers/data-plane-jdbc/build/libs/*-all.jar  ../jars-v0.10.1/extensions/data-plane-jdbc.jar
cp launchers/data-plane-sftp/build/libs/*-all.jar  ../jars-v0.10.1/extensions/data-plane-sftp.jar
```

> **如果暂时不想为每个扩展独立做 launcher**：可以让 dataplane.jar 直接 shade 进所有扩展，跳过单独的 extensions/ 目录。代价是镜像/jar 体积变大、所有 Provider 都强制带全扩展（无法按需）。

### 2.4 上传到 OSS

```bash
cd ../jars-v0.10.1
ossutil cp -r . oss://dataspace2026/edc-jars/v0.10.1/ --include "*.jar"
```

**不需要**额外设置 bucket policy 或 object ACL — 平台后端用 AK/SK 签 URL 给客户端访问。

### 2.5 验证可下载

直接 curl object URL 会 403（bucket 私有）。需要通过平台生成 deploy zip，里面的 deploy.sh 才有签名 URL：

```bash
curl -s -o /tmp/d.zip "https://ds.huayihui.art/api/provider/deploy-script/<id>"
unzip -o /tmp/d.zip -d /tmp/d
# deploy.sh 第 2 步 fetch_jar 的 URL 参数都是带 Signature 的；可以手工 curl 验证
grep "fetch_jar.*Signature" /tmp/d/deploy.sh | head -1
```

---

## 3. 部署脚本如何拉取

`templates/deploy/deploy.sh.ftl` 渲染后的 deploy.sh 在 `[2/6] Fetching JAR files` 步骤会执行：

```bash
EDC_VERSION="v0.10.1"
JAR_CACHE_DIR="${EDC_JAR_CACHE:-/opt/edc/shared/jars/$EDC_VERSION}"

fetch_jar "controlplane.jar" "<24h-signed-url>"
fetch_jar "dataplane.jar"    "<24h-signed-url>"
fetch_jar "identity-hub.jar" "<24h-signed-url>"
fetch_jar "sts.jar"          "<24h-signed-url>"
# 启用了 s3 时 fetch_jar extensions/data-plane-aws-s3.jar
# 启用了 jdbc / sftp 同理
```

每个 URL 由平台后端 `OssUtils.createPublicSignedUrl()` 用 AK/SK 签发，24h 有效。zip 过期需要重新从平台下载。

### 环境变量覆盖

| 变量 | 用途 | 默认 |
|------|------|------|
| `EDC_JAR_CACHE` | 改本地缓存目录 | `/opt/edc/shared/jars/<version>` |

注：bucket / 路径 prefix 等绑定到平台 OSS 配置，不在客户端覆盖。

### 后端配置项

`application-prod.yml`：

```yaml
edc:
  version: ${EDC_VERSION:v0.10.1}
  jar:
    oss-prefix: ${EDC_JAR_OSS_PREFIX:edc-jars}
```

OSS bucket 名沿用 `aliyun.oss.bucketName`（dataspace2026），凭证沿用 `aliyun.oss.access-key-id` / `access-key-secret`。`OssConfig` 单独构造一个 `ossPublicSigningClient` bean 用 **公网 endpoint** 签 URL（业务上传仍走 internal endpoint）。

---

## 4. 版本升级流程

升级 EDC（如 v0.10.1 → v0.10.2）：

1. 跑 §2 构建步骤生成新版本 jar
2. 上传到 `oss://dataspace2026/edc-jars/v0.10.2/`（旧版本目录保留至少 30 天）
3. 修改 `application-prod.yml` 的 `edc.version` 为 `v0.10.2`，或 `.env` 加 `EDC_VERSION=v0.10.2`
4. 重启后端
5. 已运行的 Provider EDC **不会自动升级**；需要 Provider 服务器重新跑 `bash deploy.sh` 拉新 jar 重启容器
6. 30 天后清理旧版本 OSS 目录

---

## 5. 故障排查

| 现象 | 排查 |
|------|------|
| `wget` 报 403 | OSS 公共读权限未开 / Bucket Policy 路径不匹配 |
| `wget` 拿到的 jar 大小为 0 | OSS object 上传失败或被覆盖；用 `ossutil ls` 检查 |
| `java -jar` 启动报 `NoClassDefFoundError` | jar 不是 fat jar 或 launcher 没 shadow 进去对应模块；重新构建 |
| dataplane 扩展不生效 | extensions/ 目录下 jar 没挂到 `/app/extensions/`；确认 `enabled_dataplane_extensions` 字段值与 `# @if:<ext>` 模板匹配 |

---

## 6. 待办

- [ ] 把 §2 构建步骤封装成 GitHub Actions / 阿里云效流水线，避免每次手工
- [ ] 加 sha256 校验：上传时生成 `<jar>.sha256`，部署脚本下载后校验
- [ ] dataplane 扩展支持懒加载（不勾选时连下载都不要）— 当前已经实现
