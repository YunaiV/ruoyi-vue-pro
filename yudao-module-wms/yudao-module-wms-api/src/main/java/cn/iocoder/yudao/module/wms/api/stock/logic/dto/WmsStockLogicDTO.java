package cn.iocoder.yudao.module.wms.api.stock.logic.dto;

import lombok.Data;

/**
 * 库存所有权 DTO
 *
 * @author 李方捷
 */
@Data
public class WmsStockLogicDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 仓库编号
     */
    private Long warehouseId;

    /**
     * 公司编号
     */
    private Long companyId;

    /**
     * 部门编号
     */
    private Long deptId;

    /**
     * 产品编号
     */
    private Long productId;

    /**
     * 可用数量
     */
    private Integer availableQty;

    /**
     * 出库待处理数量
     */
    private Integer outboundPendingQty;

    /**
     * 上架待处理数量
     */
    private Integer shelvingPendingQty;

} 