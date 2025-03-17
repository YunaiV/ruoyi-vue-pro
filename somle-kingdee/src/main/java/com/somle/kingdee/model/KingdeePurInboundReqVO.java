package com.somle.kingdee.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeePurInboundReqVO {
    /**
     * 单据编码 (Bill number)
     * 非必填 (Optional)
     */
    private String billNo;

    /**
     * 单据状态 (Bill status): 所有：""，已审核："C"，未审核："Z"
     * 非必填 (Optional)
     */
    private String billStatus;

    /**
     * 创建时间-结束时间的时间戳(毫秒) (Creation end time - timestamp in milliseconds)
     * 非必填 (Optional)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createEndTime;

    /**
     * 创建时间-开始时间的时间戳(毫秒) (Creation start time - timestamp in milliseconds)
     * 非必填 (Optional)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Timestamp createStartTime;

    /**
     * 调入部门 (List of department IDs for transfer-in)
     * 非必填 (Optional)
     */
    private List<String> deptId;

    /**
     * 部门编码 (Department code)
     * 非必填 (Optional)
     */
    private String deptNumber;

    /**
     * 职员ID (List of employee IDs)
     * 非必填 (Optional)
     */
    private List<String> empId;

    /**
     * 结束日期 (End bill date, format: "yyyy-MM-dd", leave empty for no filtering)
     * 非必填 (Optional)
     */
    private LocalDate endBillDate;

    /**
     * 商品规格型号 (Material model/specification)
     * 非必填 (Optional)
     */
    private String materialModel;

    /**
     * 商品名称 (Material name)
     * 非必填 (Optional)
     */
    private String materialName;

    /**
     * 商品编码 (Material code)
     * 非必填 (Optional)
     */
    private String materialNumber;

    /**
     * 修改时间-结束时间的时间戳(毫秒) (Modification end time - timestamp in milliseconds)
     * 非必填 (Optional)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private String modifyEndTime;

    /**
     * 修改时间-开始时间的时间戳(毫秒) (Modification start time - timestamp in milliseconds)
     * 非必填 (Optional)
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private String modifyStartTime;

    /**
     * 外部标识集合 (List of external identifiers)
     * 非必填 (Optional)
     */
    private List<String> outSidePks;

    /**
     * 当前页，默认1 (Current page, default is 1)
     * 非必填 (Optional)
     */
    private String page;

    /**
     * 每页显示条数默认10 (Page size, default is 10)
     * 非必填 (Optional)
     */
    private String pageSize;

    /**
     * 开始日期 (Start bill date, format: "yyyy-MM-dd", leave empty for no filtering)
     * 非必填 (Optional)
     */
    private LocalDate startBillDate;

    /**
     * 供应商ID (List of supplier IDs)
     * 非必填 (Optional)
     */
    private List<String> supplierId;

    /**
     * 供应商名称 (Supplier name)
     * 非必填 (Optional)
     */
    private String supplierName;

    /**
     * 供应商编码 (Supplier code)
     * 非必填 (Optional)
     */
    private String supplierNumber;
}
