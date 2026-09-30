package cn.iocoder.yudao.module.ai1.controller.admin.session.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 对话消息 Response VO")
@Data
public class Ai1MessageRespVO {

    @Schema(description = "编号；助手消息的编号同时作为续传的结果流标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sessionId;

    @Schema(description = "角色", requiredMode = Schema.RequiredMode.REQUIRED, example = "assistant")
    private String role;

    @Schema(description = "思考过程", example = "用户想了解……")
    private String reasoning;

    @Schema(description = "内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "四光年")
    private String content;

    @Schema(description = "生成状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
