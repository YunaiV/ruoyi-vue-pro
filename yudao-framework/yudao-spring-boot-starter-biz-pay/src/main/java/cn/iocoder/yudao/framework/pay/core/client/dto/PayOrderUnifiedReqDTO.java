package cn.iocoder.yudao.framework.pay.core.client.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 统一下单 Request DTO
 */
@Data
public class PayOrderUnifiedReqDTO {

    /**
     * 客户端 IP
     */
    @NotEmpty(message = "客户端 IP 不能为空")
    private String clientIp;

    // ========== 商户相关字段 ==========

    /**
     * 商户订单编号
     */
    @NotEmpty(message = "商户订单编号不能为空")
    private String merchantOrderId;
    /**
     * 商品标题
     */
    @NotEmpty(message = "商品标题不能为空")
    @Length(max = 32, message = "商品标题不能超过 32")
    private String subject;
    /**
     * 商品描述信息
     */
    @NotEmpty(message = "商品描述信息不能为空")
    @Length(max = 128, message = "商品描述信息长度不能超过128")
    private String body;
    /**
     * 支付结果的回调地址
     */
    @NotEmpty(message = "支付结果的回调地址不能为空")
    @URL(message = "支付结果的回调地址必须是 URL 格式")
    private String notifyUrl;

    // ========== 订单相关字段 ==========

    /**
     * 支付金额，单位：分
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "支付金额必须大于零")
    private Integer amount;

    /**
     * 支付过期时间
     */
    @NotNull(message = "支付过期时间不能为空")
    private Date expireTime;

    // ========== 拓展参数 ==========
    // TODO 芋艿：待完善

}
