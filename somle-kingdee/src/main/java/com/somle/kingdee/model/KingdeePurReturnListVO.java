package com.somle.kingdee.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

/**
 * 采购退货单列表响应对象
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurReturnListVO {

    /**
     * 单据日期，yyyy-MM-dd
     */
    private String billDate;

    /**
     * 单据编码
     */
    private String billNo;

    /**
     * 单据状态，已审核："C"，未审核："Z"
     */
    private String billStatus;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 业务员id
     */
    private String empId;

    /**
     * 业务员名称
     */
    private String empName;

    /**
     * 业务员编码
     */
    private String empNumber;

    /**
     * 单据ID
     */
    private String id;

    /**
     * 备注
     */
    private String remark;

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 供应商编码
     */
    private String supplierNumber;
} 