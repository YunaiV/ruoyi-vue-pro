package cn.iocoder.yudao.module.iot.service.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.product.IotProductDO;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.time.LocalDateTime;
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
     * 根据产品 key 获得产品
     *
     * @param productKey 产品 key
     * @return 产品
     */
    IotProductDO getProductByProductKey(String productKey);

    /**
     * 校验产品存在
     *
     * @param id 编号
     * @return 产品
     */
    IotProductDO validateProductExists(Long id);

    /**
     * 校验产品存在
     *
     * @param productKey 产品 key
     * @return 产品
     */
    IotProductDO validateProductExists(String productKey);

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

    /**
     * 获得产品数量
     *
     * @param createTime 创建时间，如果为空，则统计所有产品数量
     * @return 产品数量
     */
    Long getProductCount(@Nullable LocalDateTime createTime);


}