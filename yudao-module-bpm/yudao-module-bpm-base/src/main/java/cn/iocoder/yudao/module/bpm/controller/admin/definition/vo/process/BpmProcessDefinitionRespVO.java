package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel("管理后台 - 流程定义 Response VO")
@Data
public class BpmProcessDefinitionRespVO {

    @ApiModelProperty(value = "编号", required = true, example = "1024")
    private String id;

    @ApiModelProperty(value = "版本", required = true, example = "1")
    private Integer version;

    @ApiModelProperty(value = "流程名称", required = true, example = "芋道")
    @NotEmpty(message = "流程名称不能为空")
    private String name;

    @ApiModelProperty(value = "流程描述", example = "我是描述")
    private String description;

    @ApiModelProperty(value = "流程分类", notes = "参见 bpm_model_category 数据字典", example = "1")
    @NotEmpty(message = "流程分类不能为空")
    private String category;

    @ApiModelProperty(value = "表单类型", notes = "参见 bpm_model_form_type 数据字典", example = "1")
    private Integer formType;
    @ApiModelProperty(value = "表单编号", example = "1024", notes = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private Long formId;
    @ApiModelProperty(value = "表单的配置", required = true,
            notes = "JSON 字符串。在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private String formConf;
    @ApiModelProperty(value = "表单项的数组", required = true,
            notes = "JSON 字符串的数组。在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private List<String> formFields;
    @ApiModelProperty(value = "自定义表单的提交路径，使用 Vue 的路由地址", example = "/bpm/oa/leave/create",
            notes = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private String formCustomCreatePath;
    @ApiModelProperty(value = "自定义表单的查看路径，使用 Vue 的路由地址", example = "/bpm/oa/leave/view",
            notes = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private String formCustomViewPath;

    @ApiModelProperty(value = "中断状态", required = true, example = "1", notes = "参见 SuspensionState 枚举")
    private Integer suspensionState;

}
