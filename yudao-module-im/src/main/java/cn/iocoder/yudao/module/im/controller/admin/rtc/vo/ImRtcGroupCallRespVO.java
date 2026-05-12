package cn.iocoder.yudao.module.im.controller.admin.rtc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Schema(description = "管理后台 - 群活跃通话 Response VO；不含 token，胶囊条仅展示用")
@Data
public class ImRtcGroupCallRespVO {

    @Schema(description = "业务通话编号")
    private String room;

    @Schema(description = "群编号")
    private Long groupId;

    @Schema(description = "媒体类型")
    private Integer mediaType; // 参见 ImCallMediaTypeEnum 枚举类

    @Schema(description = "发起人编号")
    private Long inviterId;

    @Schema(description = "已加入房间的用户编号集合")
    private Set<Long> joinedUserIds;

    @Schema(description = "被邀请池；用于胶囊条展开时显示待加入头像")
    private Set<Long> inviteeIds;

}
