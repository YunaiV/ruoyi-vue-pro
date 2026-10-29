package cn.iocoder.yudao.module.iot.controller.admin.product.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 管理后台 - IoT 产品数据解析脚本调试 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 产品数据解析脚本调试 Response VO")
@Data
public class IotProductScriptDebugRespVO {

    @Schema(description = "是否成功", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean success;

    @Schema(description = "执行耗时（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long durationMs;

    @Schema(description = "decode：解析出的消息；为空表示没有业务消息（只记录原始消息）")
    private Map<String, Object> message;

    @Schema(description = "encode：是否走标准 JSON 下发", example = "false")
    private Boolean standard;

    @Schema(description = "encode：下行字节的 HEX", example = "0103000A0001")
    private String outputHex;

    @Schema(description = "失败信息")
    private ErrorInfo error;

    @Schema(description = "调试失败信息")
    @Data
    public static class ErrorInfo {

        @Schema(description = "类型：ERROR、TIMEOUT、REJECTED、OUTPUT_INVALID、ENCODE_EMPTY", example = "ERROR")
        private String type;

        @Schema(description = "错误信息")
        private String message;

        @Schema(description = "出错行号", example = "3")
        private Integer line;

    }

}
