package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(title = "管理后台 - 流程实例的 Response VO")
@Data
public class BpmProcessInstanceRespVO {

    @Schema(title = "流程实例的编号", required = true, example = "1024")
    private String id;

    @Schema(title = "流程名称", required = true, example = "芋道")
    private String name;

    @Schema(title = "流程分类", required = true, description = "参见 bpm_model_category 数据字典", example = "1")
    private String category;

    @Schema(title = "流程实例的状态", required = true, description = "参见 bpm_process_instance_status", example = "1")
    private Integer status;

    @Schema(title = "流程实例的结果", required = true, description = "参见 bpm_process_instance_result", example = "2")
    private Integer result;

    @Schema(title = "提交时间", required = true)
    private LocalDateTime createTime;

    @Schema(title = "结束时间", required = true)
    private LocalDateTime endTime;

    @Schema(title = "提交的表单值", required = true)
    private Map<String, Object> formVariables;

    @Schema(title = "业务的唯一标识", example = "1", description = "例如说，请假申请的编号")
    private String businessKey;

    /**
     * 发起流程的用户
     */
    private User startUser;

    /**
     * 流程定义
     */
    private ProcessDefinition processDefinition;

    @Schema(title = "用户信息")
    @Data
    public static class User {

        @Schema(title = "用户编号", required = true, example = "1")
        private Long id;
        @Schema(title = "用户昵称", required = true, example = "芋艿")
        private String nickname;

        @Schema(title = "部门编号", required = true, example = "1")
        private Long deptId;
        @Schema(title = "部门名称", required = true, example = "研发部")
        private String deptName;

    }

    @Schema(title = "流程定义信息")
    @Data
    public static class ProcessDefinition {

        @Schema(title = "编号", required = true, example = "1024")
        private String id;

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

        @Schema(title = "BPMN XML", required = true)
        private String bpmnXml;

    }

}
