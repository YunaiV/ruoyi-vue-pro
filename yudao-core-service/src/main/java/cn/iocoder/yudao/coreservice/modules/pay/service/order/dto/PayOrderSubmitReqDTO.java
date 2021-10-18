package cn.iocoder.yudao.coreservice.modules.pay.service.order.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 支付单提交 Request DTO
 */
@Data
@Accessors(chain = true)
public class PayOrderSubmitReqDTO implements Serializable {

    /**
     * 应用编号
     */
    @NotEmpty(message = "应用编号不能为空")
    private String appId;

    /**
     * 支付单编号
     */
    @NotNull(message = "支付单编号不能为空")
    private Long id;

    /**
     * 支付渠道
     */
    @NotNull(message = "支付渠道")
    private String channelCode;

    /**
     * 客户端 IP
     */
    @NotEmpty(message = "客户端 IP 不能为空")
    private String clientIp;

}
