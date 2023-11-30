package cn.iocoder.yudao.module.crm.controller.admin.business.vo.business;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 商机 Base VO，提供给添加、修改、详细的子 VO 使用
 * 如果子 VO 存在差异的字段，请不要添加到这里，影响 Swagger 文档生成
 */
@Data
public class CrmBusinessBaseVO {

    @Schema(description = "商机名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @NotNull(message = "商机名称不能为空")
    private String name;

    @Schema(description = "商机状态类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25714")
    @NotNull(message = "商机状态类型不能为空")
    private Long statusTypeId;

    @Schema(description = "商机状态编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30320")
    @NotNull(message = "商机状态不能为空")
    private Long statusId;

    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime contactNextTime;

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10299")
    @NotNull(message = "客户不能为空")
    private Long customerId;

    @Schema(description = "预计成交日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime dealTime;

    @Schema(description = "商机金额", example = "12371")
    private Integer price;

    // TODO @ljileo：折扣使用 Integer 类型，存储时，默认 * 100；展示的时候，前端需要 / 100；避免精度丢失问题
    @Schema(description = "整单折扣")
    private Integer discountPercent;

    @Schema(description = "产品总金额", example = "12025")
    private BigDecimal productPrice;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
