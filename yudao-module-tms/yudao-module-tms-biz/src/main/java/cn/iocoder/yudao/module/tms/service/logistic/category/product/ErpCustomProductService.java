package cn.iocoder.yudao.module.tms.service.logistic.category.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.ErpCustomProductPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.category.product.vo.ErpCustomProductSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.ErpCustomProductDO;
import jakarta.validation.Valid;

/**
 * 海关产品分类表 Service 接口
 *
 * @author 王岽宇
 */
public interface ErpCustomProductService {

    /**
     * 创建海关产品分类表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomProduct(@Valid ErpCustomProductSaveReqVO createReqVO);

    /**
     * 更新海关产品分类表
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomProduct(@Valid ErpCustomProductSaveReqVO updateReqVO);

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
    ErpCustomProductDO getCustomProduct(Long id);

    /**
     * 获得海关产品分类表分页
     *
     * @param pageReqVO 分页查询
     * @return 海关产品分类表分页
     */
    PageResult<ErpCustomProductDO> getCustomProductPage(ErpCustomProductPageReqVO pageReqVO);

    //根据产品id获取海关产品分类表
    ErpCustomProductDO getCustomProductByProductId(Long productId);
}