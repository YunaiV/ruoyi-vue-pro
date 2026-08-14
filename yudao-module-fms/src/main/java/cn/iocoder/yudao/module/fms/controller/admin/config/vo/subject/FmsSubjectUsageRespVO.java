package cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * FMS 科目使用情况 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 科目使用情况 Response VO")
@Data
public class FmsSubjectUsageRespVO {

    @Schema(description = "下级科目数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long childCount;

    @Schema(description = "凭证分录数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long voucherEntryCount;

    @Schema(description = "初始余额数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long initialBalanceCount;

    @Schema(description = "辅助核算组合数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long auxiliaryCombinationCount;

    @Schema(description = "包含数量数据的记录数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long quantityDataCount;

    @Schema(description = "是否已被业务使用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean used;

}
