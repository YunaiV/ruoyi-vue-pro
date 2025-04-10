package cn.iocoder.yudao.module.tms.service.logistic.category.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.TmsCustomProductPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.TmsCustomProductSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.TmsCustomProductDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 海关产品分类表 Service 接口
 *
 * @author 王岽宇
 */
public interface TmsCustomProductService {

    /**
     * 创建海关产品分类表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomProduct(@Valid TmsCustomProductSaveReqVO createReqVO);

    /**
     * 更新海关产品分类表
     *
     * @param updateReqVO 更新信息
     */
    TmsCustomProductDO updateCustomProduct(@Valid TmsCustomProductSaveReqVO updateReqVO);

    /**
     * 删除海关产品分类表
     *
     * @param id 编号
     */
    void deleteCustomProduct(Long id);

    /**
     * 获得海关产品分类表
     *
     * @param id 编号
     * @return 海关产品分类表
     */
    TmsCustomProductDO getCustomProduct(Long id);


    /**
     * 获得海关产品分类表分页
     *
     * @param pageReqVO 分页查询
     * @return 海关产品分类表分页
     */
    PageResult<TmsCustomProductDO> getCustomProductPage(TmsCustomProductPageReqVO pageReqVO);

    /**
     * 获取所有海关产品关联
     */
    List<TmsCustomProductDO> listCustomProductList();
    //根据产品id获取海关产品分类表
    TmsCustomProductDO getCustomProductByProductId(Long productId);
}