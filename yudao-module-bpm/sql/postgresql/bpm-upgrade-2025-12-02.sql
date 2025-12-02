/*
 PostgreSQL 兼容性修复脚本

 Date: 2025-12-02

 修复内容:
 1. bpm_process_definition_info 表字段类型修复
    - visible: smallint -> boolean (Java 实体类定义为 Boolean)
    - allow_cancel_running_process: smallint -> boolean (Java 实体类定义为 Boolean)
    - allow_withdraw_task: smallint -> boolean (Java 实体类定义为 Boolean)

 2. bpm_process_expression 表字段类型修复
    - deleted: smallint -> boolean (Java 实体类定义为 Boolean)
*/

-- =============================================
-- 修复 bpm_process_definition_info 表字段类型
-- =============================================

DO $$
BEGIN
    -- 修复 visible 字段
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'bpm_process_definition_info'
        AND column_name = 'visible'
        AND data_type = 'smallint'
    ) THEN
        ALTER TABLE bpm_process_definition_info ALTER COLUMN visible DROP DEFAULT;
        ALTER TABLE bpm_process_definition_info ALTER COLUMN visible TYPE boolean USING (visible::int::boolean);
        ALTER TABLE bpm_process_definition_info ALTER COLUMN visible SET DEFAULT true;
        RAISE NOTICE 'bpm_process_definition_info.visible 字段类型已从 smallint 修改为 boolean';
    ELSE
        RAISE NOTICE 'bpm_process_definition_info.visible 字段类型已经是 boolean，无需修改';
    END IF;

    -- 修复 allow_cancel_running_process 字段
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'bpm_process_definition_info'
        AND column_name = 'allow_cancel_running_process'
        AND data_type = 'smallint'
    ) THEN
        ALTER TABLE bpm_process_definition_info ALTER COLUMN allow_cancel_running_process DROP DEFAULT;
        ALTER TABLE bpm_process_definition_info ALTER COLUMN allow_cancel_running_process TYPE boolean USING (allow_cancel_running_process::int::boolean);
        ALTER TABLE bpm_process_definition_info ALTER COLUMN allow_cancel_running_process SET DEFAULT true;
        RAISE NOTICE 'bpm_process_definition_info.allow_cancel_running_process 字段类型已从 smallint 修改为 boolean';
    ELSE
        RAISE NOTICE 'bpm_process_definition_info.allow_cancel_running_process 字段类型已经是 boolean，无需修改';
    END IF;

    -- 修复 allow_withdraw_task 字段
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'bpm_process_definition_info'
        AND column_name = 'allow_withdraw_task'
        AND data_type = 'smallint'
    ) THEN
        ALTER TABLE bpm_process_definition_info ALTER COLUMN allow_withdraw_task DROP DEFAULT;
        ALTER TABLE bpm_process_definition_info ALTER COLUMN allow_withdraw_task TYPE boolean USING (allow_withdraw_task::int::boolean);
        ALTER TABLE bpm_process_definition_info ALTER COLUMN allow_withdraw_task SET DEFAULT true;
        RAISE NOTICE 'bpm_process_definition_info.allow_withdraw_task 字段类型已从 smallint 修改为 boolean';
    ELSE
        RAISE NOTICE 'bpm_process_definition_info.allow_withdraw_task 字段类型已经是 boolean，无需修改';
    END IF;
END $$;

-- =============================================
-- 修复 bpm_process_expression 表字段类型
-- =============================================

DO $$
BEGIN
    -- 修复 deleted 字段
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'bpm_process_expression'
        AND column_name = 'deleted'
        AND data_type = 'smallint'
    ) THEN
        ALTER TABLE bpm_process_expression ALTER COLUMN deleted DROP DEFAULT;
        ALTER TABLE bpm_process_expression ALTER COLUMN deleted TYPE boolean USING (deleted::int::boolean);
        ALTER TABLE bpm_process_expression ALTER COLUMN deleted SET DEFAULT false;
        RAISE NOTICE 'bpm_process_expression.deleted 字段类型已从 smallint 修改为 boolean';
    ELSE
        RAISE NOTICE 'bpm_process_expression.deleted 字段类型已经是 boolean，无需修改';
    END IF;
END $$;
