package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import javax.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 回款管理 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ReceivableBaseVO {

    @Schema(description = "回款编号")
    private String no;

    @Schema(description = "回款计划ID", example = "31177")
    private Long planId;

    @Schema(description = "客户ID", example = "4963")
    private Long customerId;

    @Schema(description = "合同ID", example = "30305")
    private Long contractId;

    @Schema(description = "审批状态", example = "1")
    private Integer checkStatus;

    @Schema(description = "工作流编号", example = "16568")
    private Long processInstanceId;

    @Schema(description = "回款日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime returnTime;

    @Schema(description = "回款方式", example = "2")
    private String returnType;

    @Schema(description = "回款金额", example = "31859")
    private BigDecimal price;

    @Schema(description = "负责人", example = "22202")
    private Long ownerUserId;

    @Schema(description = "批次", example = "2539")
    private Long batchId;

    @Schema(description = "显示顺序")
    private Integer sort;

    @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    private Integer dataScope;

    @Schema(description = "数据范围(指定部门数组)")
    private String dataScopeDeptIds;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
