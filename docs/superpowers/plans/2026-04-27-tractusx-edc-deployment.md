# Tractus-X EDC 镜像化部署实施计划

> 关联：[issue #22](https://github.com/zjucgl/trustdataspace-service/issues/22)（决策来自 #21，方案 B）

**Goal:** 用 Tractus-X 已发布的 docker 镜像替换"OSS 预签名 URL → wget jar → java -jar"部署链。Provider 服务器跑 `bash deploy.sh` 即 docker compose pull/up，不再需要本地 jar / launcher 构建。

---

## Tasks

- [x] **T1** 调研 Tractus-X EDC 镜像（tag、env vars、是否含 s3/jdbc 扩展）
- [x] **T2** application-prod.yml 加 edc.tractusx 配置项
- [x] **T3** ProviderDeployServiceImpl 移除 jar URL 预签名，加镜像 tag 模板变量
- [x] **T4** docker-compose.yml.ftl 用 Tractus-X 镜像替代裸 jar
- [x] **T5** deploy.sh.ftl 简化（去掉 fetch_jar 整段）
- [x] **T6** 文档：jar-distribution.md → deployment.md 重写
- [x] **T7** 后端编译 + 前端构建 + 提交推送（commit 7030b2f）
- [x] **T8** 部署到生产 + 验证 deploy zip 内容（image=tractusx/edc-runtime-memory:0.12.0，5 端口正确）
- [ ] **T9** 端到端验证：干净测试机跑 deploy.sh，4 容器 healthy
- [ ] **T10** 多 Provider 隔离验证

## T1 调研产出

### 镜像选择：`tractusx/edc-runtime-memory:0.12.0`

| 候选 | 含义 | 选用 |
|------|------|------|
| `tractusx-connector` chart | 4-container 拆分（cp/dp + postgres + vault） | ❌ 复杂、依赖多 |
| `tractusx-connector-memory` chart | **单容器全栈、内存状态、内置 vault** | ✅ |
| `edc-runtime-memory:0.12.0` | 即上者使用的镜像 | ✅ |

**优点：**
- 单容器，4 件套（controlplane/dataplane/IH/STS）合一进程
- 不需要 postgres、不需要 Hashicorp Vault（内置 in-memory vault）
- 与 EDC 0.12.x SPI 兼容
- Dockerhub 上 stable tag `0.12.0` 已发布、可直接 docker pull

**缺点：**
- Tractus-X 官方明确："仅用于开发或测试，禁止生产负载"——这点对当前阶段（demo）OK，未来生产需要换 postgres+vault 变体（issue #15 后续）
- 重启数据丢失（in-memory）

### 必备环境变量（写到 docker-compose 的 environment 段）

| 变量 | 值 / 来源 |
|------|-----------|
| `EDC_PARTICIPANT_ID` | `did:web:${deployHost}:${providerName}` |
| `EDC_IAM_ISSUER_ID` | 同上 |
| `EDC_PARTICIPANT_CONTEXT_ID` | UUID（部署时生成或固化到 provider_config） |
| `TRACTUSX_EDC_PARTICIPANT_BPN` | `BPN${providerName}` 兜底 |
| `WEB_HTTP_PORT` / `WEB_HTTP_PATH` | `${controlplaneMgmtPort}` / `/api`（默认健康检查端点） |
| `WEB_HTTP_MANAGEMENT_PORT` / `_PATH` | `${controlplaneMgmtPort}` / `/management` |
| `WEB_HTTP_MANAGEMENT_AUTH_TYPE` | `tokenbased` |
| `WEB_HTTP_MANAGEMENT_AUTH_KEY` | `password`（TODO 后续随机生成） |
| `WEB_HTTP_PROTOCOL_PORT` / `_PATH` | `${controlplaneProtocolPort}` / `/api/v1/dsp` |
| `WEB_HTTP_PUBLIC_PORT` / `_PATH` | `${dataplanePublicPort}` / `/api/public` |
| `WEB_HTTP_CONTROL_PORT` / `_PATH` | `${controlplanePublicPort}` / `/control` |
| `WEB_HTTP_CATALOG_PORT` / `_PATH` | `${identityHubPort}` / `/catalog`（端口复用） |
| `WEB_HTTP_CATALOG_AUTH_TYPE` | `tokenbased` |
| `WEB_HTTP_CATALOG_AUTH_KEY` | `password` |
| `EDC_DSP_CALLBACK_ADDRESS` | `http://${deployHost}:${controlplaneProtocolPort}/api/v1/dsp` |
| `EDC_IAM_STS_OAUTH_TOKEN_URL` | `https://stub-sts.local/token`（**stub，未做联邦时无影响**） |
| `EDC_IAM_STS_OAUTH_CLIENT_ID` | `stub-client` |
| `EDC_IAM_STS_OAUTH_CLIENT_SECRET_ALIAS` | `sts-client-secret` |
| `TX_EDC_IAM_STS_DIM_URL` | `https://stub-dim.local`（stub） |
| `TX_EDC_VAULT_SECRETS` | `sts-client-secret:stub;<other>:<other>`（分号分隔） |
| `TX_EDC_DPF_CONSUMER_PROXY_AUTH_APIKEY` | `password` |

**端口映射方案**：把现有 `provider_config` 的 6 个端口映射到 Tractus-X 单容器的多端点：
- controlplaneMgmtPort → WEB_HTTP_MANAGEMENT_PORT + WEB_HTTP_PORT
- controlplaneProtocolPort → WEB_HTTP_PROTOCOL_PORT
- controlplanePublicPort → WEB_HTTP_CONTROL_PORT
- dataplanePublicPort → WEB_HTTP_PUBLIC_PORT
- identityHubPort → WEB_HTTP_CATALOG_PORT
- stsPort → 暂不暴露（memory 变体没独立 STS 端点）

### S3/JDBC/SFTP 扩展

`edc-runtime-memory` 是"集大成"运行时，**S3 dataplane 扩展默认包含**（test 配置里直接 `dataplane.aws.endpointOverride` 即可用）。JDBC / SFTP 不确定，需 T9 验证；如果不在 image 里，单独追加 jar 到 `/app/extensions/` 挂载。

### IATP 兼容性说明

Tractus-X 的 IATP（STS + DIM）是 Catena-X 联邦专用。我们没有真实 STS/DIM 服务时，配 stub URL 即可让容器启动；只有走真实 federation contract negotiation 才会触发联系 STS，那时再补真实端点（issue #18）。

### Decision Log

- ✅ 用 `tractusx/edc-runtime-memory:0.12.0`，单容器
- ✅ stub IATP 配置，启动通过 = #22 验收；真实 federation 后续
- ✅ `provider_config` schema 不变，6 端口映射到 Tractus-X 多端点
- ✅ `TX_EDC_VAULT_SECRETS` 用 stub，未来再换真实 vault
