package cn.iocoder.yudao.module.mes.service.wm.transfer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.MesWmTransferPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.transfer.vo.MesWmTransferSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transfer.MesWmTransferDO;
import jakarta.validation.Valid;

/**
 * MES 转移单 Service 接口
 */
public interface MesWmTransferService {

    /**
     * 创建转移单
     */
    Long createTransfer(@Valid MesWmTransferSaveReqVO createReqVO);

    /**
     * 修改转移单
     */
    void updateTransfer(@Valid MesWmTransferSaveReqVO updateReqVO);

    /**
     * 删除转移单
     */
    void deleteTransfer(Long id);

    /**
     * 获得转移单
     */
    MesWmTransferDO getTransfer(Long id);

    /**
     * 获得转移单分页
     */
    PageResult<MesWmTransferDO> getTransferPage(MesWmTransferPageReqVO pageReqVO);

    /**
     * 提交转移单（草稿 → 待确认/待上架）
     */
    void submitTransfer(Long id);

    /**
     * 确认转移单（待确认 → 待上架）
     */
    void confirmTransfer(Long id);

    /**
     * 执行上架（待上架 → 待执行）
     */
    void stockTransfer(Long id);

    /**
     * 完成转移单（待执行 → 已完成）
     */
    void finishTransfer(Long id);

    /**
     * 取消转移单
     */
    void cancelTransfer(Long id);

    /**
     * 校验转移单存在且处于可编辑状态
     */
    MesWmTransferDO validateTransferEditable(Long id);

}
