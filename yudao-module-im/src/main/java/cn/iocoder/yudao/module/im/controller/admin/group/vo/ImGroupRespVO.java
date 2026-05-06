package cn.iocoder.yudao.module.im.controller.admin.group.vo;

import cn.iocoder.yudao.module.im.controller.admin.message.vo.group.ImGroupMessageRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @Schema(description = "是否全群禁言")
    private Boolean mutedAll;

    @Schema(description = "加群方式", example = "0")
    private Integer joinType; // 参见 ImGroupJoinTypeEnum 枚举类

    // TODO @AI：是不是不回填；前端自己查询 + 推送维护？类似微信，会展示有人加群，需要审批之类的。（晚点我可以给截图）；
    @Schema(description = "未处理加群申请数；仅当登录用户是该群群主 / 管理员时回填，否则为 null", example = "3")
    private Long pendingRequestCount;

    @Schema(description = "封禁时间")
    private LocalDateTime bannedTime;

    @Schema(description = "群状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer status;

    @Schema(description = "解散时间")
    private LocalDateTime dissolvedTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "群置顶消息列表，按 pin 顺序（最先置顶的在前）；非该群有效成员时为空")
    private List<ImGroupMessageRespVO> pinnedMessages;

}