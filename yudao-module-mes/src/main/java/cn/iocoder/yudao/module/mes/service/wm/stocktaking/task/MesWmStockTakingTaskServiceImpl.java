package cn.iocoder.yudao.module.mes.service.wm.stocktaking.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.MesWmStockTakingTaskPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.MesWmStockTakingTaskSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.stocktaking.task.MesWmStockTakingTaskMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingTaskStatusEnum;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.stocktaking.plan.MesWmStockTakingPlanService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 盘点任务 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmStockTakingTaskServiceImpl implements MesWmStockTakingTaskService {

    @Resource
    private MesWmStockTakingTaskMapper stockTakingTaskMapper;

    @Resource
    private MesWmMaterialStockService materialStockService;
    @Resource
    private MesWmStockTakingPlanService stockTakingPlanService;
    @Lazy
    @Resource
    private MesWmStockTakingTaskLineService stockTakingTaskLineService;
    @Lazy
    @Resource
    private MesWmStockTakingTaskResultService stockTakingTaskResultService;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockTakingTask(MesWmStockTakingTaskSaveReqVO createReqVO) {
        // 1.1 校验 code 唯一
        validateStockTakingTaskCodeUnique(null, createReqVO.getCode());
        // 1.2 校验方案存在
        adminUserApi.validateUser(createReqVO.getUserId());
        // 1.3 校验方案可用（如果有 planId）
        if (createReqVO.getPlanId() != null) {
            stockTakingPlanService.validateStockTakingPlanEnabled(createReqVO.getPlanId());
        }

        // 2. 插入任务
        MesWmStockTakingTaskDO task = BeanUtils.toBean(createReqVO, MesWmStockTakingTaskDO.class)
                .setStatus(MesWmStockTakingTaskStatusEnum.PREPARE.getStatus());
        stockTakingTaskMapper.insert(task);

        // 3. 根据方案生成盘点明细行（仅当有 plan 时）
        if (task.getPlanId() != null) {
            stockTakingTaskLineService.generateStockTakingLines(task, true);
        }
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockTakingTask(MesWmStockTakingTaskSaveReqVO updateReqVO) {
        // 1.1 校验任务存在且为草稿状态
        validateStockTakingTaskExistsAndPrepare(updateReqVO.getId());
        // 1.2 校验 code 唯一
        validateStockTakingTaskCodeUnique(updateReqVO.getId(), updateReqVO.getCode());
        // 1.3 校验方案存在
        adminUserApi.validateUser(updateReqVO.getUserId());
        // 1.4 校验方案可用（如果有 planId）
        if (updateReqVO.getPlanId() != null) {
            stockTakingPlanService.validateStockTakingPlanEnabled(updateReqVO.getPlanId());
        }

        // 2. 更新任务
        MesWmStockTakingTaskDO updateObj = BeanUtils.toBean(updateReqVO, MesWmStockTakingTaskDO.class);
        stockTakingTaskMapper.updateById(updateObj);

        // 3. 重新生成盘点明细行（仅当有 plan 时）
        if (updateObj.getPlanId() != null) {
            stockTakingTaskLineService.generateStockTakingLines(updateObj, false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockTakingTask(Long id) {
        // 1. 校验任务存在且为草稿状态
        validateStockTakingTaskExistsAndPrepare(id);

        // 2. 删除任务和明细
        stockTakingTaskResultService.deleteStockTakingTaskResultByTaskId(id);
        stockTakingTaskLineService.deleteStockTakingTaskLineByTaskId(id);
        stockTakingTaskMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitStockTakingTask(Long id) {
        // 1.1 校验任务存在且为草稿状态
        MesWmStockTakingTaskDO task = validateStockTakingTaskExistsAndPrepare(id);
        // 1.2 检查要盘点的内容
        List<MesWmStockTakingTaskLineDO> lines = stockTakingTaskLineService.getStockTakingTaskLineListByTaskId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_STOCK_TAKING_TASK_NO_LINE);
        }

        // 2. 更新任务状态为审批中
        stockTakingTaskMapper.updateById(new MesWmStockTakingTaskDO().setId(id)
                .setStatus(MesWmStockTakingTaskStatusEnum.APPROVING.getStatus()));

        // 3. 根据冻结标识，对物资进行冻结
        if (Boolean.TRUE.equals(task.getFrozen())) {
            updateMaterialStockFrozen(lines, true);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishStockTakingTask(Long id) {
        // 1. 校验任务存在且为审批中状态
        MesWmStockTakingTaskDO task = validateStockTakingTaskExistsAndApproving(id);

        // 2. 更新任务状态为已完成
        stockTakingTaskMapper.updateById(new MesWmStockTakingTaskDO().setId(id)
                .setStatus(MesWmStockTakingTaskStatusEnum.FINISHED.getStatus()));

        // 3. 解冻库存
        if (Boolean.TRUE.equals(task.getFrozen())) {
            List<MesWmStockTakingTaskLineDO> lines = stockTakingTaskLineService.getStockTakingTaskLineListByTaskId(id);
            updateMaterialStockFrozen(lines, false);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelStockTakingTask(Long id) {
        MesWmStockTakingTaskDO task = validateStockTakingTaskExists(id);
        if (MesWmStockTakingTaskStatusEnum.FINISHED.getStatus().equals(task.getStatus())
                || MesWmStockTakingTaskStatusEnum.CANCELED.getStatus().equals(task.getStatus())) {
            throw exception(WM_STOCK_TAKING_TASK_CANNOT_CANCEL);
        }
        task.setStatus(MesWmStockTakingTaskStatusEnum.CANCELED.getStatus());
        stockTakingTaskMapper.updateById(task);
        if (Boolean.TRUE.equals(task.getFrozen())) {
            updateMaterialStockFrozen(stockTakingTaskLineService.getStockTakingTaskLineListByTaskId(id), false);
        }
    }

    @Override
    public MesWmStockTakingTaskDO getStockTakingTask(Long id) {
        return stockTakingTaskMapper.selectById(id);
    }

    @Override
    public MesWmStockTakingTaskDO validateStockTakingTaskExists(Long id) {
        MesWmStockTakingTaskDO task = stockTakingTaskMapper.selectById(id);
        if (task == null) {
            throw exception(WM_STOCK_TAKING_TASK_NOT_EXISTS);
        }
        return task;
    }

    @Override
    public MesWmStockTakingTaskDO validateStockTakingTaskExistsAndPrepare(Long id) {
        MesWmStockTakingTaskDO task = validateStockTakingTaskExists(id);
        if (ObjUtil.notEqual(MesWmStockTakingTaskStatusEnum.PREPARE.getStatus(), task.getStatus())) {
            throw exception(WM_STOCK_TAKING_TASK_NOT_PREPARE);
        }
        return task;
    }

    @Override
    public MesWmStockTakingTaskDO validateStockTakingTaskExistsAndApproving(Long id) {
        MesWmStockTakingTaskDO task = validateStockTakingTaskExists(id);
        if (ObjUtil.notEqual(MesWmStockTakingTaskStatusEnum.APPROVING.getStatus(), task.getStatus())) {
            throw exception(WM_STOCK_TAKING_TASK_NOT_APPROVING);
        }
        return task;
    }

    @Override
    public PageResult<MesWmStockTakingTaskDO> getStockTakingTaskPage(MesWmStockTakingTaskPageReqVO pageReqVO) {
        return stockTakingTaskMapper.selectPage(pageReqVO);
    }

    private void updateMaterialStockFrozen(List<MesWmStockTakingTaskLineDO> lines, boolean frozen) {
        if (CollUtil.isEmpty(lines)) {
            return;
        }
        List<Long> materialStockIds = convertList(lines,
                MesWmStockTakingTaskLineDO::getMaterialStockId, line -> line.getMaterialStockId() != null);
        materialStockService.updateMaterialStockFrozen(materialStockIds, frozen);
    }

    private void validateStockTakingTaskCodeUnique(Long id, String code) {
        MesWmStockTakingTaskDO task = stockTakingTaskMapper.selectByCode(code);
        if (task == null) {
            return;
        }
        if (ObjUtil.notEqual(task.getId(), id)) {
            throw exception(WM_STOCK_TAKING_TASK_CODE_DUPLICATE);
        }
    }

}
