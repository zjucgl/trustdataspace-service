# 改造设计：阿里云 RDS + OSS + 华艺链适配

## 概述

将可信数据空间后端（trustdataspace-service）从自建基础设施迁移到阿里云托管服务 + 华艺链，涉及三个模块的改造。

## 改造范围

| 模块 | 原方案 | 目标 | 改动程度 |
|------|--------|------|----------|
| 数据库 | 自建 MySQL (211.91.61.25:23306) | 阿里云 RDS MySQL | 零代码，仅配置 |
| 文件存储 | 自建 MinIO (211.91.61.25:29000) | 阿里云 OSS | 5 个文件改动 |
| 区块链 | Geth 以太坊私链 (192.168.14.3:8545) | 华艺链 REST API | 14 个文件改动 |

---

## 模块 1：阿里云 RDS MySQL

### 改动内容

- 新建 `application-prod.yml`，数据库连接串、用户名、密码通过环境变量注入
- 开启 SSL 连接
- 保留现有 Dynamic DataSource 框架，仅配置 master 数据源
- `application-dev.yml` 保留不动（开发环境继续用原配置）

### 配置示例

```yaml
spring:
  datasource:
    dynamic:
      primary: master
      strict: false
      datasource:
        master:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/mvd_end_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=true
          username: ${DB_USERNAME}
          password: ${DB_PASSWORD}
```

### 代码改动

无。

---

## 模块 2：阿里云 OSS 替换 MinIO

### 改动文件清单

| 文件 | 操作 |
|------|------|
| `pom.xml` | 移除 `minio` 依赖，新增 `aliyun-sdk-oss` 3.17.4 |
| `config/cfg/MinioConfig.java` | 重写为 `config/cfg/OssConfig.java`，创建 `OSS` Bean |
| `utils/MinioUtils.java` | 重写为 `utils/OssUtils.java` |
| `controller/system/UtilsController.java` | 注入改为 `OssUtils`，`address` 改读 OSS endpoint |
| `application-prod.yml` | 新增 `aliyun.oss` 配置段 |

另需检查 `IAssetServiceImpl.java` 和 `IpFileService.java` 中对 `MinioUtils` 的引用并同步修改。

### 方法映射

| MinioUtils 方法 | OssUtils 等价 |
|----------------|---------------|
| `upload(MultipartFile[])` | `ossClient.putObject(bucket, key, stream, metadata)` |
| `uploadWithoutTime(MultipartFile[])` | 同上，不加时间戳 |
| `download(String)` | `ossClient.getObject(bucket, key).getObjectContent()` |
| `listObjectNames(String)` | `ossClient.listObjects(bucket)` |
| `removeObject(String, String)` | `ossClient.deleteObject(bucket, key)` |
| `createUrl(String)` | `ossClient.generatePresignedUrl(bucket, key, expiration)` |

### 接口兼容性

Controller 的 URL path 保持原样（`/utils/minio/upload` 等），前端零改动。

### 配置示例

```yaml
aliyun:
  oss:
    endpoint: ${OSS_ENDPOINT}
    accessKeyId: ${OSS_ACCESS_KEY_ID}
    accessKeySecret: ${OSS_ACCESS_KEY_SECRET}
    bucketName: ${OSS_BUCKET_NAME:trustdataspace}
```

---

## 模块 3：华艺链替换 Geth/Web3j

### 背景

