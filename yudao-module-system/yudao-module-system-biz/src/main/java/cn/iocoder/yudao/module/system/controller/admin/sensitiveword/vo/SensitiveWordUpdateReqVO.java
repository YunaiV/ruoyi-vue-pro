package cn.iocoder.yudao.module.system.controller.admin.sensitiveword.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("管理后台 - 敏感词更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SensitiveWordUpdateReqVO extends SensitiveWordBaseVO {

    @ApiModelProperty(value = "编号", required = true, example = "1")
    @NotNull(message = "编号不能为空")
    private Long id;

}
