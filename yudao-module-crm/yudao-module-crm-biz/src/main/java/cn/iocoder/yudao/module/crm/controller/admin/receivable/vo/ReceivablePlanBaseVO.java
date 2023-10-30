package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 回款计划 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ReceivablePlanBaseVO {

    // TODO 芋艿：这个字段，在想想命名；
    @Schema(description = "期数")
    private Integer period;

    // TODO @liuhongfeng：中英文之间，有个空格，这样更干净；
    @Schema(description = "回款ID", example = "19852")
    private Long receivableId;

    @Schema(description = "完成状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    //@NotNull(message = "完成状态不能为空")
    private Integer status;

    // TODO @liuhongfeng：这个字段，可以写个枚举，然后 InEnum 去校验下；
    // TODO @liuhongfeng：这个字段，应该不是前端传递的噢，而是后端自己生成的
    @Schema(description = "审批状态", example = "1")
    private Integer checkStatus;

    // TODO @liuhongfeng：这个字段，应该不是前端传递的噢，而是后端自己生成的，所以不适合放在 base 里面；
    @Schema(description = "工作流编号", example = "8909")
    private Long processInstanceId;

    // TODO @liuhongfeng：使用 Int 哈，分；
    @Schema(description = "计划回款金额", example = "29675")
    private BigDecimal price;

    @Schema(description = "计划回款日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime returnTime;

    // TODO @liuhongfeng：这个字段，Integer
    @Schema(description = "提前几天提醒")
    private Long remindDays;

    @Schema(description = "提醒日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime remindTime;

    @Schema(description = "客户ID", example = "18026")
    private Long customerId;

    @Schema(description = "合同ID", example = "3473")
    private Long contractId;

    // TODO @liuhongfeng：这个字段，应该不是前端传递的噢，而是后端自己生成的，所以不适合放在 base 里面；
    @Schema(description = "负责人", example = "17828")
    private Long ownerUserId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
