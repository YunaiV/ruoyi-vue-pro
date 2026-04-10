package cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 工作站 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesMdWorkstationRespVO {

    @Schema(description = "工作站编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("工作站编号")
    private Long id;

    @Schema(description = "工作站编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "WK001")
    @ExcelProperty("工作站编码")
    private String code;

    @Schema(description = "工作站名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "一号工作站")
    @ExcelProperty("工作站名称")
    private String name;

    @Schema(description = "工作站地点", example = "A区1号线")
    @ExcelProperty("工作站地点")
    private String address;

    @Schema(description = "所在车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workshopId;

    @Schema(description = "车间名称", example = "一号车间")
    @ExcelProperty("车间名称")
    private String workshopName;

    @Schema(description = "工序编号", example = "1")
    private Long processId;

    @Schema(description = "工序名称", example = "打磨")
    @ExcelProperty("工序名称")
    private String processName;

    @Schema(description = "线边库编号", example = "1")
    private Long warehouseId;

    @Schema(description = "库区编号", example = "1")
    private Long locationId;

    @Schema(description = "库位编号", example = "1")
    private Long areaId;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
