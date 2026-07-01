-- ============================================
-- 数据集建设子系统 数据库初始化脚本
-- 数据库：达梦 DM8
-- ============================================

-- 数据集表
CREATE TABLE t_dataset (
    id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    modality INT NOT NULL COMMENT '1-文本,2-图像,3-音频,4-视频',
    data_level INT NOT NULL COMMENT '1-L1公开,2-L2内部,3-L3敏感,4-L4核心',
    status INT DEFAULT 0 COMMENT '0-新建,1-原始,2-已清洗,3-已标注,4-质量通过,5-质量不通过,6-已发布,7-已归档',
    version VARCHAR(50) DEFAULT '1.0.0',
    sample_count INT DEFAULT 0,
    total_size BIGINT DEFAULT 0,
    tags VARCHAR(2000),
    fair_metadata CLOB,
    croissant_metadata CLOB,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 数据样本表
CREATE TABLE t_data_sample (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    name VARCHAR(500) NOT NULL,
    modality INT NOT NULL,
    data_level INT NOT NULL,
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

CREATE INDEX idx_sample_dataset ON t_data_sample(dataset_id);
CREATE INDEX idx_sample_modality ON t_data_sample(modality);
CREATE INDEX idx_sample_status ON t_data_sample(status);

-- 清洗规则表
CREATE TABLE t_etl_rule (
    id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
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

-- 清洗任务表
CREATE TABLE t_etl_task (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    task_name VARCHAR(200) NOT NULL,
    status INT DEFAULT 0 COMMENT '0-待执行,1-执行中,2-已完成,3-失败',
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

CREATE INDEX idx_etl_task_dataset ON t_etl_task(dataset_id);

-- 标注任务表
CREATE TABLE t_label_task (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    task_name VARCHAR(200) NOT NULL,
    modality INT NOT NULL,
    label_type VARCHAR(100),
    label_schema CLOB,
    double_blind INT DEFAULT 1,
    annotator_count INT DEFAULT 0,
    annotator_ids VARCHAR(2000),
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

CREATE INDEX idx_label_task_dataset ON t_label_task(dataset_id);

-- 标注记录表
CREATE TABLE t_label_record (
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

CREATE INDEX idx_label_record_task ON t_label_record(task_id);
CREATE INDEX idx_label_record_sample ON t_label_record(sample_id);

-- 仲裁记录表
CREATE TABLE t_label_arbitration (
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
CREATE TABLE t_qa_task (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    modality INT NOT NULL,
    batch VARCHAR(100),
    status INT DEFAULT 0,
    metrics CLOB,
    passed INT DEFAULT 0,
    gates CLOB,
    review_stage INT DEFAULT 0 COMMENT '0-自动预审,1-人工复审,2-专家终审,3-发布',
    review_comment VARCHAR(4000),
    reviewer VARCHAR(100),
    review_time TIMESTAMP,
    create_by VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(100),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE INDEX idx_qa_task_dataset ON t_qa_task(dataset_id);

-- 交付记录表
CREATE TABLE t_delivery_record (
    id BIGINT PRIMARY KEY,
    dataset_id BIGINT NOT NULL,
    version VARCHAR(50) NOT NULL,
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

CREATE INDEX idx_delivery_dataset ON t_delivery_record(dataset_id);
