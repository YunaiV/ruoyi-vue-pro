package cn.iocoder.yudao.module.ai1.controller.admin.session.vo.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - AI1 对话 Response VO")
@Data
public class Ai1SessionRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "Agent 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long agentId;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long userId;

    @Schema(description = "用户昵称", example = "芋道源码")
    private String userNickname;

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "三体舰队的航速")
    private String title;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间，即最近活跃时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}
