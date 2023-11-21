package cn.iocoder.yudao.module.crm.service.product;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 产品 Service 接口
 *
 * @author ZanGe丶
 */
public interface ProductService {

    /**
     * 创建产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProduct(@Valid ProductCreateReqVO createReqVO);

    /**
     * 更新产品
     *
     * @param updateReqVO 更新信息
     */
    void updateProduct(@Valid ProductUpdateReqVO updateReqVO);

    /**
     * 删除产品
     *
     * @param id 编号
     */
    void deleteProduct(Long id);

    /**
     * 获得产品
     *
     * @param id 编号
     * @return 产品
     */
    ProductDO getProduct(Long id);

    /**
     * 获得产品列表
     *
     * @param ids 编号
     * @return 产品列表
     */
    List<ProductDO> getProductList(Collection<Long> ids);

    /**
     * 获得产品分页
     *
     * @param pageReqVO 分页查询
     * @return 产品分页
     */
    PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO);

    /**
     * 获得产品列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 产品列表
     */
    List<ProductDO> getProductList(ProductExportReqVO exportReqVO);

}
