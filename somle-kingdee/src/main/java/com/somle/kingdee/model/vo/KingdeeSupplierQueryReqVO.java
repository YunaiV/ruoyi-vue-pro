package com.somle.kingdee.model.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 金蝶供应商查询请求参数
 */
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KingdeeSupplierQueryReqVO {

    /**
     * 创建时间-结束时间的时间戳(毫秒)
     */
    private Long createEndTime;

    /**
     * 创建时间-开始时间的时间戳(毫秒)
     */
    private Long createStartTime;

    /**
     * 状态(1:启用 0:禁用 -1：不限)
     */
    private String enable;

    /**
     * 供应商分类ID列表
     */
    private List<String> group;

    /**
     * 是否添加数据权限校验，默认false
     */
    private Boolean isDataPerm;

    /**
     * 修改时间-结束时间的时间戳(毫秒)
     */
    private Long modifyEndTime;

    /**
     * 修改时间-开始时间的时间戳(毫秒)
     */
    private Long modifyStartTime;

    /**
     * 当前页，默认1
     */
    private Integer page = 1;

    /**
     * 每页显示条数默认10
     */
    private Integer pageSize = 100;

    /**
     * 模糊搜索-名称
     */
    private String search;

    /**
     * 是否显示客户联系人信息；默认false-不显示
     */
    private Boolean showContactDetail;

    /**
     * 是否显示客户欠款；默认false-不显示
     */
    private Boolean showDebt;

    /**
     * 是否分页，1：不分页，其余情况下都分页
     */
    private String unPage;

    /**
     * 设置创建时间范围
     */
    public void setCreateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null) {
            this.createStartTime = startTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
        }
        if (endTime != null) {
            this.createEndTime = endTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
        }
    }

    /**
     * 设置修改时间范围
     */
    public void setModifyTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null) {
            this.modifyStartTime = startTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
        }
        if (endTime != null) {
            this.modifyEndTime = endTime.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
        }
    }
} 