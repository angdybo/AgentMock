-- API 调试工具 - 请求历史记录表
DROP TABLE IF EXISTS `anymock_api_tester_history`;

CREATE TABLE `anymock_api_tester_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '请求URL',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'GET' COMMENT '请求方法',
  `request_body` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '请求Body',
  `response_status` int(11) DEFAULT NULL COMMENT '响应状态码',
  `response_body` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '响应体',
  `cost_time` int(11) DEFAULT NULL COMMENT '耗时(ms)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;
