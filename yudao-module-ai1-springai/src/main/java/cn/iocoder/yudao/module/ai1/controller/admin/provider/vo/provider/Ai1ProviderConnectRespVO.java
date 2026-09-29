package cn.iocoder.yudao.module.ai1.controller.admin.provider.vo.provider;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI1 供应商连通测试 Response VO")
@Data
public class Ai1ProviderConnectRespVO {

    @Schema(description = "是否连通", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean connectable;

    @Schema(description = "最近一次请求的 HTTP 状态码，0 表示网络异常未收到响应", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Integer httpCode;

    @Schema(description = "测试耗时，单位：毫秒", requiredMode = Schema.RequiredMode.REQUIRED, example = "320")
    private Long elapsedMs;

    @Schema(description = "测试过程描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "连通正常：GET /models 返回 HTTP 200")
    private String message;

}
