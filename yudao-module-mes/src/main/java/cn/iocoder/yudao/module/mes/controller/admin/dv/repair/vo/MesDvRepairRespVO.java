package cn.iocoder.yudao.module.mes.controller.admin.dv.repair.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.mes.enums.DictTypeConstants;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES 维修工单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MesDvRepairRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("编号")
    private Long id;

    @Schema(description = "维修工单编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "REP2024001")
    @ExcelProperty("维修工单编码")
    private String code;

    @Schema(description = "维修工单名称", example = "注塑机液压系统维修")
    @ExcelProperty("维修工单名称")
    private String name;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long machineryId;

    @Schema(description = "设备编码", example = "M001")
    @ExcelProperty("设备编码")
    private String machineryCode;

    @Schema(description = "设备名称", example = "注塑机")
    @ExcelProperty("设备名称")
    private String machineryName;

    @Schema(description = "品牌", example = "西门子")
    @ExcelProperty("品牌")
    private String machineryBrand;

    @Schema(description = "规格型号", example = "X-100")
    @ExcelProperty("规格型号")
    private String machinerySpecification;

    @Schema(description = "报修日期")
    @ExcelProperty("报修日期")
    private LocalDateTime requireDate;

    @Schema(description = "维修完成日期")
    @ExcelProperty("维修完成日期")
    private LocalDateTime finishDate;

    @Schema(description = "验收日期")
    @ExcelProperty("验收日期")
    private LocalDateTime confirmDate;

    @Schema(description = "维修结果", example = "1")
    @ExcelProperty(value = "维修结果", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DV_REPAIR_RESULT)
    private Integer result;

    @Schema(description = "维修人用户编号", example = "1")
    private Long acceptedUserId;

    @Schema(description = "维修人名称", example = "张三")
    @ExcelProperty("维修人")
    private String acceptedUserNickname;

    @Schema(description = "验收人用户编号", example = "1")
    private Long confirmUserId;

    @Schema(description = "验收人名称", example = "李四")
    @ExcelProperty("验收人")
    private String confirmUserNickname;

    @Schema(description = "来源单据类型", example = "1")
    private Integer sourceDocType;

    @Schema(description = "来源单据编号", example = "1")
    private Long sourceDocId;

    @Schema(description = "来源单据编码", example = "DOC001")
    private String sourceDocCode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.MES_DV_REPAIR_STATUS)
    private Integer status;

    @Schema(description = "备注", example = "测试备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
