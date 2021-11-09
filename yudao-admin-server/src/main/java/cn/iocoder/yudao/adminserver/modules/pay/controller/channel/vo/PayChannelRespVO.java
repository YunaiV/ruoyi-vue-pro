package cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("支付渠道 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelRespVO extends PayChannelBaseVO {

    @ApiModelProperty(value = "商户编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
