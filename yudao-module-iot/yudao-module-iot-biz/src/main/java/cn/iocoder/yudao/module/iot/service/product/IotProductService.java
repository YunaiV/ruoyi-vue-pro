package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * IoT 产品 Service 接口
 *
 * @author ahh
 */
public interface IotProductService {

    /**
     * 创建产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProduct(@Valid IotProductSaveReqVO createReqVO);

    /**
     * 更新产品
     *
     * @param updateReqVO 更新信息
     */
    void updateProduct(@Valid IotProductSaveReqVO updateReqVO);

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
    IotProductDO getProduct(Long id);

    /**
     * 获得产品分页
     *
     * @param pageReqVO 分页查询
     * @return 产品分页
     */
    PageResult<IotProductDO> getProductPage(IotProductPageReqVO pageReqVO);

    /**
     * 更新产品状态
     *
     * @param id 编号
     * @param status 状态
     */
    void updateProductStatus(Long id, Integer status);

    /**
     * 获得所有产品
     *
     * @return 产品列表
     */
    List<IotProductDO> getProductList();

}