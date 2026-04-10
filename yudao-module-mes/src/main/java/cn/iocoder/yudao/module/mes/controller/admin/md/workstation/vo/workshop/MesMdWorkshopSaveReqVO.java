package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - MES 车间新增/修改 Request VO")
@Data
public class MesMdWorkshopSaveReqVO {

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "车间编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WS001")
    @NotEmpty(message = "车间编码不能为空")
    private String code;

    @Schema(description = "车间名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号车间")
    @NotEmpty(message = "车间名称不能为空")
    private String name;

    @Schema(description = "面积", example = "1000.00")
    private BigDecimal area;

    @Schema(description = "负责人用户编号", example = "1")
    private Long chargeUserId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    }
