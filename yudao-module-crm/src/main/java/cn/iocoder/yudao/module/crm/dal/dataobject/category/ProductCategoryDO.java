package cn.iocoder.yudao.module.crm.dal.dataobject.category;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品分类 DO
 *
 * @author 芋道源码
 */
@TableName("product_category")
@KeySequence("product_category_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductCategoryDO extends BaseDO {

    /**
     * 分类ID
     */
    @TableId
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-启用 1-禁用
     */
    private Integer status;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 备注
     */
    private String remark;

}
