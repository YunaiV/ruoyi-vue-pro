package cn.iocoder.yudao.module.mes.service.qc.indicatorresult;

import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDetailDO;

import java.util.List;

/**
 * MES 检验结果明细 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcIndicatorResultDetailService {

    /**
     * 批量创建检验结果明细
     *
     * @param details 明细列表
     */
    void createDetailList(List<MesQcIndicatorResultDetailDO> details);

    /**
     * 批量新增或更新检验结果明细
     *
     * @param details 明细列表
     */
    void createOrUpdateDetailList(List<MesQcIndicatorResultDetailDO> details);

    /**
     * 根据检验结果 ID 获取明细列表
     *
     * @param resultId 检验结果 ID
     * @return 明细列表
     */
    List<MesQcIndicatorResultDetailDO> getDetailListByResultId(Long resultId);

    /**
     * 根据检验结果 ID 删除所有明细
     *
     * @param resultId 检验结果 ID
     */
    void deleteDetailByResultId(Long resultId);

}
