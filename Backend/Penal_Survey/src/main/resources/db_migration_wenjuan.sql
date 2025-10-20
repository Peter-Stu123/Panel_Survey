-- 数据库迁移脚本：添加问卷网集成字段
-- 执行时间: 2025-10-17
-- 说明: 为survey_questionnaire表添加问卷网相关字段

USE panel_survey;

-- 添加问卷网相关字段
ALTER TABLE `survey_questionnaire` 
ADD COLUMN `wenjuan_short_id` VARCHAR(50) COMMENT '问卷网项目短ID' AFTER `qr_code_path`,
ADD COLUMN `wenjuan_project_id` VARCHAR(50) COMMENT '问卷网项目ID' AFTER `wenjuan_short_id`,
ADD COLUMN `wenjuan_respondent_count` INT DEFAULT 0 COMMENT '问卷网答卷数量' AFTER `wenjuan_project_id`,
ADD COLUMN `last_sync_time` DATETIME COMMENT '最后同步时间' AFTER `wenjuan_respondent_count`,
ADD COLUMN `is_wenjuan_published` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已发布到问卷网' AFTER `last_sync_time`,
ADD INDEX idx_wenjuan_short_id (`wenjuan_short_id`);

-- 更新survey_url字段注释
ALTER TABLE `survey_questionnaire` 
MODIFY COLUMN `survey_url` VARCHAR(500) COMMENT '问卷分发链接(问卷网公网链接)';

-- 显示修改后的表结构
SHOW CREATE TABLE `survey_questionnaire`;

