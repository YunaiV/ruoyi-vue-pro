package cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// TODO @haohao：后续需要使用下
@Schema(description = "管理后台 - IoT 插件实例 Response VO")
@Data
public class PluginInstanceRespVO {

    @Schema(description = "主键编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23864")
    private Long id;

    @Schema(description = "插件主程序id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23738")
    private String mainId;

    @Schema(description = "插件id", requiredMode = Schema.RequiredMode.REQUIRED, example = "26498")
    private Long pluginId;

    @Schema(description = "插件主程序所在ip", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ip;

    @Schema(description = "插件主程序端口", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer port;

    @Schema(description = "心跳时间，心路时间超过30秒需要剔除", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long heartbeatAt;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}