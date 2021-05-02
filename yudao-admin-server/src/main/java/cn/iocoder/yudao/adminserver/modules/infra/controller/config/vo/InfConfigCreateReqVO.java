package cn.iocoder.yudao.adminserver.modules.infra.controller.config.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@ApiModel("参数配置创建 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class InfConfigCreateReqVO extends InfConfigBaseVO {

    @ApiModelProperty(value = "参数键名", required = true, example = "yunai.db.username")
    @NotBlank(message = "参数键名长度不能为空")
    @Size(max = 100, message = "参数键名长度不能超过100个字符")
    private String key;

}
