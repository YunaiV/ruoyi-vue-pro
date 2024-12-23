package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.process;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 流程定义 Response VO")
@Data
public class BpmProcessDefinitionRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String id;

    @Schema(description = "版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer version;

    @Schema(description = "流程名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
    private String name;

    @Schema(description = "流程标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "yudao")
    private String key;

    @Schema(description = "流程图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://www.iocoder.cn/yudao.jpg")
    private String icon;

    @Schema(description = "流程描述", example = "我是描述")
    private String description;

    @Schema(description = "流程分类", example = "1")
    private String category;
    @Schema(description = "流程分类名字", example = "请假")
    private String categoryName;

    @Schema(description = "流程模型的类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer modelType; // 参见 BpmModelTypeEnum 枚举类

    @Schema(description = "表单类型-参见 bpm_model_form_type 数据字典", example = "1")
    private Integer formType;
    @Schema(description = "表单编号-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空", example = "1024")
    private Long formId;
    @Schema(description = "表单名字", example = "请假表单")
    private String formName;
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
    private Integer suspensionState; // 参见 SuspensionState 枚举

    @Schema(description = "部署时间")
    private LocalDateTime deploymentTime; // 需要从对应的 Deployment 读取，非必须返回

    @Schema(description = "BPMN XML")
    private String bpmnXml; // 需要从对应的 BpmnModel 读取，非必须返回

    @Schema(description = "SIMPLE 设计器模型数据 json 格式")
    private String simpleModel; // 非必须返回

    @Schema(description = "流程定义排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long sort;
    
    @Schema(description = "BPMN UserTask 用户任务")
    @Data
    public static class UserTask {

        @Schema(description = "任务标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "sudo")
        private String id;

        @Schema(description = "任务名", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
        private String name;

    }

}
