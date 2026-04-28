# DID 文档生成与 did:web 发布实施计划

> 关联：[issue #18](https://github.com/zjucgl/trustdataspace-service/issues/18)

**Goal:** Provider 部署完成后，`http://<deployHost>/<providerName>/did.json` 返回合法的 W3C DID 文档（含 publicKeyJwk + service endpoint），第三方可解析该 DID。

---

## Tasks

- [x] **T1** `did.json.template` — Java 模板填 `${participantDid}`/`${deployHost}`/`${controlplaneProtocolPort}`/`${controlplaneMgmtPort}`；deploy.sh 填 `{{X}}`/`{{Y}}`
- [x] **T2** `deploy.sh.ftl` — 用 `openssl pkey -text` + awk 解析、xxd 转十六进制、base64url 编码后 sed 替换
- [x] **T3** `nginx-provider.conf.ftl` — 增加 `/<providerName>/did.json` 路由（alias + Content-Type: application/did+json）
- [x] **T4** `ProviderDeployServiceImpl` — 把 did.json.template 加入 zip
- [x] **T5** 编译 + 提交 + 推送（commit 3bc0750）
- [x] **T6** 部署生产 + 验证 zip 内容（4 文件齐全：deploy.sh / docker-compose.yml / nginx-provider.conf / did.json.template，placeholder 渲染正确）
- [x] **T7** 端到端：deploy.sh 跑通，did.json 是合法 JSON，x/y 各 32 字节（P-256 EC 坐标对），nginx 配置已生成（运维侧合并即可对外暴露）
