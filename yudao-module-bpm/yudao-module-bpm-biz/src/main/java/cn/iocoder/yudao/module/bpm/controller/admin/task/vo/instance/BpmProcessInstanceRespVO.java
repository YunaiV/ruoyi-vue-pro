package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@ApiModel("管理后台 - 流程实例的 Response VO")
@Data
public class BpmProcessInstanceRespVO {

    @ApiModelProperty(value = "流程实例的编号", required = true, example = "1024")
    private String id;

    @ApiModelProperty(value = "流程名称", required = true, example = "芋道")
    private String name;

    @ApiModelProperty(value = "流程分类", required = true, notes = "参见 bpm_model_category 数据字典", example = "1")
    private String category;

    @ApiModelProperty(value = "流程实例的状态", required = true, notes = "参见 bpm_process_instance_status", example = "1")
    private Integer status;

    @ApiModelProperty(value = "流程实例的结果", required = true, notes = "参见 bpm_process_instance_result", example = "2")
    private Integer result;

    @ApiModelProperty(value = "提交时间", required = true)
    private Date createTime;

    @ApiModelProperty(value = "结束时间", required = true)
    private Date endTime;

    @ApiModelProperty(value = "提交的表单值", required = true)
    private Map<String, Object> formVariables;

    @ApiModelProperty(value = "业务的唯一标识", example = "1", notes = "例如说，请假申请的编号")
    private String businessKey;

    /**
     * 发起流程的用户
     */
    private User startUser;

    /**
     * 流程定义
     */
    private ProcessDefinition processDefinition;

    @ApiModel("用户信息")
    @Data
    public static class User {

        @ApiModelProperty(value = "用户编号", required = true, example = "1")
        private Long id;
        @ApiModelProperty(value = "用户昵称", required = true, example = "芋艿")
        private String nickname;

        @ApiModelProperty(value = "部门编号", required = true, example = "1")
        private Long deptId;
        @ApiModelProperty(value = "部门名称", required = true, example = "研发部")
        private String deptName;

    }

    @ApiModel("流程定义信息")
    @Data
    public static class ProcessDefinition {

        @ApiModelProperty(value = "编号", required = true, example = "1024")
        private String id;

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

        @ApiModelProperty(value = "BPMN XML", required = true)
        private String bpmnXml;

    }

}
