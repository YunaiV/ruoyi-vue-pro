package cn.iocoder.yudao.module.pay.controller.admin.refund.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ApiModel("管理后台 - 退款订单分页查询 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayRefundPageItemRespVO extends PayRefundBaseVO {

    @ApiModelProperty(value = "支付订单编号", required = true)
    private Long id;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "应用名称")
    private String  appName;

    @ApiModelProperty(value = "渠道名称")
    private String channelCodeName;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
