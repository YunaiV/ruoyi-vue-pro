package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * 回款计划 Excel VO
 *
 * @author 芋道源码
 */
@Data
public class ReceivablePlanExcelVO {

    @ExcelProperty("ID")
    private Long id;

    @ExcelProperty("期数")
    private Long indexNo;

    @ExcelProperty("回款ID")
    private Long receivableId;

    @ExcelProperty(value = "完成状态", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty(value = "审批状态", converter = DictConvert.class)
    @DictFormat("crm_receivable_check_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer checkStatus;

    //@ExcelProperty("工作流编号")
    //private Long processInstanceId;

    @ExcelProperty("计划回款金额")
    private BigDecimal price;

    @ExcelProperty("计划回款日期")
    private LocalDateTime returnTime;

    @ExcelProperty("提前几天提醒")
    private Long remindDays;

    @ExcelProperty("提醒日期")
    private LocalDateTime remindTime;

    @ExcelProperty("客户ID")
    private Long customerId;

    @ExcelProperty("合同ID")
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
