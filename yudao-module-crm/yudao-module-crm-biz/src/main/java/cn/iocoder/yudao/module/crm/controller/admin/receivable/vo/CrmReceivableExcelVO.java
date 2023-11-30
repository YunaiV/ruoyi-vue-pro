package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

// TODO liuhongfeng：导出可以等其它功能做完，统一在搞；
@Data
public class CrmReceivableExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("回款编号")
    private String no;

    @ExcelProperty("回款计划ID")
    private Long planId;

    @ExcelProperty("客户名称")
    private Long customerId;

    @ExcelProperty("合同名称")
    private Long contractId;

    @ExcelProperty(value = "审批状态", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_AUDIT_STATUS)
    private Integer checkStatus;

    @ExcelProperty("工作流编号")
    private Long processInstanceId;

    @ExcelProperty("回款日期")
    private LocalDateTime returnTime;

    @ExcelProperty("回款方式")
    private String returnType;

    @ExcelProperty("回款金额")
    private Integer price;

    @ExcelProperty("负责人")
    private Long ownerUserId;

    @ExcelProperty("批次")
    private Long batchId;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
