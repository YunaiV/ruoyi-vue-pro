-- ----------------------------
-- IoT 模块表结构验证脚本 - PostgreSQL
-- ----------------------------

-- 查询所有 IoT 相关表
SELECT
    schemaname AS "模式",
    tablename AS "表名",
    tableowner AS "所有者"
FROM
    pg_tables
WHERE
    schemaname = 'public'
    AND tablename LIKE 'iot_%'
ORDER BY
    tablename;

-- 查询所有 IoT 相关序列
SELECT
    schemaname AS "模式",
    sequencename AS "序列名",
    last_value AS "当前值"
FROM
    pg_sequences
WHERE
    schemaname = 'public'
    AND sequencename LIKE 'iot_%'
ORDER BY
    sequencename;

-- 统计表数量
SELECT
    COUNT(*) AS "IoT表总数"
FROM
    pg_tables
WHERE
    schemaname = 'public'
    AND tablename LIKE 'iot_%';

-- 统计序列数量
SELECT
    COUNT(*) AS "IoT序列总数"
FROM
    pg_sequences
WHERE
    schemaname = 'public'
    AND sequencename LIKE 'iot_%';

-- 预期结果：13 张表 + 13 个序列
-- 表：
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
