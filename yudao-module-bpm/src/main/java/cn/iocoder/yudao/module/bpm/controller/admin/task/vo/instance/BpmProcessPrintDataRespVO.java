package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import cn.iocoder.yudao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

// TODO @lesan：这个可能复用 BpmApprovalDetailRespVO 哇？@芋艿：暂时先这样吧，BpmApprovalDetailRespVO 太大了。。。
@Schema(description = "管理后台 - 流程实例的打印数据 Response VO")
@Data
public class BpmProcessPrintDataRespVO {

    private Boolean printTemplateEnable;

    private Integer processStatus;

    private String processInstanceId;

    private String processBusinessKey;

    private String processName;

    private UserSimpleBaseVO startUser;

    private String startTime;

    private String endTime;

    private List<ApproveNode> approveNodes;

    private List<String> formFields;

    private String printTemplateHtml;

    private Map<String, Object> processVariables;

    @Data
    public static class ApproveNode {

        private String nodeName;

        private String nodeDesc;

        private String signUrl;

        private String nodeId;

    }

}
