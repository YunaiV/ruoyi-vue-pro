package cn.iocoder.yudao.module.iot.controller.admin.plugininstance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - IoT 插件实例新增/修改 Request VO")
@Data
public class PluginInstanceSaveReqVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23864")
    private Long id;

    @Schema(description = "插件主程序id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23738")
    @NotEmpty(message = "插件主程序id不能为空")
    private String mainId;

    @Schema(description = "插件id", requiredMode = Schema.RequiredMode.REQUIRED, example = "26498")
    @NotNull(message = "插件id不能为空")
    private Long pluginId;

    @Schema(description = "插件主程序所在ip", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "插件主程序所在ip不能为空")
    private String ip;

    @Schema(description = "插件主程序端口", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "插件主程序端口不能为空")
    private Integer port;

    @Schema(description = "心跳时间，心路时间超过30秒需要剔除", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "心跳时间，心路时间超过30秒需要剔除不能为空")
    private Long heartbeatAt;

}