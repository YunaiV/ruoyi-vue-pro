package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurOrder {
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
     * 执行状态（所有：“”，全部执行：“C”，部分执行：“Z”，未执行：“A”，待执行（=部分执行+未执行）：“B”） (Execution status)
     */
    private String ioStatus;

    /**
     * 出库状态（所有：“”，全部出库：“C”，部分出库：“Z”，未出库：“A”，待出库（=部分出库+未出库）：“B”） (Real outbound status)
     */
    private String realIoStatus;

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
}
