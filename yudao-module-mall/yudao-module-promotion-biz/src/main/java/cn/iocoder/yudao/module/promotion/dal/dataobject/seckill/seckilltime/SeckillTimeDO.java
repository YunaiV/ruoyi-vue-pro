package cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.seckilltime;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalTime;

/**
 * 秒杀时段 DO
 *
 * @author 芋道源码
 */
@TableName("promotion_seckill_time")
@KeySequence("promotion_seckill_time_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SeckillTimeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 秒杀时段名称
     */
    private String name;
    /**
     * 开始时间点
     */
    private LocalTime startTime;
    /**
     * 结束时间点
     */
    private LocalTime endTime;
    /**
     * 秒杀活动数量
     */
    private Integer seckillActivityCount;

}
