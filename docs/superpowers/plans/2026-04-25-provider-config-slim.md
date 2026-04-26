# provider_config 瘦身（方案 C）实施计划

> 关联 issue: https://github.com/zjucgl/trustdataspace-service/issues/14

**Goal:** 按方案 C 改造 — `provider_config` 仅保留 EDC 节点部署信息；删 db_* 字段；新增 `enabled_dataplane_extensions` 声明启用哪些 dataplane 扩展。部署模板按扩展条件渲染。Asset 多源表单 / vault 集成延后。

---

## Tasks

### 后端

- [x] **T1**: 写 SQL 迁移脚本（drop db_*、add enabled_dataplane_extensions）
- [x] **T2**: 更新 `ProviderConfigEntity` / `ProviderConfigDTO`
- [x] **T3**: 更新 `ProviderConfigServiceImpl.add()/update()` 移除 db_* 赋值
- [x] **T4**: `docker-compose.yml.ftl` 按扩展条件渲染（自定义 `# @if:<ext>` 标记）
- [x] **T5**: `deploy.sh.ftl` 增加扩展 jar 存在性检查
- [x] **T6**: `ProviderDeployServiceImpl` 用自定义 `# @if:<ext>` 标记实现条件块（不引入 FreeMarker 依赖），注入 `enabledDataplaneExtensions`

### 前端

- [x] **T7**: `form.vue` 删数据源分组、加扩展多选 checkbox-group（http 默认勾选且禁用取消）
- [x] **T8**: `index.vue` 详情对话框删数据库地址、加启用扩展展示
- [x] **T9**: `api/provider/index.ts` 同步 `ProviderConfigData` 类型，新增 `DataplaneExtension` 字面量联合 + `ALL_DATAPLANE_EXTENSIONS`

### 集成

- [x] **T10**: 后端 `mvn clean compile` + 前端 `pnpm build`（含 vue-tsc 类型检查），均通过
- [x] **T11**: 后端（db9e46c）/ 前端（1114265）各自 commit + push origin/main
- [x] **T12**: 生产部署（RDS ALTER → 重打 JAR → scp → compose rebuild → rsync dist → restart nginx）
- [x] **T13**: 生产验证 — 三种组合（http only / http+s3+jdbc / http+sftp）下载脚本均正确条件挂载

### 延后任务后续处理

- [x] **Asset 多源表单**（commit ddf79fc）— `src/views/basic/asset/form.vue` 改为按 sourceType (HttpData/AmazonS3/JdbcDataAddress) 切换字段组；`index.vue.buildDataAddress()` 按类型构造 EDC dataAddress；HttpData 保持原 MinIO 上传逻辑做向后兼容
- [x] **迁移 providerQA**（无操作）— providerQA 的 EDC 在外部服务器 `211.91.61.25:29391`，不归本服务管理；本服务的 `provider_config` 仅 SmartPort 一行，db_* 为空，无数据需迁移
- [ ] **vault 集成**（**阻塞**）— 依赖外部基础设施（HashiCorp Vault 或阿里云 KMS 的 endpoint + token + 凭证路径），需用户先提供。实现位置：Asset dataAddress 的密码/AK/SK 字段，运行时由 EDC 的 VaultResolver 解析 `vault:` 前缀引用
