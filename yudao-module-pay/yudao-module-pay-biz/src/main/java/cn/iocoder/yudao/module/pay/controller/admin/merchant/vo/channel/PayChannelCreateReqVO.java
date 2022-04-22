package cn.iocoder.yudao.module.pay.controller.admin.merchant.vo.channel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@ApiModel("管理后台 - 支付渠道 创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelCreateReqVO extends PayChannelBaseVO {

    @ApiModelProperty(value = "渠道配置的 json 字符串")
    @NotBlank(message = "渠道配置不能为空")
    private String config;

}
