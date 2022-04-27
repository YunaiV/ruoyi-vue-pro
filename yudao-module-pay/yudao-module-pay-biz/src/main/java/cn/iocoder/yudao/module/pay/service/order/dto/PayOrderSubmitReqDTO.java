package cn.iocoder.yudao.module.pay.service.order.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * 支付单提交 Request DTO
 */
@Data
@Accessors(chain = true)
public class PayOrderSubmitReqDTO implements Serializable {

    /**
     * 应用编号
     */
    @NotNull(message = "应用编号不能为空")
    private Long appId;

    /**
     * 支付单编号
     */
    @NotNull(message = "支付单编号不能为空")
    private Long id;

    /**
     * 支付渠道
     */
    @NotEmpty(message = "支付渠道不能为空")
    private String channelCode;

    /**
     * 用户 IP
     */
    @NotEmpty(message = "用户 IP 不能为空")
    private String userIp;

    /**
     * 支付渠道的额外参数
     */
    private Map<String, String> channelExtras;

}
