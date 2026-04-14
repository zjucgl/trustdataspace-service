# 阿里云 RDS + OSS + 华艺链改造 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将后端从自建 MySQL/MinIO/Geth 迁移到阿里云 RDS + OSS + 华艺链 REST API

**Architecture:** 三层改造——配置层（RDS）、存储层（OSS 替换 MinIO SDK）、区块链层（REST API 替换 Web3j）。保持所有 Controller 接口签名不变，前端零改动。

**Tech Stack:** Spring Boot 2.7.6, Java 8, MyBatis-Plus, Aliyun OSS SDK 3.17.4, RestTemplate

---

## File Map

### 新建文件
| 文件 | 职责 |
|------|------|
| `src/main/resources/application-prod.yml` | 生产环境配置（RDS + OSS + 华艺链） |
| `src/main/java/sz/lab/config/cfg/OssConfig.java` | 阿里云 OSS 客户端 Bean |
| `src/main/java/sz/lab/utils/OssUtils.java` | OSS 文件操作工具类 |
| `src/main/java/sz/lab/chain/client/HuayiChainClient.java` | 华艺链 REST 客户端 |
| `src/main/java/sz/lab/chain/model/ChainResponse.java` | 华艺链响应 DTO |
| `src/main/java/sz/lab/entity/system/ChainRecordEntity.java` | 上链记录实体 |
| `src/main/java/sz/lab/mapper/system/chain/ChainRecordMapper.java` | 上链记录 Mapper |
| `src/main/resources/mapper/system/ChainRecordMapper.xml` | 上链记录 SQL 映射 |
| `src/main/resources/db/chain_record.sql` | chain_record 建表 SQL |

### 删除文件
| 文件 | 原职责 |
|------|--------|
| `src/main/java/sz/lab/config/cfg/MinioConfig.java` | MinIO 客户端配置 |
| `src/main/java/sz/lab/utils/MinioUtils.java` | MinIO 文件操作 |
| `src/main/java/sz/lab/utils/contract/Web3jClient.java` | Web3j 单例 |
| `src/main/java/sz/lab/utils/contract/AssetTraceability.java` | Solidity 合约 Wrapper |
| `src/main/java/sz/lab/utils/contract/PaymentContract.java` | 支付合约 Wrapper |
| `src/main/java/sz/lab/utils/contract/GetCredentials.java` | Keystore 凭证 |
| `src/main/java/sz/lab/service/web3j/Web3Service.java` | 挖矿/余额接口 |
| `src/main/java/sz/lab/service/web3j/impl/Web3ServiceImpl.java` | 挖矿/余额实现 |

### 修改文件
| 文件 | 改动内容 |
|------|---------|
| `pom.xml` | 移除 minio + web3j 依赖，新增 aliyun-sdk-oss |
| `src/main/java/sz/lab/controller/system/UtilsController.java` | MinioUtils → OssUtils |
| `src/main/java/sz/lab/service/system/asset/impl/IAssetServiceImpl.java` | MinioUtils → OssUtils |
| `src/main/java/sz/lab/service/trace/AssetTraceService.java` | 移除 AssetTraceability 类型引用 |
| `src/main/java/sz/lab/service/trace/impl/AssetTraceServiceImpl.java` | 全部重写为华艺链 REST 调用 |
| `src/main/java/sz/lab/controller/system/EthController.java` | 移除挖矿/余额/节点端点 |
| `src/main/java/sz/lab/service/system/ethnode/ISystemNodeinfoService.java` | 移除 Web3j 方法 |
| `src/main/java/sz/lab/service/system/ethnode/impl/SystemNodeinfoServiceImpl.java` | 移除 Web3j 调用 |
| `src/main/java/sz/lab/utils/BlockChainUtil.java` | 移除钱包生成，改为哈希工具 |
| `src/main/java/sz/lab/service/orga/user/impl/UserServiceImpl.java` | 移除钱包生成、调整 purchaseAsset 返回值处理 |
| `src/main/java/sz/lab/service/system/recharge/Impl/PaymentServiceImpl.java` | 移除 Web3j 链上支付，改为纯数据库充值 |
| `src/main/java/sz/lab/controller/system/SystemTraceHistoryController.java` | 返回类型适配 |
| `src/test/java/sz/lab/mvdwebend/MvdWebEndApplicationTests.java` | 移除 Web3j 测试代码 |

---

### Task 1: pom.xml 依赖替换

**Files:**
- Modify: `pom.xml:144-166`

- [ ] **Step 1: 替换 minio 依赖为 aliyun-sdk-oss，移除 web3j**

将 pom.xml 中的 minio 和 web3j 依赖块替换：

```xml
<!-- 移除以下两个依赖 -->
<!-- minio (行 144-149) -->
<!-- web3j (行 161-166) -->

<!-- 新增阿里云 OSS -->
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.17.4</version>
</dependency>
```

- [ ] **Step 2: 验证依赖解析**

