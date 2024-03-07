package cn.iocoder.yudao.module.crm.dal.dataobject.product;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 产品分类 DO
 *
 * @author ZanGe丶
 */
@TableName("crm_product_category")
@KeySequence("crm_product_category_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmProductCategoryDO extends BaseDO {

    /**
     * 父分类编号 - 根分类
     */
    public static final Long PARENT_ID_NULL = 0L;
    /**
     * 限定分类层级
     */
    public static final int CATEGORY_LEVEL = 2;

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 父级编号
     *
     * 关联 {@link CrmProductCategoryDO#getId()}
     */
    private Long parentId;

}
