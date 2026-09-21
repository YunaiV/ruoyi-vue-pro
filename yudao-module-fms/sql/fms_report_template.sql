-- FMS 报表模板种子数据（fms_report_template，全局表 @TenantIgnore）
-- 修复 A16：模板表零种子行导致资产负债表/利润表/现金流量表恒为空表
-- 幂等：fms_report_template 只被报表服务读取（selectListByType），无应用写入路径；
--       先按 type 删除既有模板行，再以固定 id 全量插入，可重复执行
DELETE FROM `fms_report_template` WHERE `type` IN (1, 2, 3, 4);

-- type=1 资产负债表：行次1=货币资金、30=资产总计、51=未分配利润、53=负债和所有者权益总计；
-- 行次0为表头（资产侧 sort<32，负债侧 sort>=32，两侧表头 rowId=0 配对），资产侧行次<=30，负债侧行次>30
INSERT INTO `fms_report_template`
    (`id`, `name`, `row_no`, `formula`, `editable`, `sort`, `row_id`, `type`, `category`, `level`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (1,  '资产：', 0, '[]', b'0', 0, 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (2,  '货币资金', 1, '[{"operator":"+","rules":0,"subjectNumber":"1001"},{"operator":"+","rules":0,"subjectNumber":"1002"},{"operator":"+","rules":0,"subjectNumber":"1012"}]', b'1', 1, 1, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (3,  '应收账款', 3, '[{"operator":"+","rules":0,"subjectNumber":"1122"}]', b'1', 2, 2, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (4,  '其他应收款', 5, '[{"operator":"+","rules":0,"subjectNumber":"1221"}]', b'1', 3, 3, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (5,  '存货', 9, '[{"operator":"+","rules":0,"subjectNumber":"1405"}]', b'1', 4, 4, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (6,  '固定资产', 20, '[{"operator":"+","rules":0,"subjectNumber":"1601"},{"operator":"-","rules":0,"subjectNumber":"1602"}]', b'1', 5, 5, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (7,  '在建工程', 21, '[{"operator":"+","rules":0,"subjectNumber":"1701"}]', b'1', 6, 6, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (8,  '无形资产', 22, '[{"operator":"+","rules":0,"subjectNumber":"1801"}]', b'1', 7, 7, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (9,  '资产总计', 30, '["L1+L3+L5+L9+L20+L21+L22"]', b'0', 8, 8, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (10, '负债和所有者权益：', 0, '[]', b'0', 32, 0, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (11, '短期借款', 31, '[{"operator":"+","rules":0,"subjectNumber":"2001"}]', b'1', 33, 1, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (12, '应付账款', 33, '[{"operator":"+","rules":0,"subjectNumber":"2202"}]', b'1', 34, 2, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (13, '应付职工薪酬', 34, '[{"operator":"+","rules":0,"subjectNumber":"2211"}]', b'1', 35, 3, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (14, '应交税费', 35, '[{"operator":"+","rules":0,"subjectNumber":"2221"}]', b'1', 36, 4, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (15, '长期借款', 42, '[{"operator":"+","rules":0,"subjectNumber":"2501"}]', b'1', 37, 5, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (16, '负债合计', 47, '["L31+L33+L34+L35+L42"]', b'0', 38, 8, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (17, '实收资本', 49, '[{"operator":"+","rules":0,"subjectNumber":"4001"}]', b'1', 39, 9, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (18, '未分配利润', 51, '[{"operator":"+","rules":0,"subjectNumber":"3103"},{"operator":"+","rules":0,"subjectNumber":"310415"}]', b'1', 40, 10, 1, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (19, '所有者权益合计', 52, '["L49+L51"]', b'0', 41, 11, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (20, '负债和所有者权益总计', 53, '["L47+L52"]', b'0', 42, 12, 1, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0);

-- type=2 利润表：行次32=净利润（勾稽检查硬依赖）；行次1=营业收入、2/3=营业成本与税金及附加、
-- 11/14/18=销售/管理/财务费用、30=利润总额，与首页财务指标预置（L1/L2/L3/L11/L14/L18/L30）对齐
INSERT INTO `fms_report_template`
    (`id`, `name`, `row_no`, `formula`, `editable`, `sort`, `row_id`, `type`, `category`, `level`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (21, '一、营业收入', 1, '[{"operator":"+","rules":6,"subjectNumber":"5001"},{"operator":"+","rules":6,"subjectNumber":"5051"}]', b'1', 1, 0, 2, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (22, '减：营业成本', 2, '[{"operator":"+","rules":5,"subjectNumber":"5401"},{"operator":"+","rules":5,"subjectNumber":"5402"}]', b'1', 2, 0, 2, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (23, '减：税金及附加', 3, '[{"operator":"+","rules":5,"subjectNumber":"5403"}]', b'1', 3, 0, 2, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (24, '减：销售费用', 11, '[{"operator":"+","rules":5,"subjectNumber":"5601"}]', b'1', 4, 0, 2, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (25, '减：管理费用', 14, '[{"operator":"+","rules":5,"subjectNumber":"5602"}]', b'1', 5, 0, 2, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (26, '减：财务费用', 18, '[{"operator":"+","rules":5,"subjectNumber":"5603"}]', b'1', 6, 0, 2, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (27, '二、营业利润', 19, '["L1-L2-L3-L11-L14-L18"]', b'0', 7, 0, 2, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (28, '加：营业外收入', 20, '[{"operator":"+","rules":6,"subjectNumber":"5301"}]', b'1', 8, 0, 2, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (29, '减：营业外支出', 21, '[]', b'1', 9, 0, 2, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (30, '三、利润总额', 30, '["L19+L20-L21"]', b'0', 10, 0, 2, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (31, '减：所得税费用', 31, '[{"operator":"+","rules":5,"subjectNumber":"5801"}]', b'1', 11, 0, 2, 0, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (32, '四、净利润', 32, '["L30-L31"]', b'0', 12, 0, 2, 0, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0);

-- type=3 现金流量表：行次6=支付其他与经营活动有关的现金（倒挤归集行）、行次22=期末现金余额（硬依赖）；
-- 公式为跨报表表达式：BA[行次,1期初|2期末]=资产负债表、IN行次=利润表、EX行次=辅助数据、L行次=本表
INSERT INTO `fms_report_template`
    (`id`, `name`, `row_no`, `formula`, `editable`, `sort`, `row_id`, `type`, `category`, `level`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (33, '销售商品、提供劳务收到的现金', 1, '["IN1"]', b'1', 1, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (34, '收到的其他与经营活动有关的现金', 2, '["EX1"]', b'1', 2, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (35, '经营活动现金流入小计', 3, '["L1+L2"]', b'0', 3, 0, 3, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (36, '支付给职工以及为职工支付的现金', 4, '["EX2"]', b'1', 4, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (37, '支付的各项税费', 5, '["EX3"]', b'1', 5, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (38, '支付其他与经营活动有关的现金', 6, '["EX4"]', b'1', 6, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (39, '经营活动产生的现金流量净额', 7, '["L3-L4-L5-L6"]', b'0', 7, 0, 3, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (40, '处置固定资产、无形资产和其他长期资产收回的现金净额', 8, '["EX5"]', b'1', 8, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (41, '购建固定资产、无形资产和其他长期资产支付的现金', 9, '["EX6"]', b'1', 9, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (42, '投资活动产生的现金流量净额', 10, '["L8-L9"]', b'0', 10, 0, 3, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (43, '取得借款收到的现金', 11, '["EX7"]', b'1', 11, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (44, '偿还债务支付的现金', 12, '["EX8"]', b'1', 12, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (45, '筹资活动产生的现金流量净额', 13, '["L11-L12"]', b'0', 13, 0, 3, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (46, '现金及现金等价物净增加额', 20, '["L7+L10+L13"]', b'0', 14, 0, 3, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (47, '期初现金及现金等价物余额', 21, '["BA[1,1]"]', b'1', 15, 0, 3, 1, 2, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (48, '期末现金及现金等价物余额', 22, '["L20+L21"]', b'0', 16, 0, 3, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0);

-- type=4 现金流量辅助数据（主表 EX 行次的取数来源）：科目公式=用户可配置类型，行次公式=固定类型；
-- category 必须为 1（CASH_FLOW_CATEGORY_MAIN），否则配置初始化后无法被查询命中
INSERT INTO `fms_report_template`
    (`id`, `name`, `row_no`, `formula`, `editable`, `sort`, `row_id`, `type`, `category`, `level`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`)
VALUES
    (49, '收到的其他与经营活动有关的现金', 1, '[]', b'1', 1, 0, 4, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (50, '支付给职工以及为职工支付的现金', 2, '[]', b'1', 2, 0, 4, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (51, '支付的各项税费', 3, '[]', b'1', 3, 0, 4, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (52, '支付其他与经营活动有关的现金', 4, '[]', b'1', 4, 0, 4, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (53, '处置固定资产、无形资产和其他长期资产收回的现金净额', 5, '[]', b'1', 5, 0, 4, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (54, '购建固定资产、无形资产和其他长期资产支付的现金', 6, '[]', b'1', 6, 0, 4, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (55, '取得借款收到的现金', 7, '[]', b'1', 7, 0, 4, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0),
    (56, '偿还债务支付的现金', 8, '[]', b'1', 8, 0, 4, 1, 1, 'admin', NOW(), 'admin', NOW(), b'0', 0);
