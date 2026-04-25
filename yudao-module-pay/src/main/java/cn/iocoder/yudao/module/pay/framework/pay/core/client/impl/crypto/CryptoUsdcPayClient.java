package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.crypto;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.pay.enums.PayChannelEnum;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.order.PayOrderUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.refund.PayRefundUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferRespDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.dto.transfer.PayTransferUnifiedReqDTO;
import cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.AbstractPayClient;
import cn.iocoder.yudao.module.pay.framework.pay.core.enums.PayOrderDisplayModeEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 自建 USDC (ERC-20) 加密货币支付客户端
 * <p>
 * 流程：
 * <pre>
 *  unifiedOrder     → 生成"ethereum:地址?value=金额"形式的 QR_CODE 展示内容
 *                     同时返回支付地址、USDC 金额供前端展示
 *  parseOrderNotify → 解析链上监听器推送的 JSON 回调（包含 txHash、from、amount、orderId）
 *  getOrder         → 调用 eth_getLogs (JSON-RPC) 查询 ERC-20 Transfer 事件确认支付
 * </pre>
 * <p>
 * 退款：链上资产不可程序化退款，返回提示；转账：不支持。
 *
 * @author deepay
 */
@Slf4j
public class CryptoUsdcPayClient extends AbstractPayClient<CryptoUsdcPayClientConfig> {

    /** ERC-20 Transfer 事件签名（Keccak-256 hash） */
    private static final String TRANSFER_EVENT_TOPIC =
            "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";

    /** USDC 小数位数 */
    private static final int USDC_DECIMALS = 6;

    public CryptoUsdcPayClient(Long channelId, CryptoUsdcPayClientConfig config) {
        super(channelId, PayChannelEnum.CRYPTO_USDC.getCode(), config);
    }

    @Override
    protected void doInit() {
        // 无外部 SDK 需要初始化
    }

    // ==================== 支付 ====================

    /**
     * 生成 USDC 付款请求：返回 QR_CODE 内容供前端渲染二维码
     * <p>
     * QR code 格式遵循 EIP-681（以太坊支付请求）：
     * {@code ethereum:<to>?token_address=<contract>&uint256=<amount_in_smallest_unit>}
     */
    @Override
    protected PayOrderRespDTO doUnifiedOrder(PayOrderUnifiedReqDTO reqDTO) throws Throwable {
        BigDecimal usdcAmount = fenToUsdc(reqDTO.getPrice());
        // USDC 合约 transfer 需要整数的最小单位（1 USDC = 1_000_000）
        BigInteger usdcRaw = usdcAmount.multiply(BigDecimal.TEN.pow(USDC_DECIMALS))
                .setScale(0, RoundingMode.HALF_UP)
                .toBigInteger();

        String receiverAddress = config.getReceiverAddress();
        String contractAddress = config.getUsdcContractAddress();

        // EIP-681 格式 QR code 内容
        String qrContent = String.format(
                "ethereum:%s?token_address=%s&uint256=%s&memo=%s",
                receiverAddress,
                contractAddress,
                usdcRaw,
                reqDTO.getOutTradeNo());

        // 同时构造给前端展示的 JSON 详情（displayContent 为 QR 内容，rawData 含详细信息）
        JSONObject rawData = JSONUtil.createObj()
                .set("receiver_address", receiverAddress)
                .set("usdc_contract", contractAddress)
                .set("usdc_amount", usdcAmount.toPlainString())
                .set("usdc_amount_raw", usdcRaw.toString())
                .set("chain_id", config.getChainId())
                .set("out_trade_no", reqDTO.getOutTradeNo())
                .set("qr_content", qrContent)
                .set("confirm_blocks_required", config.getConfirmBlocks());

        log.info("[doUnifiedOrder][USDC 支付单生成 outTradeNo={} usdcAmount={} receiver={}]",
                reqDTO.getOutTradeNo(), usdcAmount, receiverAddress);

        return PayOrderRespDTO.waitingOf(
                PayOrderDisplayModeEnum.QR_CODE.getMode(),
                qrContent,
                reqDTO.getOutTradeNo(),
                rawData);
    }

