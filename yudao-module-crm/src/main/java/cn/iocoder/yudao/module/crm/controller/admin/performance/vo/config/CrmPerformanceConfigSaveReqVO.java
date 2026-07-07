package cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.performance.CrmPerformanceConfigObjectTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - CRM 业绩目标设置新增/修改 Request VO")
@Data
public class CrmPerformanceConfigSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "目标对象编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标对象编号不能为空")
    private Long objectId;

    @Schema(description = "目标对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "目标对象类型不能为空")
    @InEnum(value = CrmPerformanceConfigObjectTypeEnum.class, message = "目标对象类型，必须是 {value}")
    private Integer objectType;

    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    @NotNull(message = "年份不能为空")
    private Integer year;

    @Schema(description = "一月目标金额")
    private BigDecimal januaryTargetPrice;

    @Schema(description = "二月目标金额")
    private BigDecimal februaryTargetPrice;

    @Schema(description = "三月目标金额")
    private BigDecimal marchTargetPrice;

    @Schema(description = "四月目标金额")
    private BigDecimal aprilTargetPrice;

    @Schema(description = "五月目标金额")
    private BigDecimal mayTargetPrice;

    @Schema(description = "六月目标金额")
    private BigDecimal juneTargetPrice;

    @Schema(description = "七月目标金额")
    private BigDecimal julyTargetPrice;

    @Schema(description = "八月目标金额")
    private BigDecimal augustTargetPrice;

    @Schema(description = "九月目标金额")
    private BigDecimal septemberTargetPrice;

    @Schema(description = "十月目标金额")
    private BigDecimal octoberTargetPrice;

    @Schema(description = "十一月目标金额")
    private BigDecimal novemberTargetPrice;

    @Schema(description = "十二月目标金额")
    private BigDecimal decemberTargetPrice;

    @Schema(description = "目标类型",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "目标类型不能为空")
    @InEnum(value = CrmPerformanceConfigBizTypeEnum.class, message = "目标类型，必须是 {value}")
    private Integer bizType;

}
