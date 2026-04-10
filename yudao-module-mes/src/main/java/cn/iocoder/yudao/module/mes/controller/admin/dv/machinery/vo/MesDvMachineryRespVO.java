package cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 设备台账 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesDvMachineryRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "设备编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "EQ-001")
    @ExcelProperty("设备编码")
    private String code;

    @Schema(description = "设备名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "CNC 加工中心")
    @ExcelProperty("设备名称")
    private String name;

    @Schema(description = "品牌", example = "西门子")
    @ExcelProperty("品牌")
    private String brand;

    @Schema(description = "规格型号", example = "S7-300")
    @ExcelProperty("规格型号")
    private String spec;

    @Schema(description = "设备类型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long machineryTypeId;

    @Schema(description = "设备类型名称", example = "数控机床")
    @ExcelProperty("设备类型")
    private String machineryTypeName;

    @Schema(description = "所属车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "200")
    private Long workshopId;

    @Schema(description = "所属车间名称", example = "一号车间")
    @ExcelProperty("所属车间")
    private String workshopName;

    @Schema(description = "设备状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "设备状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DV_MACHINERY_STATUS)
    private Integer status;

    @Schema(description = "最近保养时间")
    @ExcelProperty("最近保养时间")
    private LocalDateTime lastMaintenTime;

    @Schema(description = "最近点检时间")
    @ExcelProperty("最近点检时间")
    private LocalDateTime lastCheckTime;

    @Schema(description = "备注", example = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
