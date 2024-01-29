package cn.iocoder.yudao.module.crm.controller.admin.business.vo.business;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 商机 Excel VO
 *
 * @author ljlleo
 */
@Data
public class CrmBusinessExcelVO {

    @ExcelProperty("主键")
    private Long id;

    @ExcelProperty("商机名称")
    private String name;

    @ExcelProperty("商机状态类型编号")
    private Long statusTypeId;

    @ExcelProperty("商机状态编号")
    private Long statusId;

    @ExcelProperty("下次联系时间")
    private LocalDateTime contactNextTime;

    @ExcelProperty("客户编号")
    private Long customerId;

    @ExcelProperty("预计成交日期")
    private LocalDateTime dealTime;

    @ExcelProperty("商机金额")
    private BigDecimal price;

    @ExcelProperty("整单折扣")
    private BigDecimal discountPercent;

    @ExcelProperty("产品总金额")
    private BigDecimal productPrice;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("负责人的用户编号")
    private Long ownerUserId;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("只读权限的用户编号数组")
    private Set<Long> roUserIds;

    @ExcelProperty("读写权限的用户编号数组")
    private Set<Long> rwUserIds;

    @ExcelProperty("1赢单2输单3无效")
    private Integer endStatus;

    @ExcelProperty("结束时的备注")
    private String endRemark;

    @ExcelProperty("最后跟进时间")
    private LocalDateTime contactLastTime;

    @ExcelProperty("跟进状态")
    private Integer followUpStatus;

}
