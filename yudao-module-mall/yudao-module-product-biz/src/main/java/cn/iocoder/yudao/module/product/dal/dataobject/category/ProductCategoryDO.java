package cn.iocoder.yudao.module.product.dal.dataobject.category;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 商品分类 DO
 *
 * 商品分类一共两类：
 * 1）一级分类：{@link #parentId} 等于 0
 * 2）二级 + 三级分类：{@link #parentId} 不等于 0
 *
 * @author 芋道源码
 */
@TableName("product_category")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDO extends BaseDO {

    /**
     * 父分类编号 - 根分类
     */
    public static final Long PARENT_ID_NULL = 0L;

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 父分类编号
     */
    private Long parentId;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 分类图片
     *
     * 一级分类：推荐 200 x 100 分辨率
     * 二级 + 三级分类：推荐 100 x 100 分辨率
     */
    private String picUrl;
    /**
     * 分类排序
     */
    private Integer sort;
    /**
     * 分类描述
     */
    private String description;
    /**
     * 开启状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
