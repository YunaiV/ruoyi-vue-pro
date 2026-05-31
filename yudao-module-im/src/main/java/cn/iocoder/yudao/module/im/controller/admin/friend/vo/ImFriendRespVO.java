package cn.iocoder.yudao.module.im.controller.admin.friend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IM 好友 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IM 好友 Response VO")
@Data
public class ImFriendRespVO {

    @Schema(description = "关系记录编号", example = "1024")
    private Long id;

    @Schema(description = "好友的用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long friendUserId;

    @Schema(description = "是否免打扰", example = "false")
    private Boolean silent;

    @Schema(description = "好友展示备注（仅自己可见）", example = "老张")
    private String displayName;

    @Schema(description = "好友展示备注的拼音（小写无空格）", example = "laozhang")
    private String displayNamePinyin;

    @Schema(description = "添加来源", example = "1")
    private Integer addSource; // 参见 ImFriendAddSourceEnum 枚举

    @Schema(description = "是否置顶联系人", example = "false")
    private Boolean pinned;

    @Schema(description = "是否拉黑（仅自己可见）", example = "false")
    private Boolean blocked;

    @Schema(description = "好友状态", example = "0")
    private Integer status;

    @Schema(description = "添加好友时间")
    private LocalDateTime addTime;

    @Schema(description = "删除好友时间")
    private LocalDateTime deleteTime;

    // ========== 下面是聚合字段，方便前端显示 ==========

    @Schema(description = "好友昵称（实时聚合自 AdminUser）", example = "芋道")
    private String nickname;

    @Schema(description = "好友昵称的拼音（小写无空格）", example = "yudao")
    private String nicknamePinyin;

    @Schema(description = "好友头像（实时聚合自 AdminUser）")
    private String avatar;

}
