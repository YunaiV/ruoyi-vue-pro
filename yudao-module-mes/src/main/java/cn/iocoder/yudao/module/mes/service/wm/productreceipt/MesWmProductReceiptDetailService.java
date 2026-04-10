package cn.iocoder.yudao.module.mes.service.wm.productreceipt;

import cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.detail.MesWmProductReceiptDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 产品收货单明细 Service 接口
 */
public interface MesWmProductReceiptDetailService {

    /**
     * 创建产品收货单明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductReceiptDetail(@Valid MesWmProductReceiptDetailSaveReqVO createReqVO);

    /**
     * 修改产品收货单明细
     *
     * @param updateReqVO 修改信息
     */
    void updateProductReceiptDetail(@Valid MesWmProductReceiptDetailSaveReqVO updateReqVO);

    /**
     * 删除产品收货单明细
     *
     * @param id 编号
     */
    void deleteProductReceiptDetail(Long id);

    /**
     * 获得产品收货单明细
     *
     * @param id 编号
     * @return 产品收货单明细
     */
    MesWmProductReceiptDetailDO getProductReceiptDetail(Long id);

    /**
     * 按收货单编号获得明细列表
     *
     * @param receiptId 收货单编号
     * @return 明细列表
     */
    List<MesWmProductReceiptDetailDO> getProductReceiptDetailListByRecptId(Long receiptId);

    /**
     * 按收货单行编号获得明细列表
     *
     * @param lineId 行编号
     * @return 明细列表
     */
    List<MesWmProductReceiptDetailDO> getProductReceiptDetailListByLineId(Long lineId);

    /**
     * 按收货单行编号批量删除明细
     *
     * @param lineId 行编号
     */
    void deleteProductReceiptDetailByLineId(Long lineId);

    /**
     * 按收货单编号批量删除明细
     *
     * @param receiptId 收货单编号
     */
    void deleteProductReceiptDetailByRecptId(Long receiptId);

}