Run: `cd /usr/github/trustdataspace-service && mvn dependency:resolve -q 2>&1 | tail -5`
Expected: BUILD SUCCESS（忽略编译错误，此时只验证依赖能下载）

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: 替换 minio/web3j 依赖为 aliyun-sdk-oss"
```

---

### Task 2: 新建 application-prod.yml

**Files:**
- Create: `src/main/resources/application-prod.yml`

- [ ] **Step 1: 创建生产环境配置文件**

```yaml
server:
  port: 9007

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
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    cache: true
    encoding: UTF-8
    mode: HTML
    servlet:
      content-type: text/html
    check-template-location: true
  redis:
    database: 0
    host: ${REDIS_HOST:127.0.0.1}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 1000s
    lettuce:
      pool:
        max-active: 100
        max-wait: -1
        max-idle: 10
        min-idle: 2
  pagehelper:
    helperDialect: mysql
    reasonable: true
    supportMethodsArguments: true
    params: count=countSql
  servlet:
    multipart:
      enabled: true
      file-size-threshold: 0
      max-file-size: 5MB
      max-request-size: 5MB
  aop:
    auto: true

mybatis-plus:
  type-aliases-package: sz.lab.entity
  global-config:
    db-config:
      logic-delete-field: is_deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      update-strategy: not_null
  configuration:
    map-underscore-to-camel-case: true
  mapper-locations: classpath*:/mapper/**/*.xml

logging:
  level:
    sz.lab.mapper: info
  config: classpath:logback-spring.xml
  file:
    path: ./logs

custom-attribute:
  dept-id: 1
  verification-code-expire: 30
  jwt:
    token-expire: 240
    secret: ${JWT_SECRET:ADB8E3D5838A0AE8E274014928CE2CEE}
  lock-time: 30
  pwd-reset-time: 90

aliyun:
  oss:
    endpoint: ${OSS_ENDPOINT}
    accessKeyId: ${OSS_ACCESS_KEY_ID}
    accessKeySecret: ${OSS_ACCESS_KEY_SECRET}
    bucketName: ${OSS_BUCKET_NAME:trustdataspace}

huayichain:
  base-url: ${HUAYICHAIN_BASE_URL}
  api-key: ${HUAYICHAIN_API_KEY:}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/application-prod.yml
