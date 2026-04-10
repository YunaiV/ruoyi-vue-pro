package cn.iocoder.yudao.module.mes.service.wm.stocktaking.task;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line.MesWmStockTakingTaskLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line.MesWmStockTakingTaskLineSaveReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskLineDO;

import java.math.BigDecimal;
import java.util.List;

/**
 * MES 盘点任务行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmStockTakingTaskLineService {

    /**
     * 根据盘点方案生成盘点明细行
     *
     * @param task 盘点任务
     * @param isCreate 是否为创建操作（true-创建，false-更新时需先清理旧数据）
     */
    void generateStockTakingLines(MesWmStockTakingTaskDO task, boolean isCreate);

    /**
     * 分页查询盘点任务行
     *
     * @param pageReqVO 分页查询条件
     * @return 盘点任务行分页结果
     */
    PageResult<MesWmStockTakingTaskLineDO> getStockTakingTaskLinePage(MesWmStockTakingTaskLinePageReqVO pageReqVO);

    /**
     * 获得盘点任务行
     *
     * @param id 编号
     * @return 盘点任务行
     */
    MesWmStockTakingTaskLineDO getStockTakingTaskLine(Long id);

    /**
     * 创建盘点任务行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createStockTakingTaskLine(MesWmStockTakingTaskLineSaveReqVO createReqVO);

    /**
     * 更新盘点任务行
     *
     * @param updateReqVO 更新信息
     */
    void updateStockTakingTaskLine(MesWmStockTakingTaskLineSaveReqVO updateReqVO);

    /**
     * 删除盘点任务行
     *
     * @param id 盘点任务行编号
     */
    void deleteStockTakingTaskLine(Long id);

    /**
     * 根据任务编号获取盘点任务行列表
     *
     * @param taskId 任务编号
     * @return 盘点任务行列表
     */
    List<MesWmStockTakingTaskLineDO> getStockTakingTaskLineListByTaskId(Long taskId);

    /**
     * 根据任务编号删除盘点任务行
     *
     * @param taskId 任务编号
     */
    void deleteStockTakingTaskLineByTaskId(Long taskId);

    /**
     * 从盘点结果创建盘点任务行
     *
     * 注意：当盘点结果没有对应的盘点清单行时，会自动创建一个新的盘点任务行
     *
     * @param createReqVO 盘点结果创建信息
     * @return 盘点任务行编号
     */
    Long createStockTakingTaskLine(MesWmStockTakingTaskResultSaveReqVO createReqVO);

    /**
     * 根据 taskId + itemId + areaId 查询盘点任务行
     *
     * @param taskId 盘点任务编号
     * @param itemId 物料编号
     * @param areaId 库位编号
     * @return 盘点任务行
     */
    MesWmStockTakingTaskLineDO getStockTakingTaskLine(Long taskId, Long itemId, Long areaId);

    /**
     * 校验盘点任务行存在
     *
     * @param id 盘点任务行编号
     * @return 盘点任务行
     */
    MesWmStockTakingTaskLineDO validateStockTakingTaskLineExists(Long id);

    /**
     * 更新盘点任务行的盘点数量
     *
     * @param id 盘点任务行编号
     * @param takingQuantity 盘点数量（增量）
     */
    void updateStockTakingTaskLineTakingQuantity(Long id, BigDecimal takingQuantity);

}
