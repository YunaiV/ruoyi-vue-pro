package cn.iocoder.yudao.module.oms.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * OMS店铺产品
 *
 * @author gumaomao
 */
@TableName("oms_shop_product_maomao")
@KeySequence("oms_shop_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsShopProductDO extends TenantBaseDO {

    /**
     * 店铺产品id
     */
    @TableId
    private Long id;

    /**
     * 店铺id
     **/
    private Long shopId;

    /**
     * 平台sku
     **/
    private String platformCode;

    /**
     * 外部资源来源id，唯一标识
     */
    private String sourceId;

    /**
     * 店铺产品名称
     */
    private String name;

    /**
     * 价格
     **/
    private BigDecimal price;

    /**
     * 币种
     **/
    private String currency;

    /**
     * 链接
     */
    private String url;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 部门ID
     */
    private Long deptId;

}