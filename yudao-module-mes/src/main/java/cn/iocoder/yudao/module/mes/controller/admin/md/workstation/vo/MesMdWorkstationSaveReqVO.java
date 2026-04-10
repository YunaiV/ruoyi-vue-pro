package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - MES 工作站新增/修改 Request VO")
@Data
public class MesMdWorkstationSaveReqVO {

    @Schema(description = "工作站编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "工作站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WK001")
    @NotEmpty(message = "工作站编码不能为空")
    private String code;

    @Schema(description = "工作站名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号工作站")
    @NotEmpty(message = "工作站名称不能为空")
    private String name;

    @Schema(description = "工作站地点", example = "A区1号线")
    private String address;

    @Schema(description = "所在车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所在车间不能为空")
    private Long workshopId;

    @Schema(description = "工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属工序不能为空")
    private Long processId;

    @Schema(description = "线边库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    private String remark;

    }
