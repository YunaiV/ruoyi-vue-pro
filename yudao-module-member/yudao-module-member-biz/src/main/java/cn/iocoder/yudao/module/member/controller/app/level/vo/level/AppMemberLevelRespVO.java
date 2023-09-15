package cn.iocoder.yudao.module.member.controller.app.level.vo.level;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 会员等级 Response VO")
@Data
public class AppMemberLevelRespVO {

    @Schema(description = "等级名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String name;

    @Schema(description = "等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer level;

    @Schema(description = "升级经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer experience;

    @Schema(description = "享受折扣", requiredMode = Schema.RequiredMode.REQUIRED, example = "98")
    private Integer discountPercent;

    @Schema(description = "等级图标", example = "https://www.iocoder.cn/yudao.jpg")
    private String icon;

    @Schema(description = "等级背景图", example = "https://www.iocoder.cn/yudao.jpg")
    private String backgroundUrl;

}
