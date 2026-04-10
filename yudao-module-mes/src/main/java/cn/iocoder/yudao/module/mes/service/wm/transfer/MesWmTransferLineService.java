package cn.iocoder.yudao.module.mes.service.wm.transfer;

import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.line.MesWmTransferLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 转移单行 Service 接口
 */
public interface MesWmTransferLineService {

    /**
     * 创建转移单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTransferLine(@Valid MesWmTransferLineSaveReqVO createReqVO);

    /**
     * 更新转移单行
     *
     * @param updateReqVO 更新信息
     */
    void updateTransferLine(@Valid MesWmTransferLineSaveReqVO updateReqVO);

    /**
     * 删除转移单行
     *
     * @param id 编号
     */
    void deleteTransferLine(Long id);

    /**
     * 获得转移单行
     *
     * @param id 编号
     * @return 转移单行
     */
    MesWmTransferLineDO getTransferLine(Long id);

    /**
     * 根据转移单 ID 获取行列表
     *
     * @param transferId 转移单 ID
     * @return 行列表
     */
    List<MesWmTransferLineDO> getTransferLineListByTransferId(Long transferId);

    /**
     * 根据转移单 ID 删除行
     *
     * @param transferId 转移单 ID
     */
    void deleteTransferLineByTransferId(Long transferId);

    /**
     * 校验转移单行是否存在
     *
     * @param id 编号
     * @return 转移单行
     */
    MesWmTransferLineDO validateTransferLineExists(Long id);

}
