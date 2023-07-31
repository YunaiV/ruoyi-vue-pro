package cn.iocoder.yudao.module.promotion.dal.dataobject.bargain;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 砍价助力 DO
 *
 * @author HUIHUI
 */
@TableName("promotion_bargain_assist")
@KeySequence("promotion_bargain_assist_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BargainAssistDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 砍价活动编号
     */
    private Long activityId;

    /**
     * 砍价记录编号
     */
    private Long recordId;

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 减少价格。单位分
     */
    private Integer reducePrice;

}
