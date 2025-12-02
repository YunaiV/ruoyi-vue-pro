/*
 Navicat Premium Data Transfer

 Source Server Type    : PostgreSQL
 Date: 2025-12-02

 Description: PostgreSQL 兼容性修复脚本

 修复内容:
 1. pay_demo_order.pay_status 字段类型从 smallint 改为 boolean
    - 原因: Java 实体类 PayDemoOrderDO.payStatus 定义为 Boolean 类型
    - PostgreSQL 不支持 boolean 自动转换为 smallint
*/

-- =============================================
-- 修复 pay_demo_order.pay_status 字段类型
-- =============================================

-- 检查字段当前类型，仅当类型为 smallint 时执行修复
DO $$
BEGIN
    -- 检查字段是否为 smallint 类型
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'pay_demo_order'
        AND column_name = 'pay_status'
        AND data_type = 'smallint'
    ) THEN
        -- 删除默认值
        ALTER TABLE pay_demo_order ALTER COLUMN pay_status DROP DEFAULT;

        -- 修改字段类型 (0 -> false, 非0 -> true)
        ALTER TABLE pay_demo_order ALTER COLUMN pay_status TYPE boolean USING (pay_status::int::boolean);

        -- 设置新的默认值
        ALTER TABLE pay_demo_order ALTER COLUMN pay_status SET DEFAULT false;

        RAISE NOTICE 'pay_demo_order.pay_status 字段类型已从 smallint 修改为 boolean';
    ELSE
        RAISE NOTICE 'pay_demo_order.pay_status 字段类型已经是 boolean，无需修改';
    END IF;
END $$;

-- =============================================
-- 修复 pay_notify_task 表缺失字段
-- =============================================

DO $$
BEGIN
    -- 添加 merchant_refund_id 字段
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'pay_notify_task'
        AND column_name = 'merchant_refund_id'
    ) THEN
        ALTER TABLE pay_notify_task ADD COLUMN merchant_refund_id VARCHAR(64) DEFAULT NULL;
        COMMENT ON COLUMN pay_notify_task.merchant_refund_id IS '商户退款编号（商户系统生成）';
        RAISE NOTICE 'pay_notify_task.merchant_refund_id 字段已添加';
    ELSE
        RAISE NOTICE 'pay_notify_task.merchant_refund_id 字段已存在，无需添加';
    END IF;

    -- 添加 merchant_transfer_id 字段
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'pay_notify_task'
        AND column_name = 'merchant_transfer_id'
    ) THEN
        ALTER TABLE pay_notify_task ADD COLUMN merchant_transfer_id VARCHAR(64) DEFAULT NULL;
        COMMENT ON COLUMN pay_notify_task.merchant_transfer_id IS '商户转账编号（商户系统生成）';
        RAISE NOTICE 'pay_notify_task.merchant_transfer_id 字段已添加';
    ELSE
        RAISE NOTICE 'pay_notify_task.merchant_transfer_id 字段已存在，无需添加';
    END IF;
END $$;
