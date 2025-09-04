package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import cn.iocoder.yudao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 流程实例的打印数据 Response VO")
@Data
public class BpmProcessPrintDataRespVO {

    private Boolean printTemplateEnable;

    // TODO @lesan：要不 processStatus、processInstanceId、processBusinessKey、processBusinessKey、startUser、endTime、processVariables 使用 BpmProcessInstanceRespVO ？虽然这个 VO 大了点，但是收一收字段。嘿嘿；进而只有 processInstance、tasks、formFields、printTemplateHtml 这些字段；
    private Integer processStatus;

    private String processInstanceId;

    private String processBusinessKey;

    private String processName;

    private UserSimpleBaseVO startUser;

    private String startTime;

    private String endTime;

    // TODO @lesan：变量要不改成 tasks；
    private List<ApproveNode> approveNodes;

    private List<String> formFields;

    private String printTemplateHtml;

    private Map<String, Object> processVariables;

    // TODO @lesan：类名要不要改成 tasks ？然后 id、name、signUrl、description；感觉理解成本低点；
    @Data
    public static class ApproveNode {

        private String nodeName;

        private String nodeDesc;

        private String signUrl;

        private String nodeId;

    }

}
