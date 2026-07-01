-- =============================================
-- 知识库建设子系统 (kb-fab) 数据库初始化脚本
-- 数据库：达梦 DM8
-- =============================================

-- 抽取任务表
CREATE TABLE IF NOT EXISTS extraction_task (
    id VARCHAR(64) PRIMARY KEY,
    task_name VARCHAR(200) NOT NULL,
    input_text CLOB,
    extraction_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) DEFAULT 'PENDING',
    result_json CLOB,
    error_message VARCHAR(1000),
    duration_ms BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 本体类表
CREATE TABLE IF NOT EXISTS ontology_class (
    id VARCHAR(64) PRIMARY KEY,
    class_name VARCHAR(200) NOT NULL,
    parent_class_id VARCHAR(64),
    description VARCHAR(1000),
    properties_json CLOB,
    constraints_json CLOB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 本体关系表
CREATE TABLE IF NOT EXISTS ontology_relation (
    id VARCHAR(64) PRIMARY KEY,
    relation_name VARCHAR(200) NOT NULL,
    source_class_id VARCHAR(64) NOT NULL,
    target_class_id VARCHAR(64) NOT NULL,
    description VARCHAR(1000),
    cardinality VARCHAR(32) DEFAULT '1:N',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 知识条目表
CREATE TABLE IF NOT EXISTS knowledge_entry (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content CLOB,
    knowledge_type VARCHAR(32) NOT NULL,
    status VARCHAR(32) DEFAULT 'DRAFT',
    source_doc_id VARCHAR(64),
    source_doc_name VARCHAR(500),
    domain VARCHAR(100),
    tags_json CLOB,
    vector_id VARCHAR(64),
    graph_node_id VARCHAR(64),
    version INT DEFAULT 1,
    created_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 质量评估表
CREATE TABLE IF NOT EXISTS quality_evaluation (
    id VARCHAR(64) PRIMARY KEY,
    knowledge_id VARCHAR(64) NOT NULL,
    completeness_score DOUBLE,
    consistency_score DOUBLE,
    accuracy_score DOUBLE,
    overall_score DOUBLE,
    passed INT DEFAULT 0,
    detail_json CLOB,
    evaluator VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 知识版本表
CREATE TABLE IF NOT EXISTS knowledge_version (
    id VARCHAR(64) PRIMARY KEY,
    knowledge_id VARCHAR(64) NOT NULL,
    version_number INT NOT NULL,
    title VARCHAR(500),
    content_snapshot CLOB,
    change_note VARCHAR(1000),
    operator VARCHAR(64),
    operation_type VARCHAR(32),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 知识访问权限表
CREATE TABLE IF NOT EXISTS knowledge_access (
    id VARCHAR(64) PRIMARY KEY,
    knowledge_id VARCHAR(64) NOT NULL,
    principal_id VARCHAR(64) NOT NULL,
    principal_type VARCHAR(32) DEFAULT 'USER',
    access_level VARCHAR(32) DEFAULT 'READ',
    allowed INT DEFAULT 1,
    expire_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_knowledge_entry_domain ON knowledge_entry(domain);
CREATE INDEX IF NOT EXISTS idx_knowledge_entry_type ON knowledge_entry(knowledge_type);
CREATE INDEX IF NOT EXISTS idx_knowledge_entry_status ON knowledge_entry(status);
CREATE INDEX IF NOT EXISTS idx_quality_eval_kid ON quality_evaluation(knowledge_id);
CREATE INDEX IF NOT EXISTS idx_version_kid ON knowledge_version(knowledge_id);
CREATE INDEX IF NOT EXISTS idx_access_kid ON knowledge_access(knowledge_id);
CREATE INDEX IF NOT EXISTS idx_access_principal ON knowledge_access(principal_id);
