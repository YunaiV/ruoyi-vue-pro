package cn.iocoder.yudao.module.im.controller.admin.manager.rtc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IM 通话参与者 Response VO")
@Data
public class ImRtcParticipantManagerRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "通话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long callId;

    @Schema(description = "参与者用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "参与者昵称", example = "张三")
    private String userNickname;

    @Schema(description = "参与角色", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer role; // 参见 ImRtcParticipantRoleEnum 枚举类

    @Schema(description = "参与状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status; // 参见 ImRtcParticipantStatusEnum 枚举类

    @Schema(description = "被邀请时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime inviteTime;

    @Schema(description = "接听时间；未接听为空")
    private LocalDateTime acceptTime;

    @Schema(description = "离开时间；未加入为空")
    private LocalDateTime leaveTime;

}
