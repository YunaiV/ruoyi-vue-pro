package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import cn.iocoder.yudao.module.bpm.controller.admin.base.user.UserSimpleBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 流程实例的打印数据 Response VO")
@Data
public class BpmProcessPrintDataRespVO {

    @Schema(description = "流程实例数据")
    private BpmProcessInstanceRespVO processInstance;

    @Schema(description = "是否开启自定义打印模板")
    private Boolean printTemplateEnable;

    @Schema(description = "自定义打印模板HTML")
    private String printTemplateHtml;

    @Schema(description = "审批任务列表")
    private List<ApproveTask> tasks;

    @Data
    public static class ApproveTask {

        private String id;

        private String name;

        private String description;

        private String signUrl;

    }

}
