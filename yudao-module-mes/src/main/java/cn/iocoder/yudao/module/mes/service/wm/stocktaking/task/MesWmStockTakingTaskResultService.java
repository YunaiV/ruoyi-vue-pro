package cn.iocoder.yudao.module.mes.service.wm.stocktaking.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskResultDO;

/**
 * MES 盘点结果 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmStockTakingTaskResultService {

    /**
     * 分页查询盘点结果
     *
     * @param pageReqVO 分页查询条件
     * @return 盘点结果分页结果
     */
    PageResult<MesWmStockTakingTaskResultDO> getStockTakingTaskResultPage(MesWmStockTakingTaskResultPageReqVO pageReqVO);

    /**
     * 获得盘点结果
     *
     * @param id 盘点结果编号
     * @return 盘点结果
     */
    MesWmStockTakingTaskResultDO getStockTakingTaskResult(Long id);

    /**
     * 创建盘点结果
     *
     * @param result 盘点结果
     * @return 盘点结果编号
     */
    Long createStockTakingTaskResult(MesWmStockTakingTaskResultSaveReqVO result);

    /**
     * 更新盘点结果
     *
     * @param result 盘点结果
     */
    void updateStockTakingTaskResult(MesWmStockTakingTaskResultSaveReqVO result);

    /**
     * 删除盘点结果
     *
     * @param id 盘点结果编号
     */
    void deleteStockTakingTaskResult(Long id);

    /**
     * 根据任务编号删除盘点结果
     *
     * @param taskId 任务编号
     */
    void deleteStockTakingTaskResultByTaskId(Long taskId);

}
