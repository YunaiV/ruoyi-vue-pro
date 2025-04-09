package cn.iocoder.yudao.module.oms.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * ERP 店铺产品项 DO
 *
 * @author 索迈管理员
 */
@TableName("oms_shop_product_item_maomao")
@KeySequence("oms_shop_product_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OmsShopProductItemDO extends BaseDO {

    /**
     * 店铺产品项编号
     */
    @TableId
    private Long id;
    /**
     * 产品编号
     */
    private Long productId;

    /**
     * 店铺产品编号
     */
    private Long shopProductId;

}