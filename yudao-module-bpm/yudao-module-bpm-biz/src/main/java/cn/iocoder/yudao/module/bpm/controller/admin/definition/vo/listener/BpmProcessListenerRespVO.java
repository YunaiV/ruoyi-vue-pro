package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.listener;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - BPM 流程监听器 Response VO")
@Data
public class BpmProcessListenerRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13089")
    private Long id;

    @Schema(description = "监听器名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String name;

    @Schema(description = "监听器类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "execution")
    private String type;

    @Schema(description = "监听器状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "监听事件", requiredMode = Schema.RequiredMode.REQUIRED, example = "start")
    private String event;

    @Schema(description = "监听器值类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "class")
    private String valueType;

    @Schema(description = "监听器值", requiredMode = Schema.RequiredMode.REQUIRED)
    private String value;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}