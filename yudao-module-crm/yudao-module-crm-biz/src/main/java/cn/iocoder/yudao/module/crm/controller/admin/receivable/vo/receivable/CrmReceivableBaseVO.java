package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.receivable;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.common.CrmAuditStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 回款 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmReceivableBaseVO {

    @Schema(description = "回款编号",requiredMode = Schema.RequiredMode.REQUIRED, example = "31177")
    private String no;

    // TODO @liuhongfeng：回款计划编号
    @Schema(description = "回款计划", example = "31177")
    private Long planId;

    // TODO @liuhongfeng：客户编号
    @Schema(description = "客户名称", example = "4963")
    private Long customerId;

    // TODO @liuhongfeng：客户编号
    @Schema(description = "合同名称", example = "30305")
    private Long contractId;

    // TODO @liuhongfeng：这个字段，应该不是前端传递的噢，而是后端自己生成的
    @Schema(description = "审批状态", example = "1")
    @InEnum(CrmAuditStatusEnum.class)
    private Integer checkStatus;

    @Schema(description = "回款日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime returnTime;

    @Schema(description = "回款方式", example = "2")
    private Integer returnType;

    @Schema(description = "回款金额，单位：分", example = "31859")
    private Integer price;

    // TODO @liuhongfeng：负责人编号
    @Schema(description = "负责人", example = "22202")
    private Long ownerUserId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "备注", example = "备注")
    private String remark;


}
