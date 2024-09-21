package cn.iocoder.yudao.module.promotion.dal.dataobject.point;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 积分商城活动 DO
 *
 * @author HUIHUI
 */
@TableName(value = "promotion_point_activity", autoResultMap = true)
@KeySequence("promotion_point_activity_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PointActivityDO extends BaseDO {

    /**
     * 积分商城活动编号
     */
    @TableId
    private Long id;
    /**
     * 积分商城活动商品
     */
    private Long spuId;
    /**
     * 活动状态
     *
     * 枚举 {@link CommonStatusEnum 对应的类}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 排序
     */
    private Integer sort;

}
