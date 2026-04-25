# Provider 管理与 EDC 部署脚本生成 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 管理端支持新增数据提供方，保存配置后生成 EDC Connector 部署脚本包，管理员下载执行即可完成 Provider 部署。

**Architecture:** 在现有 Spring Boot 后端新增 `provider` 模块（entity/dto/mapper/service/controller），管理 Provider 配置和端口分配。后端根据配置生成 Docker Compose + deploy.sh 脚本包供下载。前端新增 Provider 管理页面，改造 MVD API 调用支持动态 Provider 路由。

**Tech Stack:** Spring Boot 2.7.6, MyBatis-Plus 3.5.2, MySQL, Vue 3 + Element Plus, Docker Compose, EDC v0.10.1

---

## File Structure

### Backend (新建)
- `src/main/java/sz/lab/entity/provider/ProviderConfigEntity.java` — Provider 配置实体
- `src/main/java/sz/lab/dto/provider/ProviderConfigDTO.java` — Provider 配置 DTO
- `src/main/java/sz/lab/mapper/provider/ProviderConfigMapper.java` — Mapper 接口
- `src/main/java/sz/lab/service/provider/ProviderConfigService.java` — Service 接口
- `src/main/java/sz/lab/service/provider/impl/ProviderConfigServiceImpl.java` — Service 实现
- `src/main/java/sz/lab/controller/provider/ProviderConfigController.java` — REST 控制器
- `src/main/java/sz/lab/service/provider/ProviderDeployService.java` — 部署脚本生成服务
- `src/main/resources/templates/deploy/` — 部署脚本模板目录
- `src/main/resources/templates/deploy/docker-compose.yml.ftl` — Docker Compose 模板
- `src/main/resources/templates/deploy/deploy.sh.ftl` — 部署脚本模板
- `src/main/resources/templates/deploy/nginx-provider.conf.ftl` — Nginx 配置模板
- `src/main/resources/templates/deploy/env.ftl` — 环境变量模板

### Frontend (新建)
- `src/views/provider/index.vue` — Provider 列表页
- `src/views/provider/form.vue` — Provider 新增/编辑表单
- `src/router/modules/provider.ts` — Provider 路由配置
- `src/api/provider/index.ts` — Provider API

### Frontend (修改)
- `src/utils/mvd.ts` — 动态 Provider 路由
- `src/api/mvd/asset.ts` — 去掉硬编码 providerType
- `src/api/mvd/policy.ts` — 同上
- `src/api/mvd/contractdefinition.ts` — 同上

---

### Task 1: 建表 SQL 和 Entity

**Files:**
- Create: `src/main/java/sz/lab/entity/provider/ProviderConfigEntity.java`

- [x] **Step 1: 在 RDS 中创建 provider_config 表**

连接数据库执行：

