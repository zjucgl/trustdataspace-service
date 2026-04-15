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
