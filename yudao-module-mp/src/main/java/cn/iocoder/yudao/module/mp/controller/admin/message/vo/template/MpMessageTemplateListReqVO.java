package cn.iocoder.yudao.module.mp.controller.admin.message.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "管理后台 - 公众号模版消息列表 Request VO")
@Data
public class MpMessageTemplateListReqVO {

    @Schema(description = "公众号账号的编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "公众号账号的编号不能为空")
    private Long accountId;

}
