package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 回款计划 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmReceivablePlanBaseVO {

    @Schema(description = "期数", example = "1")
    private Integer period;

    @Schema(description = "回款计划编号", example = "19852")
    private Long receivableId;

    @Schema(description = "计划回款金额", example = "29675")
    private Integer price;

    @Schema(description = "计划回款日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime returnTime;

    @Schema(description = "提前几天提醒")
    private Integer remindDays;

    @Schema(description = "提醒日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime remindTime;

    @Schema(description = "客户名称", example = "18026")
    private Long customerId;

    @Schema(description = "合同编号", example = "3473")
    private Long contractId;

    // TODO @liuhongfeng：负责人编号
    @Schema(description = "负责人编号", example = "17828")
    private Long ownerUserId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "备注", example = "备注")
    private String remark;

}
