package cn.iocoder.yudao.module.crm.dal.dataobject.business;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 商机产品关联表 DO
 *
 * @author lzxhqs
 */
@TableName("crm_business_product")
@KeySequence("crm_business_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmBusinessProductDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 商机编号
     *
     * 关联 {@link CrmBusinessDO#getId()}
     */
    private Long businessId;

    /**
     * 产品编号
     *
     * 关联 {@link CrmProductDO#getId()}
     */
    private Long productId;

    // TODO @lzxhqs：改成 Integer，单位：分。目前整体倾向放大 100 倍哈
    /**
     * 产品单价
     */
    private BigDecimal price;

    /**
     * 销售价格
     */
    private BigDecimal salesPrice;

    // TODO @lzxhqs：改成 count
    /**
     * 数量
     */
    private BigDecimal num;

    // TODO @lzxhqs：改成 discountPercent
    /**
     * 折扣
     */
    private BigDecimal discount;

    // TODO @lzxhqs：改成 totalPrice；总计价格，和现有项目风格一致；
    /**
     * 小计（折扣后价格）
     */
    private BigDecimal subtotal;

    /**
     * 单位
     */
    private String unit;
}
