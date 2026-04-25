package cn.iocoder.yudao.module.pay.framework.pay.core.client.impl.crypto;

import cn.iocoder.yudao.module.pay.framework.pay.core.client.PayClientConfig;
import lombok.Data;

import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * 自建加密货币（USDC / ERC-20）支付渠道配置
 * <p>
 * 管理后台在 pay_channel 表中配置以下字段（JSON 序列化存储）：
 * <ul>
 *   <li>{@code receiverAddress}  — 平台收款钱包地址（EIP-55 校验和格式）</li>
 *   <li>{@code usdcContractAddress} — USDC ERC-20 合约地址（以太坊主网固定值）</li>
 *   <li>{@code rpcUrl}           — 以太坊节点 RPC URL（Infura / Alchemy）</li>
 *   <li>{@code chainId}          — 链 ID，主网=1，Polygon=137，BSC=56</li>
 *   <li>{@code cnyToUsdcRate}    — 人民币兑 USDC 汇率（实际应对接汇率服务）</li>
 *   <li>{@code confirmBlocks}    — 需要等待的区块确认数，默认 12</li>
 * </ul>
 *
 * @author deepay
 */
@Data
public class CryptoUsdcPayClientConfig implements PayClientConfig {

    /**
     * 以太坊主网 USDC 合约地址（默认值，可在配置中覆盖）
     */
    public static final String USDC_MAINNET_CONTRACT = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48";

    /**
     * 平台收款钱包地址
     */
    @NotBlank(message = "收款钱包地址不能为空")
    private String receiverAddress;

    /**
     * USDC ERC-20 合约地址
     */
    @NotBlank(message = "USDC 合约地址不能为空")
    private String usdcContractAddress = USDC_MAINNET_CONTRACT;

    /**
     * 以太坊节点 RPC URL
     */
    @NotBlank(message = "以太坊节点 RPC URL 不能为空")
    private String rpcUrl;

    /**
     * 链 ID（1=Ethereum 主网，137=Polygon，56=BSC）
     */
    @Positive(message = "链 ID 必须为正整数")
    private int chainId = 1;

    /**
     * 人民币兑 USDC 汇率（1 CNY = X USDC）
     * 实际部署应对接实时汇率 API，此处提供默认值
     */
    private BigDecimal cnyToUsdcRate = new BigDecimal("0.1389"); // ~1 CNY ≈ 0.1389 USDC（7.2 CNY/USD）

    /**
     * 等待的区块确认数，默认 12（约 2-3 分钟）
     */
    private int confirmBlocks = 12;

    @Override
    public void validate(Validator validator) {
        var violations = validator.validate(this);
        if (!violations.isEmpty()) {
            throw new javax.validation.ValidationException(
                    violations.iterator().next().getMessage());
        }
    }

}
