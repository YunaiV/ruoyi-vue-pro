package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel;

import lombok.*;

import java.time.LocalDateTime;
import io.swagger.annotations.*;

@ApiModel("管理后台 - 支付渠道 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelRespVO extends PayChannelBaseVO {

    @ApiModelProperty(value = "商户编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "配置", required = true)
    private String config;
}
