package cn.iocoder.yudao.module.pay.controller.admin.order.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 支付订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderPageItemRespVO extends PayOrderBaseVO {

    @ApiModelProperty(value = "支付订单编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    @ApiModelProperty(value = "应用名称")
    private String  appName;

    @ApiModelProperty(value = "渠道名称")
    private String channelCodeName;

    @ApiModelProperty(value = "支付订单号")
    private String no;


}
