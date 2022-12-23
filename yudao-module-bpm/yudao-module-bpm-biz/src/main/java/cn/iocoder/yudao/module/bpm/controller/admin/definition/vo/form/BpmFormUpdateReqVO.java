package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;

import lombok.*;
import io.swagger.annotations.*;
import javax.validation.constraints.*;
import java.util.List;

@ApiModel("管理后台 - 动态表单更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BpmFormUpdateReqVO extends BpmFormBaseVO {

    @ApiModelProperty(value = "表单编号", required = true, example = "1024")
    @NotNull(message = "表单编号不能为空")
    private Long id;

    @ApiModelProperty(value = "表单的配置", required = true, notes = "JSON 字符串")
    @NotNull(message = "表单的配置不能为空")
    private String conf;

    @ApiModelProperty(value = "表单项的数组", required = true, notes = "JSON 字符串的数组")
    @NotNull(message = "表单项的数组不能为空")
    private List<String> fields;

}
