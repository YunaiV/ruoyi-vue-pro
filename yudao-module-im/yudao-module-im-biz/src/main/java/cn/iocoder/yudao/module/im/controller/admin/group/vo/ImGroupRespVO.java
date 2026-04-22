package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 群 Response VO")
@Data
public class ImGroupRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1003")
    private Long id;

    @Schema(description = "群名称", example = "芋艿")
    private String name;

    @Schema(description = "群主用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31460")
    private Long ownerUserId;

    @Schema(description = "群头像")
    private String avatar;

    @Schema(description = "群公告")
    private String notice;

    @Schema(description = "是否封禁")
    private Boolean banned;

    @Schema(description = "封禁时间")
    private LocalDateTime bannedTime;

    @Schema(description = "是否解散")
    private Boolean dissolved;

    @Schema(description = "解散时间")
    private LocalDateTime dissolvedTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}