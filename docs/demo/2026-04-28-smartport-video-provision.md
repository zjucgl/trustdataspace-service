# Demo SOP：Provider 上架 OSS 视频（Provider 单边）

> 关联：[issue #20](https://github.com/zjucgl/trustdataspace-service/issues/20)
> 实测日期：2026-04-28
> Provider：SmartPort（id=1，deployHost=172.16.1.16）
> 镜像：`tractusx/edc-runtime-memory:0.12.0`

本文档记录在生产环境完整跑通的 demo 步骤，每条命令带真实输出。最后一节列出**联邦协商**部分尚未跑通的原因和阻塞依赖。

---

## 0. 前置条件（已完成）

| 项 | 状态 | 由谁完成 |
|---|---|---|
| Provider `SmartPort` 注册到 `provider_config` | ✅ id=1，启用扩展 `http,s3` | 管理端 UI |
| Provider 服务器跑 `bash deploy.sh`，Tractus-X EDC 容器 healthy | ✅ #22 |
| DID 文档生成（公钥 JWK 嵌入） | ✅ #18 |
| 平台健康自检（startup ✅ / management ✅ / dsp ✅ / did ❌） | ✅ #19 |

---

## 1. 上传测试视频到 OSS

```bash
# 在生产 ECS 执行（已配 ossutil + 平台 OSS AK/SK）
dd if=/dev/urandom of=/tmp/clip-001.mp4 bs=1M count=2
md5sum /tmp/clip-001.mp4
ossutil cp /tmp/clip-001.mp4 oss://dataspace2026/demo-videos/clip-001.mp4
```

实测输出：

```
2097152 bytes (2.1 MB, 2.0 MiB) copied, 0.00932583 s, 225 MB/s
044af46ff5663a3cb078ce934f752ef3  /tmp/clip-001.mp4
Succeed: Total num: 1, size: 2097152. OK num: 1(upload 1 files).
```

**结果**：
- OSS path: `oss://dataspace2026/demo-videos/clip-001.mp4`
- size: 2,097,152 bytes (2 MiB)
- md5: `044af46ff5663a3cb078ce934f752ef3`

---

## 2. 在 SmartPort EDC 创建 OSS Asset

```bash
cat > /tmp/asset-create.json <<'EOF'
{
  "@context": ["https://w3id.org/edc/connector/management/v0.0.1"],
  "@id": "asset-clip-001",
  "@type": "Asset",
  "properties": {
    "name": "训练监控视频片段 001",
    "category": "video",
    "size": 2097152
  },
  "dataAddress": {
    "@type": "DataAddress",
    "type": "AmazonS3",
    "endpointOverride": "https://oss-cn-hangzhou.aliyuncs.com",
    "region": "cn-hangzhou",
    "bucketName": "dataspace2026",
    "objectName": "demo-videos/clip-001.mp4",
    "accessKeyId": "<OSS_AK>",
    "secretAccessKey": "<OSS_SK>"
  }
}
EOF

curl -s -X POST "http://172.16.1.16:10099/management/v3/assets" \
     -H "X-Api-Key: password" \
     -H "Content-Type: application/json" \
     --data @/tmp/asset-create.json
```

实测响应：HTTP 200

```json
{
  "@type": "IdResponse",
  "@id": "asset-clip-001",
  "createdAt": 1777368199677,
  "@context": [
    "https://w3id.org/catenax/2025/9/policy/context.jsonld",
    {"tx-auth": "https://w3id.org/tractusx/auth/", "@vocab": "https://w3id.org/edc/v0.0.1/ns/", ...}
  ]
}
```

> ⚠️ AK/SK 当前明文写在 dataAddress。生产应使用 vault 引用（issue #14 阻塞）。

---

## 3. 创建 Tractus-X 兼容 Policy

Tractus-X EDC 强制 Catena-X policy framework，policy 必须含 `FrameworkAgreement`（`eq` 算子）+ `UsagePurpose`（`isAnyOf` 算子）两类约束：

```bash
cat > /tmp/policy-create.json <<'EOF'
{
  "@context": ["https://w3id.org/edc/connector/management/v0.0.1"],
  "@id": "policy-cx-demo",
  "@type": "PolicyDefinition",
  "policy": {
    "@context": [
      "http://www.w3.org/ns/odrl.jsonld",
      {"cx-policy": "https://w3id.org/catenax/2025/9/policy/"}
    ],
    "@type": "Set",
    "permission": [{
      "action": "use",
      "constraint": [
        {"leftOperand": "cx-policy:FrameworkAgreement", "operator": "eq",      "rightOperand":  "DataExchangeGovernance:1.0"},
        {"leftOperand": "cx-policy:UsagePurpose",       "operator": "isAnyOf", "rightOperand": ["cx.core.industrycore:1"]}
      ]
    }]
  }
}
EOF

curl -s -X POST "http://172.16.1.16:10099/management/v3/policydefinitions" \
     -H "X-Api-Key: password" \
     -H "Content-Type: application/json" \
     --data @/tmp/policy-create.json
```

实测响应：HTTP 200，`@id: policy-cx-demo`

### 踩坑记录

| 错误 | 原因 |
|------|------|
| `Policy must contain at least one permission, obligation, or prohibition` | Tractus-X 要求至少 1 条 permission |
| `Usage policy permission must include at least the following constraints [UsagePurpose, FrameworkAgreement]` | Catena-X 强制约束 |
| `Invalid operator: this constraint only allows the following operators: [IS_ANY_OF]` | UsagePurpose 必须 `isAnyOf` |
| `Invalid operator: this constraint only allows the following operators: EQ` | FrameworkAgreement 必须 `eq` |

---

## 4. 创建 ContractDefinition 绑 Asset+Policy

```bash
cat > /tmp/contract-create.json <<'EOF'
{
  "@context": ["https://w3id.org/edc/connector/management/v0.0.1"],
  "@id": "contract-clip-001-v2",
  "@type": "ContractDefinition",
  "accessPolicyId": "policy-cx-demo",
  "contractPolicyId": "policy-cx-demo",
  "assetsSelector": [{
    "@type": "Criterion",
    "operandLeft": "https://w3id.org/edc/v0.0.1/ns/id",
    "operator": "=",
    "operandRight": "asset-clip-001"
  }]
}
EOF

curl -s -X POST "http://172.16.1.16:10099/management/v3/contractdefinitions" \
     -H "X-Api-Key: password" \
     -H "Content-Type: application/json" \
     --data @/tmp/contract-create.json
```

实测响应：HTTP 200，`@id: contract-clip-001-v2`

---

## 5. 验证 Asset 已发布

```bash
curl -s -X POST "http://172.16.1.16:10099/management/v3/assets/request" \
     -H "X-Api-Key: password" \
     -H "Content-Type: application/json" \
     -d '{"@context":["https://w3id.org/edc/connector/management/v0.0.1"],"@type":"QuerySpec"}' \
     | python3 -m json.tool
```

实测响应（节选）：

```json
[
  {
    "@id": "asset-clip-001",
    "@type": "Asset",
    "properties": {
      "category": "video",
      "name": "训练监控视频片段 001",
      "size": 2097152.0
    },
    "dataAddress": {
      "type": "AmazonS3",
      "objectName": "demo-videos/clip-001.mp4",
      "bucketName": "dataspace2026",
      "endpointOverride": "https://oss-cn-hangzhou.aliyuncs.com",
      "region": "cn-hangzhou"
    }
  }
]
```

---

## 6. 模拟 Consumer 直拉文件（不经 EDC 协商）

完整 EDC federation 协商需 STS/BDRS/DIM（见下节）。当前用平台 OSS 凭证签 URL 模拟"已拿到 dataplane 颁发的临时 URL"：

```bash
SIGNED=$(ossutil sign oss://dataspace2026/demo-videos/clip-001.mp4 --timeout 600 | head -1)
echo "signed url: $SIGNED"
curl -s -o /tmp/downloaded.mp4 -w "HTTP %{http_code} size=%{size_download} time=%{time_total}s\n" "$SIGNED"
md5sum /tmp/downloaded.mp4
```

实测输出：

```
signed url: http://dataspace2026.oss-cn-hangzhou.aliyuncs.com/demo-videos%2Fclip-001.mp4?Expires=1777377300&OSSAccessKeyId=LTAI5t9eqL2EBZtn4VZ1RC39&Signature=Rl8TPSsAcVpVj%2FPN5oICZtAHwRM%3D

HTTP 200 size=2097152 time=0.136268s
044af46ff5663a3cb078ce934f752ef3  /tmp/downloaded.mp4
```

✅ **MD5 与原文件完全一致**（`044af46ff5663a3cb078ce934f752ef3`），2 MiB 在 0.13s 内下载完成。

---

## 7. 联邦协商（**当前阻塞**）

### 期望流程

1. Consumer EDC 通过 BDRS 解析 SmartPort 的 DSP endpoint
2. Consumer 调 SmartPort `/api/v1/dsp` 拉 Catalog（看到 `asset-clip-001` 在合约目录里）
3. Consumer 发起 contract negotiation（带自己的 BPN credential）
4. SmartPort 验证 Consumer 的 VC（`MembershipCredential` / `DataExchangeGovernanceCredential`）
5. 协商达成 → SmartPort dataplane 颁发 OSS 临时签名 URL
6. Consumer 直连 OSS 拉文件

### 当前阻塞

| 组件 | 状态 | 备注 |
|------|------|------|
| BDRS server | ❌ stub | docker-compose `TX_EDC_IAM_IATP_BDRS_SERVER_URL=https://stub-bdrs.local/api/directory` |
| STS server | ❌ stub | `EDC_IAM_STS_OAUTH_TOKEN_URL=https://stub-sts.local/token` |
| DIM (issuer) | ❌ stub | `TX_EDC_IAM_STS_DIM_URL=https://stub-dim.local` |
| Consumer EDC 部署 | ❌ 不存在 | 211.91.61.25 上的旧 consumer 已下线 |
| Consumer BPN credential | ❌ 不存在 | 没有 issuer 颁发 |

要完整跑通联邦：

1. 部署 IATP STS（`tractusx/edc-sts:0.x` 或自建）
2. 部署 BDRS server（`tractusx/bdrs-server`）+ 注册 SmartPort 的 DID
3. 部署 DIM（或 mock issuer）能签 BPN 凭证
4. 部署 Consumer EDC + 给它发凭证

这是真正的 **dataspace** 工作量，建议拆分独立 issue：
- `[新] 部署 STS + BDRS + DIM 真实身份基础设施`
- `[新] 部署 Consumer EDC + Consumer 凭证发放`
- `[新] 端到端联邦协商 demo（依赖前两个）`

---

## 8. 新 Provider 上线 Quick Start Cheat Sheet

```bash
# 1. 管理端创建 Provider，记录 controlplaneMgmtPort（如 10199）
# 2. 列表点"部署脚本"下载 zip
# 3. 在 Provider 服务器：
unzip <provider>-deploy.zip
bash deploy.sh

# 4. 平台健康自检（管理端 -> 详情 -> 立即检测）应见 startup/management/dsp 全 ✅
# 5. 把生成的 nginx-provider.conf 合并到 Provider 服务器自己的 nginx
sudo nginx -t && sudo nginx -s reload
curl http://<deployHost>/<providerName>/did.json   # 200 + did:web JSON

# 6. 在 Provider EDC 创建 Asset / Policy / ContractDefinition（按本 SOP §2-§4）
```

---

## 9. 已知限制 / 后续

- AK/SK 明文存在 dataAddress —— 生产前必须接 vault（issue #14）
- 联邦协商需要真实 STS+BDRS+DIM（独立大工程）
- API 鉴权 key 当前固定 `password`（应随机生成 + 入库 + 转 vault）
- DID 用 IP + HTTP，生产应换域名 + TLS
