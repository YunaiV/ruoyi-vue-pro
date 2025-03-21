package cn.iocoder.yudao.module.erp.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductPageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductRespVO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductSaveReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * ERP 供应商产品 Service 接口
 *
 * @author 索迈管理员
 */
public interface ErpSupplierProductService {

    /**
     * 创建ERP 供应商产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSupplierProduct(@Valid ErpSupplierProductSaveReqVO createReqVO);

    /**
     * 更新ERP 供应商产品
     *
     * @param updateReqVO 更新信息
     */
    void updateSupplierProduct(@Valid ErpSupplierProductSaveReqVO updateReqVO);

    /**
     * 删除ERP 供应商产品
     *
     * @param id 编号
     */
    void deleteSupplierProduct(Long id);

    /**
     * 获得ERP 供应商产品
     *
     * @param id 编号
     * @return ERP 供应商产品
     */
    ErpSupplierProductDO getSupplierProduct(Long id);

    /**
     * 获得ERP 供应商产品分页
     *
     * @param pageReqVO 分页查询
     * @return ERP 供应商产品分页
     */
    PageResult<ErpSupplierProductDO> getSupplierProductPage(ErpSupplierProductPageReqVO pageReqVO);

    PageResult<ErpSupplierProductRespVO> buildSupplierProductVOPageResult(PageResult<ErpSupplierProductDO> pageResult);

    List<ErpSupplierProductRespVO> getSupplierProductVOListByStatus(Integer status);
}