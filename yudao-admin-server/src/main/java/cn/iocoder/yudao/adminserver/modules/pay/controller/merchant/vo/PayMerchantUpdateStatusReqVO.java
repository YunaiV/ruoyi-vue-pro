package cn.iocoder.yudao.adminserver.modules.pay.controller.merchant.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel("商户更新状态 Request VO")
@Data
public class PayMerchantUpdateStatusReqVO {

    @ApiModelProperty(value = "商户编号", required = true, example = "1024")
    @NotNull(message = "商户编号不能为空")
    private Long id;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "见 SysCommonStatusEnum 枚举")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
