# Provider 健康自检实施计划

> 关联：[issue #19](https://github.com/zjucgl/trustdataspace-service/issues/19)

**Goal:** 平台后端可主动 ping Provider 4 项关键端点；前端"详情"对话框点击按钮触发并显示结果。

---

## Tasks

- [x] **T1** `HealthCheckResultDTO` + `ProviderHealthService`（CompletableFuture + RestTemplate，3s connect / 5s read 超时；DSP 检测把 4xx 视为"endpoint 存在"）
- [x] **T2** `ProviderConfigController` add `GET /provider/healthCheck/{id}`
- [x] **T3** 前端 API + types（HealthCheckItem / HealthCheckResult，无 any/unknown）
- [x] **T4** 前端详情对话框：健康自检 section + 4 项结果表格 + 立即检测按钮（el-table 显示项/OK/HTTP/耗时/错误）
- [x] **T5** 编译 + 提交 + 推送（后端 `0032aea`、前端 `d914efe`）
- [x] **T6** 部署 + 验证 SmartPort（startup ✅ 200/44ms, management ✅ 200/764ms, dsp ✅ 404/143ms, did ❌ 301 — 与预期完全一致：DID 路由仅在 Provider 自己 nginx 上）
