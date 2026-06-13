package cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.MesWmOutsourceReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.MesWmOutsourceReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;

/**
 * MES 外协入库单 Service 接口
 */
public interface MesWmOutsourceReceiptService {

    /**
     * 创建外协入库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutsourceReceipt(@Valid MesWmOutsourceReceiptSaveReqVO createReqVO);

    /**
     * 修改外协入库单
     *
     * @param updateReqVO 修改信息
     */
    void updateOutsourceReceipt(@Valid MesWmOutsourceReceiptSaveReqVO updateReqVO);

    /**
     * 删除外协入库单（级联删除行+明细）
     *
     * @param id 编号
     */
    void deleteOutsourceReceipt(Long id);

    /**
     * 获得外协入库单
     *
     * @param id 编号
     * @return 外协入库单
     */
    MesWmOutsourceReceiptDO getOutsourceReceipt(Long id);

    /**
     * 获得外协入库单分页
     *
     * @param pageReqVO 分页参数
     * @return 外协入库单分页
     */
    PageResult<MesWmOutsourceReceiptDO> getOutsourceReceiptPage(MesWmOutsourceReceiptPageReqVO pageReqVO);

    /**
     * 提交外协入库单（草稿 → 审批中）
     *
     * @param id 编号
     */
    void submitOutsourceReceipt(Long id);

    /**
     * 执行上架（审批中 → 已审批）
     *
     * @param id 编号
     */
    void stockOutsourceReceipt(Long id);

    /**
     * 完成入库（已审批 → 已完成），更新库存台账
     *
     * @param id 编号
     */
    void finishOutsourceReceipt(Long id);

    /**
     * 取消外协入库单（任意非已完成/已取消状态 → 已取消）
     *
     * @param id 编号
     */
    void cancelOutsourceReceipt(Long id);

    /**
     * IQC 检验完成后回写外协入库单（拆分行 + 推进主表状态）
     *
     * @param receiptId           外协入库单编号
     * @param lineId              入库单行编号
     * @param iqcId               IQC 检验单编号
     * @param qualifiedQuantity   合格品数量
     * @param unqualifiedQuantity 不合格品数量
     */
    void updateOutsourceReceiptWhenIqcFinish(Long receiptId, Long lineId, Long iqcId,
                                             BigDecimal qualifiedQuantity, BigDecimal unqualifiedQuantity);

    /**
     * 校验外协入库单和行存在
     *
     * @param receiptId 入库单编号
     * @param lineId 行编号
     */
    void validateOutsourceReceiptAndLineExists(Long receiptId, Long lineId);

    /**
     * 根据供应商 ID 统计外协入库单数量
     *
     * @param vendorId 供应商 ID
     * @return 数量
     */
    Long getOutsourceReceiptCountByVendorId(Long vendorId);

}
