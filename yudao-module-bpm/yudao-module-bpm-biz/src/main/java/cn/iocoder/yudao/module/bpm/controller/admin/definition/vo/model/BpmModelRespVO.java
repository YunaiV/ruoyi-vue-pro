package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model;

import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process.BpmProcessDefinitionRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 流程模型 Response VO")
@Data
public class BpmModelRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String id;

    @Schema(description = "流程标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "process_yudao")
    private String key;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String name;

    @Schema(description = "流程图标", example = "https://www.iocoder.cn/yudao.jpg")
    private String icon;

    @Schema(description = "流程描述", example = "我是描述")
    private String description;

    @Schema(description = "流程分类编码", example = "1")
    private String category;
    @Schema(description = "流程分类名字", example = "请假")
    private String categoryName;

    @Schema(description = "表单类型-参见 bpm_model_form_type 数据字典", example = "1")
    private Integer formType;

    @Schema(description = "表单编号", example = "1024")
    private Long formId; // 在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空
    @Schema(description = "表单名字", example = "请假表单")
    private String formName;

    @Schema(description = "自定义表单的提交路径", example = "/bpm/oa/leave/create")
    private String formCustomCreatePath; // 使用 Vue 的路由地址-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空
    @Schema(description = "自定义表单的查看路径", example = "/bpm/oa/leave/view")
    private String formCustomViewPath; // ，使用 Vue 的路由地址-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "BPMN XML", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bpmnXml;

    /**
     * 最新部署的流程定义
     */
    private BpmProcessDefinitionRespVO processDefinition;

}
