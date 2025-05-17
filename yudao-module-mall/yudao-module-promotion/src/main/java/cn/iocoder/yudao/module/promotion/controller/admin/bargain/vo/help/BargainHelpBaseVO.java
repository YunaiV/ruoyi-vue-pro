package cn.iocoder.yudao.module.promotion.controller.admin.bargain.vo.help;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 砍价助力 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class BargainHelpBaseVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "5402")
    private Long userId;

    @Schema(description = "砍价活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "16825")
    private Long activityId;

    @Schema(description = "砍价记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1800")
    private Long recordId;

    @Schema(description = "减少砍价，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "32300")
    private Integer reducePrice;

}
