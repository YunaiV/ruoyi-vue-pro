package cn.iocoder.yudao.module.mes.service.wm.productproduce;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.batch.vo.MesWmBatchGenerateReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.feedback.MesProFeedbackDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productproduce.MesWmProductProduceLineDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.productproduce.MesWmProductProduceMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmProductProduceStatusEnum;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmQualityStatusEnum;
import cn.iocoder.yudao.module.mes.enums.MesBizTypeConstants;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.MesWmTransactionService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 生产入库单 Service 实现类
 */
@Service
@Validated
public class MesWmProductProduceServiceImpl implements MesWmProductProduceService {

    @Resource
    private MesWmProductProduceMapper productProduceMapper;

    @Resource
    private MesWmProductProduceLineService productProduceLineService;
    @Resource
    private MesWmProductProduceDetailService productProduceDetailService;
    @Resource
    private MesProWorkOrderService workOrderService;
    @Resource
    private MesWmBatchService batchService;
    @Resource
    private MesWmTransactionService wmTransactionService;
    @Resource
    private MesWmWarehouseService warehouseService;
    @Resource
    private MesWmWarehouseLocationService locationService;
    @Resource
    private MesWmWarehouseAreaService areaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finishProductProduce(Long id) {
        // 1.1 校验存在 + 草稿状态
        validateProductProduceExistsAndPrepare(id);
        // 1.2 校验至少有一条行
        List<MesWmProductProduceLineDO> lines = productProduceLineService.getProductProduceLineListByProduceId(id);
        if (CollUtil.isEmpty(lines)) {
            throw exception(WM_PRODUCT_PRODUCE_NO_LINE);
        }

        // 2. 创建库存事务
        createTransactionList(id, lines);

        // 3. 更新入库单状态
        productProduceMapper.updateById(new MesWmProductProduceDO()
                .setId(id).setStatus(MesWmProductProduceStatusEnum.FINISHED.getStatus()));
    }

