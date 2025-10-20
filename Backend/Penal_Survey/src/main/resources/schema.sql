-- GuidePref Panel Survey Database Schema
-- Create Database
CREATE DATABASE IF NOT EXISTS panel_survey DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE panel_survey;

-- User Table (用户表)
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `email` VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN, USER',
    `institution` VARCHAR(200) COMMENT '所属机构',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (`username`),
    INDEX idx_email (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- Survey Project Table (问卷项目表 - Step1 & Step2)
CREATE TABLE IF NOT EXISTS `survey_project` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    `user_id` BIGINT NOT NULL COMMENT '创建用户ID',
    `project_name` VARCHAR(200) NOT NULL COMMENT '项目名称',
    `objective_type` VARCHAR(50) NOT NULL COMMENT '问卷目标类型',
    `disease_name` VARCHAR(200) COMMENT '疾病名称',
    `intervention_name` VARCHAR(200) COMMENT '干预措施名称',
    `comparison_name` VARCHAR(200) COMMENT '对照措施名称',
    `patient_description` TEXT COMMENT '患者人群描述',
    `outcome_list` JSON COMMENT '结局列表',
    `intervention_details` TEXT COMMENT '干预措施详细描述',
    `comparison_details` TEXT COMMENT '对照措施详细描述',
    `side_effects` TEXT COMMENT '副作用说明',
    `additional_info` TEXT COMMENT '其他补充信息',
    `survey_duration` INT COMMENT '预计问卷完成时间(分钟)',
    `target_respondents` INT COMMENT '目标受访者数量',
    `step_status` INT NOT NULL DEFAULT 1 COMMENT '当前进度: 1-Step1, 2-Step2, 3-Step3, 4-Step4, 5-Step5, 6-Step6',
    `status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT, GENERATED, REVIEWED, PUBLISHED, COMPLETED',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_status (`status`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷项目表';

-- Survey Questionnaire Table (问卷表 - Step3 生成的问卷)
CREATE TABLE IF NOT EXISTS `survey_questionnaire` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '问卷ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `title` VARCHAR(300) NOT NULL COMMENT '问卷标题',
    `description` TEXT COMMENT '问卷说明',
    `introduction` TEXT COMMENT '问卷引言',
    `instruction` TEXT COMMENT '填写指导',
    `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
    `is_ai_reviewed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否经过AI审核',
    `ai_review_result` JSON COMMENT 'AI审核结果',
    `ai_refined_content` JSON COMMENT 'AI润色后的内容',
    `status` VARCHAR(20) NOT NULL DEFAULT 'GENERATED' COMMENT '状态: GENERATED, REVIEWED, PUBLISHED',
    `survey_url` VARCHAR(500) COMMENT '问卷分发链接(问卷网公网链接)',
    `qr_code_path` VARCHAR(500) COMMENT '二维码图片路径',
    `wenjuan_short_id` VARCHAR(50) COMMENT '问卷网项目短ID',
    `wenjuan_project_id` VARCHAR(50) COMMENT '问卷网项目ID',
    `wenjuan_respondent_count` INT DEFAULT 0 COMMENT '问卷网答卷数量',
    `last_sync_time` DATETIME COMMENT '最后同步时间',
    `is_wenjuan_published` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已发布到问卷网',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (`project_id`),
    INDEX idx_wenjuan_short_id (`wenjuan_short_id`),
    FOREIGN KEY (`project_id`) REFERENCES `survey_project`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷表';

-- Survey Question Table (问卷题目表)
CREATE TABLE IF NOT EXISTS `survey_question` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    `questionnaire_id` BIGINT NOT NULL COMMENT '问卷ID',
    `section` VARCHAR(100) COMMENT '所属章节',
    `question_no` INT NOT NULL COMMENT '题号',
    `question_type` VARCHAR(50) NOT NULL COMMENT '题型: SINGLE_CHOICE, MULTIPLE_CHOICE, RATING, TEXT, MATRIX',
    `question_text` TEXT NOT NULL COMMENT '题目内容',
    `question_description` TEXT COMMENT '题目说明',
    `options` JSON COMMENT '选项列表',
    `is_required` TINYINT NOT NULL DEFAULT 1 COMMENT '是否必填',
    `logic_jump` JSON COMMENT '逻辑跳转规则',
    `original_text` TEXT COMMENT '原始生成的内容',
    `refined_text` TEXT COMMENT 'AI润色后的内容',
    `is_refined` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已润色',
    `display_order` INT NOT NULL COMMENT '显示顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_questionnaire_id (`questionnaire_id`),
    INDEX idx_display_order (`display_order`),
    FOREIGN KEY (`questionnaire_id`) REFERENCES `survey_questionnaire`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷题目表';

-- Survey Response Table (问卷回复表 - 受试者填写记录)
CREATE TABLE IF NOT EXISTS `survey_response` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '回复ID',
    `questionnaire_id` BIGINT NOT NULL COMMENT '问卷ID',
    `respondent_code` VARCHAR(100) COMMENT '受访者编码(匿名)',
    `respondent_ip` VARCHAR(50) COMMENT '受访者IP',
    `start_time` DATETIME COMMENT '开始填写时间',
    `submit_time` DATETIME COMMENT '提交时间',
    `duration_seconds` INT COMMENT '填写耗时(秒)',
    `is_completed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否完成',
    `device_type` VARCHAR(50) COMMENT '设备类型',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_questionnaire_id (`questionnaire_id`),
    INDEX idx_respondent_code (`respondent_code`),
    FOREIGN KEY (`questionnaire_id`) REFERENCES `survey_questionnaire`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷回复表';

-- Survey Answer Table (问卷答案明细表)
CREATE TABLE IF NOT EXISTS `survey_answer` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '答案ID',
    `response_id` BIGINT NOT NULL COMMENT '回复ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `answer_value` TEXT COMMENT '答案值(JSON格式存储)',
    `answer_text` TEXT COMMENT '答案文本',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_response_id (`response_id`),
    INDEX idx_question_id (`question_id`),
    FOREIGN KEY (`response_id`) REFERENCES `survey_response`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`question_id`) REFERENCES `survey_question`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷答案明细表';

