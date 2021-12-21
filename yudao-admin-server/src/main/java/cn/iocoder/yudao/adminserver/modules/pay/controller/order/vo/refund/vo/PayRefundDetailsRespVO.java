package cn.iocoder.yudao.adminserver.modules.pay.controller.order.vo.refund.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 退款订单详情 Response VO
 *
 * @author aquan
 */
@ApiModel("退款订单详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayRefundDetailsRespVO extends PayRefundBaseVO {

    @ApiModelProperty(value = "支付退款编号", required = true)
    private Long id;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "应用名称")
    private String appName;

    @ApiModelProperty(value = "渠道编号名称")
    private String channelCodeName;

    @NotNull(message = "商品标题不能为空")
    private String subject;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

}
