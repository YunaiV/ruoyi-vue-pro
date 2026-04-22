package cn.iocoder.yudao.module.im.controller.admin.group.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 群成员 Response VO")
@Data
public class ImGroupMemberRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17071")
    private Long id;

    @Schema(description = "群编号", example = "13279")
    private Long groupId;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21730")
    private Long userId;

    @Schema(description = "组内显示名", example = "芋艿")
    private String displayUserName;

    @Schema(description = "群显示备注", example = "核心群")
    private String displayGroupName;

    @Schema(description = "是否免打扰")
    private Boolean muted;

    @Schema(description = "成员状态", example = "0")
    private Integer status;

    @Schema(description = "入群时间")
    private LocalDateTime joinTime;

    @Schema(description = "退群时间")
    private LocalDateTime quitTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    // ========== 聚合字段：用户侧接口按需填充（admin CRUD 查询不填） ==========

    @Schema(description = "用户昵称（聚合自 AdminUser，用户侧接口返回）", example = "芋道")
    private String nickname;

    @Schema(description = "用户头像（聚合自 AdminUser，用户侧接口返回）")
    private String avatar;

    @Schema(description = "展示昵称：优先 displayUserName，回落 nickname", example = "芋道")
    private String showNickname;

}