package cn.iocoder.yudao.module.pay.controller.admin.refund.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel("管理后台 - 退款订单详情 Response VO")
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
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

}