-- Survey Report Table (分析报告表 - Step6)
CREATE TABLE IF NOT EXISTS `survey_report` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    `project_id` BIGINT NOT NULL COMMENT '项目ID',
    `questionnaire_id` BIGINT NOT NULL COMMENT '问卷ID',
    `report_title` VARCHAR(300) NOT NULL COMMENT '报告标题',
    `total_responses` INT NOT NULL DEFAULT 0 COMMENT '总回复数',
    `valid_responses` INT NOT NULL DEFAULT 0 COMMENT '有效回复数',
    `completion_rate` DECIMAL(5,2) COMMENT '完成率(%)',
    `average_duration` INT COMMENT '平均完成时间(秒)',
    `statistical_data` JSON COMMENT '统计数据',
    `chart_data` JSON COMMENT '图表数据',
    `preference_summary` TEXT COMMENT '偏好总结',
    `key_findings` TEXT COMMENT '关键发现',
    `report_content` LONGTEXT COMMENT '完整报告内容(HTML)',
    `pdf_path` VARCHAR(500) COMMENT 'PDF文件路径',
    `excel_path` VARCHAR(500) COMMENT 'Excel文件路径',
    `generate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_project_id (`project_id`),
    INDEX idx_questionnaire_id (`questionnaire_id`),
    FOREIGN KEY (`project_id`) REFERENCES `survey_project`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`questionnaire_id`) REFERENCES `survey_questionnaire`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分析报告表';

-- System Log Table (系统日志表)
CREATE TABLE IF NOT EXISTS `system_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '用户ID',
    `module` VARCHAR(50) COMMENT '模块',
    `action` VARCHAR(100) COMMENT '操作',
    `request_method` VARCHAR(10) COMMENT '请求方法',
    `request_url` VARCHAR(500) COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `result` VARCHAR(20) COMMENT '结果: SUCCESS, FAILURE',
    `error_message` TEXT COMMENT '错误信息',
    `execute_time` INT COMMENT '执行时间(ms)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_module (`module`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- Insert default admin user (password: admin123)
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `role`, `status`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@guidepref.com', 'System Admin', 'ADMIN', 1);

-- Insert test user (password: user123)
INSERT INTO `user` (`username`, `password`, `email`, `real_name`, `role`, `institution`, `status`) 
VALUES ('researcher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'researcher@guidepref.com', 'Test Researcher', 'USER', 'Test Medical Center', 1);

