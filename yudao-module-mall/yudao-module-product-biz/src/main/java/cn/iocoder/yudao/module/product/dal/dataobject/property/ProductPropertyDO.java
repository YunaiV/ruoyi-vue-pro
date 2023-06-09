package cn.iocoder.yudao.module.product.dal.dataobject.property;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品属性项 DO
 *
 * @author 芋道源码
 */
@TableName("product_property")
@KeySequence("product_property_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPropertyDO extends BaseDO {

    /**
     * SPU 单规格时，默认属性 id
     */
    public static final Long ID_DEFAULT = 0L;
    /**
     * SPU 单规格时，默认属性名字
     */
    public static final String NAME_DEFAULT = "默认";

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
