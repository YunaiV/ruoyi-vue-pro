package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.listener;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - BPM 流程监听器新增/修改 Request VO")
@Data
public class BpmProcessListenerSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13089")
    private Long id;

    @Schema(description = "监听器名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "监听器名字不能为空")
    private String name;

    @Schema(description = "监听器类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "execution")
    @NotEmpty(message = "监听器类型不能为空")
    private String type;

    @Schema(description = "监听器状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "监听器状态不能为空")
    private Integer status;

    @Schema(description = "监听事件", requiredMode = Schema.RequiredMode.REQUIRED, example = "start")
    @NotEmpty(message = "监听事件不能为空")
    private String event;

    @Schema(description = "监听器值类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "class")
    @NotEmpty(message = "监听器值类型不能为空")
    private String valueType;

    @Schema(description = "监听器值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "监听器值不能为空")
    private String value;

}