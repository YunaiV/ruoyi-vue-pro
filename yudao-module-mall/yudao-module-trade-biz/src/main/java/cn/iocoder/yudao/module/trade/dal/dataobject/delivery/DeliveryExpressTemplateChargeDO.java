package cn.iocoder.yudao.module.trade.dal.dataobject.delivery;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 快递运费模板计费配置 DO
 *
 * @author jason
 */
@TableName(value ="trade_delivery_express_template_charge")
@KeySequence("trade_delivery_express_template_charge_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class DeliveryExpressTemplateChargeDO extends BaseDO {

    /**
     * 编号，自增
     */
    @TableId
    private Long id;

    /**
     * 配送模板编号
     *
     * 关联 {@link DeliveryExpressTemplateDO#getId()}
     */
    private Long templateId;

    // TODO @Jason：全国最好使用 0  @芋艿 Area.ID_CHINA 是 1
    /**
     * 配送区域id 1:适用于全国
     */
    private Integer areaId;

    /**
     * 配送计费方式
     *
     * 冗余 {@link DeliveryExpressTemplateDO#getChargeMode()}
     */
    private Integer chargeMode;

    /**
     * 首件数量(件数,重量，或体积)
     */
    private Double startCount;
    /**
     * 起步价，单位：分
     */
    private Integer startPrice;

    /**
     * 续件数量(件, 重量，或体积)
     */
    private Double extraCount;
    /**
     * 额外价，单位：分
     */
    private Integer extraPrice;

}
