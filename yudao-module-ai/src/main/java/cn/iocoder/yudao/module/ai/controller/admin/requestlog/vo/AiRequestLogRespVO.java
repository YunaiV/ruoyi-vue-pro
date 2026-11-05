package cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - AI 请求日志 Response VO")
@Data
public class AiRequestLogRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "请求编号（本地 UUID）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "0b4f6c0e-7d3c-4f0e-9a3c-1f2a3b4c5d6e")
    private String requestId;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋艿")
    private String userNickname;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer bizType;

    @Schema(description = "业务编号", example = "2048")
    private Long bizId;

    @Schema(description = "平台", example = "TongYi")
    private String platform;

    @Schema(description = "模型编号", example = "10")
    private Long modelId;

    @Schema(description = "模型标识", example = "qwen-plus")
    private String model;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "错误信息", example = "请求超时")
    private String errorMessage;

    @Schema(description = "开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime beginTime;

    @Schema(description = "结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @Schema(description = "耗时（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "1500")
    private Long duration;

    @Schema(description = "输入 Token，未知为空", example = "120")
    private Long inputTokens;

    @Schema(description = "输出 Token，未知为空", example = "80")
    private Long outputTokens;

    @Schema(description = "合计 Token，未知为空", example = "200")
    private Long totalTokens;

    @Schema(description = "响应元数据快照")
    private Map<String, Object> responseMetadata;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