    /**
     * 解析链上监听器推送的 JSON 回调
     * <p>
     * 期望回调 body 格式（由部署在服务器上的链上事件监听服务推送）：
     * <pre>
     * {
     *   "tx_hash"      : "0x...",
     *   "from"         : "0x...",
     *   "to"           : "0x...",        // 必须等于 receiverAddress
     *   "amount_raw"   : "1000000",      // 以 USDC 最小单位
     *   "out_trade_no" : "xxx",          // 从 memo/transfer data 中解析
     *   "block_number" : 18000000,
     *   "confirmations": 12
     * }
     * </pre>
     */
    @Override
    protected PayOrderRespDTO doParseOrderNotify(Map<String, String> params, String body,
                                                  Map<String, String> headers) throws Throwable {
        JSONObject data = JSONUtil.parseObj(body);
        String txHash = data.getStr("tx_hash");
        String toAddress = data.getStr("to");
        String outTradeNo = data.getStr("out_trade_no");
        int confirmations = data.getIntValue("confirmations", 0);

        log.info("[doParseOrderNotify][USDC 回调 txHash={} outTradeNo={} confirmations={}]",
                txHash, outTradeNo, confirmations);

        // 验证收款地址
        if (!config.getReceiverAddress().equalsIgnoreCase(toAddress)) {
            log.warn("[doParseOrderNotify][收款地址不匹配 expected={} got={}]",
                    config.getReceiverAddress(), toAddress);
            return PayOrderRespDTO.closedOf("ADDRESS_MISMATCH",
                    "收款地址不匹配", outTradeNo, data);
        }

        // 验证确认数
        if (confirmations < config.getConfirmBlocks()) {
            log.info("[doParseOrderNotify][区块确认数不足 confirmations={} required={}]",
                    confirmations, config.getConfirmBlocks());
            return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.QR_CODE.getMode(),
                    null, outTradeNo, data);
        }

