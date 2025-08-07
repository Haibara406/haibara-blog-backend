/*
 Navicat Premium Dump SQL

 Source Server         : mysql
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : blog

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 07/08/2025 14:19:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_popup
-- ----------------------------
DROP TABLE IF EXISTS `sys_popup`;
CREATE TABLE `sys_popup`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '弹窗ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '弹窗标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '弹窗内容',
  `popup_type` tinyint NOT NULL DEFAULT 1 COMMENT '弹窗类型(1:通知 2:广告 3:公告 4:活动)',
  `display_position` tinyint NOT NULL DEFAULT 1 COMMENT '显示位置(1:前台 2:管理端)',
  `target_pages` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标页面路径(多个用逗号分隔,如:/,/article,/about,为空则所有页面显示)',
  `target_users` tinyint NOT NULL DEFAULT 1 COMMENT '目标用户(1:所有用户 2:登录用户 3:游客)',
  `time_range` tinyint NOT NULL DEFAULT 0 COMMENT '时间段(0:全天 1:7-12点 2:12-18点 3:18-24点 4:0-7点)',
  `start_date` datetime NULL DEFAULT NULL COMMENT '开始日期(为空则立即生效)',
  `end_date` datetime NULL DEFAULT NULL COMMENT '结束日期(为空则永久有效)',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间(若与结束时间都为null则全天都能看到)',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间(若与开始时间都为null则全天都能看到)',
  `display_mode` tinyint NOT NULL DEFAULT 1 COMMENT '显示模式(1:每次刷新 2:会话期间一次 3:每日一次 4:永久一次)',
  `content_type` tinyint NOT NULL DEFAULT 1 COMMENT '内容类型(1:Markdown 2:HTML 3:纯文本)',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '弹窗图片URL',
  `jump_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点击按钮后的跳转链接',
  `popup_position` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'center' COMMENT '弹窗位置(center/top/bottom/left/right)',
  `priority` tinyint NOT NULL DEFAULT 50 COMMENT '优先级权重(1-99,默认50,数字越大优先级越高)',
  `closeable` tinyint(1) NOT NULL DEFAULT 1 COMMENT '用户是否可关闭(0:不可关闭 1:可关闭)',
  `auto_close_time` int NULL DEFAULT NULL COMMENT '自动关闭时间(秒,为空则不自动关闭)',
  `is_disable` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否禁用(0:否 1:是)',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除(0:未删除 1:已删除)',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_display_position`(`display_position` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  INDEX `idx_status`(`is_disable` ASC, `is_deleted` ASC) USING BTREE,
  INDEX `idx_time_range`(`time_range` ASC) USING BTREE,
  INDEX `idx_display_mode`(`display_mode` ASC) USING BTREE,
  INDEX `idx_time_period`(`start_date` ASC, `end_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '弹窗管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_popup
-- ----------------------------
INSERT INTO `sys_popup` VALUES (1, '欢迎访问', '# 欢迎来到 Haibara Blog\n\n感谢您的访问！', 1, 1, '/home', 1, 0, NULL, NULL, NULL, NULL, 1, 1, NULL, NULL, 'center', 60, 1, NULL, 0, '2025-08-07 13:51:53', '2025-08-07 13:51:53', 0);
INSERT INTO `sys_popup` VALUES (2, '系统公告', '## 系统维护通知\n\n系统将于今晚进行例行维护，预计耗时30分钟。', 3, 1, NULL, 1, 0, NULL, NULL, NULL, NULL, 1, 1, NULL, NULL, 'center', 80, 1, NULL, 0, '2025-08-07 13:51:53', '2025-08-07 13:51:53', 0);

SET FOREIGN_KEY_CHECKS = 1;
