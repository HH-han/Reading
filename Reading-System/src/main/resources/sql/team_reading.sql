-- 团队读书打卡小程序 · 数据库 SQL 脚本 v1.1（无支付版）
-- MySQL 8.0+ / utf8mb4 / InnoDB

-- =====================================================
-- 一、建库
-- =====================================================
CREATE DATABASE IF NOT EXISTS `team_reading`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE `team_reading`;

-- =====================================================
-- 二、核心业务表（19张）
-- =====================================================

-- 2.1 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `open_id` VARCHAR(64) DEFAULT NULL COMMENT '微信OpenID',
  `union_id` VARCHAR(64) DEFAULT NULL COMMENT '微信UnionID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '登录账号(后台账号)',
  `password` VARCHAR(100) DEFAULT NULL COMMENT '密码(BCrypt加密,后台账号用)',
  `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `gender` TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `role` VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: SUPER_ADMIN/TEAM_ADMIN/MEMBER',
  `position` VARCHAR(50) DEFAULT '普通员工' COMMENT '职位(用于打卡规则匹配)',
  `circle_id` BIGINT DEFAULT NULL COMMENT '所属圈子/部门ID',
  `group_id` BIGINT DEFAULT NULL COMMENT '所属小组ID',
  `background_img` VARCHAR(500) DEFAULT '' COMMENT '个人主页背景图',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分',
  `checkin_days` INT NOT NULL DEFAULT 0 COMMENT '累计打卡天数',
  `follower_count` INT NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `following_count` INT NOT NULL DEFAULT 0 COMMENT '关注数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '获赞数',
  `address` VARCHAR(255) DEFAULT '' COMMENT '我的地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_circle_id` (`circle_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2.2 圈子/部门表
DROP TABLE IF EXISTS `circle`;
CREATE TABLE `circle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '圈子ID',
  `name` VARCHAR(100) NOT NULL COMMENT '圈子/部门名称',
  `cover` VARCHAR(500) DEFAULT '' COMMENT '封面图',
  `description` VARCHAR(500) DEFAULT '' COMMENT '简介',
  `owner_id` BIGINT NOT NULL COMMENT '圈主(管理员)ID',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数',
  `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='圈子/部门表';

-- 2.3 小组表
DROP TABLE IF EXISTS `t_group`;
CREATE TABLE `t_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '小组ID',
  `circle_id` BIGINT NOT NULL COMMENT '所属圈子ID',
  `name` VARCHAR(100) NOT NULL COMMENT '小组名称',
  `description` VARCHAR(500) DEFAULT '' COMMENT '小组简介',
  `leader_id` BIGINT DEFAULT NULL COMMENT '组长ID',
  `member_count` INT NOT NULL DEFAULT 0 COMMENT '成员数',
  `invite_code` VARCHAR(20) DEFAULT NULL COMMENT '邀请码',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_circle_id` (`circle_id`),
  KEY `idx_leader_id` (`leader_id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小组表';

-- 2.4 书籍表
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '书籍ID',
  `title` VARCHAR(200) NOT NULL COMMENT '书名',
  `author` VARCHAR(100) DEFAULT '' COMMENT '作者',
  `publisher` VARCHAR(100) DEFAULT '' COMMENT '出版社',
  `cover` VARCHAR(500) DEFAULT '' COMMENT '封面图URL',
  `category` VARCHAR(50) DEFAULT '' COMMENT '分类: 经营之道/人生哲学/领导力/自我精进等',
  `book_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型 1电子书 2实体书',
  `description` TEXT COMMENT '书籍简介',
  `recommend_reason` VARCHAR(500) DEFAULT '' COMMENT '推荐理由',
  `chapter_count` INT NOT NULL DEFAULT 0 COMMENT '章节数',
  `checkin_count` INT NOT NULL DEFAULT 0 COMMENT '打卡人数',
  `is_public` TINYINT NOT NULL DEFAULT 1 COMMENT '是否开放书库 0否 1是',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0下架 1上架',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_is_public` (`is_public`),
  FULLTEXT KEY `ft_title_author` (`title`, `author`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍表';

-- 2.5 书籍章节表
DROP TABLE IF EXISTS `book_chapter`;
CREATE TABLE `book_chapter` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '章节ID',
  `book_id` BIGINT NOT NULL COMMENT '书籍ID',
  `chapter_no` INT NOT NULL DEFAULT 1 COMMENT '章节序号',
  `title` VARCHAR(200) NOT NULL COMMENT '章节标题',
  `content` LONGTEXT COMMENT '章节正文内容',
  `audio_url` VARCHAR(500) DEFAULT '' COMMENT '章节音频URL',
  `duration` INT DEFAULT 0 COMMENT '音频时长(秒)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书籍章节表';

-- 2.6 书架关联表
DROP TABLE IF EXISTS `book_shelf`;
CREATE TABLE `book_shelf` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `book_id` BIGINT NOT NULL COMMENT '书籍ID',
  `shelf_type` TINYINT NOT NULL DEFAULT 1 COMMENT '书架类型 1我的藏书 2小组合集 3精选集',
  `source` TINYINT NOT NULL DEFAULT 1 COMMENT '来源 1开放书库 2管理员推荐',
  `progress` INT NOT NULL DEFAULT 0 COMMENT '阅读进度(章节序号)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_book_type` (`user_id`, `book_id`, `shelf_type`),
  KEY `idx_book_id` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书架关联表';

-- 2.7 打卡记录表
DROP TABLE IF EXISTS `checkin_record`;
CREATE TABLE `checkin_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '打卡记录ID',
  `user_id` BIGINT NOT NULL COMMENT '打卡用户ID',
  `circle_id` BIGINT NOT NULL COMMENT '所属圈子ID(冗余)',
  `group_id` BIGINT DEFAULT NULL COMMENT '所属小组ID(冗余)',
  `book_id` BIGINT DEFAULT NULL COMMENT '书籍ID(图片打卡可为空)',
  `chapter_id` BIGINT DEFAULT NULL COMMENT '章节ID',
  `chapter_title` VARCHAR(200) DEFAULT '' COMMENT '章节标题(冗余)',
  `checkin_type` TINYINT NOT NULL DEFAULT 1 COMMENT '打卡类型 1图片 2朗读音频 3心得笔记 4学习录像',
  `content` TEXT COMMENT '打卡内容(心得文字)',
  `media_url` VARCHAR(500) DEFAULT '' COMMENT '图片/视频URL(多图逗号分隔)',
  `audio_url` VARCHAR(500) DEFAULT '' COMMENT '音频URL',
  `audio_duration` INT DEFAULT 0 COMMENT '音频时长(秒)',
  `checkin_date` DATE NOT NULL COMMENT '打卡日期(用于统计去重)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0待审核 1已通过 2已驳回',
  `audit_user_id` BIGINT DEFAULT NULL COMMENT '审核人ID(管理员代打卡记录)',
  `audit_remark` VARCHAR(255) DEFAULT '' COMMENT '审核备注',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论数',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览数',
  `is_public` TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开到动态 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`, `checkin_date`),
  KEY `idx_circle_date` (`circle_id`, `checkin_date`),
  KEY `idx_group_date` (`group_id`, `checkin_date`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_checkin_type` (`checkin_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

-- 2.8 打卡规则表
DROP TABLE IF EXISTS `checkin_rule`;
CREATE TABLE `checkin_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `circle_id` BIGINT NOT NULL COMMENT '所属圈子ID',
  `position` VARCHAR(50) NOT NULL COMMENT '职位名称',
  `required_days_per_week` INT NOT NULL DEFAULT 5 COMMENT '每周合格打卡天数',
  `required_days_per_month` INT NOT NULL DEFAULT 20 COMMENT '每月合格打卡天数',
  `required_days_per_year` INT NOT NULL DEFAULT 240 COMMENT '每年合格打卡天数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_circle_position` (`circle_id`, `position`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡规则表(按职位配置)';

-- 2.9 排行榜快照表
DROP TABLE IF EXISTS `ranking_snapshot`;
CREATE TABLE `ranking_snapshot` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `rank_type` TINYINT NOT NULL COMMENT '排行类型 1周 2月 3年',
  `scope_type` TINYINT NOT NULL COMMENT '范围类型 1个人 2小组 3圈子',
  `scope_id` BIGINT NOT NULL COMMENT '范围ID(个人用户ID/小组ID/圈子ID)',
  `circle_id` BIGINT DEFAULT NULL COMMENT '所属圈子ID(便于筛选)',
  `group_id` BIGINT DEFAULT NULL COMMENT '所属小组ID(便于筛选)',
  `period_key` VARCHAR(20) NOT NULL COMMENT '周期标识 如 2026-W42 / 2026-10 / 2026',
  `score` INT NOT NULL DEFAULT 0 COMMENT '得分(打卡天数等)',
  `rank_no` INT NOT NULL DEFAULT 0 COMMENT '排名',
  `stat_date` DATE NOT NULL COMMENT '统计基准日',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_period_scope` (`rank_type`, `period_key`, `scope_type`),
  KEY `idx_circle_id` (`circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排行榜快照表';

-- 2.10 学习作业表
DROP TABLE IF EXISTS `study_job`;
CREATE TABLE `study_job` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `circle_id` BIGINT NOT NULL COMMENT '圈子ID',
  `group_id` BIGINT DEFAULT NULL COMMENT '指定小组ID(为空则全圈)',
  `title` VARCHAR(200) NOT NULL COMMENT '作业标题',
  `book_id` BIGINT NOT NULL COMMENT '指定书籍ID',
  `chapter_start` INT DEFAULT NULL COMMENT '起始章节序号',
  `chapter_end` INT DEFAULT NULL COMMENT '结束章节序号',
  `content` TEXT COMMENT '作业要求说明',
  `start_date` DATE NOT NULL COMMENT '开始日期',
  `deadline` DATE NOT NULL COMMENT '截止日期',
  `publisher_id` BIGINT NOT NULL COMMENT '发布人ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0草稿 1已发布 2已结束',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_circle_id` (`circle_id`),
  KEY `idx_book_id` (`book_id`),
  KEY `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习作业表';

-- 2.11 消息通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` VARCHAR(500) NOT NULL COMMENT '内容',
  `notify_type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型 1系统 2打卡提醒 3作业通知 4审核结果',
  `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `read_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 2.12 点赞表
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型 1打卡记录 2评论',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 收藏表
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `target_type` TINYINT NOT NULL COMMENT '目标类型 1书籍 2打卡记录',
  `target_id` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 评论表
DROP TABLE IF EXISTS `checkin_comment`;
CREATE TABLE `checkin_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `checkin_id` BIGINT NOT NULL COMMENT '打卡记录ID',
  `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID(回复)',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0隐藏 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_checkin_id` (`checkin_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡评论表';

-- 2.13 用户关注表
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
  `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
  KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- 2.14 打卡模板表
DROP TABLE IF EXISTS `checkin_template`;
CREATE TABLE `checkin_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `circle_id` BIGINT NOT NULL COMMENT '圈子ID',
  `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
  `checkin_types` VARCHAR(50) NOT NULL DEFAULT '1,2,3' COMMENT '允许的打卡类型(逗号分隔)',
  `require_audio` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填音频 0否 1是',
  `require_content` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填心得 0否 1是',
  `require_image` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填图片 0否 1是',
  `require_book` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填书籍 0否 1是',
  `require_chapter` TINYINT NOT NULL DEFAULT 0 COMMENT '是否必填章节标题 0否 1是',
  `type_config` TEXT COMMENT '各打卡类型必填项配置(JSON)',
  `min_word_count` INT NOT NULL DEFAULT 0 COMMENT '心得最少字数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_circle_id` (`circle_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡模板表';

-- 2.15 用户成长积分记录表
DROP TABLE IF EXISTS `user_growth_log`;
CREATE TABLE `user_growth_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `change_type` TINYINT NOT NULL COMMENT '变动类型 1打卡 2连续打卡奖励 3获赞 4作业完成 5管理员调整',
  `points` INT NOT NULL DEFAULT 0 COMMENT '积分变动值(正负)',
  `remark` VARCHAR(255) DEFAULT '' COMMENT '说明',
  `biz_id` BIGINT DEFAULT NULL COMMENT '关联业务ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成长积分记录表';

-- 2.16 用户每日打卡统计表
DROP TABLE IF EXISTS `user_checkin_daily_stat`;
CREATE TABLE `user_checkin_daily_stat` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `circle_id` BIGINT NOT NULL,
  `group_id` BIGINT DEFAULT NULL,
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `has_checkin` TINYINT NOT NULL DEFAULT 0 COMMENT '当日是否打卡 0否 1是',
  `audio_count` INT NOT NULL DEFAULT 0 COMMENT '听录音数(当日)',
  `read_count` INT NOT NULL DEFAULT 0 COMMENT '诵读文章数(当日)',
  `note_count` INT NOT NULL DEFAULT 0 COMMENT '写心得数(当日)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `stat_date`),
  KEY `idx_circle_date` (`circle_id`, `stat_date`),
  KEY `idx_group_date` (`group_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日打卡统计表(看板加速)';

-- 2.17 操作日志表
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '操作人ID',
  `module` VARCHAR(50) DEFAULT '' COMMENT '模块',
  `operation` VARCHAR(100) DEFAULT '' COMMENT '操作描述',
  `method` VARCHAR(200) DEFAULT '' COMMENT '请求方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
  `cost_time` BIGINT DEFAULT 0 COMMENT '耗时(ms)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 2.18 用户-读书圈关系表（支持一个用户加入多个读书圈）
DROP TABLE IF EXISTS `user_circle`;
CREATE TABLE `user_circle` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `circle_id` BIGINT NOT NULL COMMENT '读书圈ID',
  `group_id` BIGINT DEFAULT NULL COMMENT '该圈子下的小组ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_circle` (`user_id`, `circle_id`),
  KEY `idx_circle_id` (`circle_id`),
  KEY `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-读书圈关系表';

-- ---------------------------
-- 职位字典表（超级管理员职位管理）
-- ---------------------------
CREATE TABLE `sys_position` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(50) NOT NULL COMMENT '职位名称',
  `description` VARCHAR(255) DEFAULT '' COMMENT '职位说明',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0停用 1启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_position_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位字典表';

-- =====================================================
-- 三、初始化种子数据
-- =====================================================

-- 3.1 初始用户（密码统一 123456 的 BCrypt 值，上线前请重置）
INSERT INTO `sys_user` (`open_id`, `username`, `password`, `nickname`, `avatar`, `role`, `position`, `status`)
VALUES ('admin_openid_001', 'admin', '$2a$10$aS8BVTA09sG85zK6WAwDM.sJlfNRLx6rnMsCTsT6WPiIT6mUQPzcq', '系统管理员', '', 'SUPER_ADMIN', '管理员', 1);

INSERT INTO `sys_user` (`open_id`, `username`, `password`, `nickname`, `role`, `position`, `status`)
VALUES ('team_admin_openid', 'teamadmin', '$2a$10$aS8BVTA09sG85zK6WAwDM.sJlfNRLx6rnMsCTsT6WPiIT6mUQPzcq', '张经理', 'TEAM_ADMIN', '部门经理', 1);

INSERT INTO `sys_user` (`open_id`, `nickname`, `role`, `position`, `status`)
VALUES
('member_openid_001', '小懒猪爱吃薯条', 'MEMBER', '普通员工', 1),
('member_openid_002', '沙驰飞飞', 'MEMBER', '普通员工', 1),
('member_openid_003', '细雨凝香', 'MEMBER', '普通员工', 1),
('member_openid_004', '小武17010494949', 'MEMBER', '普通员工', 1);

-- 3.2 示例圈子与小组
INSERT INTO `circle` (`name`, `description`, `owner_id`, `member_count`, `invite_code`)
VALUES ('环境试验科学事业部', '团队读书学习圈', 2, 4, 'CIRCLE2026');

INSERT INTO `t_group` (`circle_id`, `name`, `leader_id`, `member_count`, `invite_code`)
VALUES
(1, '环境一组', 3, 2, 'GROUP001'),
(1, '环境二组', 4, 2, 'GROUP002');

-- 3.3 更新用户所属圈子/小组
UPDATE `sys_user` SET `circle_id` = 1, `group_id` = 1 WHERE `id` IN (1, 2, 3);
UPDATE `sys_user` SET `circle_id` = 1, `group_id` = 2 WHERE `id` IN (4, 5);

-- 3.3.1 初始化用户-读书圈关系（把用户当前的圈子/小组写入关系表）
INSERT IGNORE INTO `user_circle` (`user_id`, `circle_id`, `group_id`)
SELECT `id`, `circle_id`, `group_id` FROM `sys_user`
WHERE `circle_id` IS NOT NULL AND `deleted` = 0;

-- 3.4 打卡规则初始化（按职位）
INSERT INTO `checkin_rule` (`circle_id`, `position`, `required_days_per_week`, `required_days_per_month`, `required_days_per_year`)
VALUES
(1, '普通员工', 5, 20, 240),
(1, '部门经理', 6, 24, 280),
(1, '管理员', 7, 28, 330);

-- 3.4.1 职位字典初始化
INSERT INTO `sys_position` (`name`, `description`, `sort`, `status`)
VALUES
('管理员', '负责系统与读书圈管理', 1, 1),
('部门经理', '部门负责人', 2, 1),
('普通员工', '普通团队成员', 3, 1);


-- 3.5 示例书籍
INSERT INTO `book` (`title`, `author`, `publisher`, `category`, `book_type`, `description`, `recommend_reason`, `checkin_count`, `is_public`)
VALUES
('阿里局：中供系英雄志', '和阳', '广东经济出版社', '经营之道', 1, '讲述阿里中供铁军发展历程。', '', 0, 1),
('京瓷哲学', '(日) 稻盛和夫', '东方出版社', '人生哲学', 1, '稻盛和夫经营哲学。', '以“敬天爱人”哲学统一思想，激发内生动力，塑造高绩效团队，实现共同成长。', 0, 1),
('赌在技术开发上', '(日) 稻盛和夫', '机械工业出版社', '经营之道', 1, '技术开发与经营理念。', '汲取技术创新的信念与心法，锤炼极致“心”精神，赋能组织持续成长。', 0, 1),
('活法', '(日) 稻盛和夫', '东方出版社', '人生哲学', 1, '稻盛和夫的人生哲学。', '将感激刻于童心的“隐蔽念佛”。', 0, 1),
('日航的奇迹', '(日) 稻盛和夫', '东方出版社', '经营之道', 1, '日航重建奇迹。', '', 0, 1);

-- 3.6 示例章节
INSERT INTO `book_chapter` (`book_id`, `chapter_no`, `title`, `content`)
VALUES
(4, 1, '第一章 让梦想成真', '内容略...'),
(4, 2, '第二章 原理原则', '内容略...'),
(4, 3, '第三章 第5节：将感谢刻于童心的“隐蔽念佛”', '内容略...'),
(4, 4, '第四章 第13节：从自然界学习“知足”', '内容略...'),
(5, 1, '第七章 第四节', '内容略...');

-- 3.7 示例打卡记录
INSERT INTO `checkin_record` (`user_id`, `circle_id`, `group_id`, `book_id`, `chapter_id`, `chapter_title`, `checkin_type`, `content`, `audio_url`, `checkin_date`, `status`)
VALUES
(3, 1, 1, 4, 3, '第三章 第5节：将感谢刻于童心的“隐蔽念佛”', 3, '磨砺心志的“六项精进”...', '', '2026-10-21', 1),
(3, 1, 1, 5, 5, '第七章 第四节', 2, '', 'https://example.com/audio/demo.mp3', '2026-10-22', 1),
(4, 1, 1, 4, 3, '第三章 第5节：将感谢刻于童心的“隐蔽念佛”', 3, '当然，不仅领导者需要磨炼心志...', '', '2026-10-21', 1);

-- 3.8 打卡模板
INSERT INTO `checkin_template` (`circle_id`, `name`, `checkin_types`, `require_content`, `min_word_count`)
VALUES (1, '默认打卡模板', '1,2,3', 1, 50);
