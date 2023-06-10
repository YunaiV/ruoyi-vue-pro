package cn.iocoder.yudao.module.point.dal.dataobject.pointconfig;

import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 积分设置 DO
 *
 * @author QingX
 */
@TableName("member_point_config")
@KeySequence("member_point_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointConfigDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Integer id;
    /**
     * 1 开启积分抵扣
0 关闭积分抵扣
     *
     * 枚举 {@link TODO infra_boolean_string 对应的类}
     */
    private Integer tradeDeductEnable;
    /**
     * 积分抵扣，抵扣最低为分 以0.01表示 1积分抵扣0.01元(单位：元)
     */
    private BigDecimal tradeDeductUnitPrice;
    /**
     * 积分抵扣最大值
     */
    private Long tradeDeductMaxPrice;
    /**
     * 1元赠送多少分
     */
    private Long tradeGivePoint;

}
