package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

// TODO @lesan：这个可能复用 BpmApprovalDetailRespVO 哇？
@Schema(description = "管理后台 - 流程实例的打印数据 Response VO")
@Data
public class BpmProcessPrintDataRespVO {

    private Boolean printTemplateEnable;

    private Integer processStatus;

    // TODO @lesan：通过字典？
    private String processStatusShow;

    private String processInstanceId;

    private String processBusinessKey;

    private String processName;

    // TODO @lesan：UserSimpleBaseVO 替代 startUser、startUserDept；
    private String startUser;

    private String startUserDept;

    private String startTime;

    private List<ApproveNode> approveNodes;

    private List<FormField> formFields;

    private String printTemplateHtml;

    @Data
    public static class ApproveNode {

        private String nodeName;

        private String nodeDesc;

        private String signUrl;

        private String nodeId;

    }

    @Data
    public static class FormField {

        private String formId;

        private String formName;

        private String formType;

        private String formValue;

        private String formValueShow;

    }

}
