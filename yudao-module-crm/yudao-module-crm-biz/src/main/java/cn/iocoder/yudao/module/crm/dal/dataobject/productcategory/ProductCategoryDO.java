package cn.iocoder.yudao.module.crm.dal.dataobject.productcategory;

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
public class ProductCategoryDO extends BaseDO {

    /**
     * 主键id
     */
    @TableId
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 父级 id
     * // TODO @zange：这个要写下关联 CategoryDO 的 id 字段；参考下别的模块哈
     */
    private Long parentId;

}
