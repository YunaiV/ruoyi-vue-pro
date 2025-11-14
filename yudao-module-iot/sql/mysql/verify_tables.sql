-- ----------------------------
-- IoT 模块表结构验证脚本 - MySQL
-- ----------------------------

-- 查询所有 IoT 相关表
SELECT
    TABLE_NAME AS '表名',
    TABLE_COMMENT AS '表注释',
    TABLE_ROWS AS '预估行数',
    DATA_LENGTH AS '数据大小(字节)',
    CREATE_TIME AS '创建时间'
FROM
    information_schema.TABLES
WHERE
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME LIKE 'iot_%'
ORDER BY
    TABLE_NAME;

-- 统计表数量
SELECT
    COUNT(*) AS 'IoT表总数'
FROM
    information_schema.TABLES
WHERE
    TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME LIKE 'iot_%';

-- 预期结果：13 张表
-- iot_alert_config
-- iot_alert_record
-- iot_data_rule
-- iot_data_sink
-- iot_device
-- iot_device_group
-- iot_ota_firmware
-- iot_ota_task
-- iot_ota_task_record
-- iot_product
-- iot_product_category
-- iot_scene_rule
-- iot_thing_model
