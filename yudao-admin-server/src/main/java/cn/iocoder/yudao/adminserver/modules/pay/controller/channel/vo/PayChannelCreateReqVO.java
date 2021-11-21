package cn.iocoder.yudao.adminserver.modules.pay.controller.channel.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@ApiModel("支付渠道 创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayChannelCreateReqVO extends PayChannelBaseVO {


    @ApiModelProperty(value = "通道配置的json字符串")
    @NotBlank(message = "通道配置不能为空")
    private String config;


}
