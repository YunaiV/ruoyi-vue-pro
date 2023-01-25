package cn.iocoder.yudao.module.trade.dal.dataobject.aftersale;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.order.TradeOrderItemDO;
import cn.iocoder.yudao.module.trade.enums.aftersale.TradeAfterSaleStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 交易售后日志 DO
 *
 * // TODO 可优化：参考淘宝或者有赞：1）增加 action 表示什么操作；2）content 记录每个操作的明细
 *
 * @author 芋道源码
 */
@TableName("trade_after_sale_log")
@KeySequence("trade_after_sale_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeAfterSaleLogDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 1：AdminUserDO 的 id 字段
     * 关联 2：MemberUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;
    /**
     * 售后编号
     *
     * 关联 {@link TradeAfterSaleDO#getId()}
     */
    private Long afterSaleId;
    /**
     * 订单编号
     *
     * 关联 {@link TradeOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 订单项编号
     *
     * 关联 {@link TradeOrderItemDO#getId()}
     */
    private Long orderItemId;
    /**
     * 售后状态（之前）
     *
     * 枚举 {@link TradeAfterSaleStatusEnum}
     */
    private Integer beforeStatus;
    /**
     * 售后状态（之后）
     *
     * 枚举 {@link TradeAfterSaleStatusEnum}
     */
    private Integer afterStatus;
    /**
     * 操作明细
     */
    private String content;

}
