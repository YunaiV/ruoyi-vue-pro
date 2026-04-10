package cn.iocoder.yudao.module.mes.service.wm.productproduce;

import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDetailDO;

import java.util.List;

/**
 * MES 生产入库明细 Service 接口
 */
public interface MesWmProductProduceDetailService {

    /**
     * 创建生产入库明细（内部使用）
     *
     * @param detail 明细数据
     */
    void createProductProduceDetail(MesWmProductProduceDetailDO detail);

    /**
     * 根据行ID获取明细列表
     *
     * @param lineId 行ID
     * @return 明细列表
     */
    List<MesWmProductProduceDetailDO> getProductProduceDetailListByLineId(Long lineId);

}
