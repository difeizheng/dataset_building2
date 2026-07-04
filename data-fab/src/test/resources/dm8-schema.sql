-- ============================================
-- DM8 (达梦) Test Database Schema for data-fab
-- Dialect differences from H2:
--   TEXT -> CLOB / VARCHAR(4000)
--   BOOLEAN -> INT (0/1)
--   TIMESTAMP -> TIMESTAMP (compatible)
--   LIMIT/OFFSET -> supported in DM8 compatible mode
--   CURRENT_TIMESTAMP -> SYSDATE or CURRENT_TIMESTAMP (both work)
-- ============================================

-- 数据集表
CREATE TABLE IF NOT EXISTS t_dataset (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    modality INT,
    data_level INT,
    status INT DEFAULT 0,
    version VARCHAR(50),
    sample_count INT DEFAULT 0,
    total_size BIGINT DEFAULT 0,
    tags VARCHAR(4000),
    fair_metadata CLOB,
    croissant_metadata CLOB,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 数据样本表
CREATE TABLE IF NOT EXISTS t_data_sample (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    name VARCHAR(500) NOT NULL,
    modality INT,
    data_level INT,
    status INT DEFAULT 0,
    source VARCHAR(500),
    file_path VARCHAR(2000),
    file_size BIGINT,
    mime_type VARCHAR(200),
    file_hash VARCHAR(64),
    metadata CLOB,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sample_dataset ON t_data_sample(dataset_id);
CREATE INDEX IF NOT EXISTS idx_sample_modality ON t_data_sample(modality);
CREATE INDEX IF NOT EXISTS idx_sample_status ON t_data_sample(status);

-- 清洗任务表
CREATE TABLE IF NOT EXISTS t_etl_task (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    status INT DEFAULT 0,
    processed_count INT DEFAULT 0,
    success_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    error_message VARCHAR(4000),
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_etl_task_dataset ON t_etl_task(dataset_id);

-- 清洗规则表
CREATE TABLE IF NOT EXISTS t_etl_rule (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    modality INT,
    rule_type VARCHAR(100),
    rule_config CLOB,
    priority INT DEFAULT 0,
    enabled INT DEFAULT 1,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 标注任务表
CREATE TABLE IF NOT EXISTS t_label_task (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    modality INT,
    label_type VARCHAR(100),
    label_schema CLOB,
    double_blind INT DEFAULT 1,
    annotator_count INT DEFAULT 0,
    annotator_ids VARCHAR(4000),
    arbitrator_id BIGINT,
    status INT DEFAULT 0,
    total_samples INT DEFAULT 0,
    labeled_count INT DEFAULT 0,
    kappa_score DOUBLE,
    sop_description VARCHAR(4000),
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_label_task_dataset ON t_label_task(dataset_id);

-- 标注记录表
CREATE TABLE IF NOT EXISTS t_label_record (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    sample_id BIGINT NOT NULL,
    annotator_id BIGINT NOT NULL,
    annotations CLOB,
    status INT DEFAULT 0,
    duration_seconds BIGINT,
    submit_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_label_record_task ON t_label_record(task_id);
CREATE INDEX IF NOT EXISTS idx_label_record_sample ON t_label_record(sample_id);

-- 标注仲裁表
CREATE TABLE IF NOT EXISTS t_label_arbitration (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    sample_id BIGINT NOT NULL,
    original_kappa DOUBLE,
    conflict_annotations CLOB,
    arbitrator_id BIGINT,
    final_annotations CLOB,
    status INT DEFAULT 0,
    arbitrate_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 质量评估任务表
CREATE TABLE IF NOT EXISTS t_qa_task (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    modality INT,
    batch VARCHAR(100),
    status INT DEFAULT 0,
    metrics CLOB,
    passed INT DEFAULT 0,
    gates CLOB,
    review_stage INT DEFAULT 0,
    review_comment VARCHAR(4000),
    reviewer VARCHAR(100),
    review_time TIMESTAMP,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_qa_task_dataset ON t_qa_task(dataset_id);

-- 数据集交付记录表
CREATE TABLE IF NOT EXISTS t_delivery_record (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    version VARCHAR(50),
    dataset_uri VARCHAR(500),
    license VARCHAR(200),
    lineage CLOB,
    access_policy CLOB,
    fair_metadata CLOB,
    croissant_metadata CLOB,
    status INT DEFAULT 0,
    download_count INT DEFAULT 0,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_delivery_dataset ON t_delivery_record(dataset_id);

-- 审计日志表
CREATE TABLE IF NOT EXISTS t_audit_log (
    id BIGINT PRIMARY KEY,
    resource_id BIGINT,
    resource_type VARCHAR(100),
    action VARCHAR(100),
    user_id BIGINT,
    result VARCHAR(100),
    description VARCHAR(4000),
    ip_address VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 数据审批表
CREATE TABLE IF NOT EXISTS t_data_approval (
    id BIGINT PRIMARY KEY,
    approval_no VARCHAR(100),
    resource_id BIGINT,
    resource_type VARCHAR(100),
    action VARCHAR(100),
    applicant_id BIGINT,
    approver_id BIGINT,
    status INT DEFAULT 0,
    comment VARCHAR(4000),
    approve_time TIMESTAMP,
    expire_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
