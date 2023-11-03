package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

// TODO liuhongfeng：导出可以等其它功能做完，统一在搞；
/**
 * CRM 回款计划 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class CrmReceivablePlanExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("期数")
    private Integer period;

    @ExcelProperty("回款ID")
    private Long receivableId;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;

    @ExcelProperty(value = "审批状态", converter = DictConvert.class)
    @DictFormat(cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_RECEIVABLE_CHECK_STATUS)
    private Integer checkStatus;

    //@ExcelProperty("工作流编号")
    //private Long processInstanceId;

    @ExcelProperty("计划回款金额")
    private Integer price;

    @ExcelProperty("计划回款日期")
    private LocalDateTime returnTime;

    @ExcelProperty("提前几天提醒")
    private Integer remindDays;

    @ExcelProperty("提醒日期")
    private LocalDateTime remindTime;

    @ExcelProperty("客户ID")
    private Long customerId;

    @ExcelProperty("合同名称")
    private Long contractId;

    @ExcelProperty("负责人")
    private Long ownerUserId;

    //@ExcelProperty("显示顺序")
    //private Integer sort;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
