package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Schema(title = "管理后台 - 流程定义 Response VO")
@Data
public class BpmProcessDefinitionRespVO {

    @Schema(title = "编号", required = true, example = "1024")
    private String id;

    @Schema(title = "版本", required = true, example = "1")
    private Integer version;

    @Schema(title = "流程名称", required = true, example = "芋道")
    @NotEmpty(message = "流程名称不能为空")
    private String name;

    @Schema(title = "流程描述", example = "我是描述")
    private String description;

    @Schema(title = "流程分类", description = "参见 bpm_model_category 数据字典", example = "1")
    @NotEmpty(message = "流程分类不能为空")
    private String category;

    @Schema(title = "表单类型", description = "参见 bpm_model_form_type 数据字典", example = "1")
    private Integer formType;
    @Schema(title = "表单编号", example = "1024", description = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private Long formId;
    @Schema(title = "表单的配置", required = true,
            description = "JSON 字符串。在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private String formConf;
    @Schema(title = "表单项的数组", required = true,
            description = "JSON 字符串的数组。在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private List<String> formFields;
    @Schema(title = "自定义表单的提交路径，使用 Vue 的路由地址", example = "/bpm/oa/leave/create",
            description = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private String formCustomCreatePath;
    @Schema(title = "自定义表单的查看路径，使用 Vue 的路由地址", example = "/bpm/oa/leave/view",
            description = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private String formCustomViewPath;

    @Schema(title = "中断状态", required = true, example = "1", description = "参见 SuspensionState 枚举")
    private Integer suspensionState;

}
