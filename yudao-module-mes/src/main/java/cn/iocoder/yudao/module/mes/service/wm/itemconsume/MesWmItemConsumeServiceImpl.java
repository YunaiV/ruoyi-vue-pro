package cn.iocoder.yudao.module.mes.service.wm.itemconsume;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockListReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductBomDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemconsume.MesWmItemConsumeLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.itemconsume.MesWmItemConsumeMapper;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmItemConsumeStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteProductBomService;
import cn.iocoder.yudao.module.mes.service.pro.route.MesProRouteService;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.PRO_FEEDBACK_ROUTE_PROCESS_INVALID;

/**
 * MES 物料消耗记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesWmItemConsumeServiceImpl implements MesWmItemConsumeService {

    @Resource
    private MesWmItemConsumeMapper itemConsumeMapper;
    @Resource
    private MesWmItemConsumeLineService itemConsumeLineService;
    @Resource
    private MesWmItemConsumeDetailService itemConsumeDetailService;
    @Resource
    private MesProRouteProductBomService routeProductBomService;
    @Resource
    private MesProRouteService routeService;
    @Resource
    private MesWmTransactionService wmTransactionService;
    @Resource
    private MesWmWarehouseService warehouseService;
    @Resource
    private MesWmWarehouseLocationService locationService;
    @Resource
    private MesWmWarehouseAreaService areaService;
    @Resource
    private MesWmMaterialStockService materialStockService;

    @Override
    public MesWmItemConsumeDO generateItemConsume(MesProFeedbackDO feedback) {
        // 1.1 避免 routeId 等为 null 时 getRouteProductBomList 因 eqIfPresent 放宽条件导致误查
        if (feedback.getRouteId() == null || feedback.getProcessId() == null || feedback.getItemId() == null) {
            throw exception(PRO_FEEDBACK_ROUTE_PROCESS_INVALID);
        }
        routeService.validateRouteExists(feedback.getRouteId());
        // 1.2 查询当前工序的 BOM 物料配置
        List<MesProRouteProductBomDO> boms = routeProductBomService.getRouteProductBomList(
                feedback.getRouteId(), feedback.getProcessId(), feedback.getItemId());
        if (CollUtil.isEmpty(boms)) {
            return null;
        }
        // 1.3 获取虚拟线边库信息
        MesWmWarehouseDO virtualWarehouse = warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);
        MesWmWarehouseLocationDO virtualLocation = locationService.getWarehouseLocationByCode(
                MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION);
        MesWmWarehouseAreaDO virtualArea = areaService.getWarehouseAreaByCode(MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA);

        // 2.1 生成消耗单头
        MesWmItemConsumeDO consume = MesWmItemConsumeDO.builder()
                .workOrderId(feedback.getWorkOrderId()).taskId(feedback.getTaskId())
                .workstationId(feedback.getWorkstationId()).processId(feedback.getProcessId())
                .feedbackId(feedback.getId()).consumeDate(LocalDateTime.now())
                .status(MesWmItemConsumeStatusEnum.PREPARE.getStatus()).build();
        itemConsumeMapper.insert(consume);
        // 2.2 批量生成消耗行（消耗数量 = BOM 用料比例 × 报工数量）
        List<MesWmItemConsumeLineDO> lines = convertList(boms, bom -> MesWmItemConsumeLineDO.builder()
                .consumeId(consume.getId()).itemId(bom.getItemId())
                .quantity(bom.getQuantity().multiply(feedback.getFeedbackQuantity())).build());
        itemConsumeLineService.createItemConsumeLineBatch(lines);

        // 3. 按线边库 FIFO 生成消耗明细
        List<MesWmItemConsumeDetailDO> allDetails = new ArrayList<>();
        for (MesWmItemConsumeLineDO line : lines) {
            generateDetailForLine(line, consume.getId(),
                    virtualWarehouse, virtualLocation, virtualArea, allDetails);
        }
        if (CollUtil.isNotEmpty(allDetails)) {
            itemConsumeDetailService.createItemConsumeDetailBatch(allDetails);
        }
        return consume;
    }

    /**
     * 为单条消耗行按 FIFO 分配线边库批次，生成消耗明细
     *
     * 规则：
     * 1. 查询该物料在虚拟线边库中的库存记录，按入库时间升序（FIFO）
     * 2. 依次从各批次分配，直到消耗量满足
     * 3. 如果线边库无库存或库存不足，剩余数量生成无批次明细（待后续盘库核销）
     */
    private void generateDetailForLine(MesWmItemConsumeLineDO line, Long consumeId,
                                       MesWmWarehouseDO warehouse, MesWmWarehouseLocationDO location, MesWmWarehouseAreaDO area,
                                       List<MesWmItemConsumeDetailDO> allDetails) {
        // 查询该物料在线边库中的库存（按 receiptTime 升序 = FIFO；已排除 quantity = 0）
        MesWmMaterialStockListReqVO stockQuery = new MesWmMaterialStockListReqVO().setItemId(line.getItemId())
                .setWarehouseId(warehouse.getId()).setLocationId(location.getId()).setAreaId(area.getId());
        List<MesWmMaterialStockDO> stocks = materialStockService.getMaterialStockList(stockQuery)
                .stream().filter(s -> s.getQuantity().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());

        if (CollUtil.isEmpty(stocks)) {
            // 线边库无该物料库存 → 生成不带批次的明细
            allDetails.add(buildDetail(consumeId, line.getId(), null, line.getItemId(),
                    line.getQuantity(), null, null, warehouse, location, area));
            return;
        }

        // 按 FIFO 依次从各批次分配
        BigDecimal remaining = line.getQuantity();
        for (MesWmMaterialStockDO stock : stocks) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal allocate = stock.getQuantity().min(remaining);
            allDetails.add(buildDetail(consumeId, line.getId(), stock.getId(), line.getItemId(),
                    allocate, stock.getBatchId(), stock.getBatchCode(), warehouse, location, area));
            remaining = remaining.subtract(allocate);
        }

        // 库存不足时，剩余数量生成无批次明细（待后续盘库手工核销）
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            allDetails.add(buildDetail(consumeId, line.getId(), null, line.getItemId(),
                    remaining, null, null, warehouse, location, area));
        }
    }

    private static MesWmItemConsumeDetailDO buildDetail(Long consumeId, Long lineId, Long materialStockId,
                                                        Long itemId, BigDecimal quantity,
                                                        Long batchId, String batchCode,
                                                        MesWmWarehouseDO warehouse,
                                                        MesWmWarehouseLocationDO location,
                                                        MesWmWarehouseAreaDO area) {
        return MesWmItemConsumeDetailDO.builder()
                .consumeId(consumeId).lineId(lineId).materialStockId(materialStockId)
                .itemId(itemId).quantity(quantity)
                .batchId(batchId).batchCode(batchCode)
                .warehouseId(warehouse.getId()).locationId(location.getId()).areaId(area.getId())
                .build();
    }

    @Override
    public void finishItemConsume(Long consumeId) {
        // 1. 查询消耗明细（detail 级别，包含精确的批次/仓库信息）
        List<MesWmItemConsumeDetailDO> details = itemConsumeDetailService
                .getItemConsumeDetailListByConsumeId(consumeId);

        // 2. 遍历明细，创建库存事务（按批次精确扣减线边库）
        for (MesWmItemConsumeDetailDO detail : details) {
            wmTransactionService.createTransaction(new MesWmTransactionSaveReqDTO()
                    .setType(MesWmTransactionTypeEnum.OUT.getType())
                    .setItemId(detail.getItemId())
                    .setQuantity(detail.getQuantity().negate())
                    .setBatchId(detail.getBatchId()).setBatchCode(detail.getBatchCode())
                    .setWarehouseId(detail.getWarehouseId())
                    .setLocationId(detail.getLocationId())
                    .setAreaId(detail.getAreaId())
                    .setCheckFlag(false) // 线边库允许负库存
                    .setBizType(MesBizTypeConstants.WM_ITEM_CONSUME)
                    .setBizId(consumeId)
                    .setBizCode("")
                    .setBizLineId(detail.getLineId()));
        }

        // 3. 更新消耗单状态为已完成
        itemConsumeMapper.updateById(MesWmItemConsumeDO.builder()
                .id(consumeId).status(MesWmItemConsumeStatusEnum.FINISHED.getStatus()).build());
    }

    @Override
    public MesWmItemConsumeDO getByFeedbackId(Long feedbackId) {
        return itemConsumeMapper.selectByFeedbackId(feedbackId);
    }

}
