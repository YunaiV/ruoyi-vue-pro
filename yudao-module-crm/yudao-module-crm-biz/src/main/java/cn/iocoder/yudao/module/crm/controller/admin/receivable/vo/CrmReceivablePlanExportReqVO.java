package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// TODO liuhongfeng：导出可以等其它功能做完，统一在搞；
@Schema(description = "管理后台 - CRM 回款计划 Excel 导出 Request VO，参数和 CrmReceivablePlanPageReqVO 是一致的")
@Data
public class CrmReceivablePlanExportReqVO {

    @Schema(description = "期数")
    private Integer period;

    @Schema(description = "完成状态", example = "2")
    private Integer status;

    @Schema(description = "审批状态", example = "1")
    private Integer checkStatus;

    @Schema(description = "计划回款日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] returnTime;

    @Schema(description = "提前几天提醒")
    private Integer remindDays;

    @Schema(description = "提醒日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] remindTime;

    @Schema(description = "客户名称", example = "18026")
    private Long customerId;

    @Schema(description = "合同名称", example = "3473")
    private Long contractId;

    @Schema(description = "负责人", example = "17828")
    private Long ownerUserId;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
