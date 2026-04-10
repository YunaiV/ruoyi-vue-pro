package cn.iocoder.yudao.module.mes.service.wm.stocktaking.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.MesWmStockTakingTaskPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.MesWmStockTakingTaskSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskDO;
import jakarta.validation.Valid;

/**
 * MES 盘点任务 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmStockTakingTaskService {

    /**
     * 创建盘点任务
     *
     * @param createReqVO 创建信息
     * @return 盘点任务编号
     */
    Long createStockTakingTask(@Valid MesWmStockTakingTaskSaveReqVO createReqVO);

    /**
     * 更新盘点任务
     *
     * @param updateReqVO 更新信息
     */
    void updateStockTakingTask(@Valid MesWmStockTakingTaskSaveReqVO updateReqVO);

    /**
     * 删除盘点任务
     *
     * @param id 盘点任务编号
     */
    void deleteStockTakingTask(Long id);

    /**
     * 提交盘点任务
     *
     * @param id 盘点任务编号
     */
    void submitStockTakingTask(Long id);

    /**
     * 完成盘点任务
     *
     * @param id 盘点任务编号
     */
    void finishStockTakingTask(Long id);

    /**
     * 取消盘点任务
     *
     * @param id 盘点任务编号
     */
    void cancelStockTakingTask(Long id);

    /**
     * 获得盘点任务
     *
     * @param id 盘点任务编号
     * @return 盘点任务
     */
    MesWmStockTakingTaskDO getStockTakingTask(Long id);

    /**
     * 校验盘点任务是否存在
     *
     * @param id 盘点任务编号
     * @return 盘点任务
     */
    MesWmStockTakingTaskDO validateStockTakingTaskExists(Long id);

    /**
     * 校验盘点任务存在，并且处于【准备中】状态
     *
     * @param id 盘点任务编号
     * @return 盘点任务
     */
    MesWmStockTakingTaskDO validateStockTakingTaskExistsAndPrepare(Long id);

    /**
     * 校验盘点任务存在，并且处于【盘点中】状态
     *
     * @param id 盘点任务编号
     * @return 盘点任务
     */
    MesWmStockTakingTaskDO validateStockTakingTaskExistsAndApproving(Long id);

    /**
     * 分页查询盘点任务
     *
     * @param pageReqVO 分页查询条件
     * @return 盘点任务分页结果
     */
    PageResult<MesWmStockTakingTaskDO> getStockTakingTaskPage(MesWmStockTakingTaskPageReqVO pageReqVO);

}
