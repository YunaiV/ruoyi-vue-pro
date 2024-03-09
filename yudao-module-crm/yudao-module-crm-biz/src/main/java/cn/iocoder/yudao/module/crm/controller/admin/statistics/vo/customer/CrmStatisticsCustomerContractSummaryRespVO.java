package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.TIME_ZONE_DEFAULT;

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

    @Schema(description = "客户行业ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @JsonIgnore
    private String industryId;

    @Schema(description = "客户行业", requiredMode = Schema.RequiredMode.REQUIRED, example = "金融")
    private String industryName;

    @Schema(description = "客户来源ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonIgnore
    private String source;

    @Schema(description = "客户来源", requiredMode = Schema.RequiredMode.REQUIRED, example = "外呼")
    private String sourceName;

    @Schema(description = "负责人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @JsonIgnore
    private Long ownerUserId;

    @Schema(description = "负责人", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋道源码")
    private String ownerUserName;

    @Schema(description = "创建人编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @JsonIgnore
    private String creatorUserId;

    @Schema(description = "创建人", requiredMode = Schema.RequiredMode.REQUIRED, example = "源码")
    private String creatorUserName;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-02-01 13:24:26")
    private LocalDateTime createTime;

    @Schema(description = "下单日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-02-02 00:00:00")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY, timezone = TIME_ZONE_DEFAULT)
    private LocalDate orderDate;

}
