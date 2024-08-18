package cn.iocoder.yudao.module.pay.api.refund.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 退款单创建 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class PayRefundCreateReqDTO {

    /**
     * 应用标识
     */
    @NotNull(message = "应用标识不能为空")
    private String appKey;
    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    // ========== 商户相关字段 ==========
    /**
     * 商户订单编号
     */
    @NotEmpty(message = "商户订单编号不能为空")
    private String merchantOrderId;

    /**
     * 商户退款编号
     */
    @NotEmpty(message = "商户退款编号不能为空")
    private String merchantRefundId;

    /**
     * 退款描述
     */
    @NotEmpty(message = "退款描述不能为空")
    @Length(max = 128, message = "退款描述长度不能超过 128")
    private String reason;

    // ========== 订单相关字段 ==========

    /**
     * 退款金额，单位：分
     */
    @NotNull(message = "退款金额不能为空")
    @Min(value = 1, message = "退款金额必须大于零")
    private Integer price;

}
