-- ============================================================================================
-- 新增支付渠道：Coinbase Commerce 加密货币支付 + 自建 USDC 加密货币支付
-- 执行数据库：MySQL / MariaDB
-- ============================================================================================

-- 1. 在字典数据中注册新渠道（pay_channel_code 字典类型）
--    注：id 使用较大值避免与主库冲突；实际部署请根据最大 id 调整
INSERT INTO `system_dict_data`
    (`id`, `sort`, `label`, `value`, `dict_type`, `status`,
     `color_type`, `css_class`, `remark`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
    (9001, 30, 'Coinbase Commerce 加密货币支付', 'coinbase_commerce',
     'pay_channel_code', 0, 'warning', '', 'Coinbase Commerce 支持 USDC/ETH/BTC 等加密货币收款',
     '1', NOW(), '1', NOW(), b'0'),

    (9002, 31, '自建 USDC 加密货币支付', 'crypto_usdc',
     'pay_channel_code', 0, 'warning', '', '自建 USDC ERC-20 收款地址，链上确认到账',
     '1', NOW(), '1', NOW(), b'0');

-- ============================================================================================
-- 2. 示例：向 pay_channel 表插入渠道配置（实际 API Key 在管理后台填写，此处仅作参考）
--
--    执行前请确认：
--      - app_id 对应 pay_app 表中 appKey='wholesale' 的应用 id
--      - config JSON 中的 apiKey / webhookSecret / receiverAddress / rpcUrl 需替换为真实值
-- ============================================================================================

/*
INSERT INTO `pay_channel`
    (`id`, `code`, `status`, `remark`, `fee_rate`, `app_id`, `config`,
     `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES
    -- Coinbase Commerce
    (9001, 'coinbase_commerce', 0, 'Coinbase Commerce 加密货币支付',
     0,
     (SELECT id FROM pay_app WHERE app_key = 'wholesale' LIMIT 1),
     '{"apiKey":"YOUR_COINBASE_API_KEY","webhookSecret":"YOUR_WEBHOOK_SECRET","currency":"USD","redirectUrl":"https://your-domain.com/pay/success","cancelUrl":"https://your-domain.com/pay/cancel"}',
     '1', NOW(), '1', NOW(), b'0'),

    -- 自建 USDC
    (9002, 'crypto_usdc', 0, '自建 USDC 加密货币支付',
     0,
     (SELECT id FROM pay_app WHERE app_key = 'wholesale' LIMIT 1),
     '{"receiverAddress":"0xYOUR_WALLET_ADDRESS","usdcContractAddress":"0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48","rpcUrl":"https://mainnet.infura.io/v3/YOUR_PROJECT_ID","chainId":1,"cnyToUsdcRate":0.1389,"confirmBlocks":12}',
     '1', NOW(), '1', NOW(), b'0');
*/
