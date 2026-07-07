package cn.iocoder.yudao.module.crm.controller.admin.performance.vo.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - CRM 业绩目标设置 Response VO")
@Data
public class CrmPerformanceConfigRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "目标对象编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long objectId;

    @Schema(description = "目标对象名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "销售部")
    private String objectName;

    @Schema(description = "目标对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer objectType;

    @Schema(description = "年份", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026")
    private Integer year;

    @Schema(description = "一月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal januaryTargetPrice;

    @Schema(description = "二月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal februaryTargetPrice;

    @Schema(description = "三月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal marchTargetPrice;

    @Schema(description = "四月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal aprilTargetPrice;

    @Schema(description = "五月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal mayTargetPrice;

    @Schema(description = "六月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal juneTargetPrice;

    @Schema(description = "七月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal julyTargetPrice;

    @Schema(description = "八月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal augustTargetPrice;

    @Schema(description = "九月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal septemberTargetPrice;

    @Schema(description = "十月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal octoberTargetPrice;

    @Schema(description = "十一月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal novemberTargetPrice;

    @Schema(description = "十二月目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal decemberTargetPrice;

    @Schema(description = "目标类型",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    private Integer bizType;

    @Schema(description = "年度目标金额", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal yearTargetPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
