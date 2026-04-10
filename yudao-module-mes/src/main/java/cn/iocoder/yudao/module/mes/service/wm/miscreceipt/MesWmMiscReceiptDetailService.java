package cn.iocoder.yudao.module.mes.service.wm.miscreceipt;

import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLineSaveReqVO;

/**
 * MES 杂项入库明细 Service 接口
 */
public interface MesWmMiscReceiptDetailService {

    /**
     * 创建杂项入库明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMiscReceiptDetail(MesWmMiscReceiptLineSaveReqVO createReqVO);

    /**
     * 更新杂项入库明细
     *
     * @param updateReqVO 更新信息
     */
    void updateMiscReceiptDetail(MesWmMiscReceiptLineSaveReqVO updateReqVO);

    /**
     * 删除杂项入库明细（根据入库单ID）
     *
     * @param receiptId 入库单ID
     */
    void deleteMiscReceiptDetailByReceiptId(Long receiptId);

    /**
     * 删除杂项入库明细（根据行ID）
     *
     * @param lineId 行ID
     */
    void deleteMiscReceiptDetailByLineId(Long lineId);

    /**
     * 校验杂项入库明细是否存在
     *
     * @param id 编号
     */
    void validateMiscReceiptDetailExists(Long id);

}