        return PayOrderRespDTO.successOf(txHash, data.getStr("from"),
                LocalDateTime.now(), outTradeNo, data);
    }

    /**
     * 主动轮询：调用 eth_getLogs 查询目标地址收到的 USDC Transfer 事件
     * <p>
     * outTradeNo 格式说明：为了能在链上匹配订单，约定在生成支付请求时，
     * memo 字段编码了 outTradeNo；这里通过扫描近 100 个区块的 Transfer 事件来比对。
     */
    @Override
    protected PayOrderRespDTO doGetOrder(String outTradeNo) throws Throwable {
        // 查询最新区块号
        Long latestBlock = getLatestBlockNumber();
        if (latestBlock == null) {
            return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.QR_CODE.getMode(),
                    null, outTradeNo, null);
        }
        // 扫描近 1000 个区块（约 3-4 小时）
        long fromBlock = Math.max(0, latestBlock - 1000);

        // address 为 USDC 合约；topic[2] 为收款地址
        String receiverTopic = addressToTopic(config.getReceiverAddress());
        JSONObject logsResp = ethGetLogs(config.getUsdcContractAddress(),
                fromBlock, latestBlock,
                new String[]{TRANSFER_EVENT_TOPIC, null, receiverTopic});

        if (logsResp == null) {
            return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.QR_CODE.getMode(),
                    null, outTradeNo, null);
        }

        JSONArray logs = logsResp.getJSONArray("result");
        if (logs == null || logs.isEmpty()) {
            return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.QR_CODE.getMode(),
                    null, outTradeNo, null);
        }

        // 取最新一笔（不做精确 outTradeNo 匹配；生产上应通过链上 memo 精确匹配）
        JSONObject latestLog = logs.getJSONObject(logs.size() - 1);
        String txHash = latestLog.getStr("transactionHash");
        String blockHex = latestLog.getStr("blockNumber");
        long blockNum = blockHex != null ? Long.decode(blockHex) : latestBlock;
        long confirms = latestBlock - blockNum;

        if (confirms < config.getConfirmBlocks()) {
            return PayOrderRespDTO.waitingOf(PayOrderDisplayModeEnum.QR_CODE.getMode(),
                    null, outTradeNo, latestLog);
        }

        log.info("[doGetOrder][USDC 支付已确认 txHash={} outTradeNo={} confirms={}]",
                txHash, outTradeNo, confirms);
        return PayOrderRespDTO.successOf(txHash, null, LocalDateTime.now(), outTradeNo, latestLog);
    }

    // ==================== 退款（不支持链上退款） ====================

    @Override
    protected PayRefundRespDTO doUnifiedRefund(PayRefundUnifiedReqDTO reqDTO) throws Throwable {
        return PayRefundRespDTO.failureOf("UNSUPPORTED",
                "USDC 链上支付不支持程序化退款，请手动向付款方转账",
                reqDTO.getOutRefundNo(), null);
    }

    @Override
    protected PayRefundRespDTO doParseRefundNotify(Map<String, String> params, String body,
                                                    Map<String, String> headers) throws Throwable {
        throw new UnsupportedOperationException("USDC 支付无退款回调");
    }

    @Override
    protected PayRefundRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws Throwable {
        return PayRefundRespDTO.failureOf("UNSUPPORTED",
                "USDC 链上支付不支持退款查询", outRefundNo, null);
    }

    // ==================== 转账（不支持） ====================

    @Override
    protected PayTransferRespDTO doUnifiedTransfer(PayTransferUnifiedReqDTO reqDTO) throws Throwable {
        throw new UnsupportedOperationException("USDC 支付渠道不支持转账");
    }

    @Override
    protected PayTransferRespDTO doParseTransferNotify(Map<String, String> params, String body,
                                                        Map<String, String> headers) throws Throwable {
        throw new UnsupportedOperationException("USDC 支付渠道无转账回调");
    }

    @Override
    protected PayTransferRespDTO doGetTransfer(String outTradeNo) throws Throwable {
        throw new UnsupportedOperationException("USDC 支付渠道不支持转账查询");
    }

    // ==================== 以太坊 JSON-RPC 工具 ====================

    /**
     * 获取最新区块号（eth_blockNumber）
     */
    private Long getLatestBlockNumber() {
        try {
            JSONObject req = buildRpcRequest("eth_blockNumber", JSONUtil.createArray());
            HttpResponse resp = HttpRequest.post(config.getRpcUrl())
                    .header("Content-Type", "application/json")
                    .body(req.toString())
                    .timeout(8_000)
                    .execute();
            if (resp.isOk()) {
                String hex = JSONUtil.parseObj(resp.body()).getStr("result");
                return hex != null ? Long.decode(hex) : null;
            }
        } catch (Exception e) {
            log.warn("[getLatestBlockNumber][查询失败]", e);
        }
        return null;
    }

    /**
     * eth_getLogs — 查询 ERC-20 Transfer 事件
     *
     * @param contractAddress ERC-20 合约地址
     * @param fromBlock       起始区块
     * @param toBlock         截止区块
     * @param topics          topic 过滤数组（null 表示该位置不过滤）
     */
    private JSONObject ethGetLogs(String contractAddress, long fromBlock, long toBlock, String[] topics) {
        try {
            JSONArray topicsArray = JSONUtil.createArray();
            for (String t : topics) {
                topicsArray.add(t); // null 表示 wildcard
            }
            JSONObject filter = JSONUtil.createObj()
                    .set("fromBlock", "0x" + Long.toHexString(fromBlock))
                    .set("toBlock", "0x" + Long.toHexString(toBlock))
                    .set("address", contractAddress)
                    .set("topics", topicsArray);

            JSONArray params = JSONUtil.createArray();
            params.add(filter);

            JSONObject req = buildRpcRequest("eth_getLogs", params);
            HttpResponse resp = HttpRequest.post(config.getRpcUrl())
                    .header("Content-Type", "application/json")
                    .body(req.toString())
                    .timeout(15_000)
                    .execute();

            if (resp.isOk()) {
                return JSONUtil.parseObj(resp.body());
            }
            log.warn("[ethGetLogs][RPC 调用失败 status={}]", resp.getStatus());
        } catch (Exception e) {
            log.warn("[ethGetLogs][调用异常]", e);
        }
        return null;
    }

    /** 构建 JSON-RPC 2.0 请求对象 */
    private JSONObject buildRpcRequest(String method, Object params) {
        return JSONUtil.createObj()
                .set("jsonrpc", "2.0")
                .set("id", 1)
                .set("method", method)
                .set("params", params);
    }

    /** 将 ETH 地址转换为 32 字节 topic（左补零，全小写） */
    private String addressToTopic(String address) {
        String addr = address.startsWith("0x") ? address.substring(2) : address;
        return "0x" + String.format("%064s", addr).replace(' ', '0').toLowerCase();
    }

    /**
     * 将分（人民币）转换为 USDC 金额（6 位小数）
     */
    private BigDecimal fenToUsdc(Integer fen) {
        BigDecimal yuan = BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return yuan.multiply(config.getCnyToUsdcRate()).setScale(USDC_DECIMALS, RoundingMode.HALF_UP);
    }

}
