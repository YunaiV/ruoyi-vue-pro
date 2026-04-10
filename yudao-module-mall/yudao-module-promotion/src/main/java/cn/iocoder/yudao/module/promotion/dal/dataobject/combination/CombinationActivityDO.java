package cn.iocoder.yudao.module.promotion.dal.dataobject.combination;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 拼团活动 DO
 *
 * @author HUIHUI
 */
@TableName("promotion_combination_activity")
@KeySequence("promotion_combination_activity_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombinationActivityDO extends BaseDO {

    /**
     * 活动编号
     */
    @TableId
    private Long id;
    /**
     * 拼团名称
     */
    private String name;
    /**
     * 商品 SPU 编号
     *
     * 关联 ProductSpuDO 的 id
     */
    private Long spuId;
    /**
     * 总限购数量
     */
    private Integer totalLimitCount;
    /**
     * 单次限购数量
     */
    private Integer singleLimitCount;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 几人团
     */
    private Integer userSize;
    /**
     * 虚拟成团
     */
    private Boolean virtualGroup;
    /**
     * 活动状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 限制时长（小时）
     */
    private Integer limitDuration;

}
