package cn.iocoder.yudao.module.mes.service.qc.indicator;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo.MesQcIndicatorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo.MesQcIndicatorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 质检指标 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcIndicatorService {

    /**
     * 创建质检指标
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIndicator(@Valid MesQcIndicatorSaveReqVO createReqVO);

    /**
     * 更新质检指标
     *
     * @param updateReqVO 更新信息
     */
    void updateIndicator(@Valid MesQcIndicatorSaveReqVO updateReqVO);

    /**
     * 删除质检指标
     *
     * @param id 编号
     */
    void deleteIndicator(Long id);

    /**
     * 获得质检指标
     *
     * @param id 编号
     * @return 质检指标
     */
    MesQcIndicatorDO getIndicator(Long id);

    /**
     * 获得质检指标分页
     *
     * @param pageReqVO 分页查询
     * @return 质检指标分页
     */
    PageResult<MesQcIndicatorDO> getIndicatorPage(MesQcIndicatorPageReqVO pageReqVO);

    /**
     * 获得质检指标列表
     *
     * @return 质检指标列表
     */
    List<MesQcIndicatorDO> getIndicatorList();

    /**
     * 获得质检指标列表
     *
     * @param ids 编号数组
     * @return 质检指标列表
     */
    List<MesQcIndicatorDO> getIndicatorList(Collection<Long> ids);

    /**
     * 获得质检指标 Map
     *
     * @param ids 编号数组
     * @return 质检指标 Map
     */
    default Map<Long, MesQcIndicatorDO> getIndicatorMap(Collection<Long> ids) {
        return convertMap(getIndicatorList(ids), MesQcIndicatorDO::getId);
    }

    /**
     * 批量校验质检指标是否都存在
     *
     * @param ids 编号数组
     * @return 质检指标 Map
     */
    Map<Long, MesQcIndicatorDO> validateIndicatorListExists(Collection<Long> ids);

}
