package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 客户转化率分析 VO")
@Data
public class CrmStatisticsCustomerContractSummaryRespVO {

    @Schema(description = "客户名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String customerName;

    @Schema(description = "合同名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "演示合同")
    private String contractName;

    @Schema(description = "合同总金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1200.00")
    private BigDecimal totalPrice;

    @Schema(description = "回款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "1200.00")
    private BigDecimal receivablePrice;

    @Schema(description = "客户行业", requiredMode = Schema.RequiredMode.REQUIRED, example = "金融")
    private String customerType;

    @Schema(description = "客户来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "外呼")
    private String customerSource;

    @Schema(description = "负责人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long ownerUserId;

    @Schema(description = "负责人", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String ownerUserName;

    @Schema(description = "创建人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Long creatorUserId;

    @Schema(description = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, example = "源码")
    private String creatorUserName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-02-01 13:24:26")
    private LocalDateTime createTime;

    @Schema(description = "下单日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-02-02 00:00:00")
    private LocalDate orderDate;

}
