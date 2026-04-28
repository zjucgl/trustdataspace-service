# Tractus-X EDC Connector 部署 SOP

> 关联：[issue #22](https://github.com/zjucgl/trustdataspace-service/issues/22)（决策来自 #21，方案 B）

平台 Provider 节点用 [Tractus-X EDC](https://github.com/eclipse-tractusx/tractusx-edc) 已发布的 docker 镜像 `tractusx/edc-runtime-memory:0.12.0` 部署，**单容器**包含 controlplane + dataplane + identity-hub + sts，内存状态、内置 in-memory vault。

本文描述：

1. 镜像选择与版本约定
2. Provider 部署流程
3. 必备环境变量映射
4. 升级 Tractus-X 版本时的操作
5. 故障排查

---

## 1. 镜像与版本

| 镜像 | tag | 来源 |
|------|-----|------|
| `tractusx/edc-runtime-memory` | `0.12.0`（默认） | [Docker Hub](https://hub.docker.com/r/tractusx/edc-runtime-memory) |

Tractus-X EDC 提供两组 chart：

| Chart | 拓扑 | 何时用 |
|-------|------|------|
| `tractusx-connector` | 4-container 拆分（controlplane + dataplane + postgres + vault） | 生产 |
| `tractusx-connector-memory` | **单容器**全栈、内存状态、内置 vault | 开发 / demo（**当前选用**） |

切到生产时改 `application-prod.yml` 的 `edc.tractusx.runtime-image` 即可。

> ⚠️ Tractus-X 官方明确 `edc-runtime-memory` "仅用于开发或测试，禁止生产负载"。当前阶段（demo + 验证）OK，未来要换 postgres+vault 拓扑参考 [chart values.yaml](https://github.com/eclipse-tractusx/tractusx-edc/tree/release/0.12.0/charts/tractusx-connector)。

---

## 2. Provider 部署流程

### 2.1 平台端（管理员）

1. 登录 https://ds.huayihui.art，进入"提供方管理"
2. 新建 / 编辑 Provider，填 deploy_host（Provider 服务器 IP）+ 启用扩展（http/s3 等）
3. 列表点"部署脚本"下载 zip：`<providerName>-deploy.zip`

### 2.2 Provider 服务器（运维）

```bash
# 1. 上传并解压 zip
mkdir -p /tmp/<providerName>-deploy && cd /tmp/<providerName>-deploy
unzip /path/to/<providerName>-deploy.zip

# 2. 准备：必须有 docker 26+
docker --version

# 3. 一键部署
bash deploy.sh
```

`deploy.sh` 会：
- 创建 `/opt/edc/<providerName>/` 目录结构
- 生成 ECDSA P-256 密钥对（DID 用，写到 `credentials/`）
- `docker compose pull` 拉 Tractus-X 镜像
- `docker compose up -d` 启动容器
- 轮询 healthcheck（最多 2 分钟），调平台 callback 报告状态

期望耗时：首次拉镜像 3-5 分钟（约 700MB），二次部署 30 秒。

### 2.3 验证

```bash
# 在 Provider 服务器上
docker compose ps                              # 应见 <providerName>-edc 容器 healthy
curl http://localhost:<mgmtPort>/api/check/health   # 200

# 在管理端
# 提供方列表里该 Provider 状态变成 "运行中"
```

---

## 3. 环境变量映射

`docker-compose.yml.ftl` 把 `provider_config` 表的 6 端口映射到 Tractus-X 单容器的多端点：

| `provider_config` 字段 | Tractus-X 环境变量 | 端点路径 |
|---------|-----|------|
| controlplaneMgmtPort | `WEB_HTTP_MANAGEMENT_PORT` + `WEB_HTTP_PORT` | `/management` + `/api`（健康检查） |
| controlplaneProtocolPort | `WEB_HTTP_PROTOCOL_PORT` | `/api/v1/dsp` |
| controlplanePublicPort | `WEB_HTTP_CONTROL_PORT` | `/control` |
| dataplanePublicPort | `WEB_HTTP_PUBLIC_PORT` | `/api/public` |
| identityHubPort | `WEB_HTTP_CATALOG_PORT` | `/catalog` |
| stsPort | （memory 变体不暴露独立 STS 端点，暂保留 schema 字段） | — |

身份与 Vault：

| 变量 | 当前值 | 说明 |
|------|--------|------|
| `EDC_PARTICIPANT_ID` | `did:web:<deployHost>:<providerName>` | 默认从 deployHost 派生；`participant_id` 字段非空时取它 |
| `EDC_IAM_ISSUER_ID` | 同上 | |
| `TRACTUSX_EDC_PARTICIPANT_BPN` | `BPN<PROVIDERNAME>` | 占位 BPN，未走真实 Catena-X 联邦 |
| `EDC_IAM_STS_OAUTH_TOKEN_URL` | `https://stub-sts.local/token` | **stub**，未做联邦时无影响（issue #18 落地时换真值） |
| `TX_EDC_IAM_STS_DIM_URL` | `https://stub-dim.local` | **stub** |
| `TX_EDC_VAULT_SECRETS` | `sts-client-secret:stub-secret-value` | 内置 vault 种子；分号分隔 `key:value` |
| `WEB_HTTP_MANAGEMENT_AUTH_KEY` | `password` | TODO：随机生成 + 保存到 provider_config |

---

## 4. 升级 Tractus-X 版本

修改 `application-prod.yml`：

```yaml
edc:
  tractusx:
    runtime-image: ${EDC_TRACTUSX_RUNTIME_IMAGE:tractusx/edc-runtime-memory:0.13.0}
```

或通过 docker-compose `.env` 注入 `EDC_TRACTUSX_RUNTIME_IMAGE=tractusx/edc-runtime-memory:0.13.0` 后重启后端。

已运行的 Provider EDC 不会自动升级；需要 Provider 服务器重新跑 `bash deploy.sh`（会重新下载 zip / docker pull 新 image / restart）。

---

## 5. 故障排查

| 现象 | 可能原因 | 排查 |
|------|----------|------|
| `docker compose pull` 失败 | dockerhub 限流 / 网络 | 配镜像加速（参考 issue #9 已配置）；或换私有 registry |
| 容器起来但 healthcheck 一直 `unhealthy` | 配置错误（端口冲突 / 必备 env 缺失） | `docker logs <providerName>-edc` 看 EDC 日志 |
| `Required env variable X is not set` | 模板变量没注入 | 检查 `ProviderDeployServiceImpl.buildVars` 是否覆盖该 key |
| 502 from nginx-provider.conf | Provider 容器没起 / 端口没暴露 | `docker compose ps`、`docker port` 检查 |
| 调用 `/api/v1/dsp` 报 IATP 相关错 | STS / DIM 是 stub URL，真实 federation 必须接真服务 | issue #18 落地后再测 |
| 想用 S3 dataplane 但失败 | env vars `EDC_DATAPLANE_AWS_*` 没填真值 | 编辑生成的 docker-compose.yml 填上 endpoint/AK/SK 后重启 |

调试技巧：

```bash
# 进容器看挂载和文件
docker exec -it <providerName>-edc sh

# 看 EDC 实时日志
docker logs -f <providerName>-edc

# 直接查 Tractus-X 健康端点
curl http://<deployHost>:<mgmtPort>/api/check/health
curl http://<deployHost>:<mgmtPort>/api/check/startup
curl http://<deployHost>:<mgmtPort>/api/check/liveness
```

---

## 6. 已知限制

- **联邦发现**需要真实 STS + DIM（见 issue #18 / Tractus-X DCP 文档）
- **数据持久化**：memory variant 容器重启丢数据；生产换 postgres 变体
- **凭证**：管理端 API key 当前固定 `password`，TODO 随机化 + 入库
- **Tractus-X BPN**：当前用占位 BPN，未对接 Catena-X 业务伙伴注册中心
