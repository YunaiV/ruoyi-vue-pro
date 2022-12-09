package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Schema(title = "管理后台 - 流程模型的更新 Request VO")
@Data
public class BpmModelUpdateReqVO {

    @Schema(title = "编号", required = true, example = "1024")
    @NotEmpty(message = "编号不能为空")
    private String id;

    @Schema(title = "流程名称", example = "芋道")
    private String name;

    @Schema(title = "流程描述", example = "我是描述")
    private String description;

    @Schema(title = "流程分类", description = "参见 bpm_model_category 数据字典", example = "1")
    private String category;

    @Schema(title = "BPMN XML", required = true)
    private String bpmnXml;

    @Schema(title = "表单类型", description = "参见 bpm_model_form_type 数据字典", example = "1")
    private Integer formType;
    @Schema(title = "表单编号", example = "1024", description = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private Long formId;
    @Schema(title = "自定义表单的提交路径，使用 Vue 的路由地址", example = "/bpm/oa/leave/create",
            description = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private String formCustomCreatePath;
    @Schema(title = "自定义表单的查看路径，使用 Vue 的路由地址", example = "/bpm/oa/leave/view",
            description = "在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空")
    private String formCustomViewPath;

}
