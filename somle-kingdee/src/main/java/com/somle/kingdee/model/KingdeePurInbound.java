package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurInbound {

    /**
     * 关闭状态 (Bill close state)
     */
    private String billCloseState;

    /**
     * 单据日期，yyyy-MM-dd (Bill date, format: yyyy-MM-dd)
     */
    private String billDate;

    /**
     * 单据编码 (Bill number)
     */
    private String billNo;

    /**
     * 单据状态，已审核：“C”，未审核：“Z” (Bill status: Reviewed: "C", Unreviewed: "Z")
     */
    private String billStatus;

    /**
     * 创建时间 (Creation time)
     */
    private String createTime;

    /**
     * 交货方式id (Delivery type ID)
     */
    private String deliveryTypeId;

    /**
     * 交货方式名称 (Delivery type name)
     */
    private String deliveryTypeName;

    /**
     * 交货方式编码 (Delivery type number)
     */
    private String deliveryTypeNumber;

    /**
     * 业务员id (Employee ID)
     */
    private String empId;

    /**
     * 业务员名称 (Employee name)
     */
    private String empName;

    /**
     * 业务员编码 (Employee code)
     */
    private String empNumber;

    /**
     * 单据ID (Bill ID)
     */
    private String id;

    /**
     * 备注 (Remark)
     */
    private String remark;

    /**
     * 供应商id (Supplier ID)
     */
    private String supplierId;

    /**
     * 供应商名称 (Supplier name)
     */
    private String supplierName;

    /**
     * 供应商编码 (Supplier code)
     */
    private String supplierNumber;

    /**
     * 本次应付账款 (Total payable amount)
     */
    private Double totalAmount;
}
