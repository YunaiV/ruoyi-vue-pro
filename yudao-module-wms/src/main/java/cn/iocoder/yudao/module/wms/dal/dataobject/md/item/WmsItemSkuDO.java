package cn.iocoder.yudao.module.wms.dal.dataobject.md.item;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * WMS 商品 SKU DO
 *
 * @author 芋道源码
 */
@TableName("wms_item_sku")
@KeySequence("wms_item_sku_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsItemSkuDO extends BaseDO {

    /**
     * 主键编号
     */
    @TableId
    private Long id;
    /**
     * 规格名称
     */
    private String name;
    /**
     * 商品编号
     *
     * 关联 {@link WmsItemDO#getId()}
     */
    private Long itemId;
    /**
     * 条码
     */
    private String barCode;
    /**
     * 规格编号
     */
    private String code;

    /**
     * 长，单位 cm
     */
    private BigDecimal length;
    /**
     * 宽，单位 cm
     */
    private BigDecimal width;
    /**
     * 高，单位 cm
     */
    private BigDecimal height;

    /**
     * 毛重，单位 kg
     */
    private BigDecimal grossWeight;
    /**
     * 净重，单位 kg
     */
    private BigDecimal netWeight;

    /**
     * 成本价，单位元
     */
    private BigDecimal costPrice;
    /**
     * 销售价，单位元
     */
    private BigDecimal sellingPrice;

}
