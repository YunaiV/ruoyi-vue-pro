package cn.iocoder.yudao.module.crm.service.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.category.vo.ProductCategoryPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.category.vo.ProductCategorySaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.category.ProductCategoryDO;

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
     * @return 分类编号
     */
    Long createCategory(ProductCategorySaveReqVO createReqVO);

    /**
     * 更新商品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateCategory(ProductCategorySaveReqVO updateReqVO);

    /**
     * 删除商品分类
     *
     * @param id 分类编号
     */
    void deleteCategory(Long id);

    /**
     * 获得商品分类
     *
     * @param id 分类编号
     * @return 商品分类
     */
    ProductCategoryDO getCategory(Long id);

    /**
     * 获得商品分类分页列表
     *
     * @param pageReqVO 分页请求
     * @return 商品分类分页列表
     */
    PageResult<ProductCategoryDO> getCategoryPage(ProductCategoryPageReqVO pageReqVO);

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<ProductCategoryDO> getCategoryListByParentId(Long parentId);

}
