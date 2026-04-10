package cn.iocoder.yudao.module.mes.service.qc.indicatorresult;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo.MesQcIndicatorResultPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo.MesQcIndicatorResultSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 检验结果 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcIndicatorResultService {

    /**
     * 创建检验结果（含明细）
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIndicatorResult(@Valid MesQcIndicatorResultSaveReqVO createReqVO);

    /**
     * 更新检验结果（含明细）
     *
     * @param updateReqVO 更新信息
     */
    void updateIndicatorResult(@Valid MesQcIndicatorResultSaveReqVO updateReqVO);

    /**
     * 删除检验结果（级联删除明细）
     *
     * @param id 编号
     */
    void deleteIndicatorResult(Long id);

    /**
     * 获得检验结果
     *
     * @param id 编号
     * @return 检验结果
     */
    MesQcIndicatorResultDO getIndicatorResult(Long id);

    /**
     * 获得检验结果分页
     *
     * @param pageReqVO 分页查询
     * @return 检验结果分页
     */
    PageResult<MesQcIndicatorResultDO> getIndicatorResultPage(MesQcIndicatorResultPageReqVO pageReqVO);

    /**
     * 获取检验结果明细列表
     *
     * @param resultId 检验结果 ID
     * @return 明细列表
     */
    List<MesQcIndicatorResultDetailDO> getIndicatorResultDetailListByResultId(Long resultId);

}
