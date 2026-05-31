package cn.iocoder.yudao.module.im.controller.admin.message.vo.group;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 群聊历史消息列表 Request VO")
@Data
public class ImGroupMessageListReqVO {

    @Schema(description = "群编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "群编号不能为空")
    private Long groupId;

    @Schema(description = "起始消息编号，从该 id 往前拉取（不含）。为空则从最新消息开始", example = "1024")
    private Long maxId;

    @Schema(description = "拉取数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "拉取数量不能为空")
    @Min(value = 1, message = "拉取数量最小值为 1")
    @Max(value = 200, message = "拉取数量最大值为 200")
    private Integer limit;

}
