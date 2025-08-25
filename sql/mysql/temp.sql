CREATE TABLE coal_production_plan (
                                      id BIGINT PRIMARY KEY IDENTITY(1,1),
                                      plan_code NVARCHAR(50) NOT NULL,              -- 计划编号
                                      plan_type TINYINT NOT NULL,                   -- 计划类型：1年度 2月度 3日计划 4班计划
                                      plan_year INT,                                -- 计划年度
                                      plan_month TINYINT,                           -- 计划月份
                                      plan_date DATE,                               -- 计划日期
                                      shift_id BIGINT,                              -- 班次ID

    -- 计划层级关系
                                      parent_plan_id BIGINT DEFAULT 0,              -- 父级计划ID（年→月→日→班的层级关系）
                                      plan_level TINYINT NOT NULL,                  -- 计划层级：1年度 2月度 3日计划 4班计划
                                      plan_path NVARCHAR(500),                      -- 计划层级路径（如：1/2/3表示年/月/日）

    -- 计划产量数据（基础）
                                      raw_coal_plan DECIMAL(10,2),                  -- 计划入洗原煤量(吨)

    -- 精煤产量计划（按粒度分类）
                                      fine_coal_plan DECIMAL(10,2),                 -- 计划末煤产量(吨)
                                      granular_coal_plan DECIMAL(10,2),             -- 计划粒煤产量(吨)
                                      large_block_coal_plan DECIMAL(10,2),          -- 计划大块煤产量(吨)
                                      medium_block_coal_plan DECIMAL(10,2),         -- 计划中块煤产量(吨)
                                      small_block_coal_plan DECIMAL(10,2),          -- 计划小块煤产量(吨)

    -- 其他产品计划产量
                                      middling_coal_plan DECIMAL(10,2),             -- 计划中煤产量(吨)
                                      slime_plan DECIMAL(10,2),                     -- 计划煤泥产量(吨)
                                      gangue_plan DECIMAL(10,2),                    -- 计划矸石产量(吨)

    -- 预留计划产量字段
                                      reserved_product_plan1 DECIMAL(10,2),         -- 预留计划产量字段1(吨)
                                      reserved_product_plan2 DECIMAL(10,2),         -- 预留计划产量字段2(吨)

    -- 质量指标
                                      fine_coal_ash DECIMAL(5,2),                   -- 末煤灰分(%)
                                      granular_coal_ash DECIMAL(5,2),               -- 粒煤灰分(%)
                                      large_block_coal_ash DECIMAL(5,2),            -- 大块煤灰分(%)
                                      medium_block_coal_ash DECIMAL(5,2),           -- 中块煤灰分(%)
                                      small_block_coal_ash DECIMAL(5,2),            -- 小块煤灰分(%)
                                      middling_coal_ash DECIMAL(5,2),               -- 中煤灰分(%)

    -- 计划状态
                                      plan_status TINYINT DEFAULT 1,                -- 计划状态：1待执行 2执行中 3已完成 4已取消

    -- 审批信息
                                      creator_id BIGINT,                            -- 制定人ID
                                      approver_id BIGINT,                           -- 审批人ID
                                      approve_time DATETIME,                        -- 审批时间

    -- 基础字段
                                      creator NVARCHAR(64) DEFAULT '',
                                      create_time DATETIME DEFAULT GETDATE(),
                                      updater NVARCHAR(64) DEFAULT '',
                                      update_time DATETIME DEFAULT GETDATE(),
                                      deleted BIT DEFAULT 0,
                                      tenant_id BIGINT DEFAULT 0
);