package com.somle.kingdee.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购退货单列表查询请求对象
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurReturnReqVO {

    /**
     * 条形码
     */
    private String barcode;

    /**
     * 单据编码
     */
    private String billNo;

    /**
     * 单据状态（所有："", 已审核："C", 未审核："Z"）
     */
    private String billStatus;

    /**
     * 创建时间-结束时间的时间戳(毫秒)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createEndTime;

    /**
     * 创建时间-开始时间的时间戳(毫秒)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createStartTime;

    /**
     * 调入部门
     */
    private List<String> deptId;

    /**
     * 部门编码
     */
    private String deptNumber;

    /**
     * 职员ID
     */
    private List<String> empId;

    /**
     * 结束日期（格式："yyyy-MM-dd"，为空表示不过滤），单据日期
     */
    private LocalDate endBillDate;

    /**
     * 商品规格型号
     */
    private String materialModel;

    /**
     * 商品名称
     */
    private String materialName;

    /**
     * 商品编码
     */
    private String materialNumber;

    /**
     * 修改时间-结束时间的时间戳(毫秒)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp modifyEndTime;

    /**
     * 修改时间-开始时间的时间戳(毫秒)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp modifyStartTime;

    /**
     * 外部标识集合
     */
    private List<String> outSidePks;

    /**
     * 当前页，默认1
     */
    private String page = "1";

    /**
     * 每页显示条数默认10
     */
    private String pageSize = "10";

    /**
     * 开始日期（格式："yyyy-MM-dd"，为空表示不过滤），单据日期
     */
    private LocalDate startBillDate;

    /**
     * 供应商ID
     */
    private List<String> supplierId;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 供应商编码
     */
    private String supplierNumber;
} 