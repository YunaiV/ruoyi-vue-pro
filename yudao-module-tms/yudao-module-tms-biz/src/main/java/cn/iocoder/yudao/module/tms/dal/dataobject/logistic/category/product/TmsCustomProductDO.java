package cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product;


import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 海关产品分类表 DO
 *
 * @author 王岽宇
 */
@TableName("erp_custom_product")
@KeySequence("erp_custom_product_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TmsCustomProductDO extends TenantBaseDO {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 产品id
     */
    private Long productId;
    /**
     * 海关分类id
     */
    private Long customCategoryId;

    private Boolean deleted;
}