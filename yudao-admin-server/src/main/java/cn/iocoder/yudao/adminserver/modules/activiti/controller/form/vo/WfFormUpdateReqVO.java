package cn.iocoder.yudao.adminserver.modules.activiti.controller.form.vo;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;

@ApiModel("动态表单更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WfFormUpdateReqVO extends WfFormBaseVO {

    @ApiModelProperty(value = "表单编号", required = true)
    @NotNull(message = "表单编号不能为空")
    private Long id;

}
