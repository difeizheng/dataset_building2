-- H2 Test Database Schema for data-fab
-- Compatible with DM8 syntax via MODE=DMDB

-- 数据集表
CREATE TABLE IF NOT EXISTS t_dataset (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    modality INT,
    data_level INT,
    status INT DEFAULT 0,
    version VARCHAR(50),
    sample_count INT DEFAULT 0,
    total_size BIGINT DEFAULT 0,
    tags TEXT,
    fair_metadata TEXT,
    croissant_metadata TEXT,
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
    name VARCHAR(255) NOT NULL,
    modality INT,
    data_level INT,
    status INT DEFAULT 0,
    source VARCHAR(255),
    file_path VARCHAR(1000),
    file_size BIGINT,
    mime_type VARCHAR(100),
    file_hash VARCHAR(64),
    metadata TEXT,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

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
    error_message TEXT,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 清洗规则表
CREATE TABLE IF NOT EXISTS t_etl_rule (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    modality INT,
    rule_type VARCHAR(50),
    rule_config TEXT,
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
    label_type VARCHAR(50),
    label_schema TEXT,
    double_blind INT DEFAULT 1,
    annotator_count INT DEFAULT 0,
    annotator_ids TEXT,
    arbitrator_id BIGINT,
    status INT DEFAULT 0,
    total_samples INT DEFAULT 0,
    labeled_count INT DEFAULT 0,
    kappa_score DECIMAL(10,4),
    sop_description TEXT,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 标注记录表
CREATE TABLE IF NOT EXISTS t_label_record (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    sample_id BIGINT NOT NULL,
    annotator_id BIGINT NOT NULL,
    annotations TEXT,
    status INT DEFAULT 0,
    duration_seconds BIGINT,
    submit_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 标注仲裁表
CREATE TABLE IF NOT EXISTS t_label_arbitration (
    id BIGINT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    sample_id BIGINT NOT NULL,
    original_kappa DECIMAL(10,4),
    conflict_annotations TEXT,
    arbitrator_id BIGINT,
    final_annotation TEXT,
    status INT DEFAULT 0,
    arbitrate_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 质量评估任务表
CREATE TABLE IF NOT EXISTS t_qa_task (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    modality INT,
    batch VARCHAR(100),
    status INT DEFAULT 0,
    metrics TEXT,
    passed INT DEFAULT 0,
    gates TEXT,
    review_stage INT DEFAULT 0,
    review_comment TEXT,
    reviewer VARCHAR(100),
    review_time TIMESTAMP,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 数据集交付记录表
CREATE TABLE IF NOT EXISTS t_delivery_record (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    version VARCHAR(50),
    dataset_uri VARCHAR(1000),
    license VARCHAR(100),
    lineage TEXT,
    access_policy TEXT,
    fair_metadata TEXT,
    croissant_metadata TEXT,
    status INT DEFAULT 0,
    download_count INT DEFAULT 0,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 审计日志表
CREATE TABLE IF NOT EXISTS t_audit_log (
    id BIGINT PRIMARY KEY,
    resource_id BIGINT,
    resource_type VARCHAR(50),
    action VARCHAR(50),
    user_id BIGINT,
    result VARCHAR(50),
    description TEXT,
    ip_address VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 数据审批表
CREATE TABLE IF NOT EXISTS t_data_approval (
    id BIGINT PRIMARY KEY,
    approval_no VARCHAR(100),
    resource_id BIGINT,
    resource_type VARCHAR(50),
    action VARCHAR(50),
    applicant_id BIGINT,
    approver_id BIGINT,
    status INT DEFAULT 0,
    comment TEXT,
    approve_time TIMESTAMP,
    expire_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
