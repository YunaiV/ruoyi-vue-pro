package cn.iocoder.yudao.module.member.dal.dataobject.point;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 会员积分配置 DO
 *
 * @author QingX
 */
@TableName(value = "member_point_config", autoResultMap = true)
@KeySequence("member_point_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPointConfigDO extends BaseDO {

    /**
     * 自增主键
     */
    @TableId
    private Long id;
    /**
     * 积分抵扣开关
     */
    private Boolean tradeDeductEnable;
    /**
     * 积分抵扣，单位：分
     *
     * 1 积分抵扣多少分
     */
    private Integer tradeDeductUnitPrice;
    /**
     * 积分抵扣最大值
     */
    private Integer tradeDeductMaxPrice;
    /**
     * 1 元赠送多少分
     */
    private Integer tradeGivePoint;

}
