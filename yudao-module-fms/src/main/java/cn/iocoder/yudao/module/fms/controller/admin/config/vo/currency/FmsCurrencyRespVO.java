package cn.iocoder.yudao.module.fms.controller.admin.config.vo.currency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FMS 币别 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 币别 Response VO")
@Data
public class FmsCurrencyRespVO {

    @Schema(description = "币别编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long accountSetId;

    @Schema(description = "币别编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "USD")
    private String code;

    @Schema(description = "币别名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "美元")
    private String name;

    @Schema(description = "汇率", requiredMode = Schema.RequiredMode.REQUIRED, example = "7.120000")
    private BigDecimal exchangeRate;

    @Schema(description = "是否本位币", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean standard;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
