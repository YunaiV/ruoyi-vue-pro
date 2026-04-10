package cn.iocoder.yudao.module.mes.controller.admin.tm.tool.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 工具台账新增/修改 Request VO")
@Data
public class MesTmToolSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "工具编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "T-001")
    @NotEmpty(message = "工具编码不能为空")
    private String code;

    @Schema(description = "工具名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "5mm 铣刀")
    @NotEmpty(message = "工具名称不能为空")
    private String name;

    @Schema(description = "品牌", example = "三菱")
    private String brand;

    @Schema(description = "型号规格", example = "M5-100")
    private String spec;

    @Schema(description = "工具类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "工具类型不能为空")
    private Long toolTypeId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @Schema(description = "可用数量", example = "1")
    private Integer availableQuantity;

    @Schema(description = "保养维护类型", example = "1")
    private Integer maintenType;

    @Schema(description = "下次保养周期（次数）", example = "100")
    private Integer nextMaintenPeriod;

    @Schema(description = "下次保养日期", example = "2024-06-01 00:00:00")
    private LocalDateTime nextMaintenDate;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    }
