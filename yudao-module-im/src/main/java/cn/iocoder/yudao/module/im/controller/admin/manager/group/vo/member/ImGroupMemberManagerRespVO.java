package cn.iocoder.yudao.module.im.controller.admin.manager.group.vo.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IM 群成员 Response VO")
@Data
public class ImGroupMemberManagerRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "组内显示名", example = "三哥")
    private String displayUserName;

    @Schema(description = "群备注", example = "技术交流群")
    private String groupRemark;

    @Schema(description = "是否免打扰", example = "false")
    private Boolean silent;

    @Schema(description = "成员状态", example = "0")
    private Integer status; // 参见 CommonStatusEnum 枚举类

    @Schema(description = "成员角色；1=群主 2=管理员 3=普通成员", example = "3")
    private Integer role; // 参见 ImGroupMemberRoleEnum 枚举类

    @Schema(description = "入群时间")
    private LocalDateTime joinTime;

    @Schema(description = "退群时间")
    private LocalDateTime quitTime;

    @Schema(description = "禁言到期时间")
    private LocalDateTime muteEndTime;

}
