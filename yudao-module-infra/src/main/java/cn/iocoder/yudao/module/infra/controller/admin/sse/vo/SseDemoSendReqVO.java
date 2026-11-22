package cn.iocoder.yudao.module.infra.controller.admin.sse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - SSE 演示消息发送 Request VO")
@Data
public class SseDemoSendReqVO {

    @Schema(description = "接收人编号，为空时发送给所有管理员", example = "1")
    private Long toUserId;

    @Schema(description = "消息内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "你好，SSE")
    @NotEmpty(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容不能超过 500 个字符")
    private String text;

}
