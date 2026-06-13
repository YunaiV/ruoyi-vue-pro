package cn.iocoder.yudao.module.wms.dal.dataobject.order.check;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * WMS 盘库单 DO
 *
 * @author 芋道源码
 */
@TableName("wms_check_order")
@KeySequence("wms_check_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsCheckOrderDO extends BaseDO {

    /**
     * 主键编号
     */
    @TableId
    private Long id;
    /**
     * 盘库单号
     */
    private String no;
    /**
     * 单据日期
     */
    private LocalDateTime orderTime;
    /**
     * 盘库状态
     *
     * 字典 {@link DictTypeConstants#ORDER_STATUS}
     */
    private Integer status;
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
     * 盈亏数量（实盘数量 - 账面数量）
     */
    private BigDecimal totalQuantity;
    /**
     * 总金额（账面数量 * 单价）
     */
    private BigDecimal totalPrice;
    /**
     * 实际金额（实盘数量 * 单价）
     */
    private BigDecimal actualPrice;

}
