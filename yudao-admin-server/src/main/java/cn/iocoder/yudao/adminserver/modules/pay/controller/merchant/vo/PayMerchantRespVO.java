package cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo;

import lombok.*;
import java.util.*;
import io.swagger.annotations.*;

@ApiModel("支付商户信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayMerchantRespVO extends PayMerchantBaseVO {

    @ApiModelProperty(value = "商户编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private Date createTime;

}
