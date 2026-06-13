package cn.iocoder.yudao.module.wms.service.inventory.dto;

import cn.iocoder.yudao.module.wms.enums.order.WmsOrderTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * WMS 库存盘点请求 DTO
 *
 * @author 芋道源码
 */
@Data
public class WmsInventoryCheckReqDTO {

    /**
     * 单据编号
     */
    private Long orderId;
    /**
     * 单据号
     */
    private String orderNo;
    /**
     * 单据类型
     *
     * 枚举 {@link WmsOrderTypeEnum#getType()}
     */
    private Integer orderType;

    /**
     * 库存盘点明细
     */
    private List<Item> items;

    /**
     * WMS 库存盘点明细
     */
    @Data
    public static class Item {

        /**
         * 库存编号
         */
        private Long inventoryId;
        /**
         * SKU 编号
         */
        private Long skuId;
        /**
         * 仓库编号
         */
        private Long warehouseId;
        /**
         * 账面数量
         */
        private BigDecimal quantity;
        /**
         * 实盘数量
         */
        private BigDecimal checkQuantity;

        // ========= 单价备注相关字段 =========

        /**
         * 单价
         */
        private BigDecimal price;
        /**
         * 备注
         */
        private String remark;

    }

}
