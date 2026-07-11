package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 产品分类销售分析 Response VO")
@Data
public class CrmStatisticsProductCategoryRespVO {

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long categoryId;

    @Schema(description = "产品分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "软件")
    private String categoryName;

    @Schema(description = "合同数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Integer contractCount;

    @Schema(description = "销售数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private BigDecimal productCount;

    @Schema(description = "销售金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "20000.00")
    private BigDecimal productTotalPrice;

}
