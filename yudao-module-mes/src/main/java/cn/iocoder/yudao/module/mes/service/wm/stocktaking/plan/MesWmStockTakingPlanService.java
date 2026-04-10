package cn.iocoder.yudao.module.mes.service.wm.stocktaking.plan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.MesWmStockTakingPlanPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.plan.vo.MesWmStockTakingPlanSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan.MesWmStockTakingPlanDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 盘点方案 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmStockTakingPlanService {

    /**
     * 创建盘点方案
     *
     * @param createReqVO 创建信息
     * @return 盘点方案编号
     */
    Long createStockTakingPlan(@Valid MesWmStockTakingPlanSaveReqVO createReqVO);

    /**
     * 更新盘点方案
     *
     * @param updateReqVO 更新信息
     */
    void updateStockTakingPlan(@Valid MesWmStockTakingPlanSaveReqVO updateReqVO);

    /**
     * 删除盘点方案
     *
     * @param id 盘点方案编号
     */
    void deleteStockTakingPlan(Long id);

    /**
     * 更新盘点方案状态。
     *
     * @param id 盘点方案编号
     * @param status 状态
     */
    void updateStockTakingPlanStatus(Long id, Integer status);

    /**
     * 获得盘点方案
     *
     * @param id 盘点方案编号
     * @return 盘点方案
     */
    MesWmStockTakingPlanDO getStockTakingPlan(Long id);

    /**
     * 校验盘点方案是否存在
     *
     * @param id 盘点方案编号
     * @return 盘点方案
     */
    MesWmStockTakingPlanDO validateStockTakingPlanExists(Long id);

    /**
     * 校验盘点方案是否可编辑
     *
     * @param id 盘点方案编号
     * @return 盘点方案
     */
    MesWmStockTakingPlanDO validateStockTakingPlanEditable(Long id);

    /**
     * 校验盘点方案存在且已启用
     *
     * @param id 盘点方案编号
     * @return 盘点方案
     */
    MesWmStockTakingPlanDO validateStockTakingPlanEnabled(Long id);

    /**
     * 获得盘点方案列表。
     *
     * @param ids 盘点方案编号集合
     * @return 盘点方案列表
     */
    List<MesWmStockTakingPlanDO> getStockTakingPlanList(Collection<Long> ids);

    /**
     * 获得盘点方案 Map
     *
     * @param ids 盘点方案编号集合
     * @return 盘点方案 Map
     */
    default Map<Long, MesWmStockTakingPlanDO> getStockTakingPlanMap(Collection<Long> ids) {
        return convertMap(getStockTakingPlanList(ids), MesWmStockTakingPlanDO::getId);
    }

    /**
     * 分页查询盘点方案
     *
     * @param pageReqVO 分页查询条件
     * @return 盘点方案分页结果
     */
    PageResult<MesWmStockTakingPlanDO> getStockTakingPlanPage(MesWmStockTakingPlanPageReqVO pageReqVO);

    /**
     * 根据状态获得盘点方案列表。
     *
     * @param status 状态
     * @return 盘点方案列表
     */
    List<MesWmStockTakingPlanDO> getStockTakingPlanListByStatus(Integer status);

}
