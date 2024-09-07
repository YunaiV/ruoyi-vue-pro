package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.ProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.ProductDO;
import jakarta.validation.Valid;

/**
 * IOT 产品 Service 接口
 *
 * @author ahh
 */
public interface ProductService {

    /**
     * 创建产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProduct(@Valid ProductSaveReqVO createReqVO);

    /**
     * 更新产品
     *
     * @param updateReqVO 更新信息
     */
    void updateProduct(@Valid ProductSaveReqVO updateReqVO);

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
     * 获得产品分页
     *
     * @param pageReqVO 分页查询
     * @return 产品分页
     */
    PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO);

    /**
     * 更新产品状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateProductStatus(Long id, Integer status);
}