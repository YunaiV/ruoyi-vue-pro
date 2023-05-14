package cn.iocoder.yudao.module.product.dal.dataobject.delivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

// TODO @Jason：要不就叫 DeliveryExpressTemplateFreeDO；detail 主要用来作为明细，不适合作为条目
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
     * 配送模板编号
     *
     * 关联 {@link DeliveryTemplateDO#getId()}
     */
    private Long templateId;

    /**
     * 包邮区域id
     */
    private Integer areaId;

    /**
     * 包邮金额，单位：分
     *
     * 订单总金额 > 包邮金额时，才免运费
     */
    private Integer freePrice;

    // TODO @Jason：freeCount；一般 count 作为数量哈
    /**
     * 包邮件数
     *
     * 订单总件数 > 包邮件数时，才免运费
     */
    private Integer freeNumber;

}
