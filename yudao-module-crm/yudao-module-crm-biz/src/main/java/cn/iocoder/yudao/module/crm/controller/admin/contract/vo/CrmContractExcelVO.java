package cn.iocoder.yudao.module.crm.controller.admin.contract.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * CRM 合同 Excel VO
 *
 * @author dhb52
 */
@Data
public class CrmContractExcelVO {

    @ExcelProperty("合同编号")
    private Long id;

    @ExcelProperty("合同名称")
    private String name;

    @ExcelProperty("客户编号")
    private Long customerId;

    @ExcelProperty("商机编号")
    private Long businessId;

    @ExcelProperty("工作流编号")
    private Long processInstanceId;

    @ExcelProperty("下单日期")
    private LocalDateTime orderDate;

    @ExcelProperty("负责人的用户编号")
    private Long ownerUserId;

    @ExcelProperty("合同编号")
    private String no;

    @ExcelProperty("开始时间")
    private LocalDateTime startTime;

    @ExcelProperty("结束时间")
    private LocalDateTime endTime;

    @ExcelProperty("合同金额")
    private Integer price;

    @ExcelProperty("整单折扣")
    private Integer discountPercent;

    @ExcelProperty("产品总金额")
    private Integer productPrice;

    @ExcelProperty("联系人编号")
    private Long contactId;

    @ExcelProperty("公司签约人")
    private Long signUserId;

    @ExcelProperty("最后跟进时间")
    private LocalDateTime contactLastTime;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
