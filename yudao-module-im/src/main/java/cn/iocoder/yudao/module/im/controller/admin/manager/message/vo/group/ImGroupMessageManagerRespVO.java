package cn.iocoder.yudao.module.im.controller.admin.manager.message.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - IM 群聊消息 Response VO")
@Data
public class ImGroupMessageManagerRespVO {

    @Schema(description = "消息编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "客户端消息编号", example = "c-uuid-xxx")
    private String clientMessageId;

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long groupId;

    @Schema(description = "群名称", example = "技术交流群")
    private String groupName;

    @Schema(description = "发送人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long senderId;

    @Schema(description = "发送人昵称", example = "张三")
    private String senderNickname;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type; // 参见 ImContentTypeEnum 枚举类

    @Schema(description = "消息内容（JSON 格式）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "消息状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status; // 参见 ImMessageStatusEnum 枚举类

    @Schema(description = "@ 目标用户编号列表（-1 表示 @所有人）")
    private List<Long> atUserIds;
    @Schema(description = "@ 目标用户昵称列表（-1 位置为 null，前端根据 atUserIds 自行展示「@所有人」）")
    private List<String> atUserNicknames;

    @Schema(description = "回执状态", example = "0")
    private Integer receiptStatus; // 参见 ImMessageReceiptStatusEnum 枚举类

    @Schema(description = "发送时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime sendTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
