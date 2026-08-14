package cn.iocoder.yudao.module.fms.controller.admin.config.vo.financeindicator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

// TODO DONE @AI：字段 Swagger 注解和空行已补齐。
@Schema(description = "管理后台 - FMS 财务指标 Response VO")
@Data
public class FmsFinanceIndicatorRespVO {

    @Schema(description = "指标编号", example = "1024")
    private Long id;

    @Schema(description = "账套编号", example = "1")
    private Long accountSetId;

    @Schema(description = "指标名称", example = "现金")
    private String name;

    @Schema(description = "指标编码", example = "cash")
    private String code;

    @Schema(description = "取数报表类型", example = "1")
    private Integer type;

    @Schema(description = "指标公式")
    private String formula;

    @Schema(description = "展示顺序", example = "10")
    private Integer sort;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
