package cn.iocoder.yudao.module.oms.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * OMS订单项 DO
 *
 * @author 谷毛毛
 */
@TableName("oms_order_item")
@KeySequence("oms_order_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsOrderItemDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 销售订单id
     */
    private Long orderId;
    /**
     * 店铺产品编号
     */
    private Long shopProductId;

    /**
     * 店铺产品外部来源id
     */
    private String shopProductExternalId;

    /**
     * 店铺产品外部编码
     */
    private String shopProductExternalCode;

    /***
     * 外部来源唯一标识
     */
    private String externalId;
    /**
     * 店铺产品数量
     */
    private Integer qty;
    /**
     * 单价
     */
    private BigDecimal price;

}