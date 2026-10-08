package cn.iocoder.yudao.module.ai1.controller.admin.mcp.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - AI1 MCP Response VO")
@Data
public class Ai1McpRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "天气查询")
    private String name;

    @Schema(description = "传输方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "http")
    private String transport;

    @Schema(description = "服务地址", example = "http://127.0.0.1:8081/mcp")
    private String url;

    @Schema(description = "请求头", example = "{\"Authorization\":\"Bearer xxx\"}")
    private Map<String, String> headers;

    @Schema(description = "完整 MCP 配置，JSON 对象", example = "{\"transport\":\"http\",\"url\":\"http://127.0.0.1:8081/mcp\"}")
    private String config;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "备注", example = "查询城市天气")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
