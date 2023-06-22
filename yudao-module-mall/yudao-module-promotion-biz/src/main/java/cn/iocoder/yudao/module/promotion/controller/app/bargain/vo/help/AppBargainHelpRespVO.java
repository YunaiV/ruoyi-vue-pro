package cn.iocoder.yudao.module.promotion.controller.app.bargain.vo.help;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 砍价助力 Response VO")
@Data
public class AppBargainHelpRespVO {

    @Schema(description = "助力用户的昵称", required = true, example = "1024")
    private String nickname;

    @Schema(description = "助力用户的头像", required = true, example = "1024")
    private String avatar;

    @Schema(description = "助力用户的砍价金额", required = true, example = "1024")
    private Integer reducePrice;

    @Schema(description = "创建时间", required = true, example = "1024")
    private LocalDateTime createTime;

}
