package cn.iocoder.yudao.module.mes.service.wm.productproduce;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productproduce.vo.MesWmProductProduceLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceLineDO;

import java.util.List;

/**
 * MES 生产入库单行 Service 接口
 */
public interface MesWmProductProduceLineService {

    /**
     * 创建生产入库单行（内部使用）
     *
     * @param line 行数据
     */
    void createProductProduceLine(MesWmProductProduceLineDO line);

    /**
     * 更新生产入库单行（内部使用）
     *
     * @param line 行数据
     */
    void updateProductProduceLine(MesWmProductProduceLineDO line);

    /**
     * 根据入库单 ID 获取行列表
     *
     * @param produceId 入库单 ID
     * @return 行列表
     */
    List<MesWmProductProduceLineDO> getProductProduceLineListByProduceId(Long produceId);

    /**
     * 根据报工记录 ID 获取行列表
     *
     * @param feedbackId 报工记录 ID
     * @return 行列表
     */
    List<MesWmProductProduceLineDO> getProductProduceLineListByFeedbackId(Long feedbackId);

    /**
     * 获得生产入库单行分页
     *
     * @param pageReqVO 分页查询参数
     * @return 行分页列表
     */
    PageResult<MesWmProductProduceLineDO> getProductProduceLinePage(MesWmProductProduceLinePageReqVO pageReqVO);

}
