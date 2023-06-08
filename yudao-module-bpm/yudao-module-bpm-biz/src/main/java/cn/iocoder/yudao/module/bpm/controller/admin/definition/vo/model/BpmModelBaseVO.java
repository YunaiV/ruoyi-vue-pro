package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
* 流程模型 Base VO，提供给添加、修改、详细的子 VO 使用
* 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
*/
@Data
public class BpmModelBaseVO {

    @Schema(description = "流程标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "process_yudao")
    @NotEmpty(message = "流程标识不能为空")
    private String key;

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
    @Schema(description = "自定义表单的提交路径，使用 Vue 的路由地址-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空",
            example = "/bpm/oa/leave/create")
    private String formCustomCreatePath;
    @Schema(description = "自定义表单的查看路径，使用 Vue 的路由地址-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空",
            example = "/bpm/oa/leave/view")
    private String formCustomViewPath;

}