git commit -m "config: 新增生产环境配置 application-prod.yml"
```

---

### Task 3: OSS 存储层替换

**Files:**
- Create: `src/main/java/sz/lab/config/cfg/OssConfig.java`
- Create: `src/main/java/sz/lab/utils/OssUtils.java`
- Delete: `src/main/java/sz/lab/config/cfg/MinioConfig.java`
- Delete: `src/main/java/sz/lab/utils/MinioUtils.java`
- Modify: `src/main/java/sz/lab/controller/system/UtilsController.java`
- Modify: `src/main/java/sz/lab/service/system/asset/impl/IAssetServiceImpl.java`

- [ ] **Step 1: 创建 OssConfig.java**

```java
package sz.lab.config.cfg;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
```

- [ ] **Step 2: 创建 OssUtils.java**

```java
package sz.lab.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Component
public class OssUtils {
    @Resource
    private OSS ossClient;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    public List<String> upload(MultipartFile[] multipartFile) {
        List<String> names = new ArrayList<>(multipartFile.length);
        for (MultipartFile file : multipartFile) {
            String fileName = file.getOriginalFilename();
            String[] split = fileName.split("\\.");
            if (split.length > 1) {
                fileName = split[0] + "_" + System.currentTimeMillis() + "." + split[1];
            } else {
                fileName = fileName + System.currentTimeMillis();
            }
            try (InputStream in = file.getInputStream()) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                ossClient.putObject(bucketName, fileName, in, metadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
            names.add(fileName);
        }
        return names;
    }

    public List<String> uploadWithoutTime(MultipartFile[] multipartFile) {
        List<String> names = new ArrayList<>(multipartFile.length);
        for (MultipartFile file : multipartFile) {
            String fileName = file.getOriginalFilename();
            try (InputStream in = file.getInputStream()) {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                ossClient.putObject(bucketName, fileName, in, metadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
            names.add(fileName);
        }
        return names;
    }

    public ResponseEntity<byte[]> download(String fileName) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, fileName);
            try (InputStream in = ossObject.getObjectContent();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                IOUtils.copy(in, out);
                byte[] bytes = out.toByteArray();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition",
                        "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
                headers.setContentLength(bytes.length);
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setAccessControlExposeHeaders(Arrays.asList("*"));
                return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> listObjectNames(String bucketName) {
        List<String> names = new ArrayList<>();
        ObjectListing listing = ossClient.listObjects(bucketName);
        for (OSSObjectSummary summary : listing.getObjectSummaries()) {
            names.add(summary.getKey());
        }
        return names;
    }

    public boolean removeObject(String bucketName, String objectName) {
        if (ossClient.doesObjectExist(bucketName, objectName)) {
            ossClient.deleteObject(bucketName, objectName);
            return true;
        }
        return false;
    }

    public String createUrl(String fileName) {
        Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
        URL url = ossClient.generatePresignedUrl(bucketName, fileName, expiration);
        return url.toString();
    }
}
```

- [ ] **Step 3: 删除 MinioConfig.java 和 MinioUtils.java**

```bash
git rm src/main/java/sz/lab/config/cfg/MinioConfig.java
git rm src/main/java/sz/lab/utils/MinioUtils.java
```

- [ ] **Step 4: 修改 UtilsController.java**

将 `sz/lab/controller/system/UtilsController.java` 中的 MinioUtils 引用替换为 OssUtils：

```java
// 替换 import
// 旧: import sz.lab.utils.MinioUtils;
// 新: import sz.lab.utils.OssUtils;

// 替换 @Value 注解
// 旧: @Value("${minio.endpoint}")
// 新: @Value("${aliyun.oss.endpoint}")

// 替换字段
// 旧: private MinioUtils minioUtils;
// 新: private OssUtils ossUtils;

// 替换方法调用（两处）
// 旧: minioUtils.upload(...)
// 新: ossUtils.upload(...)
// 旧: minioUtils.uploadWithoutTime(...)
// 新: ossUtils.uploadWithoutTime(...)
```

- [ ] **Step 5: 修改 IAssetServiceImpl.java**

在 `sz/lab/service/system/asset/impl/IAssetServiceImpl.java` 中：

```java
// 替换 import
// 旧: import sz.lab.utils.MinioUtils;
// 新: import sz.lab.utils.OssUtils;

// 替换字段 (行 43)
// 旧: private MinioUtils minioUtils;
// 新: private OssUtils ossUtils;

// 替换方法调用 (行 264)
// 旧: String url = minioUtils.createUrl(fileName);
// 新: String url = ossUtils.createUrl(fileName);
```

- [ ] **Step 6: 在 application-dev.yml 中添加 aliyun.oss 配置**

在 `application-dev.yml` 末尾的 `minio:` 块替换为：

```yaml
aliyun:
  oss:
    endpoint: http://211.91.61.25:29000
    accessKeyId: minioadmin
    accessKeySecret: minioadmin
    bucketName: data
```

注意：开发环境中如果仍然使用 MinIO，阿里云 OSS SDK 可以兼容 S3 协议的 MinIO 端点。

- [ ] **Step 7: Commit**

```bash
git add -A src/main/java/sz/lab/config/cfg/OssConfig.java \
  src/main/java/sz/lab/utils/OssUtils.java \
  src/main/java/sz/lab/controller/system/UtilsController.java \
  src/main/java/sz/lab/service/system/asset/impl/IAssetServiceImpl.java \
  src/main/resources/application-dev.yml
git commit -m "feat: 阿里云 OSS 替换 MinIO 存储层"
```

---

### Task 4: 华艺链客户端和数据模型

**Files:**
- Create: `src/main/java/sz/lab/chain/client/HuayiChainClient.java`
- Create: `src/main/java/sz/lab/chain/model/ChainResponse.java`
- Create: `src/main/java/sz/lab/entity/system/ChainRecordEntity.java`
- Create: `src/main/java/sz/lab/mapper/system/chain/ChainRecordMapper.java`
- Create: `src/main/resources/mapper/system/ChainRecordMapper.xml`
- Create: `src/main/resources/db/chain_record.sql`

- [ ] **Step 1: 创建华艺链响应 DTO**

```java
package sz.lab.chain.model;

import lombok.Data;

@Data
public class ChainResponse {
    private String txHash;
    private String status;
}
```

- [ ] **Step 2: 创建华艺链 REST 客户端**

```java
package sz.lab.chain.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import sz.lab.chain.model.ChainResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class HuayiChainClient {

    @Value("${huayichain.base-url}")
    private String baseUrl;

    @Value("${huayichain.api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 提交记录到华艺链
     *
     * @param artifactId  资产ID
     * @param artifactNo  资产编号
     * @param action      操作类型: CREATE / UPDATE / TRANSFER
     * @param operator    操作者名称
     * @param dataHash    数据SHA256哈希
     * @param remark      备注
     * @return 华艺链响应（txHash + status）
     */
    public ChainResponse submitRecord(String artifactId, String artifactNo,
                                       String action, String operator,
                                       String dataHash, String remark) {
        String url = baseUrl + "/api/v1/records";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("X-API-Key", apiKey);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("artifact_id", artifactId);
        body.put("artifact_no", artifactNo);
        body.put("action", action);
        body.put("operator", operator);
        body.put("timestamp", Instant.now().toString());
        body.put("data_hash", dataHash);
        body.put("remark", remark);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, entity, ChainResponse.class);
    }
}
```

- [ ] **Step 3: 创建 ChainRecordEntity**

```java
package sz.lab.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chain_record")
public class ChainRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String artifactId;
    private String artifactNo;
    private String action;
    private Integer operatorId;
    private String operatorName;
    private String dataHash;
    private String txHash;
    private String chainStatus;
    private String remark;
    private LocalDateTime chainTime;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 4: 创建 ChainRecordMapper**

```java
package sz.lab.mapper.system.chain;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import sz.lab.entity.system.ChainRecordEntity;

@Mapper
public interface ChainRecordMapper extends BaseMapper<ChainRecordEntity> {
}
```

- [ ] **Step 5: 创建 ChainRecordMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="sz.lab.mapper.system.chain.ChainRecordMapper">
</mapper>
```

- [ ] **Step 6: 创建建表 SQL**

```sql
-- src/main/resources/db/chain_record.sql
CREATE TABLE IF NOT EXISTS chain_record (
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

- [ ] **Step 7: Commit**

```bash
git add src/main/java/sz/lab/chain/ \
  src/main/java/sz/lab/entity/system/ChainRecordEntity.java \
  src/main/java/sz/lab/mapper/system/chain/ \
  src/main/resources/mapper/system/ChainRecordMapper.xml \
  src/main/resources/db/chain_record.sql
git commit -m "feat: 新增华艺链客户端、ChainRecord 数据模型和建表SQL"
```

---

### Task 5: 删除 Web3j 相关文件

**Files:**
- Delete: `src/main/java/sz/lab/utils/contract/Web3jClient.java`
- Delete: `src/main/java/sz/lab/utils/contract/AssetTraceability.java`
- Delete: `src/main/java/sz/lab/utils/contract/PaymentContract.java`
- Delete: `src/main/java/sz/lab/utils/contract/GetCredentials.java`
- Delete: `src/main/java/sz/lab/service/web3j/Web3Service.java`
- Delete: `src/main/java/sz/lab/service/web3j/impl/Web3ServiceImpl.java`

- [ ] **Step 1: 删除所有 Web3j 专用文件**

```bash
git rm src/main/java/sz/lab/utils/contract/Web3jClient.java
git rm src/main/java/sz/lab/utils/contract/AssetTraceability.java
git rm src/main/java/sz/lab/utils/contract/PaymentContract.java
git rm src/main/java/sz/lab/utils/contract/GetCredentials.java
git rm src/main/java/sz/lab/service/web3j/Web3Service.java
git rm src/main/java/sz/lab/service/web3j/impl/Web3ServiceImpl.java
```

- [ ] **Step 2: Commit**

```bash
git commit -m "refactor: 删除 Web3j 合约和服务文件"
```

---

### Task 6: 重写 BlockChainUtil

**Files:**
- Modify: `src/main/java/sz/lab/utils/BlockChainUtil.java`

- [ ] **Step 1: 重写为数据哈希工具**

将整个文件替换为：

```java
package sz.lab.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class BlockChainUtil {

    /**
     * 计算 SHA256 哈希
     */
    public String sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/sz/lab/utils/BlockChainUtil.java
git commit -m "refactor: BlockChainUtil 简化为 SHA256 哈希工具"
```

---

### Task 7: 重写 AssetTraceService 接口和实现

**Files:**
- Modify: `src/main/java/sz/lab/service/trace/AssetTraceService.java`
- Modify: `src/main/java/sz/lab/service/trace/impl/AssetTraceServiceImpl.java`

- [ ] **Step 1: 重写 AssetTraceService 接口**

移除所有 AssetTraceability 类型引用，替换为通用返回类型：

```java
package sz.lab.service.trace;

import sz.lab.dto.system.OperateResultDTO;

import java.math.BigInteger;
import java.util.List;

public interface AssetTraceService {
    void registerAsset(String assetId, String name, String description, BigInteger price, String operatorName) throws Exception;

    void updatePrice(String assetId, BigInteger newPrice) throws Exception;

    OperateResultDTO purchaseAsset(String assetId, Integer userId) throws Exception;

    void updateStatus(String assetId, String newStatus) throws Exception;

    List getAssetHistory(String assetId, Integer userId) throws Exception;

    OperateResultDTO checkBalance(String assetId, Integer userId) throws Exception;

    OperateResultDTO isProvider(Integer userId, String assetId) throws Exception;
}
```

- [ ] **Step 2: 重写 AssetTraceServiceImpl**

将整个文件替换为：

```java
package sz.lab.service.trace.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Service;
import sz.lab.chain.client.HuayiChainClient;
import sz.lab.chain.model.ChainResponse;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.basic.PersonalAssetEntity;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.system.ChainRecordEntity;
import sz.lab.entity.system.IAssetEntity;
import sz.lab.mapper.basic.PersonalAssetMapper;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.system.asset.IAssetMapper;
import sz.lab.mapper.system.chain.ChainRecordMapper;
import sz.lab.service.trace.AssetTraceService;
import sz.lab.utils.BlockChainUtil;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssetTraceServiceImpl implements AssetTraceService {
    @Resource
    private IAssetMapper assetMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private PersonalAssetMapper personalAssetMapper;
    @Resource
    private ChainRecordMapper chainRecordMapper;
    @Resource
    private HuayiChainClient chainClient;
    @Resource
    private BlockChainUtil blockChainUtil;

    @Override
    public void registerAsset(String assetId, String name, String description,
                              BigInteger price, String operatorName) throws Exception {
        String dataHash = blockChainUtil.sha256(assetId + name + description + price);
        ChainResponse response = chainClient.submitRecord(
                assetId, assetId, "CREATE", operatorName, dataHash,
                "注册资产: " + name);
        saveChainRecord(assetId, "CREATE", null, operatorName, dataHash, response);
    }

    @Override
    public void updatePrice(String assetId, BigInteger newPrice) throws Exception {
        String dataHash = blockChainUtil.sha256(assetId + newPrice);
        ChainResponse response = chainClient.submitRecord(
                assetId, assetId, "UPDATE", "system", dataHash,
                "更新价格: " + newPrice);
        saveChainRecord(assetId, "UPDATE", null, "system", dataHash, response);
    }

    @Override
    public OperateResultDTO purchaseAsset(String assetId, Integer userId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class)
                .eq(IAssetEntity::getAssetId, assetId)
                .eq(IAssetEntity::getIsFirst, 1));
        if (assetEntity == null) {
            return new OperateResultDTO(false, "资产不存在", null);
        }
        UserEntity userEntity = userMapper.selectOne(Wrappers.lambdaQuery(UserEntity.class)
                .eq(UserEntity::getUserId, userId));
        if (userEntity == null) {
            return new OperateResultDTO(false, "用户不存在", null);
        }

        String traceAssetId = assetId + System.currentTimeMillis();
        String dataHash = blockChainUtil.sha256(traceAssetId + userId + assetEntity.getAssetPrice());

        // 先注册资产到链上
        ChainResponse registerResponse = chainClient.submitRecord(
                traceAssetId, assetEntity.getAssetId(), "CREATE", "system", dataHash,
                "交易资产注册");
        saveChainRecord(traceAssetId, "CREATE", null, "system", dataHash, registerResponse);

        // 再记录转移
        ChainResponse transferResponse = chainClient.submitRecord(
                traceAssetId, assetEntity.getAssetId(), "TRANSFER", userEntity.getUserName(), dataHash,
                "资产交易转移");
        saveChainRecord(traceAssetId, "TRANSFER", userId, userEntity.getUserName(), dataHash, transferResponse);

        String txHash = transferResponse != null ? transferResponse.getTxHash() : "";
        return new OperateResultDTO(true, "购买成功",
                new Object[]{traceAssetId, userId, txHash,
                        BigInteger.ZERO, assetEntity.getAssetPrice(), userEntity.getUserName()});
    }

    @Override
    public void updateStatus(String assetId, String newStatus) throws Exception {
        String dataHash = blockChainUtil.sha256(assetId + newStatus);
        ChainResponse response = chainClient.submitRecord(
                assetId, assetId, "UPDATE", "system", dataHash,
                "更新状态: " + newStatus);
        saveChainRecord(assetId, "UPDATE", null, "system", dataHash, response);
    }

    @Override
    public List getAssetHistory(String assetId, Integer userId) throws Exception {
        PersonalAssetEntity personalAssetEntity = personalAssetMapper.selectOne(
                Wrappers.lambdaQuery(PersonalAssetEntity.class)
                        .eq(PersonalAssetEntity::getAssetId, assetId)
                        .eq(PersonalAssetEntity::getUserId, userId));
        if (personalAssetEntity == null) {
            return null;
        }

        String ethAssetId = personalAssetEntity.getEthAssetId();
        if (ethAssetId == null || ethAssetId.isEmpty()) {
            ethAssetId = assetId;
        }

        List<ChainRecordEntity> records = chainRecordMapper.selectList(
                new LambdaQueryWrapper<ChainRecordEntity>()
                        .eq(ChainRecordEntity::getArtifactId, ethAssetId)
                        .orderByAsc(ChainRecordEntity::getCreatedAt));

        return records.stream()
                .map(r -> {
                    if (r.getOperatorId() != null) {
                        UserEntity user = userMapper.selectById(r.getOperatorId());
                        return user != null ? user.getUserName() : r.getOperatorName();
                    }
                    return r.getOperatorName();
                })
                .collect(Collectors.toList());
    }

    @Override
    public OperateResultDTO checkBalance(String assetId, Integer userId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(
                Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
        UserEntity userEntity = userMapper.selectById(userId);
        if (userEntity.getUserDeposits() < assetEntity.getAssetPrice()) {
            return new OperateResultDTO(false, "用户余额不足", null);
        }
        return new OperateResultDTO(true, "用户余额足够", null);
    }

    @Override
    public OperateResultDTO isProvider(Integer userId, String assetId) throws Exception {
        IAssetEntity assetEntity = assetMapper.selectOne(
                Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
        if (assetEntity == null) {
            return new OperateResultDTO(false, "资产不存在", null);
        }
        if (Objects.equals(assetEntity.getUserId(), userId)) {
            return new OperateResultDTO(false, "资产拥有者不能购买自己的资产", null);
        }
        return new OperateResultDTO(true, "不是资产拥有者", null);
    }

    private void saveChainRecord(String artifactId, String action, Integer operatorId,
                                  String operatorName, String dataHash, ChainResponse response) {
        ChainRecordEntity record = new ChainRecordEntity();
        record.setArtifactId(artifactId);
        record.setArtifactNo(artifactId);
        record.setAction(action);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setDataHash(dataHash);
        if (response != null) {
            record.setTxHash(response.getTxHash());
            record.setChainStatus(response.getStatus());
            record.setChainTime(LocalDateTime.now());
        } else {
            record.setChainStatus("FAILED");
        }
        record.setCreatedAt(LocalDateTime.now());
        chainRecordMapper.insert(record);
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/sz/lab/service/trace/AssetTraceService.java \
  src/main/java/sz/lab/service/trace/impl/AssetTraceServiceImpl.java
git commit -m "feat: AssetTraceService 改为华艺链 REST API 实现"
```

---

### Task 8: 重写 EthController 和 SystemNodeinfoService

**Files:**
- Modify: `src/main/java/sz/lab/controller/system/EthController.java`
- Modify: `src/main/java/sz/lab/service/system/ethnode/ISystemNodeinfoService.java`
- Modify: `src/main/java/sz/lab/service/system/ethnode/impl/SystemNodeinfoServiceImpl.java`

- [ ] **Step 1: 简化 EthController**

移除挖矿、余额、矿工列表端点，仅保留 checkBalance 和 getNodeList（数据库查询）：

```java
package sz.lab.controller.system;

import org.springframework.web.bind.annotation.*;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.system.ethnode.ISystemNodeinfoService;
import sz.lab.service.trace.AssetTraceService;

import javax.annotation.Resource;

@RestController
@RequestMapping("/eth")
public class EthController extends BaseController {

    @Resource
    private ISystemNodeinfoService systemNodeinfoService;

    @Resource
    private AssetTraceService assetTraceService;

    @RequestMapping("/get/NodeList")
    public OperateResultDTO getNodeList(@RequestParam("nodeName") String nodeName) {
        return systemNodeinfoService.getNodeList(nodeName);
    }

    @RequestMapping("/check/balance")
    public OperateResultDTO checkBalance(@RequestParam("assetId") String assetId) throws Exception {
        Integer userid = userId.get();
        return assetTraceService.checkBalance(assetId, userid);
    }
}
```

- [ ] **Step 2: 简化 ISystemNodeinfoService**

移除 getBlockNumber 和 getMinerList 方法：

```java
package sz.lab.service.system.ethnode;

import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemNodeinfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISystemNodeinfoService extends IService<SystemNodeinfo> {
    OperateResultDTO getNodeList(String nodeName);
}
```

- [ ] **Step 3: 简化 SystemNodeinfoServiceImpl**

移除所有 Web3j 调用，仅保留数据库查询：

```java
package sz.lab.service.system.ethnode.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemNodeinfo;
import sz.lab.mapper.system.ethnode.SystemNodeinfoMapper;
import sz.lab.service.system.ethnode.ISystemNodeinfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SystemNodeinfoServiceImpl extends ServiceImpl<SystemNodeinfoMapper, SystemNodeinfo> implements ISystemNodeinfoService {

    @Resource
    private SystemNodeinfoMapper systemNodeinfoMapper;

    @Override
    public OperateResultDTO getNodeList(String nodeName) {
        LambdaQueryWrapper<SystemNodeinfo> queryWrapper = new LambdaQueryWrapper<>();
        if (nodeName != null) {
            queryWrapper.like(SystemNodeinfo::getNodeName, nodeName);
        } else {
            queryWrapper.eq(SystemNodeinfo::getIsDelete, 0);
        }
        List<SystemNodeinfo> nodeinfo = systemNodeinfoMapper.selectList(queryWrapper);
        return new OperateResultDTO(true, "获取节点信息成功", nodeinfo);
    }
}
```

- [ ] **Step 4: Commit**

```bash
git add src/main/java/sz/lab/controller/system/EthController.java \
  src/main/java/sz/lab/service/system/ethnode/ISystemNodeinfoService.java \
  src/main/java/sz/lab/service/system/ethnode/impl/SystemNodeinfoServiceImpl.java
git commit -m "refactor: EthController 和 NodeinfoService 移除 Web3j 依赖"
```

---

### Task 9: 修改 UserServiceImpl（移除钱包生成）

**Files:**
- Modify: `src/main/java/sz/lab/service/orga/user/impl/UserServiceImpl.java`

- [ ] **Step 1: 修改 add 方法，移除钱包生成**

在 `UserServiceImpl.java` 中：

移除 import：
```java
// 删除这些 import:
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
```

修改 add 方法（约行 142-155）：
```java
@Override
public OperateResultDTO add(UserDTO userDTO) throws Exception {
    UserEntity userEntity = dtoToEntity(userDTO);
    // 不再生成以太坊钱包
    userMapper.insertUser(userEntity);
    if(userDTO.getRoleIdList()!= null && !userDTO.getRoleIdList().isEmpty()) {
        userRoleService.updateUserRoleCode(userEntity.getUserId(), userDTO.getRoleIdList());
    }
    UserEntity user = userMapper.selectUserByLoginCode(userEntity.getLoginCode());
    return new OperateResultDTO(true,"成功",user.getUserId());
}
```

修改 updateprice 方法（约行 190-228）中 purchaseAsset 的返回值处理：

```java
@Override
public OperateResultDTO updateprice(Integer userId, String assetId) throws Exception {
    IAssetEntity assetEntity = assetMapper.selectOne(Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
    UserEntity userEntity = userMapper.selectById(userId);
    if (userEntity.getUserDeposits() < assetEntity.getAssetPrice()) {
        return new OperateResultDTO(false,"用户余额不足",null);
    }
    if (assetEntity.getAssetQuantity() == 0) {
        return new OperateResultDTO(false,"该商品已售完",null);
    }
    OperateResultDTO operateResultDTO = assetTraceService.purchaseAsset(assetId, userId);
    int code0 = 0;
    Object[] result = (Object[]) operateResultDTO.getResult();
    // 华艺链不再有 Gas 费用，totalcost 设为 0
    BigInteger totalcost = BigInteger.ZERO;

    if (operateResultDTO.isSuccess()) {
        code0 = userMapper.update(null,Wrappers.lambdaUpdate(UserEntity.class)
                .eq(UserEntity::getUserId, userId)
                .set(UserEntity::getUserDeposits, userEntity.getUserDeposits()-assetEntity.getAssetPrice())
                .set(UserEntity::getUserDepositsExtra, userEntity.getUserDepositsExtra()));
        UserEntity userEntitySeller = userMapper.selectById(assetEntity.getUserId());
        code0 = userMapper.update(null,Wrappers.lambdaUpdate(UserEntity.class)
                .eq(UserEntity::getUserId, assetEntity.getUserId())
                .set(UserEntity::getUserDeposits, userEntitySeller.getUserDeposits()+assetEntity.getAssetPrice()));
    }
    int code2 = userMapper.updatequantity(assetEntity.getAssetId());

    if (code0 > 0 && code2 > 0) {
        if (assetEntity.getAssetQuantity() == 1) {
            assetMapper.delete(Wrappers.lambdaQuery(IAssetEntity.class).eq(IAssetEntity::getAssetId, assetId));
        }
        return new OperateResultDTO(true,"修改成功",operateResultDTO);
    } else {
        return new OperateResultDTO(false, "修改失败", null);
    }
}
```

移除 `blockChainUtil` 字段（如果不再被其他方法使用）。注意保留 `assetTraceService` 字段。

- [ ] **Step 2: Commit**

```bash
git add src/main/java/sz/lab/service/orga/user/impl/UserServiceImpl.java
git commit -m "refactor: UserServiceImpl 移除钱包生成和 Web3j 依赖"
```

---

### Task 10: 重写 PaymentServiceImpl（移除链上支付）

**Files:**
- Modify: `src/main/java/sz/lab/service/system/recharge/Impl/PaymentServiceImpl.java`

- [ ] **Step 1: 简化为纯数据库充值**

将整个文件替换为：

```java
package sz.lab.service.system.recharge.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sz.lab.chain.client.HuayiChainClient;
import sz.lab.chain.model.ChainResponse;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.entity.system.RechargeLogEntity;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.mapper.system.recharge.RechargeLogMapper;
import sz.lab.mapper.system.recharge.RechargeMapper;
import sz.lab.service.system.recharge.PaymentService;
import sz.lab.utils.BlockChainUtil;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private RechargeLogMapper rechargeLogMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RechargeMapper rechargeMapper;
    @Autowired
    private HuayiChainClient chainClient;
    @Autowired
    private BlockChainUtil blockChainUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperateResultDTO sendPayment(Integer logId, Integer userId, BigInteger amount, Integer fromId) {
        // 数据库充值
        BigInteger extra = amount;
        rechargeMapper.recharge(userId, amount, extra);

        UserEntity userEntity = userMapper.selectUserById(userId);
        String userName = userEntity.getUserName();

        UserEntity adminEntity = userMapper.selectUserById(fromId);

        try {
            // 记录到华艺链
            String dataHash = blockChainUtil.sha256(
                    "recharge-" + userId + "-" + amount + "-" + System.currentTimeMillis());
            ChainResponse response = chainClient.submitRecord(
                    "recharge-" + logId, "recharge-" + logId,
                    "TRANSFER", adminEntity.getUserName(), dataHash,
                    "充值 " + amount + " 给 " + userName);

            String txHash = response != null ? response.getTxHash() : "";
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            RechargeLogEntity rechargeLogEntity = new RechargeLogEntity();
            rechargeLogEntity.setId(logId);
            rechargeLogEntity.setFromEthAccount(adminEntity.getEthAccount());
            rechargeLogEntity.setFromId(fromId);
            rechargeLogEntity.setUserId(userId);
            rechargeLogEntity.setUserName(userName);
            rechargeLogEntity.setUserEthAccount(userEntity.getEthAccount());
            rechargeLogEntity.setAmount(amount);
            rechargeLogEntity.setTxHash(txHash);
            rechargeLogEntity.setStatus(1);
            rechargeLogMapper.update(rechargeLogEntity);

            return new OperateResultDTO(true, "充值成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("充值失败", e);
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/sz/lab/service/system/recharge/Impl/PaymentServiceImpl.java
git commit -m "refactor: PaymentServiceImpl 移除 Web3j，改用华艺链记录"
```

---

### Task 11: 修改 SystemTraceHistoryController 和测试文件

**Files:**
- Modify: `src/main/java/sz/lab/controller/system/SystemTraceHistoryController.java`
- Modify: `src/test/java/sz/lab/mvdwebend/MvdWebEndApplicationTests.java`

- [ ] **Step 1: SystemTraceHistoryController 不需要改动**

该 Controller 调用 `assetTraceService.getAssetHistory()`，接口返回类型仍然是 `List`，无需改动。确认即可。

- [ ] **Step 2: 清理测试文件**

将 `MvdWebEndApplicationTests.java` 中的 Web3j 相关 import 和注释代码清理：

```java
package sz.lab.mvdwebend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MvdWebEndApplicationTests {

    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add src/test/java/sz/lab/mvdwebend/MvdWebEndApplicationTests.java
git commit -m "test: 清理测试文件中的 Web3j 引用"
```

---

### Task 12: 清理 application-dev.yml 配置

**Files:**
- Modify: `src/main/resources/application-dev.yml`

- [ ] **Step 1: 更新开发环境配置**

在 `application-dev.yml` 中：

1. 将 `minio:` 配置段替换为 `aliyun.oss` 配置段：

```yaml
# 删除:
# minio:
#   endpoint: http://211.91.61.25:29000
#   accessKey: minioadmin
#   secretKey: minioadmin

# 新增:
aliyun:
  oss:
    endpoint: http://211.91.61.25:29000
    accessKeyId: minioadmin
    accessKeySecret: minioadmin
    bucketName: data
```

2. 将 `gethAddress` 和 `keystore` 替换为 `huayichain` 配置：

```yaml
# 删除:
# keystore:
#   dir: /data/li/data/keystore
# gethAddress: http://192.168.14.3:8545

# 新增:
huayichain:
  base-url: http://192.168.14.3:8545
  api-key:
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/application-dev.yml
git commit -m "config: application-dev.yml 替换 minio/geth 为 oss/huayichain 配置"
```

---

### Task 13: 编译验证

**Files:** 无新文件

- [ ] **Step 1: Maven 编译**

Run: `cd /usr/github/trustdataspace-service && mvn clean compile -q 2>&1 | tail -20`
Expected: BUILD SUCCESS

- [ ] **Step 2: 修复编译错误（如有）**

检查编译输出，逐个修复遗漏的 import 或引用问题。常见问题：
- `TraceLog` 注解的 AOP 切面可能引用了已删除的类
- 某些 Entity/DTO 可能有 Web3j 类型的字段

- [ ] **Step 3: 确认无 web3j 或 minio 残留引用**

Run: `grep -r "import org.web3j\|import io.minio" src/main/java/ 2>/dev/null`
Expected: 无输出

Run: `grep -r "import org.web3j\|import io.minio" src/test/java/ 2>/dev/null`
Expected: 无输出

- [ ] **Step 4: Commit（如有修复）**

```bash
git add -A
git commit -m "fix: 修复编译错误，清理残留引用"
```

---

### Task 14: 最终验证和总结提交

**Files:** 无新文件

- [ ] **Step 1: 完整构建（跳过测试）**

Run: `cd /usr/github/trustdataspace-service && mvn clean package -DskipTests -q 2>&1 | tail -10`
Expected: BUILD SUCCESS，生成 JAR 文件

- [ ] **Step 2: 确认 JAR 生成**

Run: `ls -lh target/*.jar`
Expected: 看到 `trusted-data-space-back-end-0.0.1-SNAPSHOT.jar`

- [ ] **Step 3: 查看完整变更统计**

Run: `git log --oneline` 确认所有提交
Run: `git diff --stat HEAD~N` 查看变更文件统计（N=提交数量）
