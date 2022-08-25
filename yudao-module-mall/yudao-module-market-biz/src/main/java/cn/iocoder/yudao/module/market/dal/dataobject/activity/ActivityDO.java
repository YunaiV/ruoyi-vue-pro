package cn.iocoder.yudao.module.market.dal.dataobject.activity;

import lombok.*;
import java.util.*;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 促销活动 DO
 *
 * @author 芋道源码
 */
@TableName("market_activity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDO extends BaseDO {

    /**
     * 活动编号
     */
    @TableId
    private Long id;
    /**
     * 活动标题
     */
    private String title;
    /**
     * 活动类型MarketActivityTypeEnum
     */
    private Integer activityType;
    /**
     * 活动状态MarketActivityStatusEnum
     */
    private Integer status;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 失效时间
     */
    private Date invalidTime;
    /**
     * 删除时间
     */
    private Date deleteTime;
    /**
     * 限制折扣字符串，使用 JSON 序列化成字符串存储
     */
    private String timeLimitedDiscount;
    /**
     * 限制折扣字符串，使用 JSON 序列化成字符串存储
     */
    private String fullPrivilege;

}
