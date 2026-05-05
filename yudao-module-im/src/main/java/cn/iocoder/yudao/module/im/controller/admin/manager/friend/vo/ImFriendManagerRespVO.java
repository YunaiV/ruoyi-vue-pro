package cn.iocoder.yudao.module.im.controller.admin.manager.friend.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IM 好友关系 Response VO")
@Data
public class ImFriendManagerRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "好友用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long friendUserId;

    @Schema(description = "好友昵称", example = "李四")
    private String friendNickname;

    @Schema(description = "好友展示备注")
    private String displayName;

    @Schema(description = "添加来源", example = "1")
    private Integer addSource; // 参见 ImFriendAddSourceEnum 枚举类

    @Schema(description = "是否免打扰", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean silent;

    @Schema(description = "是否置顶联系人", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean pinned;

    @Schema(description = "是否拉黑", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean blocked;

    @Schema(description = "好友状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status; // 参见 CommonStatusEnum 枚举类

    @Schema(description = "添加好友时间")
    private LocalDateTime addTime;

    @Schema(description = "删除好友时间")
    private LocalDateTime deleteTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
