package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 回款管理 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class ReceivableBaseVO {

    // TODO @liuhongfeng：部分缺少 example 的字段，要补充下；
    // TODO @liuhongfeng：部分字段，需要必传，要写 requiredMode = Schema.RequiredMode.REQUIRED，以及对应的 validator 非空校验

    @Schema(description = "回款编号")
    private String no;

    // TODO @liuhongfeng：中英文之间，有个空格，这样更干净；
    @Schema(description = "回款计划ID", example = "31177")
    private Long planId;

    @Schema(description = "客户ID", example = "4963")
    private Long customerId;

    @Schema(description = "合同ID", example = "30305")
    private Long contractId;

    // TODO @liuhongfeng：这个字段，可以写个枚举，然后 InEnum 去校验下；
    // TODO @liuhongfeng：这个字段，应该不是前端传递的噢，而是后端自己生成的
    @Schema(description = "审批状态", example = "1")
    private Integer checkStatus;

    // TODO @liuhongfeng：这个字段，应该不是前端传递的噢，而是后端自己生成的，所以不适合放在 base 里面；
    @Schema(description = "工作流编号", example = "16568")
    private Long processInstanceId;

    @Schema(description = "回款日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime returnTime;

    @Schema(description = "回款方式", example = "2")
    private String returnType;

    // TODO @liuhongfeng：使用 Int 哈，分；
    @Schema(description = "回款金额", example = "31859")
    private BigDecimal price;

    @Schema(description = "负责人", example = "22202")
    private Long ownerUserId;

    @Schema(description = "批次", example = "2539")
    private Long batchId;

    @Schema(description = "显示顺序")
    private Integer sort;

    // TODO @芋艿：这个字段在看看；dataScope、dataScopeDeptIds
    @Schema(description = "数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    private Integer dataScope;

    @Schema(description = "数据范围(指定部门数组)")
    private String dataScopeDeptIds;

    // TODO @liuhongfeng：这个字段，这个字段，应该不是前端传递的噢，而是后端自己生成的，所以不适合放在 base 里面；
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
