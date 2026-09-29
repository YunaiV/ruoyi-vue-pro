package cn.iocoder.yudao.module.ai1.controller.admin.chat.vo.conversation;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - AI1 对话分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Ai1ChatConversationPageReqVO extends PageParam {

    @Schema(description = "Agent 编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "Agent 编号不能为空")
    private Long agentId;

    @Schema(description = "标题，模糊匹配", example = "三体")
    private String title;

    @Schema(description = "用户编号", example = "1")
    private Long userId;

}
