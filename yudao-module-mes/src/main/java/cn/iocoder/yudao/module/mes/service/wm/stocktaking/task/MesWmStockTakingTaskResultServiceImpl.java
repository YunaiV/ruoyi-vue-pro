package cn.iocoder.yudao.module.mes.service.wm.stocktaking.task;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskResultDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.stocktaking.task.MesWmStockTakingTaskResultMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_STOCK_TAKING_TASK_LINE_ALREADY_TAKEN;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_STOCK_TAKING_TASK_RESULT_NOT_EXISTS;

/**
 * MES 盘点结果 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmStockTakingTaskResultServiceImpl implements MesWmStockTakingTaskResultService {

    @Resource
    private MesWmStockTakingTaskResultMapper stockTakingTaskResultMapper;

    @Resource
    private MesWmStockTakingTaskLineService stockTakingTaskLineService;
    @Resource
    private MesWmStockTakingTaskService stockTakingTaskService;

    @Override
    public PageResult<MesWmStockTakingTaskResultDO> getStockTakingTaskResultPage(
            MesWmStockTakingTaskResultPageReqVO pageReqVO) {
        return stockTakingTaskResultMapper.selectPage(pageReqVO);
    }

    @Override
    public MesWmStockTakingTaskResultDO getStockTakingTaskResult(Long id) {
        return stockTakingTaskResultMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockTakingTaskResult(MesWmStockTakingTaskResultSaveReqVO createReqVO) {
        // 1. 校验任务处于盘点中状态
        stockTakingTaskService.validateStockTakingTaskExistsAndApproving(createReqVO.getTaskId());

        // 2. 确定 lineId 和账面数量
        Long lineId = createReqVO.getLineId();
        MesWmStockTakingTaskLineDO existingLine = null;
        if (lineId == null) {
            // 2.1 基于 taskId + itemId + areaId 查询已存在的 line
            existingLine = stockTakingTaskLineService.getStockTakingTaskLine(
                    createReqVO.getTaskId(), createReqVO.getItemId(), createReqVO.getAreaId());
            if (existingLine != null) {
                lineId = existingLine.getId();
            } else {
                // 2.2 创建新的 line（注意：当盘点结果没有对应的盘点清单行时，会自动创建一个新的盘点任务行）
                lineId = stockTakingTaskLineService.createStockTakingTaskLine(createReqVO);
            }
        } else {
            // 2.3 如果传递了 lineId，从 line 中获取 quantity
            existingLine = stockTakingTaskLineService.validateStockTakingTaskLineExists(lineId);
        }
        if (existingLine != null
                && existingLine.getTakingQuantity() != null
                && existingLine.getTakingQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw exception(WM_STOCK_TAKING_TASK_LINE_ALREADY_TAKEN);
        }
        BigDecimal quantity = existingLine != null ? existingLine.getQuantity() : BigDecimal.ZERO; // 账面数量

        // 3. 插入 result 记录
        MesWmStockTakingTaskResultDO result = BeanUtils.toBean(createReqVO, MesWmStockTakingTaskResultDO.class)
                .setLineId(lineId).setQuantity(ObjUtil.defaultIfNull(quantity, createReqVO.getTakingQuantity()));
        stockTakingTaskResultMapper.insert(result);

        // 4. 更新 line 的盘点数量和状态
        stockTakingTaskLineService.updateStockTakingTaskLineTakingQuantity(lineId, createReqVO.getTakingQuantity());
        return result.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockTakingTaskResult(MesWmStockTakingTaskResultSaveReqVO updateReqVO) {
        updateReqVO.setLineId(null); // lineId 不能修改，避免误操作
        // 1.1 校验记录存在
        MesWmStockTakingTaskResultDO oldResult = validateStockTakingTaskResultExists(updateReqVO.getId());
        // 1.2 校验任务处于盘点中状态
        stockTakingTaskService.validateStockTakingTaskExistsAndApproving(oldResult.getTaskId());

        // 2. 更新 result 记录
        MesWmStockTakingTaskResultDO result = BeanUtils.toBean(updateReqVO, MesWmStockTakingTaskResultDO.class);
        stockTakingTaskResultMapper.updateById(result);

        // 3. 关联更新 line 的盘点数量（覆盖式更新）
        stockTakingTaskLineService.updateStockTakingTaskLineTakingQuantity(
                oldResult.getLineId(), updateReqVO.getTakingQuantity());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockTakingTaskResult(Long id) {
        // 1.1 校验 result 是否存在
        MesWmStockTakingTaskResultDO result = validateStockTakingTaskResultExists(id);
        // 1.2 校验 task 是否处于 APPROVING 状态
        stockTakingTaskService.validateStockTakingTaskExistsAndApproving(result.getTaskId());

        // 2. 删除 result 记录
        stockTakingTaskResultMapper.deleteById(id);

        // 3. 更新对应 line 的盘点数量和状态
        stockTakingTaskLineService.updateStockTakingTaskLineTakingQuantity(
                result.getLineId(), result.getTakingQuantity().negate());
    }

    @Override
    public void deleteStockTakingTaskResultByTaskId(Long taskId) {
        stockTakingTaskResultMapper.deleteByTaskId(taskId);
    }

    /**
     * 校验盘点结果是否存在
     *
     * @param id 盘点结果编号
     * @return 盘点结果
     */
    private MesWmStockTakingTaskResultDO validateStockTakingTaskResultExists(Long id) {
        MesWmStockTakingTaskResultDO result = stockTakingTaskResultMapper.selectById(id);
        if (result == null) {
            throw exception(WM_STOCK_TAKING_TASK_RESULT_NOT_EXISTS);
        }
        return result;
    }

}
