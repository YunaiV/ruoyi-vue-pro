package cn.iocoder.yudao.module.wms.dal.dataobject.order.shipment;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.merchant.WmsMerchantDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.DictTypeConstants;
import cn.iocoder.yudao.module.wms.enums.order.WmsShipmentOrderTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WMS 出库单 DO
 *
 * @author 芋道源码
 */
@TableName("wms_shipment_order")
@KeySequence("wms_shipment_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsShipmentOrderDO extends BaseDO {

    /**
     * 主键编号
     */
    @TableId
    private Long id;
    /**
     * 出库单号
     */
    private String no;
    /**
     * 出库类型
     *
     * 枚举 {@link WmsShipmentOrderTypeEnum}
     * 字典 {@link DictTypeConstants#SHIPMENT_ORDER_TYPE}
     */
    private Integer type;
    /**
     * 单据日期
     */
    private LocalDateTime orderTime;
    /**
     * 出库状态
     *
     * 字典 {@link DictTypeConstants#ORDER_STATUS}
     */
    private Integer status;
    /**
     * 业务订单号
     */
    private String bizOrderNo;
    /**
     * 客户编号
     *
     * 关联 {@link WmsMerchantDO#getId()}
     */
    private Long merchantId;
    /**
     * 备注
     */
    private String remark;

    // ========= 仓库字段 =========

    /**
     * 仓库编号
     *
     * 关联 {@link WmsWarehouseDO#getId()}
     */
    private Long warehouseId;

    // ========= 汇总金额字段 =========

    /**
     * 总数量
     */
    private BigDecimal totalQuantity;
    /**
     * 总金额
     */
    private BigDecimal totalPrice;

}