华艺链是基于 Hyperledger 的联盟链，不兼容 EVM，通过 REST API 交互。参考项目 [huayihui-traceability](https://github.com/zjucgl/huayihui-traceability) 已有成熟的集成模式。

### 华艺链 API 接口

```
POST {base-url}/api/v1/records
Headers: X-API-Key: {api-key}

Request:
{
  "artifact_id": "资产ID",
  "artifact_no": "资产编号",
  "action": "CREATE | UPDATE | TRANSFER",
  "operator": "操作者",
  "timestamp": "ISO8601",
  "data_hash": "SHA256",
  "remark": "备注"
}

Response:
{
  "txHash": "交易哈希",
  "status": "SUCCESS | FAILED"
}
```

### 删除的文件（6个）

| 文件 | 原功能 |
|------|--------|
| `utils/contract/Web3jClient.java` | Web3j 单例连接 |
| `utils/contract/AssetTraceability.java` | Solidity 合约 Wrapper |
| `utils/contract/PaymentContract.java` | 支付合约 Wrapper |
| `utils/contract/GetCredentials.java` | Keystore 凭证加载 |
| `service/web3j/Web3Service.java` | 挖矿/余额接口定义 |
| `service/web3j/impl/Web3ServiceImpl.java` | 挖矿/余额实现 |

### 新增的文件（3个）

**`chain/client/HuayiChainClient.java`**

- 使用 Spring RestTemplate 调用华艺链 REST API
- 支持 X-API-Key 认证
- base-url 和 api-key 通过配置注入

**`chain/model/ChainRecord.java`**

- 上链记录实体，对应 `chain_record` 表
- 字段：id, artifact_id, artifact_no, action, operator_id, operator_name, data_hash, tx_hash, chain_status, remark, chain_time, created_at
- chain_status 状态流：PENDING → SUCCESS / FAILED

**`chain/model/ChainResponse.java`**

- 华艺链返回的 DTO：txHash, status

### 重写的文件（2个）

**`service/trace/impl/AssetTraceServiceImpl.java`**

业务方法映射：

| 原方法 | 原实现 | 新实现 |
|--------|--------|--------|
| `registerAsset()` | Web3j 合约调用 | `chainClient.submitRecord(action=CREATE)` + 写 chain_record |
| `purchaseAsset()` | Web3j 合约调用 + Gas 计算 | `chainClient.submitRecord(action=TRANSFER)` + 写 chain_record |
| `updatePrice()` | Web3j 合约调用 | `chainClient.submitRecord(action=UPDATE)` + 写 chain_record |
| `updateStatus()` | Web3j 合约调用 | `chainClient.submitRecord(action=UPDATE)` + 写 chain_record |
| `getAsset()` | Web3j 合约查询 | 查 MySQL chain_record 表 |
| `getAssetHistory()` | Web3j 合约查询历史 | 查 MySQL chain_record 表按 artifact_id 排序 |
| `checkBalance()` | 查 ETH 余额 | 查 user_deposits 字段（保留现有逻辑） |
| `isProvider()` | 不涉及链 | 保持不变 |

**`controller/system/EthController.java`**

- 移除：`/eth/start`（挖矿启动）、`/eth/stop`（挖矿停止）、`/eth/balance`（ETH 余额）、`/eth/get/MinerList`（矿工列表）
- 移除：`/eth/get/BlockNumber`（区块号）、`/eth/add/nodeInfo`（添加节点）
- 保留/新增：查询链上记录状态的简化接口（可选）

### 修改的文件（3个）

| 文件 | 改动 |
|------|------|
| `pom.xml` | 移除 `web3j-core` 4.9.4 依赖 |
| `utils/BlockChainUtil.java` | 移除钱包生成，简化为数据哈希工具（SHA256） |
| `application-prod.yml` | 移除 `gethAddress`、`keystore.dir`，新增 `huayichain` 配置段 |

### 数据库变更

新增 `chain_record` 表：

```sql
CREATE TABLE chain_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  artifact_id VARCHAR(255) NOT NULL COMMENT '资产ID',
  artifact_no VARCHAR(255) COMMENT '资产编号',
  action VARCHAR(50) NOT NULL COMMENT '操作类型: CREATE/UPDATE/TRANSFER',
  operator_id INT COMMENT '操作者用户ID',
  operator_name VARCHAR(100) COMMENT '操作者名称',
  data_hash VARCHAR(128) COMMENT 'SHA256数据哈希',
  tx_hash VARCHAR(255) COMMENT '链上交易哈希',
  chain_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '上链状态: PENDING/SUCCESS/FAILED',
  remark VARCHAR(500) COMMENT '备注',
  chain_time DATETIME COMMENT '上链时间',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_artifact_id (artifact_id),
  INDEX idx_chain_status (chain_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='链上记录表';
```

`orga_user` 表的 `eth_account`、`eth_credentials` 字段保留不动，不做结构变更。注册新用户时不再生成以太坊钱包。

### 前端影响

- 资产相关 API 接口签名不变，前端不需要改动
- 挖矿/余额/矿工列表页面通过后端 `getAsyncRoutes` 不返回对应菜单项来隐藏，前端代码不动

### 配置示例

```yaml
huayichain:
  base-url: ${HUAYICHAIN_BASE_URL}
  api-key: ${HUAYICHAIN_API_KEY:}
```

---

## 环境变量汇总（application-prod.yml）

| 变量 | 用途 | 示例 |
|------|------|------|
| `DB_HOST` | RDS 内网地址 | `rm-xxxx.mysql.rds.aliyuncs.com` |
| `DB_PORT` | RDS 端口 | `3306` |
| `DB_USERNAME` | 数据库用户名 | `tds_admin` |
| `DB_PASSWORD` | 数据库密码 | - |
| `OSS_ENDPOINT` | OSS 地域端点 | `https://oss-cn-hangzhou.aliyuncs.com` |
| `OSS_ACCESS_KEY_ID` | 阿里云 AK | - |
| `OSS_ACCESS_KEY_SECRET` | 阿里云 SK | - |
| `OSS_BUCKET_NAME` | Bucket 名称 | `trustdataspace` |
| `HUAYICHAIN_BASE_URL` | 华艺链 API 地址 | `http://chain.example.com` |
| `HUAYICHAIN_API_KEY` | 华艺链 API Key | - |
| `JWT_SECRET` | JWT 签名密钥 | - |

---

## 不改动的部分

- Redis：继续自建（ECS Docker 或阿里云 Redis），配置格式不变
- EDC Connector：独立部署，与本次改造无关
- 前端代码：无需改动
- 现有业务逻辑：用户管理、角色权限、日志、充值等全部保留
