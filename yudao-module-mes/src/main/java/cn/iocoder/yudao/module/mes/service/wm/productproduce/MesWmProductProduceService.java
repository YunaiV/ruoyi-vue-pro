package cn.iocoder.yudao.module.mes.service.wm.productproduce;

import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDO;

import java.math.BigDecimal;

/**
 * MES 生产入库单 Service 接口
 */
public interface MesWmProductProduceService {

    /**
     * 校验生产入库单是否存在
     *
     * @param id 编号
     * @return 生产入库单
     */
    MesWmProductProduceDO validateProductProduceExists(Long id);

    /**
     * 完成生产入库单（草稿 → 已完成）
     *
     * @param id 编号
     */
    void finishProductProduce(Long id);

    /**
     * 根据报工记录，自动生成产品产出单（头 + 行 + 明细）
     *
     * @param feedback  报工记录
     * @param checkFlag 是否需要检验（true=待检验，false=按合格/不合格拆分行）
     * @return 生成的产品产出单
     */
    MesWmProductProduceDO generateProductProduce(MesProFeedbackDO feedback, boolean checkFlag);

    /**
     * IPQC 检验完成回调：将待检产出拆分为合格/不合格行、生成明细、完成入库
     *
     * <p>调用场景：IPQC 完成时，需要将之前 {@link #generateProductProduce} 生成的
     * 待检产出（{@code checkFlag=true}）按检验结果拆分行，并执行入库。
     *
     * @param feedbackId      报工记录 ID
     * @param qualifiedQty    合格品数量
     * @param unqualifiedQty  不合格品数量
     */
    void splitPendingAndFinishProduce(Long feedbackId, BigDecimal qualifiedQty, BigDecimal unqualifiedQty);

}
