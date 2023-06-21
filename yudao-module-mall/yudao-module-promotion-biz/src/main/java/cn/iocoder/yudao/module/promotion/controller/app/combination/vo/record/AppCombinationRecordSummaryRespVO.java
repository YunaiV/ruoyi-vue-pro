package cn.iocoder.yudao.module.promotion.controller.app.combination.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 拼团记录的简要概括 Response VO")
@Data
public class AppCombinationRecordSummaryRespVO {

    @Schema(description = "拼团用户数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer userCount;

    @Schema(description = "拼团用户头像列表", requiredMode = Schema.RequiredMode.REQUIRED) // 只返回最近的 7 个
    private List<String> avatars;

}
