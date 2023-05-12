package cn.iocoder.yudao.module.product.dal.dataobject.delivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 配送包邮详情 DO
 *
 * @author jason
 */
@TableName(value ="delivery_free_detail")
@KeySequence("delivery_free_detail_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class DeliveryFreeDetailDO extends BaseDO {
    /**
     * 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 配送模板编号, 对应delivery_template表id
     */
    private Long templateId;

    /**
     * 包邮区域id
     */
    private Integer areaId;

    /**
     * 包邮金额(单位分) 订单总金额>包邮金额才免运费
     */
    private Integer freePrice;

    /**
     * 包邮件数,订单总件数>包邮件数才免运费
     */
    private Integer freeNumber;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}