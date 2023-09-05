package cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 佣金记录 DO
 *
 * @author owen
 */
@TableName("member_brokerage_record")
@KeySequence("member_brokerage_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberBrokerageRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Integer id;
    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 业务编号
     */
    private String bizId;
    /**
     * 业务类型
     * <p>
     * 枚举 {@link BrokerageRecordBizTypeEnum 对应的类}
     */
    private Integer bizType;
    /**
     * 标题
     */
    private String title;
    /**
     * 金额
     */
    private Integer price;
    /**
     * 当前总佣金
     */
    private Integer totalPrice;
    /**
     * 说明
     */
    private String description;
    /**
     * 状态
     * <p>
     * 枚举 {@link BrokerageRecordStatusEnum 对应的类}
     */
    private Integer status;
    /**
     * 冻结时间（天）
     */
    private Integer frozenDays;
    /**
     * 解冻时间
     */
    private LocalDateTime unfreezeTime;

}
