package cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 设备点检记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesDvCheckRecordRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "点检计划编号", example = "1")
    private Long planId;

    @Schema(description = "计划名称", example = "日常点检计划")
    @ExcelProperty("计划名称")
    private String planName;

    @Schema(description = "计划编码", example = "P001")
    @ExcelProperty("计划编码")
    private String planCode;

    @Schema(description = "开始时间")
    @ExcelProperty("开始时间")
    private LocalDateTime planStartDate;

    @Schema(description = "结束日期")
    @ExcelProperty("结束日期")
    private LocalDateTime planEndDate;

    @Schema(description = "频率类型", example = "1")
    @ExcelProperty(value = "频率类型", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DV_CYCLE_TYPE)
    private Integer planCycleType;

    @Schema(description = "频率数量", example = "5")
    @ExcelProperty(value = "频率数量")
    private Integer planCycleCount;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long machineryId;

    @Schema(description = "设备编码", example = "M001")
    @ExcelProperty("设备编码")
    private String machineryCode;

    @Schema(description = "设备名称", example = "机床A")
    @ExcelProperty("设备名称")
    private String machineryName;

    @Schema(description = "品牌", example = "西门子")
    @ExcelProperty("品牌")
    private String machineryBrand;

    @Schema(description = "规格型号", example = "X-100")
    @ExcelProperty("规格型号")
    private String machinerySpec;

    @Schema(description = "点检时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("点检时间")
    private LocalDateTime checkTime;

    @Schema(description = "点检人编号", example = "1")
    private Long userId;

    @Schema(description = "点检人名称", example = "张三")
    @ExcelProperty("点检人")
    private String nickname;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DV_CHECK_RECORD_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "测试备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
