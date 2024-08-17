package cn.iocoder.yudao.module.iot.service.product;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.*;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.ProductDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * iot 产品 Service 接口
 *
 * @author 芋道源码
 */
public interface ProductService {

    /**
     * 创建iot 产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProduct(@Valid ProductSaveReqVO createReqVO);

    /**
     * 更新iot 产品
     *
     * @param updateReqVO 更新信息
     */
    void updateProduct(@Valid ProductSaveReqVO updateReqVO);

    /**
     * 删除iot 产品
     *
     * @param id 编号
     */
    void deleteProduct(Long id);

    /**
     * 获得iot 产品
     *
     * @param id 编号
     * @return iot 产品
     */
    ProductDO getProduct(Long id);

    /**
     * 获得iot 产品分页
     *
     * @param pageReqVO 分页查询
     * @return iot 产品分页
     */
    PageResult<ProductDO> getProductPage(ProductPageReqVO pageReqVO);

}