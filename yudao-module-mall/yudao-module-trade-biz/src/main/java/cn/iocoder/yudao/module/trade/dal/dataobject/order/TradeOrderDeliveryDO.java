package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.delivery.DeliveryTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

// TODO @puhui999:
/**
 * 交易订单发货记录 DO
 *
 * @author HUIHUI
 */
@TableName("trade_order_delivery")
@KeySequence("trade_order_delivery_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderDeliveryDO extends BaseDO {

    /**
     * 订单发货记录 id
     */
    private Long id;
    /**
     * 订单 id
     */
    private Long orderId;
    /**
     * 订单项 id TODO 要不要一个发货记录可对应多个订单项？
     */
    private Long orderItemId;
    /**
     * 用户编号
     *
     * 关联 MemberUserDO 的 id 编号
     */
    private Long userId;
    /**
     * 配送方式
     *
     * 枚举 {@link DeliveryTypeEnum}
     */
    private Integer deliveryType;
    /**
     * 发货物流公司编号
     */
    private Long logisticsId;
    /**
     * 发货物流单号
     */
    private String logisticsNo;

    // TODO 同城配送

}
