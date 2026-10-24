-- =====================================================
-- MC 模组编辑器 数据库初始化脚本
-- 库：mc_create    表：user / project / forum_message
-- 对应前端 localStorage 里的 mc_users 和 mc_projects_*
-- =====================================================

CREATE DATABASE IF NOT EXISTS mc_create
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE mc_create;

-- 用户表（前端 mc_users）
CREATE TABLE IF NOT EXISTS `user` (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username    VARCHAR(20)  NOT NULL COMMENT '用户名',
  password    VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
  nickname    VARCHAR(20)  DEFAULT NULL COMMENT '昵称',
  email       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  bio         TEXT         DEFAULT NULL COMMENT '简介',
  avatar      LONGTEXT     DEFAULT NULL COMMENT '头像(base64 dataURL)',
  create_time DATETIME     DEFAULT NULL COMMENT '注册时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 项目表（前端 mc_projects_*，type 承担功能类型维度）
CREATE TABLE IF NOT EXISTS `project` (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  user_id      BIGINT       NOT NULL COMMENT '所属用户ID',
  type         VARCHAR(20)  NOT NULL DEFAULT 'default' COMMENT '功能类型',
  project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
  version      VARCHAR(20)  DEFAULT NULL COMMENT 'MC版本，如 1.20.1',
  status       VARCHAR(20)  NOT NULL DEFAULT 'draft' COMMENT '状态:draft草稿/published作品',
  cover        LONGTEXT     DEFAULT NULL COMMENT '展示图(base64 dataURL)',
  skin_data    LONGTEXT     DEFAULT NULL COMMENT '皮肤数据(base64)',
  create_time  DATETIME     DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_type (user_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 留言墙留言表（论坛）
CREATE TABLE IF NOT EXISTS `forum_message` (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '留言ID',
  user_id     BIGINT       NOT NULL COMMENT '留言人用户ID',
  content     TEXT         NOT NULL COMMENT '留言内容(最多200字)',
  create_time DATETIME     NOT NULL COMMENT '留言时间',
  PRIMARY KEY (id),
  KEY idx_user (user_id),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言墙留言表';
