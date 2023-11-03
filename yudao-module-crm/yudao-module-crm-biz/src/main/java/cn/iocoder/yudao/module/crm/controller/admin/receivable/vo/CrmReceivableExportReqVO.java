package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author 赤焰
 */
@Schema(description = "管理后台 - CRM 回款 Excel 导出 Request VO，参数和 CrmReceivablePageReqVO 是一致的")
@Data
public class CrmReceivableExportReqVO {

    @Schema(description = "回款编号")
    private String no;

    @Schema(description = "回款计划", example = "31177")
    private Long planId;

    @Schema(description = "客户名称", example = "4963")
    private Long customerId;

    @Schema(description = "合同名称", example = "30305")
    private Long contractId;

    @Schema(description = "审批状态", example = "1")
    private Integer checkStatus;

    @Schema(description = "回款日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] returnTime;

    @Schema(description = "回款方式", example = "2")
    private String returnType;

    @Schema(description = "回款金额", example = "31859")
    private Integer price;

    @Schema(description = "负责人", example = "22202")
    private Long ownerUserId;

    @Schema(description = "批次", example = "2539")
    private Long batchId;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
