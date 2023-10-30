package cn.iocoder.yudao.module.crm.controller.admin.receivable.vo;

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

    @ExcelProperty("客户ID")
    private Long customerId;

    @ExcelProperty("合同ID")
    private Long contractId;

    @ExcelProperty(value = "审批状态", converter = DictConvert.class)
    @DictFormat("crm_receivable_check_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer checkStatus;

    @ExcelProperty("工作流编号")
    private Long processInstanceId;

    @ExcelProperty("回款日期")
    private LocalDateTime returnTime;

    @ExcelProperty("回款方式")
    private String returnType;

    @ExcelProperty("回款金额")
    private BigDecimal price;

    @ExcelProperty("负责人")
    private Long ownerUserId;

    @ExcelProperty("批次")
    private Long batchId;

    //@ExcelProperty("显示顺序")
    //private Integer sort;

    //@ExcelProperty("数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）")
    //private Integer dataScope;

    //@ExcelProperty("数据范围(指定部门数组)")
    //private String dataScopeDeptIds;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 XXXDictTypeConstants 枚举类中
    private Integer status;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
