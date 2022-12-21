package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 流程实例的 Response VO")
@Data
public class BpmProcessInstanceRespVO {

    @Schema(description = "流程实例的编号", required = true, example = "1024")
    private String id;

    @Schema(description = "流程名称", required = true, example = "芋道")
    private String name;

    @Schema(description = "流程分类-参见 bpm_model_category 数据字典", required = true, example = "1")
    private String category;

    @Schema(description = "流程实例的状态-参见 bpm_process_instance_status", required = true, example = "1")
    private Integer status;

    @Schema(description = "流程实例的结果-参见 bpm_process_instance_result", required = true, example = "2")
    private Integer result;

    @Schema(description = "提交时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "结束时间", required = true)
    private LocalDateTime endTime;

    @Schema(description = "提交的表单值", required = true)
    private Map<String, Object> formVariables;

    @Schema(description = "业务的唯一标识-例如说，请假申请的编号", example = "1")
    private String businessKey;

    /**
     * 发起流程的用户
     */
    private User startUser;

    /**
     * 流程定义
     */
    private ProcessDefinition processDefinition;

    @Schema(description = "用户信息")
    @Data
    public static class User {

        @Schema(description = "用户编号", required = true, example = "1")
        private Long id;
        @Schema(description = "用户昵称", required = true, example = "芋艿")
        private String nickname;

        @Schema(description = "部门编号", required = true, example = "1")
        private Long deptId;
        @Schema(description = "部门名称", required = true, example = "研发部")
        private String deptName;

    }

    @Schema(description = "流程定义信息")
    @Data
    public static class ProcessDefinition {

        @Schema(description = "编号", required = true, example = "1024")
        private String id;

        @Schema(description = "表单类型-参见 bpm_model_form_type 数据字典", example = "1")
        private Integer formType;
        @Schema(description = "表单编号-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空", example = "1024")
        private Long formId;
        @Schema(description = "表单的配置-JSON 字符串。在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空", required = true)
        private String formConf;
        @Schema(description = "表单项的数组-JSON 字符串的数组。在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空", required = true)
        private List<String> formFields;
        @Schema(description = "自定义表单的提交路径，使用 Vue 的路由地址-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空",
                example = "/bpm/oa/leave/create")
        private String formCustomCreatePath;
        @Schema(description = "自定义表单的查看路径，使用 Vue 的路由地址-在表单类型为 {@link BpmModelFormTypeEnum#CUSTOM} 时，必须非空",
                example = "/bpm/oa/leave/view")
        private String formCustomViewPath;

        @Schema(description = "BPMN XML", required = true)
        private String bpmnXml;

    }

}
