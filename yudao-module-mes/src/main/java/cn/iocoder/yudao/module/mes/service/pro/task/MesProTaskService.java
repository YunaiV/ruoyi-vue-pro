package cn.iocoder.yudao.module.mes.service.pro.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.task.vo.MesProTaskSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.task.MesProTaskDO;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 生产任务 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProTaskService {

    /**
     * 创建生产任务
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTask(@Valid MesProTaskSaveReqVO createReqVO);

    /**
     * 更新生产任务
     *
     * @param updateReqVO 更新信息
     */
    void updateTask(@Valid MesProTaskSaveReqVO updateReqVO);

    /**
     * 删除生产任务
     *
     * @param id 编号
     */
    void deleteTask(Long id);

    /**
     * 获得生产任务
     *
     * @param id 编号
     * @return 生产任务
     */
    MesProTaskDO getTask(Long id);

    /**
     * 获得生产任务分页
     *
     * @param pageReqVO 分页查询
     * @return 生产任务分页
     */
    PageResult<MesProTaskDO> getTaskPage(MesProTaskPageReqVO pageReqVO);

    /**
     * 获得生产任务精简列表
     *
     * @param workOrderId 工单编号（可选）
     * @return 生产任务列表
     */
    List<MesProTaskDO> getTaskListByWorkOrderId(Long workOrderId);

    /**
     * 根据工单编号列表，批量获得生产任务列表
     *
     * @param workOrderIds 工单编号列表
     * @return 生产任务列表
     */
    List<MesProTaskDO> getTaskListByWorkOrderIds(Collection<Long> workOrderIds);

    /**
     * 校验任务是否存在
     *
     * @param id 编号
     * @return 生产任务
     */
    MesProTaskDO validateTaskExists(Long id);

    /**
     * 校验任务存在且未完成（非终态）
     *
     * @param id 编号
     * @return 生产任务
     */
    MesProTaskDO validateTaskNotFinished(Long id);

    /**
     * 获得生产任务列表
     *
     * @param ids 编号列表
     * @return 生产任务列表
     */
    List<MesProTaskDO> getTaskList(Collection<Long> ids);

    /**
     * 获得生产任务 Map
     *
     * @param ids 编号列表
     * @return 生产任务 Map
     */
    default Map<Long, MesProTaskDO> getTaskMap(Collection<Long> ids) {
        return convertMap(getTaskList(ids), MesProTaskDO::getId);
    }

    /**
     * 根据工单编号，完成所有关联任务
     *
     * @param workOrderId 工单编号
     */
    void finishTaskByOrderId(Long workOrderId);

    /**
     * 根据工单编号，取消所有关联任务
     *
     * @param workOrderId 工单编号
     */
    void cancelTaskByOrderId(Long workOrderId);

    /**
     * 累加任务的已生产、合格、不合格数量
     *
     * @param id                    任务编号
     * @param incrProducedQuantity  本次已生产数量增量（不良品也计入）
     * @param incrQualifyQuantity   本次合格品数量增量
     * @param incrUnqualifyQuantity 本次不合格品数量增量
     */
    void updateProducedQuantity(Long id,
                                BigDecimal incrProducedQuantity,
                                BigDecimal incrQualifyQuantity,
                                BigDecimal incrUnqualifyQuantity);

}
