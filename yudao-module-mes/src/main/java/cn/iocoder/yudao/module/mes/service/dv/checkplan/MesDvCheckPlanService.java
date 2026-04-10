package cn.iocoder.yudao.module.mes.service.dv.checkplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.MesDvCheckPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 点检保养方案 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvCheckPlanService {

    /**
     * 创建点检保养方案
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCheckPlan(@Valid MesDvCheckPlanSaveReqVO createReqVO);

    /**
     * 更新点检保养方案
     *
     * @param updateReqVO 更新信息
     */
    void updateCheckPlan(@Valid MesDvCheckPlanSaveReqVO updateReqVO);

    /**
     * 启用点检保养方案
     *
     * @param id 编号
     */
    void enableCheckPlan(Long id);

    /**
     * 停用点检保养方案
     *
     * @param id 编号
     */
    void disableCheckPlan(Long id);

    /**
     * 删除点检保养方案
     *
     * @param id 编号
     */
    void deleteCheckPlan(Long id);

    /**
     * 校验点检保养方案存在
     *
     * @param id 编号
     */
    void validateCheckPlanExists(Long id);

    /**
     * 校验点检保养方案为草稿状态
     *
     * @param planId 方案编号
     * @return 方案
     */
    MesDvCheckPlanDO validateCheckPlanPrepare(Long planId);

    /**
     * 获得点检保养方案
     *
     * @param id 编号
     * @return 方案
     */
    MesDvCheckPlanDO getCheckPlan(Long id);

    /**
     * 获得点检保养方案分页
     *
     * @param pageReqVO 分页查询
     * @return 方案分页
     */
    PageResult<MesDvCheckPlanDO> getCheckPlanPage(MesDvCheckPlanPageReqVO pageReqVO);

    /**
     * 获得点检保养方案列表
     *
     * @param ids 编号数组
     * @return 方案列表
     */
    List<MesDvCheckPlanDO> getCheckPlanList(Collection<Long> ids);

    /**
     * 获得点检保养方案 Map
     *
     * @param ids 编号数组
     * @return 方案 Map
     */
    default Map<Long, MesDvCheckPlanDO> getCheckPlanMap(Collection<Long> ids) {
        return convertMap(getCheckPlanList(ids), MesDvCheckPlanDO::getId);
    }

}
