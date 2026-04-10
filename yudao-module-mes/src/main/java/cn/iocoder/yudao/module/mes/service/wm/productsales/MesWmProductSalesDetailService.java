package cn.iocoder.yudao.module.mes.service.wm.productsales;

import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.detail.MesWmProductSalesDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 销售出库明细 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmProductSalesDetailService {

    /**
     * 创建销售出库明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductSalesDetail(@Valid MesWmProductSalesDetailSaveReqVO createReqVO);

    /**
     * 修改销售出库明细
     *
     * @param updateReqVO 修改信息
     */
    void updateProductSalesDetail(@Valid MesWmProductSalesDetailSaveReqVO updateReqVO);

    /**
     * 删除销售出库明细
     *
     * @param id 编号
     */
    void deleteProductSalesDetail(Long id);

    /**
     * 获得销售出库明细
     *
     * @param id 编号
     * @return 销售出库明细
     */
    MesWmProductSalesDetailDO getProductSalesDetail(Long id);

    /**
     * 按出库单编号获得明细列表
     *
     * @param salesId 出库单编号
     * @return 明细列表
     */
    List<MesWmProductSalesDetailDO> getProductSalesDetailListBySalesId(Long salesId);

    /**
     * 按出库单行编号获得明细列表
     *
     * @param lineId 行编号
     * @return 明细列表
     */
    List<MesWmProductSalesDetailDO> getProductSalesDetailListByLineId(Long lineId);

    /**
     * 按出库单行编号批量删除明细
     *
     * @param lineId 行编号
     */
    void deleteProductSalesDetailByLineId(Long lineId);

    /**
     * 按出库单编号批量删除明细
     *
     * @param salesId 出库单编号
     */
    void deleteProductSalesDetailBySalesId(Long salesId);

}
