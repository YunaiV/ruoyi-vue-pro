package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 合同 Excel 导出 Request VO，参数和 ContractPageReqVO 是一致的")
@Data
public class ContractExportReqVO {

    @Schema(description = "合同名称", example = "王五")
    private String name;

    @Schema(description = "客户编号", example = "18336")
    private Long customerId;

    @Schema(description = "商机编号", example = "10864")
    private Long businessId;

    @Schema(description = "下单日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] orderDate;

    @Schema(description = "合同编号")
    private String no;

    @Schema(description = "整单折扣")
    private Integer discountPercent;

    @Schema(description = "产品总金额", example = "19510")
    private Integer productPrice;

}