    private void createTransactionList(Long produceId, List<MesWmProductProduceLineDO> lines) {
        for (MesWmProductProduceLineDO line : lines) {
            // 1. 校验每行明细数量之和等于行数量
            List<MesWmProductProduceDetailDO> details = productProduceDetailService.getProductProduceDetailListByLineId(
                    line.getId());
            BigDecimal totalDetailQty = CollectionUtils.getSumValue(details,
                    MesWmProductProduceDetailDO::getQuantity, BigDecimal::add, BigDecimal.ZERO);
            if (line.getQuantity() != null && totalDetailQty.compareTo(line.getQuantity()) != 0) {
                throw exception(WM_PRODUCT_PRODUCE_DETAIL_QUANTITY_MISMATCH);
            }
            // 2. 按明细创建库存事务（产品产出到线边库）
            for (MesWmProductProduceDetailDO detail : details) {
                wmTransactionService.createTransaction(new MesWmTransactionSaveReqDTO()
                        .setType(MesWmTransactionTypeEnum.IN.getType()).setItemId(detail.getItemId())
                        .setQuantity(detail.getQuantity())
                        .setBatchId(detail.getBatchId()).setBatchCode(detail.getBatchCode())
                        .setWarehouseId(detail.getWarehouseId()).setLocationId(detail.getLocationId()).setAreaId(detail.getAreaId())
                        .setBizType(MesBizTypeConstants.WM_PRODUCT_PRODUCE).setBizId(produceId)
                        .setBizCode("").setBizLineId(line.getId()));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MesWmProductProduceDO generateProductProduce(MesProFeedbackDO feedback, boolean checkFlag) {
        // 0. 查询关联的工单（用于获取 clientId 等信息）
        MesProWorkOrderDO workOrder = workOrderService.getWorkOrder(feedback.getWorkOrderId());

        // 1. 创建产出单头
        MesWmProductProduceDO produce = MesWmProductProduceDO.builder()
                .workOrderId(feedback.getWorkOrderId()).feedbackId(feedback.getId()).taskId(feedback.getTaskId())
                .workstationId(feedback.getWorkstationId()).processId(feedback.getProcessId())
                .produceDate(LocalDateTime.now()).status(MesWmProductProduceStatusEnum.PREPARE.getStatus())
                .build();
        productProduceMapper.insert(produce);

        // 2. 获取或生成批次
        MesWmBatchGenerateReqVO batchReqVO = new MesWmBatchGenerateReqVO()
                .setItemId(feedback.getItemId())
                .setProduceDate(LocalDate.now().atStartOfDay()) // 截断到当天零点，确保同一天的报工生成相同批次号
                .setExpireDate(feedback.getExpireDate())
                .setWorkOrderId(feedback.getWorkOrderId())
                .setClientId(workOrder != null ? workOrder.getClientId() : null)
                .setSalesOrderCode(workOrder != null ? workOrder.getOrderSourceCode() : null)
                .setWorkstationId(feedback.getWorkstationId())
                .setLotNumber(feedback.getLotNumber());
        MesWmBatchDO batch = batchService.getOrGenerateBatchCode(batchReqVO);
        Long batchId = batch != null ? batch.getId() : null;
        String batchCode = batch != null ? batch.getCode() : null;

        // 1.5 获取虚拟线边库
        MesWmWarehouseDO virtualWarehouse = warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);
        MesWmWarehouseLocationDO virtualLocation = locationService.getWarehouseLocationByCode(MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION);
        MesWmWarehouseAreaDO virtualArea = areaService.getWarehouseAreaByCode(MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA);

        // 3. 根据是否需要检验分支处理
        if (checkFlag) {
            // 3.1 需要检验：创建一条行（质量状态=待检验），不生成明细
            MesWmProductProduceLineDO line = buildProduceLine(produce, feedback, batchId, batchCode,
                    feedback.getFeedbackQuantity(), MesWmQualityStatusEnum.PENDING.getStatus());
            productProduceLineService.createProductProduceLine(line);
            // 注意：checkFlag=true 时不生成明细行，等 IPQC 检验完成后由 splitPendingAndFinishProduce 按质量结果拆行+生成明细
        } else {
            // 3.2 无需检验：按合格品/不合格品各生成一行 + 明细
            BigDecimal qualifiedQty = ObjectUtil.defaultIfNull(feedback.getQualifiedQuantity(), BigDecimal.ZERO);
            BigDecimal unqualifiedQty = ObjectUtil.defaultIfNull(feedback.getUnqualifiedQuantity(), BigDecimal.ZERO);
            // 3.2.1 不合格品行 + 明细
            if (unqualifiedQty.compareTo(BigDecimal.ZERO) > 0) {
                MesWmProductProduceLineDO unqualifiedLine = buildProduceLine(produce, feedback, batchId, batchCode,
                        unqualifiedQty, MesWmQualityStatusEnum.FAIL.getStatus());
                productProduceLineService.createProductProduceLine(unqualifiedLine);
                MesWmProductProduceDetailDO unqualifiedDetail = buildProduceDetail(produce, feedback, batchId, batchCode,
                        unqualifiedLine.getId(), unqualifiedQty,
                        virtualWarehouse.getId(), virtualLocation.getId(), virtualArea.getId());
                productProduceDetailService.createProductProduceDetail(unqualifiedDetail);
            }
            // 3.2.2 合格品行 + 明细
            if (qualifiedQty.compareTo(BigDecimal.ZERO) > 0) {
                MesWmProductProduceLineDO qualifiedLine = buildProduceLine(produce, feedback, batchId, batchCode,
                        qualifiedQty, MesWmQualityStatusEnum.PASS.getStatus());
                productProduceLineService.createProductProduceLine(qualifiedLine);
                MesWmProductProduceDetailDO qualifiedDetail = buildProduceDetail(produce, feedback, batchId, batchCode,
                        qualifiedLine.getId(), qualifiedQty,
                        virtualWarehouse.getId(), virtualLocation.getId(), virtualArea.getId());
                productProduceDetailService.createProductProduceDetail(qualifiedDetail);
            }
        }
        return produce;
    }

    private MesWmProductProduceLineDO buildProduceLine(MesWmProductProduceDO produce, MesProFeedbackDO feedback,
                                                       Long batchId, String batchCode,
                                                       BigDecimal quantity, Integer qualityStatus) {
        return MesWmProductProduceLineDO.builder()
                .produceId(produce.getId()).feedbackId(feedback.getId())
                .itemId(feedback.getItemId()).quantity(quantity)
                .batchId(batchId).batchCode(batchCode)
                .expireDate(feedback.getExpireDate()).lotNumber(feedback.getLotNumber())
                .qualityStatus(qualityStatus)
                .build();
    }

    private MesWmProductProduceDetailDO buildProduceDetail(MesWmProductProduceDO produce, MesProFeedbackDO feedback,
                                                           Long batchId, String batchCode,
                                                           Long lineId, BigDecimal quantity,
                                                           Long warehouseId, Long locationId, Long areaId) {
        return MesWmProductProduceDetailDO.builder()
                .produceId(produce.getId()).lineId(lineId)
                .itemId(feedback.getItemId()).quantity(quantity)
                .batchId(batchId).batchCode(batchCode)
                .warehouseId(warehouseId).locationId(locationId).areaId(areaId)
                .build();
    }

    @Override
    public MesWmProductProduceDO validateProductProduceExists(Long id) {
        MesWmProductProduceDO produce = productProduceMapper.selectById(id);
        if (produce == null) {
            throw exception(WM_PRODUCT_PRODUCE_NOT_EXISTS);
        }
        return produce;
    }

    /**
     * 校验生产入库单存在且为准备中状态
     */
    private MesWmProductProduceDO validateProductProduceExistsAndPrepare(Long id) {
        MesWmProductProduceDO produce = validateProductProduceExists(id);
        if (ObjUtil.notEqual(produce.getStatus(), MesWmProductProduceStatusEnum.PREPARE.getStatus())) {
            throw exception(WM_PRODUCT_PRODUCE_STATUS_INVALID);
        }
        return produce;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void splitPendingAndFinishProduce(Long feedbackId, BigDecimal qualifiedQty, BigDecimal unqualifiedQty) {
        // 1.1 查询产出单
        MesWmProductProduceDO produce = productProduceMapper.selectByFeedbackId(feedbackId);
        if (produce == null) {
            throw exception(WM_PRODUCT_PRODUCE_NOT_EXISTS);
        }
        // 1.2 获取虚拟线边库
        MesWmWarehouseDO virtualWarehouse = warehouseService.getWarehouseByCode(MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);
        MesWmWarehouseLocationDO virtualLocation = locationService.getWarehouseLocationByCode(MesWmWarehouseLocationDO.WIP_VIRTUAL_LOCATION);
        MesWmWarehouseAreaDO virtualArea = areaService.getWarehouseAreaByCode(MesWmWarehouseAreaDO.WIP_VIRTUAL_AREA);

        // 2. 查找待检验行（checkFlag=true 时只有一行 PENDING）
        List<MesWmProductProduceLineDO> lines = productProduceLineService.getProductProduceLineListByProduceId(produce.getId());
        MesWmProductProduceLineDO pendingLine = CollUtil.findOne(lines,
                l -> ObjUtil.equal(l.getQualityStatus(), MesWmQualityStatusEnum.PENDING.getStatus()));
        if (pendingLine == null) {
            throw exception(WM_PRODUCT_PRODUCE_LINE_NOT_EXISTS);
        }

        // 3A. 情况一：存在不合格品数量，需要拆分行
        if (unqualifiedQty != null && unqualifiedQty.compareTo(BigDecimal.ZERO) > 0) {
            // 3A.1 不合格品：新建一行 + 明细
            MesWmProductProduceLineDO unqualifiedLine = MesWmProductProduceLineDO.builder()
                    .produceId(produce.getId()).feedbackId(feedbackId)
                    .itemId(pendingLine.getItemId()).quantity(unqualifiedQty)
                    .batchId(pendingLine.getBatchId()).batchCode(pendingLine.getBatchCode())
                    .expireDate(pendingLine.getExpireDate()).lotNumber(pendingLine.getLotNumber())
                    .qualityStatus(MesWmQualityStatusEnum.FAIL.getStatus())
                    .build();
            productProduceLineService.createProductProduceLine(unqualifiedLine);
            productProduceDetailService.createProductProduceDetail(MesWmProductProduceDetailDO.builder()
                    .produceId(produce.getId()).lineId(unqualifiedLine.getId())
                    .itemId(pendingLine.getItemId()).quantity(unqualifiedQty)
                    .batchId(pendingLine.getBatchId()).batchCode(pendingLine.getBatchCode())
                    .warehouseId(virtualWarehouse.getId()).locationId(virtualLocation.getId()).areaId(virtualArea.getId())
                    .build());
            // 3A.2 合格品：复用原待检行，更新数量和状态 + 明细
            pendingLine.setQuantity(ObjectUtil.defaultIfNull(qualifiedQty, BigDecimal.ZERO));
            pendingLine.setQualityStatus(MesWmQualityStatusEnum.PASS.getStatus());
            productProduceLineService.updateProductProduceLine(pendingLine);
            if (qualifiedQty != null && qualifiedQty.compareTo(BigDecimal.ZERO) > 0) {
                productProduceDetailService.createProductProduceDetail(MesWmProductProduceDetailDO.builder()
                        .produceId(produce.getId()).lineId(pendingLine.getId())
                        .itemId(pendingLine.getItemId()).quantity(qualifiedQty)
                        .batchId(pendingLine.getBatchId()).batchCode(pendingLine.getBatchCode())
                        .warehouseId(virtualWarehouse.getId()).locationId(virtualLocation.getId()).areaId(virtualArea.getId())
                        .build());
            }
        } else {
            // 3B. 情况二：全部合格，直接更新行状态 + 明细
            pendingLine.setQualityStatus(MesWmQualityStatusEnum.PASS.getStatus());
            productProduceLineService.updateProductProduceLine(pendingLine);
            productProduceDetailService.createProductProduceDetail(MesWmProductProduceDetailDO.builder()
                    .produceId(produce.getId()).lineId(pendingLine.getId())
                    .itemId(pendingLine.getItemId()).quantity(pendingLine.getQuantity())
                    .batchId(pendingLine.getBatchId()).batchCode(pendingLine.getBatchCode())
                    .warehouseId(virtualWarehouse.getId()).locationId(virtualLocation.getId()).areaId(virtualArea.getId())
                    .build());
        }

        // 4. 完成产出单（创建库存事务 + 更新状态为已完成）
        finishProductProduce(produce.getId());
    }

}
