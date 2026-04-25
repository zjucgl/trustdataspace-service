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
- [ ] **T11**: 后端 / 前端各自 commit + push origin/main
- [ ] **T12**: 生产部署（RDS ALTER → 重打 JAR → scp → compose rebuild → rsync dist → restart nginx）
- [ ] **T13**: 生产验证（新建 Provider 勾选 http+s3，下载部署脚本验证 docker-compose 内容）

### 延后（不在本次 scope）

- ~~Asset 多源表单（issue #14 中 Task 4，建议单独 issue）~~
- ~~vault 集成（issue #14 中 Task 3，短期内网明文）~~
- ~~迁移现有 providerQA（issue #14 中 Task 5，条件性，T13 后视情况决定）~~
