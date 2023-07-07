package cn.iocoder.yudao.module.trade.dal.dataobject.order;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

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
public class OrderLogDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 订单号
     */
    private Long orderId;
    /**
     * 订单日志信息
     */
    private String content;
    /**
     * 操作类型
     */
    private Integer operateType;
    /**
     * 创建者ID
     */
    private Long userId;
    /**
     * 创建者类型
     */
    private Integer userType;

}
