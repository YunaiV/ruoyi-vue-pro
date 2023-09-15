package cn.iocoder.yudao.module.member.controller.app.level.vo.experience;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 会员经验记录 Response VO")
@Data
public class AppMemberExperienceRecordRespVO {

    @Schema(description = "标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "增加经验")
    private String title;

    @Schema(description = "经验", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer experience;

    @Schema(description = "描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "下单增加 100 经验")
    private String description;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
