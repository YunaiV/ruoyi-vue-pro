package cn.iocoder.yudao.module.fms.controller.admin.config.vo.currency;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * FMS 币别保存 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - FMS 币别保存 Request VO")
@Data
public class FmsCurrencySaveReqVO {

    @Schema(description = "币别编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "账套不能为空")
    private Long accountSetId;

    @Schema(description = "币别编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "USD")
    @NotBlank(message = "币别编码不能为空")
    @Size(max = 64, message = "币别编码长度不能超过 64 个字符")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]*$", message = "币别编码必须以字母开头，只能包含字母、数字和下划线")
    private String code;

    @Schema(description = "币别名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "美元")
    @NotBlank(message = "币别名称不能为空")
    @Size(max = 255, message = "币别名称长度不能超过 255 个字符")
    private String name;

    @Schema(description = "汇率", requiredMode = Schema.RequiredMode.REQUIRED, example = "7.120000")
    @NotNull(message = "汇率不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "汇率必须大于 0")
    @Digits(integer = 12, fraction = 6, message = "汇率最多 12 位整数和 6 位小数")
    private BigDecimal exchangeRate;

}
