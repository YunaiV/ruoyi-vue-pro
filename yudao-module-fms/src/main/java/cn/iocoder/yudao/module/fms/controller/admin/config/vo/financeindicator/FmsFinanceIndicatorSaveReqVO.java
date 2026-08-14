package cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeindicator;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.fms.enums.config.FmsFinanceIndicatorTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// TODO DONE @AI：字段空行已对齐现有 SaveReqVO。
@Schema(description = "管理后台 - FMS 财务指标保存 Request VO")
@Data
public class FmsFinanceIndicatorSaveReqVO {

    @Schema(description = "指标编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "账套编号不能为空")
    private Long accountSetId;

    @Schema(description = "指标名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "现金")
    @NotBlank(message = "指标名称不能为空")
    @Size(max = 100, message = "指标名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "指标编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "cash")
    @NotBlank(message = "指标编码不能为空")
    @Size(max = 64, message = "指标编码长度不能超过 64 个字符")
    private String code;

    @Schema(description = "取数报表类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "取数报表类型不能为空")
    @InEnum(FmsFinanceIndicatorTypeEnum.class)
    private Integer type;

    @Schema(description = "指标公式，支持 L1+L2 或科目公式 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "指标公式不能为空")
    @Size(max = 2000, message = "指标公式长度不能超过 2000 个字符")
    private String formula;

    @Schema(description = "展示顺序", example = "10")
    @NotNull(message = "展示顺序不能为空")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
