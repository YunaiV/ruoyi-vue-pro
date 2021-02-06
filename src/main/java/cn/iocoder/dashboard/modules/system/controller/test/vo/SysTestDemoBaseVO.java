package cn.iocoder.dashboard.modules.system.controller.test.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
* 字典类型 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class SysTestDemoBaseVO {

    @ApiModelProperty(value = "字典名称", required = true, example = "性别额")
    @NotNull(message = "字典名称不能为空")
    private String name;

    @ApiModelProperty(value = "字典类型", required = true, example = "sys_sex")
    @NotNull(message = "字典类型不能为空")
    private String dictType;

    @ApiModelProperty(value = "状态", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty(value = "备注", example = "我是备注")
    private String remark;

}
