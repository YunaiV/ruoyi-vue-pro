package cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

// TODO @疯狂：因为独立了表，是不是可以把字段的 brokerage 去掉了哈？
/**
 * 分销用户 DO
 *
 * @author owen
 */
@TableName("trade_brokerage_user")
@KeySequence("trade_brokerage_user_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageUserDO extends BaseDO {

    /**
     * 用户编号
     */
    @TableId
    private Long id;

    // TODO @疯狂：貌似改成 bindUserId，更明确？
    /**
     * 推广员编号
     *
     * 关联 MemberUserDO 的 id 字段
     */
    private Long brokerageUserId;
    /**
     * 推广员绑定时间
     */
    private LocalDateTime brokerageBindTime;

    /**
     * 推广资格
     */
    private Boolean brokerageEnabled;
    /**
     * 成为分销员时间
     */
    private LocalDateTime brokerageTime;

    /**
     * 可用佣金
     */
    private Integer brokeragePrice;
    /**
     * 冻结佣金
     */
    private Integer frozenBrokeragePrice;

}
