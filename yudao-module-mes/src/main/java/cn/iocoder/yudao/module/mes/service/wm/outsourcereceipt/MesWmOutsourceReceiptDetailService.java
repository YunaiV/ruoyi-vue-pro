package cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt;

import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.detail.MesWmOutsourceReceiptDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDetailDO;

import java.util.List;

/**
 * MES 委外收货明细 Service 接口
 */
public interface MesWmOutsourceReceiptDetailService {

    /**
     * 创建外协入库明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutsourceReceiptDetail(MesWmOutsourceReceiptDetailSaveReqVO createReqVO);

    /**
     * 更新外协入库明细
     *
     * @param updateReqVO 更新信息
     */
    void updateOutsourceReceiptDetail(MesWmOutsourceReceiptDetailSaveReqVO updateReqVO);

    /**
     * 删除外协入库明细
     *
     * @param id 编号
     */
    void deleteOutsourceReceiptDetail(Long id);

    /**
     * 获得外协入库明细
     *
     * @param id 编号
     * @return 外协入库明细
     */
    MesWmOutsourceReceiptDetailDO getOutsourceReceiptDetail(Long id);

    /**
     * 获得委外收货明细列表（根据收货单编号）
     *
     * @param receiptId 收货单编号
     * @return 委外收货明细列表
     */
    List<MesWmOutsourceReceiptDetailDO> getOutsourceReceiptDetailListByReceiptId(Long receiptId);

    /**
     * 获得委外收货明细列表（根据行编号）
     *
     * @param lineId 行编号
     * @return 委外收货明细列表
     */
    List<MesWmOutsourceReceiptDetailDO> getOutsourceReceiptDetailListByLineId(Long lineId);

    /**
     * 删除委外收货明细（根据收货单编号）
     *
     * @param receiptId 收货单编号
     */
    void deleteOutsourceReceiptDetailByReceiptId(Long receiptId);

    /**
     * 删除委外收货明细（根据行编号）
     *
     * @param lineId 行编号
     */
    void deleteOutsourceReceiptDetailByLineId(Long lineId);

}
