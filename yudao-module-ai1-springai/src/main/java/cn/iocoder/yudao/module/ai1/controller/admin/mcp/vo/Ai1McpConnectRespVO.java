package cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI1 MCP 连通测试 Response VO")
@Data
public class Ai1McpConnectRespVO {

    @Schema(description = "是否连通", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean connectable;

    @Schema(description = "服务名称", example = "weather-server")
    private String serverName;

    @Schema(description = "服务版本", example = "1.0.0")
    private String serverVersion;

    @Schema(description = "服务说明", example = "提供城市天气查询")
    private String instructions;

    @Schema(description = "可用工具数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer toolCount;

    @Schema(description = "测试耗时，单位：毫秒", requiredMode = Schema.RequiredMode.REQUIRED, example = "320")
    private Long elapsedMs;

    @Schema(description = "测试过程描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "连接成功，发现 2 个工具")
    private String message;

    @Schema(description = "可用工具明细")
    private List<Tool> tools;

    @Schema(description = "MCP 工具")
    @Data
    public static class Tool {

        @Schema(description = "工具名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "get_weather")
        private String name;

        @Schema(description = "工具标题", example = "天气查询")
        private String title;

        @Schema(description = "工具描述", example = "查询指定城市的天气")
        private String description;

    }

}
