package cn.iocoder.yudao.module.trade.dal.dataobject.delivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 快递运费模板包邮配置 DO
 *
 * @author jason
 */
@TableName(value ="trade_delivery_express_template_free")
@KeySequence("trade_delivery_express_template_free_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class DeliveryExpressTemplateFreeDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 配送模板编号
     *
     * 关联 {@link DeliveryExpressTemplateDO#getId()}
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

    /**
     * 包邮件数
     *
     * 订单总件数 > 包邮件数时，才免运费
     */
    private Integer freeCount;

}
