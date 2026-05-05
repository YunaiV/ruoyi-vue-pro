package cn.iocoder.yudao.module.im.controller.admin.manager.group.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IM 群聊 Response VO")
@Data
public class ImGroupManagerRespVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "群名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "技术交流群")
    private String name;

    @Schema(description = "群头像")
    private String avatar;

    @Schema(description = "群公告")
    private String notice;

    @Schema(description = "群主用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long ownerUserId;

    @Schema(description = "群主昵称", example = "张三")
    private String ownerNickname;

    @Schema(description = "群成员数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer memberCount;

    @Schema(description = "群状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status; // 参见 CommonStatusEnum 枚举类

    @Schema(description = "解散时间")
    private LocalDateTime dissolvedTime;

    @Schema(description = "是否封禁", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean banned;

    @Schema(description = "是否全群禁言")
    private Boolean mutedAll;

    @Schema(description = "封禁原因")
    private String bannedReason;

    @Schema(description = "封禁时间")
    private LocalDateTime bannedTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
