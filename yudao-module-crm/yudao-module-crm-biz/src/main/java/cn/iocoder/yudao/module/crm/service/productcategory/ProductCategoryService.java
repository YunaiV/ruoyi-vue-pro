package cn.iocoder.yudao.module.crm.service.productcategory;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.crm.controller.admin.productcategory.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.productcategory.ProductCategoryDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 产品分类 Service 接口
 *
 * @author ZanGe丶
 */
public interface ProductCategoryService {

    /**
     * 创建产品分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductCategory(@Valid ProductCategoryCreateReqVO createReqVO);

    /**
     * 更新产品分类
     *
     * @param updateReqVO 更新信息
     */
    void updateProductCategory(@Valid ProductCategoryUpdateReqVO updateReqVO);

    /**
     * 删除产品分类
     *
     * @param id 编号
     */
    void deleteProductCategory(Long id);

    /**
     * 获得产品分类
     *
     * @param id 编号
     * @return 产品分类
     */
    ProductCategoryDO getProductCategory(Long id);

    /**
     * 获得产品分类列表
     *
     * @param ids 编号
     * @return 产品分类列表
     */
    List<ProductCategoryDO> getProductCategoryList(ProductCategoryListReqVO treeListReqVO);

}
