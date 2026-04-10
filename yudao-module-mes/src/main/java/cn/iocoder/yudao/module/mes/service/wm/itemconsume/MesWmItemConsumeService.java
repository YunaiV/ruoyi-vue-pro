package cn.iocoder.yudao.module.mes.service.wm.itemconsume;

import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDO;

/**
 * MES 物料消耗记录 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmItemConsumeService {

    /**
     * 根据报工单生成物料消耗记录
     *
     * 1. 查询当前工序的 BOM 物料配置
     * 2. 生成消耗单头 + 行（消耗数量 = BOM 用料比例 × 报工数量）
     *
     * @param feedback 报工记录
     * @return 生成的消耗记录，无 BOM 时返回 null
     */
    MesWmItemConsumeDO generateItemConsume(MesProFeedbackDO feedback);

    /**
     * 完成物料消耗（库存扣减）
     *
     * 遍历消耗明细（detail），按批次精确扣减线边库库存，更新消耗单状态为已完成
     *
     * @param consumeId 消耗记录编号
     */
    void finishItemConsume(Long consumeId);

    /**
     * 根据报工记录编号获取消耗记录
     *
     * @param feedbackId 报工记录编号
     * @return 消耗记录，不存在返回 null
     */
    MesWmItemConsumeDO getByFeedbackId(Long feedbackId);

}
