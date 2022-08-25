package cn.iocoder.yudao.module.product.dal.dataobject.property;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;


/**
 * 规格值 DO
 *
 * @author 芋道源码
 */
@TableName("product_property_value")
@KeySequence("product_property_value_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPropertyValueDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 规格键 id
     *
     * TODO @franky：加个 关联 {@link ProductPropertyDO#getId()} ，这样就能更好的知道
     */
    private Long propertyId;
    /**
     * 规格值名字
     */
    private String name;
    /**
     * 状态： 1 开启 ，2 禁用
     */
    private Integer status;

}
