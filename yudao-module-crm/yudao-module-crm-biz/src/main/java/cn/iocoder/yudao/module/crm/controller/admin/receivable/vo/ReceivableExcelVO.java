package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;


/**
 * CRM 回款管理 Excel VO
 *
 * @author 赤焰
 */
@Data
public class ReceivableExcelVO {

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
    @DictFormat(cn.iocoder.yudao.module.crm.enums.DictTypeConstants.CRM_RECEIVABLE_CHECK_STATUS)
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
