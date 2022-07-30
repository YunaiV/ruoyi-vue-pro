package cn.iocoder.yudao.module.product.service.category;

import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryListReqVO;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.ProductCategoryUpdateReqVO;
import cn.iocoder.yudao.module.product.dal.dataobject.category.ProductCategoryDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 商品分类 Service 接口
 *
 * @author 芋道源码
 */
public interface ProductCategoryService {

    /**
     * 创建商品分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductCategory(@Valid ProductCategoryCreateReqVO createReqVO);

    /**
     * 更新商品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateProductCategory(@Valid ProductCategoryUpdateReqVO updateReqVO);

    /**
     * 删除商品分类
     *
     * @param id 编号
     */
    void deleteProductCategory(Long id);

    /**
     * 获得商品分类
     *
     * @param id 编号
     * @return 商品分类
     */
    ProductCategoryDO getProductCategory(Long id);

    /**
     * 获得商品分类列表
     *
     * @param ids 编号
     * @return 商品分类列表
     */
    List<ProductCategoryDO> getEnableProductCategoryList(Collection<Long> ids);

    /**
     * 获得商品分类列表
     *
     * @param listReqVO 查询条件
     * @return 商品分类列表
     */
    List<ProductCategoryDO> getEnableProductCategoryList(ProductCategoryListReqVO listReqVO);

    /**
     * 验证选择的商品分类是否合法
     *
     * @param id 分类编号
     */
    void validateProductCategory(Long id);

    /**
     * 获得开启状态的商品分类列表
     *
     * @return 商品分类列表
     */
    List<ProductCategoryDO> getEnableProductCategoryList();

}
