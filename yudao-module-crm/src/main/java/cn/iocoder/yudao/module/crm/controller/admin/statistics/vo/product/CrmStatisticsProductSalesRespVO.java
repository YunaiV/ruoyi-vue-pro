package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 产品销售情况 Response VO")
@Data
public class CrmStatisticsProductSalesRespVO {

    @Schema(description = "产品分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long categoryId;

    @Schema(description = "产品分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "软件")
    private String categoryName;

    @Schema(description = "产品编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Long productId;

    @Schema(description = "产品名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "CRM 专业版")
    private String productName;

    @Schema(description = "合同编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Long contractId;

    @Schema(description = "合同编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "HT20240101001")
    private String contractNo;

    @Schema(description = "合同名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "CRM 采购合同")
    private String contractName;

    @Schema(description = "负责人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long ownerUserId;

    @Schema(description = "负责人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String ownerUserName;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "40")
    private Long customerId;

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试客户")
    private String customerName;

    @Schema(description = "产品单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    private BigDecimal productPrice;

    @Schema(description = "销售数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private BigDecimal productCount;

    @Schema(description = "销售金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "200.00")
    private BigDecimal productTotalPrice;

}
