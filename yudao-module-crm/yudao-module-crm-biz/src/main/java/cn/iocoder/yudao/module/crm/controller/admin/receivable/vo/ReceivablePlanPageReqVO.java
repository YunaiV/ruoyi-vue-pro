package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 回款计划分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReceivablePlanPageReqVO extends PageParam {

    // TODO 芋艿：筛选字段，需要去掉几个，在想想；

    @Schema(description = "期数")
    private Long indexNo;

    @Schema(description = "完成状态", example = "2")
    private Integer status;

    @Schema(description = "审批状态", example = "1")
    private Integer checkStatus;

    @Schema(description = "计划回款日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] returnTime;

    @Schema(description = "提前几天提醒")
    private Long remindDays;

    @Schema(description = "提醒日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] remindTime;

    @Schema(description = "客户ID", example = "18026")
    private Long customerId;

    @Schema(description = "合同ID", example = "3473")
    private Long contractId;

    @Schema(description = "负责人", example = "17828")
    private Long ownerUserId;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
