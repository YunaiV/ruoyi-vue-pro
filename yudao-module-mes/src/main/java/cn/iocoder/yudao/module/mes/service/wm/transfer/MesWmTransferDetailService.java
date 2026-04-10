package cn.iocoder.yudao.module.mes.service.wm.transfer;

import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.detail.MesWmTransferDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 调拨明细 Service 接口
 */
public interface MesWmTransferDetailService {

    /**
     * 创建调拨明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTransferDetail(@Valid MesWmTransferDetailSaveReqVO createReqVO);

    /**
     * 更新调拨明细
     *
     * @param updateReqVO 更新信息
     */
    void updateTransferDetail(@Valid MesWmTransferDetailSaveReqVO updateReqVO);

    /**
     * 删除调拨明细
     *
     * @param id 编号
     */
    void deleteTransferDetail(Long id);

    /**
     * 获得调拨明细
     *
     * @param id 编号
     * @return 调拨明细
     */
    MesWmTransferDetailDO getTransferDetail(Long id);

    /**
     * 根据行 ID 获取明细列表
     *
     * @param lineId 行 ID
     * @return 明细列表
     */
    List<MesWmTransferDetailDO> getTransferDetailListByLineId(Long lineId);

    /**
     * 根据转移单 ID 获取明细列表
     *
     * @param transferId 转移单 ID
     * @return 明细列表
     */
    List<MesWmTransferDetailDO> getTransferDetailListByTransferId(Long transferId);

    /**
     * 根据转移单 ID 删除明细
     *
     * @param transferId 转移单 ID
     */
    void deleteTransferDetailByTransferId(Long transferId);

    /**
     * 根据行 ID 删除明细
     *
     * @param lineId 行 ID
     */
    void deleteTransferDetailByLineId(Long lineId);

}
