package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 规则场景（场景联动） Response VO")
@Data
public class IotRuleSceneRespVO {

    @Schema(description = "场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15865")
    private Long id;

    @Schema(description = "场景名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    private String name;

    @Schema(description = "场景描述", example = "你猜")
    private String description;

    @Schema(description = "场景状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "触发器数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private String triggers;

    @Schema(description = "执行器数组", requiredMode = Schema.RequiredMode.REQUIRED)
    private String actions;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}