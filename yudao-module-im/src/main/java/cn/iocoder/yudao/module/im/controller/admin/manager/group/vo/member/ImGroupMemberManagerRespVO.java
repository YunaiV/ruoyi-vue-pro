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

    @Schema(description = "入群时间")
    private LocalDateTime joinTime;

}
