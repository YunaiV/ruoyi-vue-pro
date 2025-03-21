package cn.iocoder.yudao.module.srm.api.stock.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ErpWarehouseDTO {
    /**
     * 仓库编号
     */
    private Long id;
    /**
     * 仓库名称
     */
    private String name;
    /**
     * 仓库地址
     */
    private String address;
    /**
     * 排序
     */
    private Long sort;
    /**
     * 备注
     */
    private String remark;
    /**
     * 负责人
     */
    private String principal;
    /**
     * 仓储费，单位：元
     */
    private BigDecimal warehousePrice;
    /**
     * 搬运费，单位：元
     */
    private BigDecimal truckagePrice;
    /**
     * 开启状态
     * <p>
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 是否默认
     */
    private Boolean defaultStatus;

}
