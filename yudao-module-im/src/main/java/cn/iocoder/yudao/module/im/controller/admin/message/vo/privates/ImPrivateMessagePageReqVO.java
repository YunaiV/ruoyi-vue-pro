package cn.iocoder.yudao.module.im.controller.admin.message.vo.privates;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 私聊消息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ImPrivateMessagePageReqVO extends PageParam {

    @Schema(description = "接收人编号（对方）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "接收人编号不能为空")
    private Long receiverId;

}
