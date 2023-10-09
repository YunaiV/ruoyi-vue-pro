package cn.iocoder.yudao.module.promotion.dal.dataobject.combination;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.promotion.enums.combination.CombinationRecordStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

// TODO 芋艿：把字段的顺序，和 do 顺序对齐下
/**
 * 拼团记录 DO
 *
 * 1. 用户参与拼团时，会创建一条记录
 * 2. 团长的拼团记录，和参团人的拼团记录，通过 {@link #headId} 关联
 *
 * @author HUIHUI
 */
@TableName("promotion_combination_record")
@KeySequence("promotion_combination_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombinationRecordDO extends BaseDO {

    /**
     * 团长编号 - 团长
     */
    public static final Long HEAD_ID_GROUP = 0L;

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;

    /**
     * 拼团活动编号
     *
     * 关联 {@link CombinationActivityDO#getId()}
     */
    private Long activityId;
    /**
     * 拼团商品单价
     *
     * 冗余 {@link CombinationProductDO#getCombinationPrice()}
     */
    private Integer combinationPrice;
    /**
     * SPU 编号
     */
    private Long spuId;
    /**
     * 商品名字
     */
    private String spuName;
    /**
     * 商品图片
     */
    private String picUrl;
    /**
     * SKU 编号
     */
    private Long skuId;
    /**
     * 购买的商品数量
     */
    private Integer count;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 团长编号
     *
     * 关联 {@link CombinationRecordDO#getId()}
     *
     * 如果是团长，则它的值是 {@link #HEAD_ID_GROUP}
     */
    private Long headId;
    /**
     * 开团状态
     *
     * 关联 {@link CombinationRecordStatusEnum}
     */
    private Integer status;
    /**
     * 订单编号
     */
    private Long orderId;
    /**
     * 开团需要人数
     *
     * 关联 {@link CombinationActivityDO#getUserSize()}
     */
    private Integer userSize;
    /**
     * 已加入拼团人数
     */
    private Integer userCount;
    /**
     * 是否虚拟成团
     *
     * 默认为 false。
     * 拼团过期都还没有成功，如果 {@link CombinationActivityDO#getVirtualGroup()} 为 true，则执行虚拟成团的逻辑，才会更新该字段为 true
     */
    private Boolean virtualGroup;

    /**
     * 过期时间
     *
     * 基于 {@link CombinationRecordDO#getStartTime()} + {@link CombinationActivityDO#getLimitDuration()} 计算
     */
    private LocalDateTime expireTime;
    /**
     * 开始时间 (订单付款后开始的时间)
     */
    private LocalDateTime startTime;
    /**
     * 结束时间（成团时间/失败时间）
     */
    private LocalDateTime endTime;

}
