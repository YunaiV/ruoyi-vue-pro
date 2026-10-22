package cn.iocoder.yudao.module.ai1.controller.admin.memory.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 长期记忆 Response VO")
@Data
public class Ai1MemoryRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Agent 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long agentId;

    @Schema(description = "Agent 名称", example = "客服助手")
    private String agentName;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道源码")
    private String userNickname;

    @Schema(description = "记忆正文", requiredMode = Schema.RequiredMode.REQUIRED, example = "## 用户画像\n- 后端工程师")
    private String content;

    @Schema(description = "已归档的最后一条消息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long lastMessageId;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
