package cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 仓库新增/修改 Request VO")
@Data
public class MesWmWarehouseSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WH001")
    @NotEmpty(message = "仓库编码不能为空")
    private String code;

    @Schema(description = "仓库名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "原料仓")
    @NotEmpty(message = "仓库名称不能为空")
    private String name;

    @Schema(description = "仓库地址", example = "A 区一号楼")
    private String address;

    @Schema(description = "面积", example = "1200.50")
    private BigDecimal area;

    @Schema(description = "负责人用户编号", example = "1")
    private Long chargeUserId;

    @Schema(description = "是否冻结", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @NotNull(message = "是否冻结不能为空")
    private Boolean frozen;

    @Schema(description = "备注", example = "备注")
    private String remark;

    }
