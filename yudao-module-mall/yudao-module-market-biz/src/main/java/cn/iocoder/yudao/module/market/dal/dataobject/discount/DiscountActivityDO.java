package cn.iocoder.yudao.module.market.dal.dataobject.discount;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.market.enums.common.PromotionActivityStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 限时折扣活动 DO
 *
 * 一个活动下，可以有 {@link DiscountProductDO} 商品；
 * 一个商品，在指定时间段内，只能属于一个活动；
 *
 * @author 芋道源码
 */
@TableName(value = "promotion_discount_activity", autoResultMap = true)
@KeySequence("promotion_discount_activity_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountActivityDO extends BaseDO {

    /**
     * 活动编号，主键自增
     */
    @TableId
    private Long id;
    /**
     * 活动标题
     */
    private String name;
    /**
     * 状态
     *
     * 枚举 {@link PromotionActivityStatusEnum}
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
     * 备注
     */
    private String remark;

}
