package cn.iocoder.yudao.module.mes.service.wm.miscreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.MesWmMiscReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.MesWmMiscReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptDO;
import jakarta.validation.Valid;

/**
 * MES 杂项入库单 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmMiscReceiptService {

    /**
     * 创建杂项入库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMiscReceipt(@Valid MesWmMiscReceiptSaveReqVO createReqVO);

    /**
     * 修改杂项入库单
     *
     * @param updateReqVO 修改信息
     */
    void updateMiscReceipt(@Valid MesWmMiscReceiptSaveReqVO updateReqVO);

    /**
     * 删除杂项入库单（级联删除行）
     *
     * @param id 编号
     */
    void deleteMiscReceipt(Long id);

    /**
     * 获得杂项入库单
     *
     * @param id 编号
     * @return 杂项入库单
     */
    MesWmMiscReceiptDO getMiscReceipt(Long id);

    /**
     * 获得杂项入库单分页
     *
     * @param pageReqVO 分页参数
     * @return 杂项入库单分页
     */
    PageResult<MesWmMiscReceiptDO> getMiscReceiptPage(MesWmMiscReceiptPageReqVO pageReqVO);

    /**
     * 提交审批（草稿 → 已审批）
     *
     * @param id 编号
     */
    void submitMiscReceipt(Long id);

    /**
     * 执行入库（已审批 → 已完成），更新库存台账
     *
     * @param id 编号
     */
    void finishMiscReceipt(Long id);

    /**
     * 取消杂项入库单（草稿/已审批 → 已取消）
     *
     * @param id 编号
     */
    void cancelMiscReceipt(Long id);

    /**
     * 校验杂项入库单存在
     *
     * @param id 编号
     * @return 杂项入库单
     */
    MesWmMiscReceiptDO validateMiscReceiptExists(Long id);

    /**
     * 校验杂项入库单是否可编辑（存在且为草稿状态）
     *
     * @param id 编号
     */
    void validateMiscReceiptEditable(Long id);

}