```sql
CREATE TABLE `provider_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` int DEFAULT NULL COMMENT '关联 orga_dept',
  `provider_name` varchar(100) NOT NULL COMMENT 'Provider 标识名，如 providerA',
  `provider_label` varchar(200) DEFAULT NULL COMMENT 'Provider 显示名称',
  `participant_id` varchar(255) DEFAULT NULL COMMENT 'DID 身份',
  `controlplane_mgmt_port` int DEFAULT 9193 COMMENT 'controlplane management API 端口',
  `controlplane_protocol_port` int DEFAULT 9194 COMMENT 'controlplane protocol 端口',
  `controlplane_public_port` int DEFAULT 9291 COMMENT 'controlplane public API 端口',
  `dataplane_public_port` int DEFAULT 9295 COMMENT 'dataplane public 端口',
  `identity_hub_port` int DEFAULT 7083 COMMENT 'identity-hub 端口',
  `sts_port` int DEFAULT 7084 COMMENT 'STS 端口',
  `db_host` varchar(255) DEFAULT NULL COMMENT '提供方数据库地址',
  `db_port` int DEFAULT 3306 COMMENT '数据库端口',
  `db_name` varchar(100) DEFAULT NULL COMMENT '数据库名',
  `db_readonly_user` varchar(100) DEFAULT NULL COMMENT '只读用户名',
  `db_readonly_pwd` varchar(255) DEFAULT NULL COMMENT '只读密码（加密）',
  `deploy_host` varchar(255) DEFAULT NULL COMMENT 'EDC 部署目标服务器 IP',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING/DEPLOYING/RUNNING/STOPPED',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `is_deleted` int DEFAULT 0,
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modify` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_name` (`provider_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据提供方配置';
```

- [x] **Step 2: 创建 ProviderConfigEntity**

```java
package sz.lab.entity.provider;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("provider_config")
public class ProviderConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dept_id")
    private Integer deptId;

    @TableField("provider_name")
    private String providerName;

    @TableField("provider_label")
    private String providerLabel;

    @TableField("participant_id")
    private String participantId;

    @TableField("controlplane_mgmt_port")
    private Integer controlplaneMgmtPort;

    @TableField("controlplane_protocol_port")
    private Integer controlplaneProtocolPort;

    @TableField("controlplane_public_port")
    private Integer controlplanePublicPort;

    @TableField("dataplane_public_port")
    private Integer dataplanePublicPort;

    @TableField("identity_hub_port")
    private Integer identityHubPort;

    @TableField("sts_port")
    private Integer stsPort;

    @TableField("db_host")
    private String dbHost;

    @TableField("db_port")
    private Integer dbPort;

    @TableField("db_name")
    private String dbName;

    @TableField("db_readonly_user")
    private String dbReadonlyUser;

    @TableField("db_readonly_pwd")
    private String dbReadonlyPwd;

    @TableField("deploy_host")
    private String deployHost;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField(value = "is_deleted")
    @TableLogic
    private Integer isDeleted;

    @TableField(value = "gmt_create", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtCreate;

    @TableField(value = "gmt_modify", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime gmtModify;
}
```

- [ ] **Step 3: 验证编译**

Run: `cd /usr/github/trustdataspace-service && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/sz/lab/entity/provider/ProviderConfigEntity.java
git commit -m "feat: add provider_config table and entity"
```

---

### Task 2: DTO + Mapper

**Files:**
- Create: `src/main/java/sz/lab/dto/provider/ProviderConfigDTO.java`
- Create: `src/main/java/sz/lab/mapper/provider/ProviderConfigMapper.java`

- [x] **Step 1: 创建 ProviderConfigDTO**

```java
package sz.lab.dto.provider;

import lombok.Data;

@Data
public class ProviderConfigDTO {
    private Long id;
    private Integer deptId;
    private String providerName;
    private String providerLabel;
    private String participantId;
    private Integer controlplaneMgmtPort;
    private Integer controlplaneProtocolPort;
    private Integer controlplanePublicPort;
    private Integer dataplanePublicPort;
    private Integer identityHubPort;
    private Integer stsPort;
    private String dbHost;
    private Integer dbPort;
    private String dbName;
    private String dbReadonlyUser;
    private String dbReadonlyPwd;
    private String deployHost;
    private String status;
    private String remark;
    private String deptName;  // 关联部门名称，用于列表展示
}
```

- [ ] **Step 2: 创建 ProviderConfigMapper**

```java
package sz.lab.mapper.provider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import sz.lab.entity.provider.ProviderConfigEntity;

@Mapper
public interface ProviderConfigMapper extends BaseMapper<ProviderConfigEntity> {

    @Select("SELECT COALESCE(MAX(controlplane_mgmt_port), 9999) FROM provider_config WHERE is_deleted = 0")
    Integer getMaxMgmtPort();
}
```

- [ ] **Step 3: 验证编译**

Run: `cd /usr/github/trustdataspace-service && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/sz/lab/dto/provider/ProviderConfigDTO.java \
        src/main/java/sz/lab/mapper/provider/ProviderConfigMapper.java
git commit -m "feat: add ProviderConfigDTO and ProviderConfigMapper"
```

---

### Task 3: Service 层

**Files:**
- Create: `src/main/java/sz/lab/service/provider/ProviderConfigService.java`
- Create: `src/main/java/sz/lab/service/provider/impl/ProviderConfigServiceImpl.java`

- [x] **Step 1: 创建 ProviderConfigService 接口**

```java
package sz.lab.service.provider;

import com.baomidou.mybatisplus.extension.service.IService;
import sz.lab.dto.provider.ProviderConfigDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.provider.ProviderConfigEntity;

public interface ProviderConfigService extends IService<ProviderConfigEntity> {
    OperateResultDTO add(ProviderConfigDTO dto);
    OperateResultDTO list();
    OperateResultDTO detail(Long id);
    OperateResultDTO update(ProviderConfigDTO dto);
    OperateResultDTO remove(Long id);
    OperateResultDTO updateStatus(Long id, String status);
}
```

- [ ] **Step 2: 创建 ProviderConfigServiceImpl**

```java
package sz.lab.service.provider.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sz.lab.dto.provider.ProviderConfigDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.dept.DeptEntity;
import sz.lab.entity.provider.ProviderConfigEntity;
import sz.lab.mapper.orga.dept.DeptMapper;
import sz.lab.mapper.provider.ProviderConfigMapper;
import sz.lab.service.provider.ProviderConfigService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProviderConfigServiceImpl
        extends ServiceImpl<ProviderConfigMapper, ProviderConfigEntity>
        implements ProviderConfigService {

    @Resource
    private ProviderConfigMapper providerConfigMapper;
    @Resource
    private DeptMapper deptMapper;

    private static final int PORT_BASE = 10000;
    private static final int PORT_STEP = 100;

    @Override
    public OperateResultDTO add(ProviderConfigDTO dto) {
        // 检查 providerName 唯一性
        ProviderConfigEntity existing = baseMapper.selectOne(
                Wrappers.lambdaQuery(ProviderConfigEntity.class)
                        .eq(ProviderConfigEntity::getProviderName, dto.getProviderName()));
        if (existing != null) {
            return new OperateResultDTO(false, "Provider名称已存在", null);
        }

        ProviderConfigEntity entity = new ProviderConfigEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus("PENDING");

        // 自动分配端口
        Integer maxPort = providerConfigMapper.getMaxMgmtPort();
        int basePort = Math.max(maxPort + PORT_STEP, PORT_BASE);
        entity.setControlplaneMgmtPort(basePort);
        entity.setControlplaneProtocolPort(basePort + 1);
        entity.setControlplanePublicPort(basePort + 2);
        entity.setDataplanePublicPort(basePort + 3);
        entity.setIdentityHubPort(basePort + 4);
        entity.setStsPort(basePort + 5);

        baseMapper.insert(entity);
        return new OperateResultDTO(true, "创建成功", entity.getId());
    }

    @Override
    public OperateResultDTO list() {
        List<ProviderConfigEntity> entities = baseMapper.selectList(
                Wrappers.lambdaQuery(ProviderConfigEntity.class)
                        .orderByDesc(ProviderConfigEntity::getId));

        List<Integer> deptIds = entities.stream()
                .map(ProviderConfigEntity::getDeptId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Integer, String> deptMap = new java.util.HashMap<>();
        if (!deptIds.isEmpty()) {
            List<DeptEntity> depts = deptMapper.selectListByDeptIds(deptIds);
            deptMap = depts.stream()
                    .collect(Collectors.toMap(DeptEntity::getDeptId, DeptEntity::getDeptName));
        }

        Map<Integer, String> finalDeptMap = deptMap;
        List<ProviderConfigDTO> dtos = entities.stream().map(e -> {
            ProviderConfigDTO d = new ProviderConfigDTO();
            BeanUtils.copyProperties(e, d);
            if (e.getDeptId() != null && finalDeptMap.containsKey(e.getDeptId())) {
                d.setDeptName(finalDeptMap.get(e.getDeptId()));
            }
            return d;
        }).collect(Collectors.toList());

        return new OperateResultDTO(true, "查询成功", dtos);
    }

    @Override
    public OperateResultDTO detail(Long id) {
        ProviderConfigEntity entity = baseMapper.selectById(id);
        if (entity == null) {
            return new OperateResultDTO(false, "Provider不存在", null);
        }
        ProviderConfigDTO dto = new ProviderConfigDTO();
        BeanUtils.copyProperties(entity, dto);
        return new OperateResultDTO(true, "查询成功", dto);
    }

    @Override
    public OperateResultDTO update(ProviderConfigDTO dto) {
        ProviderConfigEntity entity = baseMapper.selectById(dto.getId());
        if (entity == null) {
            return new OperateResultDTO(false, "Provider不存在", null);
        }
        // 只允许修改部分字段
        entity.setProviderLabel(dto.getProviderLabel());
        entity.setDbHost(dto.getDbHost());
        entity.setDbPort(dto.getDbPort());
        entity.setDbName(dto.getDbName());
        entity.setDbReadonlyUser(dto.getDbReadonlyUser());
        entity.setDbReadonlyPwd(dto.getDbReadonlyPwd());
        entity.setDeployHost(dto.getDeployHost());
        entity.setRemark(dto.getRemark());
        baseMapper.updateById(entity);
        return new OperateResultDTO(true, "修改成功", null);
    }

    @Override
    public OperateResultDTO remove(Long id) {
        baseMapper.deleteById(id);
        return new OperateResultDTO(true, "删除成功", null);
    }

    @Override
    public OperateResultDTO updateStatus(Long id, String status) {
        ProviderConfigEntity entity = baseMapper.selectById(id);
        if (entity == null) {
            return new OperateResultDTO(false, "Provider不存在", null);
        }
        entity.setStatus(status);
        baseMapper.updateById(entity);
        return new OperateResultDTO(true, "状态更新成功", null);
    }
}
```

- [ ] **Step 3: 验证编译**

Run: `cd /usr/github/trustdataspace-service && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/sz/lab/service/provider/ProviderConfigService.java \
        src/main/java/sz/lab/service/provider/impl/ProviderConfigServiceImpl.java
git commit -m "feat: add ProviderConfigService with CRUD and port allocation"
```

---

### Task 4: Controller

**Files:**
- Create: `src/main/java/sz/lab/controller/provider/ProviderConfigController.java`

- [x] **Step 1: 创建 ProviderConfigController**

```java
package sz.lab.controller.provider;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sz.lab.controller.BaseController;
import sz.lab.dto.provider.ProviderConfigDTO;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.service.provider.ProviderConfigService;
import sz.lab.service.provider.ProviderDeployService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Validated
@RequestMapping("/provider")
@RestController
public class ProviderConfigController extends BaseController {

    @Resource
    private ProviderConfigService providerConfigService;

    @Resource
    private ProviderDeployService providerDeployService;

    @PostMapping("/add")
    public OperateResultDTO add(@RequestBody ProviderConfigDTO input) {
        return providerConfigService.add(input);
    }

    @PostMapping("/list")
    public OperateResultDTO list() {
        return providerConfigService.list();
    }

    @GetMapping("/detail/{id}")
    public OperateResultDTO detail(@PathVariable("id") Long id) {
        return providerConfigService.detail(id);
    }

    @PostMapping("/update")
    public OperateResultDTO update(@RequestBody ProviderConfigDTO input) {
        return providerConfigService.update(input);
    }

    @PostMapping("/remove/{id}")
    public OperateResultDTO remove(@PathVariable("id") Long id) {
        return providerConfigService.remove(id);
    }

    @PostMapping("/updateStatus")
    public OperateResultDTO updateStatus(@RequestParam Long id, @RequestParam String status) {
        return providerConfigService.updateStatus(id, status);
    }

    @GetMapping("/deploy-script/{id}")
    public void downloadDeployScript(@PathVariable("id") Long id,
                                     HttpServletResponse response) throws IOException {
        providerDeployService.generateAndDownload(id, response);
    }
}
```

- [ ] **Step 2: 验证编译**

此时 ProviderDeployService 还未创建，编译会失败。先创建一个空的接口占位（Task 5 实现）：

```java
package sz.lab.service.provider;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ProviderDeployService {
    void generateAndDownload(Long providerId, HttpServletResponse response) throws IOException;
}
```

Run: `cd /usr/github/trustdataspace-service && mvn compile -q`
Expected: BUILD SUCCESS（会有警告因为 ProviderDeployService 没有实现类，但编译通过）

注：实际上 Spring 启动时会因为找不到 ProviderDeployService 的 Bean 而报错，但编译阶段不影响。Task 5 会立即补全实现类。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/sz/lab/controller/provider/ProviderConfigController.java \
        src/main/java/sz/lab/service/provider/ProviderDeployService.java
git commit -m "feat: add ProviderConfigController with deploy script endpoint"
```

---

### Task 5: 部署脚本生成服务

**Files:**
- Create: `src/main/java/sz/lab/service/provider/impl/ProviderDeployServiceImpl.java`
- Create: `src/main/resources/templates/deploy/docker-compose.yml.ftl`
- Create: `src/main/resources/templates/deploy/deploy.sh.ftl`
- Create: `src/main/resources/templates/deploy/nginx-provider.conf.ftl`

- [x] **Step 1: 创建 Docker Compose 模板**

文件 `src/main/resources/templates/deploy/docker-compose.yml.ftl`：

```yaml
# EDC Connector for ${providerName}
# Generated at ${generatedAt}
version: '3.8'

services:
  ${providerName}-controlplane:
    image: eclipse-temurin:21-jre-alpine
    container_name: ${providerName}-controlplane
    volumes:
      - ./jars/controlplane.jar:/app/controlplane.jar
      - ./config:/app/config
    ports:
      - "${controlplaneMgmtPort}:${controlplaneMgmtPort}"
      - "${controlplaneProtocolPort}:${controlplaneProtocolPort}"
      - "${controlplanePublicPort}:${controlplanePublicPort}"
    environment:
      - EDC_FS_CONFIG=/app/config/controlplane.properties
    entrypoint: ["java", "-jar", "/app/controlplane.jar"]
    restart: unless-stopped

  ${providerName}-dataplane:
    image: eclipse-temurin:21-jre-alpine
    container_name: ${providerName}-dataplane
    volumes:
      - ./jars/dataplane.jar:/app/dataplane.jar
      - ./config:/app/config
    ports:
      - "${dataplanePublicPort}:${dataplanePublicPort}"
    environment:
      - EDC_FS_CONFIG=/app/config/dataplane.properties
    entrypoint: ["java", "-jar", "/app/dataplane.jar"]
    restart: unless-stopped

  ${providerName}-identity-hub:
    image: eclipse-temurin:21-jre-alpine
    container_name: ${providerName}-identity-hub
    volumes:
      - ./jars/identity-hub.jar:/app/identity-hub.jar
      - ./config:/app/config
    ports:
      - "${identityHubPort}:${identityHubPort}"
    environment:
      - EDC_FS_CONFIG=/app/config/identity-hub.properties
    entrypoint: ["java", "-jar", "/app/identity-hub.jar"]
    restart: unless-stopped

  ${providerName}-sts:
    image: eclipse-temurin:21-jre-alpine
    container_name: ${providerName}-sts
    volumes:
      - ./jars/sts.jar:/app/sts.jar
      - ./config:/app/config
    ports:
      - "${stsPort}:${stsPort}"
    environment:
      - EDC_FS_CONFIG=/app/config/sts.properties
    entrypoint: ["java", "-jar", "/app/sts.jar"]
    restart: unless-stopped
```

- [ ] **Step 2: 创建 deploy.sh 模板**

文件 `src/main/resources/templates/deploy/deploy.sh.ftl`：

```bash
#!/bin/bash
# EDC Connector deployment script for ${providerName}
# Generated at ${generatedAt}
set -e

PROVIDER_NAME="${providerName}"
DEPLOY_DIR="/opt/edc/${providerName}"
CALLBACK_URL="${callbackUrl}"
PROVIDER_ID="${providerId}"

echo "=========================================="
echo "  Deploying EDC Connector: $PROVIDER_NAME"
echo "=========================================="

# 1. Create directory structure
echo "[1/6] Creating directories..."
mkdir -p $DEPLOY_DIR/{jars,config,credentials}

# 2. Copy JARs (from shared location or download)
echo "[2/6] Checking JAR files..."
JAR_SOURCE="/opt/edc/shared/jars"
if [ ! -d "$JAR_SOURCE" ]; then
    echo "ERROR: Shared JAR directory not found at $JAR_SOURCE"
    echo "Please copy EDC v0.10.1 JARs (controlplane.jar, dataplane.jar, identity-hub.jar, sts.jar) to $JAR_SOURCE"
    exit 1
fi
cp $JAR_SOURCE/*.jar $DEPLOY_DIR/jars/

# 3. Generate DID key pair
echo "[3/6] Generating DID key pair..."
openssl ecparam -name prime256v1 -genkey -noout -out $DEPLOY_DIR/credentials/private-key.pem
openssl ec -in $DEPLOY_DIR/credentials/private-key.pem -pubout -out $DEPLOY_DIR/credentials/public-key.pem

# 4. Copy config and docker-compose
echo "[4/6] Setting up configuration..."
cp docker-compose.yml $DEPLOY_DIR/
cp config/* $DEPLOY_DIR/config/ 2>/dev/null || true

# 5. Start containers
echo "[5/6] Starting containers..."
cd $DEPLOY_DIR
docker compose up -d

# 6. Wait and verify
echo "[6/6] Verifying deployment..."
sleep 10
if docker compose ps | grep -q "running"; then
    echo "SUCCESS: All containers are running"
    # Callback to update status
    if [ -n "$CALLBACK_URL" ]; then
        curl -s -X POST "$CALLBACK_URL/provider/updateStatus?id=$PROVIDER_ID&status=RUNNING" \
             -H "Content-Type: application/json" || echo "Warning: callback failed"
    fi
else
    echo "WARNING: Some containers may not be running. Check with: docker compose ps"
fi

echo ""
echo "=========================================="
echo "  Deployment complete!"
echo "  Management API: http://$(hostname -I | awk '{print $1}'):${controlplaneMgmtPort}"
echo "=========================================="
```

- [ ] **Step 3: 创建 Nginx 配置模板**

文件 `src/main/resources/templates/deploy/nginx-provider.conf.ftl`：

```nginx
# Nginx proxy config for ${providerName}
# Add this to your main nginx.conf server block

# ${providerName} - Controlplane Management API
location /${providerName}/management/ {
    proxy_pass http://${deployHost}:${controlplaneMgmtPort}/management/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

# ${providerName} - Controlplane Protocol API
location /${providerName}/protocol/ {
    proxy_pass http://${deployHost}:${controlplaneProtocolPort}/protocol/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

# ${providerName} - Dataplane Public API
location /${providerName}/public/ {
    proxy_pass http://${deployHost}:${dataplanePublicPort}/public/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}
```

- [ ] **Step 4: 创建 ProviderDeployServiceImpl**

```java
package sz.lab.service.provider.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import sz.lab.entity.provider.ProviderConfigEntity;
import sz.lab.mapper.provider.ProviderConfigMapper;
import sz.lab.service.provider.ProviderDeployService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ProviderDeployServiceImpl implements ProviderDeployService {

    @Resource
    private ProviderConfigMapper providerConfigMapper;

    @Value("${server.port:9007}")
    private int serverPort;

    @Override
    public void generateAndDownload(Long providerId, HttpServletResponse response) throws IOException {
        ProviderConfigEntity config = providerConfigMapper.selectById(providerId);
        if (config == null) {
            response.sendError(404, "Provider not found");
            return;
        }

        Map<String, String> vars = buildVars(config);

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + config.getProviderName() + "-deploy.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            addTemplateToZip(zos, "templates/deploy/docker-compose.yml.ftl",
                    "docker-compose.yml", vars);
            addTemplateToZip(zos, "templates/deploy/deploy.sh.ftl",
                    "deploy.sh", vars);
            addTemplateToZip(zos, "templates/deploy/nginx-provider.conf.ftl",
                    "nginx-provider.conf", vars);
        }
    }

    private Map<String, String> buildVars(ProviderConfigEntity config) {
        Map<String, String> vars = new HashMap<>();
        vars.put("providerName", config.getProviderName());
        vars.put("providerId", String.valueOf(config.getId()));
        vars.put("controlplaneMgmtPort", String.valueOf(config.getControlplaneMgmtPort()));
        vars.put("controlplaneProtocolPort", String.valueOf(config.getControlplaneProtocolPort()));
        vars.put("controlplanePublicPort", String.valueOf(config.getControlplanePublicPort()));
        vars.put("dataplanePublicPort", String.valueOf(config.getDataplanePublicPort()));
        vars.put("identityHubPort", String.valueOf(config.getIdentityHubPort()));
        vars.put("stsPort", String.valueOf(config.getStsPort()));
        vars.put("deployHost", config.getDeployHost() != null ? config.getDeployHost() : "127.0.0.1");
        vars.put("callbackUrl", "https://ds.huayihui.art/api");
        vars.put("generatedAt", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return vars;
    }

    private void addTemplateToZip(ZipOutputStream zos, String templatePath,
                                   String entryName, Map<String, String> vars) throws IOException {
        ClassPathResource resource = new ClassPathResource(templatePath);
        String content;
        try (InputStream is = resource.getInputStream()) {
            content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Simple template variable replacement: ${varName} -> value
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        zos.putNextEntry(new ZipEntry(entryName));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
```

- [ ] **Step 5: 验证编译**

Run: `cd /usr/github/trustdataspace-service && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/sz/lab/service/provider/impl/ProviderDeployServiceImpl.java \
        src/main/resources/templates/deploy/
git commit -m "feat: add deploy script generation service with templates"
```

---

### Task 6: 前端 — Provider API 和路由

**Files:**
- Create: `src/api/provider/index.ts` (前端)
- Create: `src/router/modules/provider.ts` (前端)

- [x] **Step 1: 创建 Provider API**

文件 `/usr/github/trustdataspace-front/src/api/provider/index.ts`：

```typescript
import { http } from "@/utils/http";

export type ProviderConfigData = {
  id?: number;
  deptId?: number;
  providerName?: string;
  providerLabel?: string;
  participantId?: string;
  controlplaneMgmtPort?: number;
  controlplaneProtocolPort?: number;
  controlplanePublicPort?: number;
  dataplanePublicPort?: number;
  identityHubPort?: number;
  stsPort?: number;
  dbHost?: string;
  dbPort?: number;
  dbName?: string;
  dbReadonlyUser?: string;
  dbReadonlyPwd?: string;
  deployHost?: string;
  status?: string;
  remark?: string;
  deptName?: string;
};

export const getProviderList = () => {
  return http.request<OperateResult<ProviderConfigData[]>>("post", "/provider/list");
};

export const addProvider = (data: ProviderConfigData) => {
  return http.request<OperateResult<number>>("post", "/provider/add", { data });
};

export const getProviderDetail = (id: number) => {
  return http.request<OperateResult<ProviderConfigData>>("get", `/provider/detail/${id}`);
};

export const updateProvider = (data: ProviderConfigData) => {
  return http.request<OperateResult<any>>("post", "/provider/update", { data });
};

export const removeProvider = (id: number) => {
  return http.request<OperateResult<any>>("post", `/provider/remove/${id}`);
};

export const downloadDeployScript = (id: number) => {
  window.open(`/api/provider/deploy-script/${id}`, "_blank");
};
```

- [x] **Step 2: 创建路由配置**

文件 `/usr/github/trustdataspace-front/src/router/modules/provider.ts`：

```typescript
export default {
  path: "/provider",
  meta: {
    icon: "ep:connection",
    title: "提供方管理",
    rank: 5,
    roles: ["admin"]
  },
  children: [
    {
      path: "/provider/index",
      name: "ProviderList",
      component: () => import("@/views/provider/index.vue"),
      meta: {
        icon: "ep:connection",
        title: "数据提供方",
        showParent: true,
        roles: ["admin"]
      }
    }
  ]
} as RouteConfigsTable;
```

- [x] **Step 3: Commit**

```bash
cd /usr/github/trustdataspace-front
git add src/api/provider/index.ts src/router/modules/provider.ts
git commit -m "feat: add provider API and router config"
```

---

### Task 7: 前端 — Provider 列表页和表单

**Files:**
- Create: `src/views/provider/index.vue` (前端)
- Create: `src/views/provider/form.vue` (前端)

- [x] **Step 1: 创建 Provider 表单组件**

文件 `/usr/github/trustdataspace-front/src/views/provider/form.vue`：

```vue
<template>
  <el-dialog
    :title="data?.id ? '编辑数据提供方' : '新增数据提供方'"
    :model-value="visible"
    width="600px"
    @close="close"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="Provider标识" prop="providerName">
        <el-input
          v-model="formData.providerName"
          placeholder="如 providerA（英文，唯一）"
          :disabled="!!data?.id"
        />
      </el-form-item>
      <el-form-item label="显示名称" prop="providerLabel">
        <el-input v-model="formData.providerLabel" placeholder="如 某某数据提供方" />
      </el-form-item>
      <el-form-item label="关联部门" prop="deptId">
        <el-tree-select
          v-model="formData.deptId"
          :data="deptOptions"
          :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
          placeholder="选择部门"
          check-strictly
        />
      </el-form-item>
      <el-divider content-position="left">数据源配置（可选）</el-divider>
      <el-form-item label="数据库地址">
        <el-input v-model="formData.dbHost" placeholder="如 rm-xxx.mysql.rds.aliyuncs.com" />
      </el-form-item>
      <el-form-item label="数据库端口">
        <el-input-number v-model="formData.dbPort" :min="1" :max="65535" />
      </el-form-item>
      <el-form-item label="数据库名">
        <el-input v-model="formData.dbName" placeholder="数据库 schema 名称" />
      </el-form-item>
      <el-form-item label="只读用户名">
        <el-input v-model="formData.dbReadonlyUser" placeholder="只读账号" />
      </el-form-item>
      <el-form-item label="只读密码">
        <el-input v-model="formData.dbReadonlyPwd" type="password" show-password placeholder="只读密码" />
      </el-form-item>
      <el-divider content-position="left">部署配置</el-divider>
      <el-form-item label="部署服务器IP">
        <el-input v-model="formData.deployHost" placeholder="EDC 部署目标服务器 IP" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="submit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from "vue";
import type { FormInstance, FormRules } from "element-plus";

const props = defineProps<{
  visible: boolean;
  data: any;
  deptOptions: any[];
}>();

const emit = defineEmits(["update:visible", "save"]);

const formRef = ref<FormInstance>();
const formData = reactive({
  id: undefined as number | undefined,
  providerName: "",
  providerLabel: "",
  deptId: undefined as number | undefined,
  dbHost: "",
  dbPort: 3306,
  dbName: "",
  dbReadonlyUser: "",
  dbReadonlyPwd: "",
  deployHost: "",
  remark: ""
});

const rules = reactive<FormRules>({
  providerName: [
    { required: true, message: "请输入Provider标识", trigger: "blur" },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: "仅支持英文字母开头", trigger: "blur" }
  ],
  providerLabel: [
    { required: true, message: "请输入显示名称", trigger: "blur" }
  ]
});

watch(
  () => props.data,
  val => {
    if (val) {
      Object.assign(formData, val);
    } else {
      Object.keys(formData).forEach(key => {
        (formData as any)[key] = key === "dbPort" ? 3306 : undefined;
      });
      formData.providerName = "";
      formData.providerLabel = "";
      formData.dbHost = "";
      formData.dbName = "";
      formData.dbReadonlyUser = "";
      formData.dbReadonlyPwd = "";
      formData.deployHost = "";
      formData.remark = "";
    }
  },
  { immediate: true }
);

function close() {
  emit("update:visible", false);
}

function submit() {
  formRef.value?.validate(valid => {
    if (valid) {
      emit("save", { ...formData });
      close();
    }
  });
}
</script>
```

- [x] **Step 2: 创建 Provider 列表页**

文件 `/usr/github/trustdataspace-front/src/views/provider/index.vue`：

```vue
<template>
  <el-watermark :font="font" :content="watermark" class="h-full">
    <div class="p-8">
      <div class="flex justify-between mb-4">
        <div class="text-lg font-bold">数据提供方管理</div>
        <el-button type="primary" @click="add">新增提供方</el-button>
      </div>

      <div style="height: calc(100vh - 260px)">
        <el-table
          v-loading="loading"
          :data="tableData"
          class="w-full"
          :header-cell-style="{
            backgroundColor: '#F5F7FA',
            color: '#606266',
            fontWeight: 400
          }"
          height="100%"
        >
          <el-table-column type="index" width="60" label="序号" align="center" />
          <el-table-column prop="providerName" label="Provider标识" align="center" width="140" />
          <el-table-column prop="providerLabel" label="显示名称" align="center" />
          <el-table-column prop="deptName" label="关联部门" align="center" />
          <el-table-column prop="status" label="状态" align="center" width="100">
            <template #default="{ row }">
              <el-tag
                :type="statusType(row.status)"
                effect="plain"
              >
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="controlplaneMgmtPort" label="管理端口" align="center" width="100" />
          <el-table-column prop="deployHost" label="部署服务器" align="center" width="140" />
          <el-table-column prop="gmtCreate" label="创建时间" align="center" width="170" />
          <el-table-column label="操作" width="260" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="edit(row)">编辑</el-button>
              <el-button link type="primary" @click="detail(row)">详情</el-button>
              <el-button link type="success" @click="deploy(row)">部署脚本</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <provider-form
        v-model:visible="formVisible"
        :data="formData"
        :dept-options="deptTree"
        @save="save"
      />

      <!-- Detail Dialog -->
      <el-dialog v-model="detailVisible" title="Provider 详情" width="500px">
        <el-descriptions :column="1" border v-if="detailData">
          <el-descriptions-item label="Provider标识">{{ detailData.providerName }}</el-descriptions-item>
          <el-descriptions-item label="显示名称">{{ detailData.providerLabel }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel(detailData.status) }}</el-descriptions-item>
          <el-descriptions-item label="Controlplane管理端口">{{ detailData.controlplaneMgmtPort }}</el-descriptions-item>
          <el-descriptions-item label="Controlplane协议端口">{{ detailData.controlplaneProtocolPort }}</el-descriptions-item>
          <el-descriptions-item label="Controlplane公开端口">{{ detailData.controlplanePublicPort }}</el-descriptions-item>
          <el-descriptions-item label="Dataplane公开端口">{{ detailData.dataplanePublicPort }}</el-descriptions-item>
          <el-descriptions-item label="Identity Hub端口">{{ detailData.identityHubPort }}</el-descriptions-item>
          <el-descriptions-item label="STS端口">{{ detailData.stsPort }}</el-descriptions-item>
          <el-descriptions-item label="数据库地址" v-if="detailData.dbHost">
            {{ detailData.dbHost }}:{{ detailData.dbPort }}/{{ detailData.dbName }}
          </el-descriptions-item>
          <el-descriptions-item label="部署服务器">{{ detailData.deployHost }}</el-descriptions-item>
        </el-descriptions>
      </el-dialog>
    </div>
  </el-watermark>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import {
  type ProviderConfigData,
  getProviderList,
  addProvider,
  updateProvider,
  removeProvider,
  downloadDeployScript
} from "@/api/provider/index";
import { getDeptPage } from "@/api/orga/dept";
import { showErrorMessage, showSuccessMessage } from "@/utils/message";
import { ElMessageBox } from "element-plus";
import providerForm from "./form.vue";
import { getWatermark } from "@/utils/role";

const font = reactive({ color: "rgba(0, 0, 0, .15)" });
const watermark = getWatermark();

const loading = ref(true);
const tableData = ref<ProviderConfigData[]>([]);
const deptTree = ref<any[]>([]);
const formVisible = ref(false);
const formData = ref<ProviderConfigData | null>(null);
const detailVisible = ref(false);
const detailData = ref<ProviderConfigData | null>(null);

function statusType(status: string) {
  const map: Record<string, string> = {
    PENDING: "info",
    DEPLOYING: "warning",
    RUNNING: "success",
    STOPPED: "danger"
  };
  return map[status] || "info";
}

function statusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING: "待部署",
    DEPLOYING: "部署中",
    RUNNING: "运行中",
    STOPPED: "已停止"
  };
  return map[status] || status;
}

async function fetchList() {
  loading.value = true;
  try {
    const res = await getProviderList();
    if (res.success) {
      tableData.value = res.result || [];
    }
  } finally {
    loading.value = false;
  }
}

async function fetchDeptTree() {
  const res = await getDeptPage();
  if (res.success) {
    deptTree.value = res.result || [];
  }
}

function add() {
  formData.value = null;
  formVisible.value = true;
}

function edit(row: ProviderConfigData) {
  formData.value = { ...row };
  formVisible.value = true;
}

function detail(row: ProviderConfigData) {
  detailData.value = row;
  detailVisible.value = true;
}

function deploy(row: ProviderConfigData) {
  downloadDeployScript(row.id!);
}

async function save(data: ProviderConfigData) {
  try {
    const res = data.id
      ? await updateProvider(data)
      : await addProvider(data);
    if (res.success) {
      showSuccessMessage(data.id ? "修改成功" : "创建成功");
      fetchList();
    } else {
      showErrorMessage(res.message);
    }
  } catch (e: any) {
    showErrorMessage(e.message || "操作失败");
  }
}

async function remove(row: ProviderConfigData) {
  await ElMessageBox.confirm(`确认删除提供方 "${row.providerLabel || row.providerName}"？`, "提示", {
    type: "warning"
  });
  const res = await removeProvider(row.id!);
  if (res.success) {
    showSuccessMessage("删除成功");
    fetchList();
  } else {
    showErrorMessage(res.message);
  }
}

onMounted(() => {
  fetchList();
  fetchDeptTree();
});
</script>
```

- [x] **Step 3: 验证前端编译**

Run: `cd /usr/github/trustdataspace-front && pnpm build`
Expected: Build success

- [x] **Step 4: Commit**

```bash
cd /usr/github/trustdataspace-front
git add src/views/provider/index.vue src/views/provider/form.vue
git commit -m "feat: add provider management list and form pages"
```

---

### Task 8: 前端 — 动态 Provider 路由改造

**Files:**
- Modify: `src/utils/mvd.ts` (前端)
- Modify: `src/api/mvd/asset.ts` (前端)
- Modify: `src/api/mvd/policy.ts` (前端)
- Modify: `src/api/mvd/contractdefinition.ts` (前端)

- [x] **Step 1: 改造 mvd.ts 中 getParticipantType()**

将 `/usr/github/trustdataspace-front/src/utils/mvd.ts` 中硬编码的 IP 判断改为从用户 store 获取 providerName：

原文件内容需要修改 `getParticipantType()` 函数：

```typescript
// 旧代码（删除）:
export function getParticipantType(): any {
  const did = useUserStoreHook()?.participantId;
  if (!did) {
    console.log("participantId is not available");
    return "consumer";
  }
  const ipPattern = /(?<=did:web:)[^%]+/;
  const matchResult = did.match(ipPattern);
  if (!matchResult) {
    console.log("No match found for IP pattern in participantId");
    return "consumer";
  }
  const ip = matchResult[0];
  if (ip === "192.168.14.3") {
    return "consumer";
  } else {
    return "providerQA";
  }
}

// 新代码（替换）:
export function getParticipantType(): any {
  const store = useUserStoreHook();
  // 优先使用 store 中的 providerName（由后端 provider_config 提供）
  const providerName = store?.providerName;
  if (providerName) {
    return providerName;
  }
  // 回退：根据 DID 判断
  const did = store?.participantId;
  if (!did) {
    console.log("participantId is not available");
    return "consumer";
  }
  const ipPattern = /(?<=did:web:)[^%]+/;
  const matchResult = did.match(ipPattern);
  if (!matchResult) {
    console.log("No match found for IP pattern in participantId");
    return "consumer";
  }
  const ip = matchResult[0];
  if (ip === "192.168.14.3") {
    return "consumer";
  } else {
    return "providerQA";
  }
}
```

- [x] **Step 2: 改造 asset.ts — 动态 providerType**

修改 `/usr/github/trustdataspace-front/src/api/mvd/asset.ts` 第 4 行：

```typescript
// 旧:
const providerType = "providerQA";

// 新:
import { getParticipantType } from "@/utils/mvd";
```

然后将所有 `providerType +` 替换为 `getParticipantType() +`。

- [x] **Step 3: 同样改造 policy.ts 和 contractdefinition.ts**

检查 `/usr/github/trustdataspace-front/src/api/mvd/policy.ts` 和 `contractdefinition.ts`，如果有硬编码 providerType，做同样的替换。

- [x] **Step 4: 验证前端编译**

Run: `cd /usr/github/trustdataspace-front && pnpm build`
Expected: Build success

- [x] **Step 5: Commit**

```bash
cd /usr/github/trustdataspace-front
git add src/utils/mvd.ts src/api/mvd/asset.ts src/api/mvd/policy.ts src/api/mvd/contractdefinition.ts
git commit -m "feat: dynamic provider routing - replace hardcoded providerQA"
```

---

### Task 9: 后端编译验证和整体测试

**Files:** None (verification only)

- [x] **Step 1: 后端完整编译**

Run: `cd /usr/github/trustdataspace-service && mvn clean compile -q`
Expected: BUILD SUCCESS

- [x] **Step 2: 运行后端单元测试（如果有）**

Run: `cd /usr/github/trustdataspace-service && mvn test -q`
Expected: Tests pass (or no tests to run)

- [x] **Step 3: 前端完整编译**

Run: `cd /usr/github/trustdataspace-front && pnpm build`
Expected: Build success

- [x] **Step 4: 最终 Commit**

```bash
cd /usr/github/trustdataspace-service
git add -A
git commit -m "feat: provider management - complete backend and deploy script generation"
```
