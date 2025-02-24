package cn.iocoder.yudao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

// TODO @jason：要不去掉这个接口，单体通过 asyncHttpTriggerCallback？
@Schema(description = "管理后台 - Bpm 异步 Http 触发器请求回调 Request VO")
@Data
public class BpmHttpTriggerCallbackReqVO {

    @Schema(description = "流程实例的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "dca1cdcc-b8fe-11ef-99b5-00ff4722db8b")
    @NotEmpty(message = "流程实例的编号不能为空")
    private String id;

    @Schema(description = "回调编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "dca1cdcc-b8fe-11ef-99b5-01ff4722db8b")
    @NotEmpty(message = "回调编号不能为空")
    private String callbackId;

}
