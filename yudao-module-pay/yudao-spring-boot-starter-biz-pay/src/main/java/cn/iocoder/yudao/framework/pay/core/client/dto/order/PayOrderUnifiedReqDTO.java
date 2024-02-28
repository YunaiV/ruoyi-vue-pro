package cn.iocoder.yudao.framework.pay.core.client.dto.order;

import cn.iocoder.yudao.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 统一下单 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class PayOrderUnifiedReqDTO {

    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    // ========== 商户相关字段 ==========

    /**
     * 外部订单号
     *
     * 对应 PayOrderExtensionDO 的 no 字段
     */
    @NotEmpty(message = "外部订单编号不能为空")
    private String outTradeNo;
    /**
     * 商品标题
     */
    @NotEmpty(message = "商品标题不能为空")
    @Length(max = 32, message = "商品标题不能超过 32")
    private String subject;
    /**
     * 商品描述信息
     */
    @Length(max = 128, message = "商品描述信息长度不能超过128")
    private String body;
    /**
     * 支付结果的 notify 回调地址
     */
    @NotEmpty(message = "支付结果的回调地址不能为空")
    @URL(message = "支付结果的 notify 回调地址必须是 URL 格式")
    private String notifyUrl;
    /**
     * 支付结果的 return 回调地址
     */
    @URL(message = "支付结果的 return 回调地址必须是 URL 格式")
    private String returnUrl;

    // ========== 订单相关字段 ==========

    /**
     * 支付金额，单位：分
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "支付金额必须大于零")
    private Integer price;

    /**
     * 支付过期时间
     */
    @NotNull(message = "支付过期时间不能为空")
    private LocalDateTime expireTime;

    // ========== 拓展参数 ==========
    /**
     * 支付渠道的额外参数
     *
     * 例如说，微信公众号需要传递 openid 参数
     */
    private Map<String, String> channelExtras;

    /**
     * 展示模式
     *
     * 如果不传递，则每个支付渠道使用默认的方式
     *
     * 枚举 {@link PayOrderDisplayModeEnum}
     */
    private String displayMode;

}
