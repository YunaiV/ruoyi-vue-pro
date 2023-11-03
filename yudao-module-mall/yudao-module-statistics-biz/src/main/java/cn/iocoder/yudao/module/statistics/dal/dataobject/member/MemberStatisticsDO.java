package cn.iocoder.yudao.module.statistics.dal.dataobject.member;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 会员统计 DO
 * <p>
 * 以天为维度，统计全部的数据
 *
 * @author 芋道源码
 */
@TableName("member_statistics")
@KeySequence("member_statistics_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatisticsDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 统计日期
     */
    private LocalDateTime time;

    /**
     * 注册用户数量
     */
    private Integer userRegisterCount;
    /**
     * 访问用户数量（UV）
     */
    private Integer userVisitCount;
    /**
     * 访问页面数量（PV）
     */
    private Integer pageVisitCount;

    /**
     * 充值用户数量
     */
    private Integer rechargeUserCount;

    /**
     * 创建订单用户数
     */
    private Integer orderCreateUserCount;
    /**
     * 支付订单用户数
     */
    private Integer orderPayUserCount;
    /**
     * 总支付金额，单位：分
     */
    private Integer orderPayPrice;

}
