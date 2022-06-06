package cn.iocoder.yudao.module.product.service.category;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.category.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.category.CategoryDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

// TODO @JeromeSoar：需要 Product 前缀
/**
 * 商品分类 Service 接口
 *
 * @author 芋道源码
 */
public interface CategoryService {

    /**
     * 创建商品分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategory(@Valid CategoryCreateReqVO createReqVO);

    /**
     * 更新商品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateCategory(@Valid CategoryUpdateReqVO updateReqVO);

    /**
     * 删除商品分类
     *
     * @param id 编号
     */
    void deleteCategory(Long id);

    /**
     * 获得商品分类
     *
     * @param id 编号
     * @return 商品分类
     */
    CategoryDO getCategory(Long id);

    /**
     * 获得商品分类列表
     *
     * @param ids 编号
     * @return 商品分类列表
     */
    List<CategoryDO> getCategoryList(Collection<Long> ids);

    /**
     * 获得商品分类分页
     *
     * @param pageReqVO 分页查询
     * @return 商品分类分页
     */
    PageResult<CategoryDO> getCategoryPage(CategoryPageReqVO pageReqVO);

    /**
     * 获得商品分类列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 商品分类列表
     */
    List<CategoryDO> getCategoryList(CategoryExportReqVO exportReqVO);

    /**
     * 获得商品分类列表
     *
     * @param treeListReqVO 查询条件
     * @return 商品分类列表
     */
    List<CategoryDO> getCategoryTreeList(CategoryTreeListReqVO treeListReqVO);

    /**
     *  验证选择的分类的合法性
     * @param categoryId 分类id
     */
    void validatedCategoryById(Long categoryId);
}
