package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.merchant;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@ApiModel("管理后台 - 支付商户信息 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayMerchantRespVO extends PayMerchantBaseVO {

    @ApiModelProperty(value = "商户编号", required = true)
    private Long id;

    @ApiModelProperty(value = "创建时间", required = true)
    private LocalDateTime createTime;

    @ApiModelProperty(value = "商户号", required = true, example = "M233666999")
    private String no;

}
