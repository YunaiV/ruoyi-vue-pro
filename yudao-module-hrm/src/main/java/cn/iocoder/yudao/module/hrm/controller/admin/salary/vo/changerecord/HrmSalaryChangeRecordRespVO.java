package cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.changerecord;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - HRM 定薪/调薪记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class HrmSalaryChangeRecordRespVO {

    @ExcelProperty(value = "记录编号", index = 0)
    @Schema(description = "记录编号")
    private Long id;

    @ExcelProperty(value = "员工编号", index = 1)
    @Schema(description = "员工编号")
    private Long employeeId;

    @Schema(description = "记录类型")
    private Integer recordType;

    @ExcelProperty(value = "记录类型", index = 2)
    @Schema(description = "记录类型")
    private String recordTypeName;

    @ExcelProperty(value = "调整原因", index = 3, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_SALARY_CHANGE_REASON)
    @Schema(description = "调整原因")
    private Integer changeReason;

    @ExcelProperty(value = "生效日期", index = 4)
    @Schema(description = "生效日期")
    private LocalDateTime effectTime;

    @ExcelProperty(value = "调整前正式工资", index = 5)
    @Schema(description = "调整前正式工资")
    private BigDecimal beforeTotal;

    @ExcelProperty(value = "调整后正式工资", index = 6)
    @Schema(description = "调整后正式工资")
    private BigDecimal afterTotal;

    @ExcelProperty(value = "调整前试用期工资", index = 7)
    @Schema(description = "调整前试用期工资")
    private BigDecimal probationBeforeTotal;

    @ExcelProperty(value = "调整后试用期工资", index = 8)
    @Schema(description = "调整后试用期工资")
    private BigDecimal probationAfterTotal;

    @ExcelProperty(value = "状态", index = 9, converter = DictConvert.class)
    @DictFormat(DictTypeConstants.HRM_SALARY_CHANGE_RECORD_STATUS)
    @Schema(description = "状态")
    private Integer status;

    @ExcelProperty(value = "备注", index = 10)
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "薪资项列表")
    private List<HrmSalaryOptionValueVO> salaryOptions;

    @Schema(description = "试用期薪资项列表")
    private List<HrmSalaryOptionValueVO> probationSalaryOptions;

    @ExcelProperty(value = "创建时间", index = 11)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
