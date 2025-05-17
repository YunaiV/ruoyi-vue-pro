package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.help;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Schema(description = "用户 App - 砍价助力的创建 Request VO")
@Data
public class AppBargainHelpCreateReqVO {

    @Schema(description = "砍价记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "砍价记录编号不能为空")
    private Long recordId;

}
