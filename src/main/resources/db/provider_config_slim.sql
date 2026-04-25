-- provider_config 瘦身（方案 C）：删除关系库专用字段，新增 dataplane 扩展声明
-- 当前生产 provider_config 表 0 行数据，可直接 DROP 列

ALTER TABLE `provider_config`
  DROP COLUMN `db_host`,
  DROP COLUMN `db_port`,
  DROP COLUMN `db_name`,
  DROP COLUMN `db_readonly_user`,
  DROP COLUMN `db_readonly_pwd`;

ALTER TABLE `provider_config`
  ADD COLUMN `enabled_dataplane_extensions` varchar(500) NOT NULL DEFAULT 'http'
    COMMENT '启用的 dataplane 扩展，逗号分隔：http,s3,jdbc,sftp'
  AFTER `deploy_host`;
