package cn.iocoder.yudao.module.member.controller.app.point.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 用户积分记录 Response VO")
@Data
public class AppMemberPointRecordRespVO {

    @Schema(description = "自增主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31457")
    private Long id;;

    @Schema(description = "积分标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "你猜")
    private String title;

    @Schema(description = "积分描述", example = "你猜")
    private String description;

    @Schema(description = "积分", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer point;

    @Schema(description = "发生时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
