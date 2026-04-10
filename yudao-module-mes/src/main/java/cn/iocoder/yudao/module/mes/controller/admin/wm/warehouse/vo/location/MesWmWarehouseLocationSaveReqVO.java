package cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.location;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 库区新增/修改 Request VO")
@Data
public class MesWmWarehouseLocationSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "库区编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "L001")
    @NotEmpty(message = "库区编码不能为空")
    private String code;

    @Schema(description = "库区名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "原料区")
    @NotEmpty(message = "库区名称不能为空")
    private String name;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @Schema(description = "面积", example = "500.00")
    private BigDecimal area;

    @Schema(description = "库位管理状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "库位管理状态不能为空")
    private Integer areaStatus;

    @Schema(description = "是否冻结", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否冻结不能为空")
    private Boolean frozen;

    @Schema(description = "备注", example = "备注")
    private String remark;

    }
