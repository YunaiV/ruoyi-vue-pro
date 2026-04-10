package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 流程实例的打印数据 Response VO")
@Data
public class BpmProcessPrintDataRespVO {

    @Schema(description = "流程实例数据")
    private BpmProcessInstanceRespVO processInstance;

    @Schema(description = "是否开启自定义打印模板", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean printTemplateEnable;

    @Schema(description = "自定义打印模板 HTML")
    private String printTemplateHtml;

    @Schema(description = "审批任务列表")
    private List<Task> tasks;

    @Schema(description = "流程任务")
    @Data
    public static class Task {

        @Schema(description = "流程任务的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private String id;

        @Schema(description = "任务名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道")
        private String name;

        @Schema(description = "签名 URL", example = "https://www.iocoder.cn/sign.png")
        private String signPicUrl;

        @Schema(description = "任务描述", requiredMode = Schema.RequiredMode.REQUIRED)
        private String description; // 该字段由后端拼接

    }

}
