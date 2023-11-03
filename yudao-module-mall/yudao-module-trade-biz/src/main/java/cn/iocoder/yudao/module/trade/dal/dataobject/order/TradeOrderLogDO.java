package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.order.TradeOrderOperateTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 订单日志 DO
 *
 * @author 陈賝
 */
@TableName("trade_order_log")
@KeySequence("trade_order_log_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderLogDO extends BaseDO {

    /**
     * 用户类型 - 系统
     *
     * 例如说：Job 自动过期订单时，通过系统自动操作
     */
    public static final Integer USER_TYPE_SYSTEM = 0;
    /**
     * 用户编号 - 系统
     */
    public static final Long USER_ID_SYSTEM = 0L;

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 AdminUserDO 的 id 字段、或者 MemberUserDO 的 id 字段
     */
    private Long userId;
    /**
     * 用户类型
     *
     * 枚举 {@link UserTypeEnum}
     */
    private Integer userType;

    /**
     * 订单号
     *
     * 关联 {@link TradeOrderDO#getId()}
     */
    private Long orderId;
    /**
     * 操作前状态
     */
    private Integer beforeStatus;
    /**
     * 操作后状态
     */
    private Integer afterStatus;

    /**
     * 操作类型
     *
     * {@link TradeOrderOperateTypeEnum}
     */
    private Integer operateType;
    /**
     * 订单日志信息
     */
    private String content;

}
