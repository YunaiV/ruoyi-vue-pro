package cn.iocoder.yudao.module.product.api.category;

import java.util.Collection;

/**
 * 商品分类 API 接口
 *
 * @author owen
 */
public interface ProductCategoryApi {

    /**
     * 校验商品分类是否有效。如下情况，视为无效：
     * 1. 商品分类编号不存在
     * 2. 商品分类被禁用
     *
     * @param ids 商品分类编号数组
     */
    void validateCategoryList(Collection<Long> ids);
}
