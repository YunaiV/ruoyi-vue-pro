package cn.iocoder.yudao.module.system.controller.admin.dict.vo.type;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 字典类型 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class DictTypeBaseVO {

    @ApiModelProperty(value = "字典名称", required = true, example = "性别")
    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典类型名称长度不能超过100个字符")
    private String name;

    @ApiModelProperty(value = "状态", required = true, example = "1", notes = "参见 CommonStatusEnum 枚举类")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "备注", example = "快乐的备注")
    private String remark;

}
