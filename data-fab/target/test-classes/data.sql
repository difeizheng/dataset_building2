-- H2 Test Data for data-fab
-- 测试数据初始化

-- 测试数据集
INSERT INTO t_dataset (id, name, description, modality, data_level, status, version, sample_count, total_size, tags, create_by, create_time, update_time, deleted)
VALUES (1, '测试数据集-文本', '用于集成测试的文本数据集', 1, 2, 1, '1.0.0', 100, 1048576, '["测试","文本"]', 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_dataset (id, name, description, modality, data_level, status, version, sample_count, total_size, tags, create_by, create_time, update_time, deleted)
VALUES (2, '测试数据集-图像', '用于集成测试的图像数据集', 2, 2, 1, '1.0.0', 50, 5242880, '["测试","图像"]', 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 测试数据样本
INSERT INTO t_data_sample (id, dataset_id, name, modality, data_level, status, source, file_path, file_size, mime_type, file_hash, metadata, create_by, create_time, update_time, deleted)
VALUES (1, 1, '样本1.txt', 1, 2, 1, '测试', '/data/sample1.txt', 1024, 'text/plain', 'abc123', '{"language":"zh"}', 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_data_sample (id, dataset_id, name, modality, data_level, status, source, file_path, file_size, mime_type, file_hash, metadata, create_by, create_time, update_time, deleted)
VALUES (2, 1, '样本2.txt', 1, 2, 1, '测试', '/data/sample2.txt', 2048, 'text/plain', 'def456', '{"language":"zh"}', 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_data_sample (id, dataset_id, name, modality, data_level, status, source, file_path, file_size, mime_type, file_hash, metadata, create_by, create_time, update_time, deleted)
VALUES (3, 2, '样本1.jpg', 2, 2, 1, '测试', '/data/image1.jpg', 102400, 'image/jpeg', 'ghi789', '{"width":800,"height":600}', 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 测试清洗任务
INSERT INTO t_etl_task (id, dataset_id, task_name, status, processed_count, success_count, failed_count, create_by, create_time, update_time, deleted)
VALUES (1, 1, '测试清洗任务', 2, 100, 98, 2, 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 测试清洗规则
INSERT INTO t_etl_rule (id, name, description, modality, rule_type, rule_config, priority, enabled, create_by, create_time, update_time, deleted)
VALUES (1, '文本去重', '去除重复的文本内容', 1, 'dedup', '{"threshold":0.95}', 10, 1, 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_etl_rule (id, name, description, modality, rule_type, rule_config, priority, enabled, create_by, create_time, update_time, deleted)
VALUES (2, '敏感词过滤', '过滤敏感词汇', 1, 'filter', '{"words":["敏感","违规"]}', 20, 1, 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 测试标注任务
INSERT INTO t_label_task (id, dataset_id, task_name, modality, label_type, label_schema, double_blind, annotator_count, annotator_ids, status, total_samples, labeled_count, kappa_score, create_by, create_time, update_time, deleted)
VALUES (1, 1, '文本分类标注', 1, 'classification', '{"labels":["正面","负面","中性"]}', 1, 2, '[1,2]', 2, 100, 80, 0.8500, 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_label_task (id, dataset_id, task_name, modality, label_type, label_schema, double_blind, annotator_count, annotator_ids, status, total_samples, labeled_count, kappa_score, create_by, create_time, update_time, deleted)
VALUES (2, 2, '图像目标检测', 2, 'detection', '{"labels":["猫","狗","鸟"]}', 1, 2, '[1,2]', 2, 50, 30, 0.8200, 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 测试标注记录
INSERT INTO t_label_record (id, task_id, sample_id, annotator_id, annotation, duration_seconds, create_time, update_time, deleted)
VALUES (1, 1, 1, 1, '{"label":"正面"}', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_label_record (id, task_id, sample_id, annotator_id, annotation, duration_seconds, create_time, update_time, deleted)
VALUES (2, 1, 1, 2, '{"label":"正面"}', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_label_record (id, task_id, sample_id, annotator_id, annotation, duration_seconds, create_time, update_time, deleted)
VALUES (3, 1, 2, 1, '{"label":"负面"}', 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_label_record (id, task_id, sample_id, annotator_id, annotation, duration_seconds, create_time, update_time, deleted)
VALUES (4, 1, 2, 2, '{"label":"中性"}', 32, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 测试质量评估任务
INSERT INTO t_qa_task (id, dataset_id, modality, batch, status, metrics, passed, gates, review_stage, create_by, create_time, update_time, deleted)
VALUES (1, 1, 1, 'batch-001', 2, '{"completeness":0.98,"accuracy":0.95,"consistency":0.85}', 1, '[{"name":"完整性","threshold":0.95,"actual":0.98,"passed":true},{"name":"准确性","threshold":0.90,"actual":0.95,"passed":true}]', 1, 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO t_qa_task (id, dataset_id, modality, batch, status, metrics, passed, gates, review_stage, create_by, create_time, update_time, deleted)
VALUES (2, 2, 2, 'batch-002', 2, '{"completeness":0.92,"accuracy":0.88,"consistency":0.80}', 0, '[{"name":"完整性","threshold":0.95,"actual":0.92,"passed":false},{"name":"准确性","threshold":0.90,"actual":0.88,"passed":false}]', 0, 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 测试交付记录
INSERT INTO t_delivery_record (id, dataset_id, version, dataset_uri, license, lineage, access_policy, fair_metadata, croissant_metadata, status, download_count, create_by, create_time, update_time, deleted)
VALUES (1, 1, '1.0.0', 'https://data.example.com/dataset/1', 'Apache-2.0', '{"source":"内部采集","processing":"清洗+标注"}', '{"level":"L2","approval_required":false}', '{"name":"测试数据集","description":"测试用"}', '{"@type":"Dataset","name":"测试数据集"}', 1, 15, 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 测试审计日志
INSERT INTO t_audit_log (id, resource_id, resource_type, action, user_id, result, description, ip_address, create_time)
VALUES (1, 1, 'DATASET', 'VIEW', 1, 'ALLOWED', '查看数据集详情', '127.0.0.1', CURRENT_TIMESTAMP);

INSERT INTO t_audit_log (id, resource_id, resource_type, action, user_id, result, description, ip_address, create_time)
VALUES (2, 1, 'DATASET', 'DOWNLOAD', 1, 'ALLOWED', '下载数据集', '127.0.0.1', CURRENT_TIMESTAMP);

-- 测试数据审批
INSERT INTO t_data_approval (id, approval_no, resource_id, resource_type, action, applicant_id, approver_id, status, comment, create_time)
VALUES (1, 'APR-2026-001', 1, 'DATASET', 'DOWNLOAD', 1, 2, 1, '审批通过', CURRENT_TIMESTAMP);

INSERT INTO t_data_approval (id, approval_no, resource_id, resource_type, action, applicant_id, approver_id, status, comment, create_time)
VALUES (2, 'APR-2026-002', 2, 'DATASET', 'EXPORT', 1, 2, 0, NULL, CURRENT_TIMESTAMP);
