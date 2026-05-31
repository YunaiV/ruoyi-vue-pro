package cn.iocoder.yudao.module.im.controller.admin.friend.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * IM 好友申请 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IM 好友申请 Response VO")
@Data
public class ImFriendRequestRespVO {

    @Schema(description = "申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "发起方用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long fromUserId;

    @Schema(description = "接收方用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Long toUserId;

    @Schema(description = "处理结果", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer handleResult; // 参见 ImFriendRequestHandleResultEnum 枚举

    @Schema(description = "申请理由", example = "我是芋艿（一种食材）")
    private String applyContent;

    @Schema(description = "处理理由（接收方拒绝时可选填）", example = "暂不通过")
    private String handleContent;

    @Schema(description = "添加来源", example = "1")
    private Integer addSource; // 参见 ImFriendAddSourceEnum 枚举

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "申请创建时间")
    private LocalDateTime createTime;

    // ========== 下面是聚合字段，方便前端显示 ==========

    @Schema(description = "发起方昵称（实时聚合自 AdminUser）", example = "芋道")
    private String fromNickname;

    @Schema(description = "发起方头像（实时聚合自 AdminUser）")
    private String fromAvatar;

    @Schema(description = "接收方昵称（实时聚合自 AdminUser）", example = "老张")
    private String toNickname;

    @Schema(description = "接收方头像（实时聚合自 AdminUser）")
    private String toAvatar;

}
