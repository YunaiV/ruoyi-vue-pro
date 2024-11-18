package com.somle.kingdee.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurRet {

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
