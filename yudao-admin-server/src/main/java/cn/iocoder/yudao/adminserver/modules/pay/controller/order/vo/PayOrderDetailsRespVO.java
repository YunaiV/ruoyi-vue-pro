package cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * 支付订单详细信息，由支付订单和支付订单扩展信息组成
 *
 * @author aquan
 */
@ApiModel("支付订单详细信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderDetailsRespVO extends PayOrderBaseVO {

    @ApiModelProperty(value = "支付订单编号")
    private Long id;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "应用名称")
    private String appName;

    @ApiModelProperty(value = "渠道编号名称")
    private String channelCodeName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 支付订单扩展
     */
    private PayOrderExtension payOrderExtension;

    @Data
    @ApiModel("支付订单扩展")
    public static class PayOrderExtension {

        @ApiModelProperty(value = "支付订单号")
        private String no;

        @ApiModelProperty(value = "支付渠道的额外参数")
        private String channelExtras;

        @ApiModelProperty(value = "支付异步通知的内容")
        private String channelNotifyData;
    }

}
