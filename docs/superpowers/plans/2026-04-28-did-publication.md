# DID 文档生成与 did:web 发布实施计划

> 关联：[issue #18](https://github.com/zjucgl/trustdataspace-service/issues/18)

**Goal:** Provider 部署完成后，`http://<deployHost>/<providerName>/did.json` 返回合法的 W3C DID 文档（含 publicKeyJwk + service endpoint），第三方可解析该 DID。

---

## Tasks

- [x] **T1** `did.json.template` — Java 模板填 `${participantDid}`/`${deployHost}`/`${controlplaneProtocolPort}`/`${controlplaneMgmtPort}`；deploy.sh 填 `{{X}}`/`{{Y}}`
- [x] **T2** `deploy.sh.ftl` — 用 `openssl pkey -text` + awk 解析、xxd 转十六进制、base64url 编码后 sed 替换
- [x] **T3** `nginx-provider.conf.ftl` — 增加 `/<providerName>/did.json` 路由（alias + Content-Type: application/did+json）
- [x] **T4** `ProviderDeployServiceImpl` — 把 did.json.template 加入 zip
- [ ] **T5** 编译 + 提交 + 推送
- [ ] **T6** 部署生产 + 验证 zip 内容
- [ ] **T7** 端到端：测试 Provider 部署 → did.json 内容正确 + curl 可访问
