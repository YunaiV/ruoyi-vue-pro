package cn.iocoder.yudao.module.srm.service.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductPageReqVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductRespVO;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.SrmSupplierProductSaveReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierProductDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * ERP 供应商产品 Service 接口
 *
 * @author 索迈管理员
 */
public interface SrmSupplierProductService {

    /**
     * 创建ERP 供应商产品
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSupplierProduct(@Valid SrmSupplierProductSaveReqVO createReqVO);

    /**
     * 更新ERP 供应商产品
     *
     * @param updateReqVO 更新信息
     */
    void updateSupplierProduct(@Valid SrmSupplierProductSaveReqVO updateReqVO);

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
    SrmSupplierProductDO getSupplierProduct(Long id);

    /**
     * 获得ERP 供应商产品分页
     *
     * @param pageReqVO 分页查询
     * @return ERP 供应商产品分页
     */
    PageResult<SrmSupplierProductDO> getSupplierProductPage(SrmSupplierProductPageReqVO pageReqVO);

    PageResult<SrmSupplierProductRespVO> buildSupplierProductVOPageResult(PageResult<SrmSupplierProductDO> pageResult);

    List<SrmSupplierProductRespVO> getSupplierProductVOListByStatus(Integer status);
}