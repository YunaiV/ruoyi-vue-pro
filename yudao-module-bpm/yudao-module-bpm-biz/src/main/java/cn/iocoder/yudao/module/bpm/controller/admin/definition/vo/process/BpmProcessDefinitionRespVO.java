package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "管理后台 - 流程定义 Response VO")
@Data
public class BpmProcessDefinitionRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String id;

    @Schema(description = "版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer version;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    @NotEmpty(message = "流程名称不能为空")
    private String name;

    @Schema(description = "流程描述", example = "我是描述")
    private String description;

    @Schema(description = "流程分类-参见 bpm_model_category 数据字典", example = "1")
    @NotEmpty(message = "流程分类不能为空")
    private String category;

    @Schema(description = "表单类型-参见 bpm_model_form_type 数据字典", example = "1")
    private Integer formType;
    @Schema(description = "表单编号-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空", example = "1024")
    private Long formId;
    @Schema(description = "表单的配置-JSON 字符串。在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空", requiredMode = Schema.RequiredMode.REQUIRED)
    private String formConf;
    @Schema(description = "表单项的数组-JSON 字符串的数组。在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> formFields;
    @Schema(description = "自定义表单的提交路径，使用 Vue 的路由地址-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空",
            example = "/bpm/oa/leave/create")
    private String formCustomCreatePath;
    @Schema(description = "自定义表单的查看路径，使用 Vue 的路由地址-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空",
            example = "/bpm/oa/leave/view")
    private String formCustomViewPath;

    @Schema(description = "中断状态-参见 SuspensionState 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer suspensionState;

}
