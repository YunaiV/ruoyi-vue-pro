package cn.iocoder.yudao.module.mes.service.wm.stocktaking.task;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line.MesWmStockTakingTaskLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.line.MesWmStockTakingTaskLineSaveReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.stocktaking.task.vo.result.MesWmStockTakingTaskResultSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.plan.MesWmStockTakingPlanParamDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.stocktaking.task.MesWmStockTakingTaskLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.stocktaking.task.MesWmStockTakingTaskLineMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingPlanParamTypeEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingTaskLineStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmStockTakingTypeEnum;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.stocktaking.plan.MesWmStockTakingPlanParamService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_STOCK_TAKING_TASK_LINE_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.WM_STOCK_TAKING_TASK_NO_STOCK;

/**
 * MES 盘点任务行 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmStockTakingTaskLineServiceImpl implements MesWmStockTakingTaskLineService {

    @Resource
    private MesWmStockTakingTaskLineMapper stockTakingTaskLineMapper;
    @Resource
    private MesWmStockTakingPlanParamService stockTakingPlanParamService;
    @Resource
    private MesWmMaterialStockService materialStockService;
    @Resource
    private MesWmStockTakingTaskService stockTakingTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateStockTakingLines(MesWmStockTakingTaskDO task, boolean isCreate) {
        // 1. 如果非创建操作，先清理旧数据
        if (!isCreate) {
            stockTakingTaskLineMapper.deleteByTaskId(task.getId());
        }

        // 2.1 构建查询条件（包含动态盘点时间校验）
        MesWmMaterialStockListReqVO reqVO = buildStockQueryReqVO(task);
        // 2.2 查询物料库存
        List<MesWmMaterialStockDO> stocks = materialStockService.getMaterialStockList(reqVO);
        if (CollUtil.isEmpty(stocks)) {
            throw exception(WM_STOCK_TAKING_TASK_NO_STOCK);
        }

        // 3. 批量生成明细行
        List<MesWmStockTakingTaskLineDO> lines = convertList(stocks, stock -> MesWmStockTakingTaskLineDO.builder()
                .taskId(task.getId()).materialStockId(stock.getId()).itemId(stock.getItemId())
                .batchId(stock.getBatchId()).quantity(stock.getQuantity()).takingQuantity(BigDecimal.ZERO)
                .warehouseId(stock.getWarehouseId()).locationId(stock.getLocationId()).areaId(stock.getAreaId())
                .status(MesWmStockTakingTaskLineStatusEnum.LOSS.getStatus()).build());
        stockTakingTaskLineMapper.insertBatch(lines);
    }

    @Override
    public PageResult<MesWmStockTakingTaskLineDO> getStockTakingTaskLinePage(MesWmStockTakingTaskLinePageReqVO pageReqVO) {
        return stockTakingTaskLineMapper.selectPage(pageReqVO);
    }

    @Override
    public MesWmStockTakingTaskLineDO getStockTakingTaskLine(Long id) {
        return stockTakingTaskLineMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockTakingTaskLine(MesWmStockTakingTaskLineSaveReqVO createReqVO) {
        // 1. 校验盘点任务存在，并且处于【准备中】状态
        stockTakingTaskService.validateStockTakingTaskExistsAndPrepare(createReqVO.getTaskId());

        // 2. 创建盘点任务行
        MesWmStockTakingTaskLineDO line = BeanUtils.toBean(createReqVO, MesWmStockTakingTaskLineDO.class);
        line.setStatus(MesWmStockTakingTaskLineStatusEnum.LOSS.getStatus()); // 默认状态：盘亏（与 generateStockTakingLines 保持一致，待盘点后由 calculateLineStatus 更新）
        stockTakingTaskLineMapper.insert(line);
        return line.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStockTakingTaskLine(MesWmStockTakingTaskLineSaveReqVO updateReqVO) {
        // 1.1 校验盘点任务行存在
        MesWmStockTakingTaskLineDO line = validateStockTakingTaskLineExists(updateReqVO.getId());
        // 1.2 校验盘点任务存在，并且处于【准备中】状态
        stockTakingTaskService.validateStockTakingTaskExistsAndPrepare(updateReqVO.getTaskId());

        // 2. 更新盘点任务行
        MesWmStockTakingTaskLineDO updateObj = BeanUtils.toBean(updateReqVO, MesWmStockTakingTaskLineDO.class);
        stockTakingTaskLineMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockTakingTaskLine(Long id) {
        // 1.1 校验盘点任务行存在
        MesWmStockTakingTaskLineDO line = validateStockTakingTaskLineExists(id);
        // 1.2 校验盘点任务存在，并且处于【准备中】状态
        stockTakingTaskService.validateStockTakingTaskExistsAndPrepare(line.getTaskId());

        // 2. 删除盘点任务行
        stockTakingTaskLineMapper.deleteById(id);
    }

    @Override
    public MesWmStockTakingTaskLineDO validateStockTakingTaskLineExists(Long id) {
        MesWmStockTakingTaskLineDO line = stockTakingTaskLineMapper.selectById(id);
        if (line == null) {
            throw exception(WM_STOCK_TAKING_TASK_LINE_NOT_EXISTS);
        }
        return line;
    }

    /**
     * 构建库存查询条件
     *
     * @param task 盘点任务
     * @return 查询条件 VO
     */
    private MesWmMaterialStockListReqVO buildStockQueryReqVO(MesWmStockTakingTaskDO task) {
        // 1. 从方案参数中提取过滤条件
        List<MesWmStockTakingPlanParamDO> params = stockTakingPlanParamService.getStockTakingPlanParamListByPlanId(task.getPlanId());
        Assert.notEmpty(params, "盘点方案参数不能为空");

        // 2.1 拼接通用参数
        MesWmMaterialStockListReqVO reqVO = new MesWmMaterialStockListReqVO()
                .setWarehouseId(extractMesWmStockTakingPlanParamValueId(params, MesWmStockTakingPlanParamTypeEnum.WAREHOUSE.getType()))
                .setLocationId(extractMesWmStockTakingPlanParamValueId(params, MesWmStockTakingPlanParamTypeEnum.LOCATION.getType()))
                .setAreaId(extractMesWmStockTakingPlanParamValueId(params, MesWmStockTakingPlanParamTypeEnum.AREA.getType()))
                .setItemId(extractMesWmStockTakingPlanParamValueId(params, MesWmStockTakingPlanParamTypeEnum.ITEM.getType()))
                .setBatchId(extractMesWmStockTakingPlanParamValueId(params, MesWmStockTakingPlanParamTypeEnum.BATCH.getType()));
        // 2.2 对于动态盘点，设置时间参数并校验
        if (MesWmStockTakingTypeEnum.DYNAMIC.getType().equals(task.getType())) {
            Assert.notNull(task.getStartTime(), "动态盘点开始时间不能为空");
            Assert.notNull(task.getEndTime(), "动态盘点结束时间不能为空");
            reqVO.setStartTime(task.getStartTime()).setEndTime(task.getEndTime());
        }
        return reqVO;
    }

    @Override
    public List<MesWmStockTakingTaskLineDO> getStockTakingTaskLineListByTaskId(Long taskId) {
        return stockTakingTaskLineMapper.selectListByTaskId(taskId);
    }

    @Override
    public void deleteStockTakingTaskLineByTaskId(Long taskId) {
        stockTakingTaskLineMapper.deleteByTaskId(taskId);
    }

    @Override
    public MesWmStockTakingTaskLineDO getStockTakingTaskLine(Long taskId, Long itemId, Long areaId) {
        return stockTakingTaskLineMapper.selectByTaskIdAndItemIdAndAreaId(taskId, itemId, areaId);
    }

    @Override
    public void updateStockTakingTaskLineTakingQuantity(Long id, BigDecimal takingQuantity) {
        // 1. 校验盘点任务行存在
        MesWmStockTakingTaskLineDO line = validateStockTakingTaskLineExists(id);
        // 2. 更新盘点数量和状态
        line.setTakingQuantity(takingQuantity)
                .setStatus(calculateLineStatus(line.getQuantity(), takingQuantity));
        stockTakingTaskLineMapper.updateById(line);
    }

    @Override
    public Long createStockTakingTaskLine(MesWmStockTakingTaskResultSaveReqVO createReqVO) {
        MesWmStockTakingTaskLineDO line = new MesWmStockTakingTaskLineDO()
                .setTaskId(createReqVO.getTaskId()).setItemId(createReqVO.getItemId()).setBatchId(createReqVO.getBatchId())
                .setWarehouseId(createReqVO.getWarehouseId()).setLocationId(createReqVO.getLocationId()).setAreaId(createReqVO.getAreaId())
                .setQuantity(BigDecimal.ZERO) // 账面数量默认为 0
                .setTakingQuantity(createReqVO.getTakingQuantity()) // 盘点数量
                .setStatus(calculateLineStatus(BigDecimal.ZERO, createReqVO.getTakingQuantity())); // 计算盈亏状态
        stockTakingTaskLineMapper.insert(line);
        return line.getId();
    }

    /**
     * 计算盘点任务行状态
     *
     * @param quantity 账面数量
     * @param takingQuantity 盘点数量
     * @return 状态
     */
    private Integer calculateLineStatus(BigDecimal quantity, BigDecimal takingQuantity) {
        Assert.notNull(takingQuantity, "盘点数量不能为空");
        Assert.notNull(quantity, "账面数量不能为空");
        int compareResult = takingQuantity.compareTo(quantity);
        if (compareResult > 0) {
            return MesWmStockTakingTaskLineStatusEnum.GAIN.getStatus(); // 盘盈
        }
        if (compareResult < 0) {
            return MesWmStockTakingTaskLineStatusEnum.LOSS.getStatus(); // 盘亏
        }
        return MesWmStockTakingTaskLineStatusEnum.NORMAL.getStatus(); // 正常
    }

    /**
     * 从参数列表中提取第一个匹配类型的参数 ID
     *
     * @param params 参数列表
     * @param type 参数类型
     * @return 参数 ID（如果有多个，取第一个）
     */
    private Long extractMesWmStockTakingPlanParamValueId(List<MesWmStockTakingPlanParamDO> params, Integer type) {
        MesWmStockTakingPlanParamDO param = CollUtil.findOne(params,
                item -> Objects.equals(item.getType(), type) && item.getValueId() != null);
        return param != null ? param.getValueId() : null;
    }

}
