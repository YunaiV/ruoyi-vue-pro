package cn.iocoder.yudao.module.mes.service.wm.transaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.batch.MesWmBatchDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.transaction.MesWmTransactionDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.transaction.MesWmTransactionMapper;
import cn.iocoder.yudao.module.mes.enums.wm.MesWmTransactionTypeEnum;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.wm.batch.MesWmBatchService;
import cn.iocoder.yudao.module.mes.service.wm.materialstock.MesWmMaterialStockService;
import cn.iocoder.yudao.module.mes.service.wm.transaction.dto.MesWmTransactionSaveReqDTO;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseLocationService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 库存事务流水 Service 实现类
 */
@Service
@Validated
public class MesWmTransactionServiceImpl implements MesWmTransactionService {

    @Resource
    private MesWmTransactionMapper transactionMapper;
    @Resource
    private MesWmMaterialStockService materialStockService;
    @Resource
    private MesWmBatchService batchService;
    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesWmWarehouseService warehouseService;
    @Resource
    private MesWmWarehouseLocationService locationService;
    @Resource
    private MesWmWarehouseAreaService areaService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransaction(MesWmTransactionSaveReqDTO reqDTO) {
        // 1.1 校验事务类型
        MesWmTransactionTypeEnum typeEnum = MesWmTransactionTypeEnum.valueOf(reqDTO.getType());
        if (typeEnum == null) {
            throw exception(WM_TRANSACTION_TYPE_NOT_EXISTS);
        }
        // 1.2 校验 quantity 正负号与事务方向一致（入库必须正数，出库必须负数）
        boolean inbound = typeEnum.isInbound();
        checkQuantity(reqDTO, inbound);
        // 1.3 入库事务：批次号校验 + batchId/batchCode 互补
        checkBatch(reqDTO, inbound);
        // 1.4 入库事务：库位混放规则校验
        if (inbound) {
            materialStockService.checkAreaMixingRule(reqDTO.getAreaId(), reqDTO.getItemId(), reqDTO.getBatchId());
        }
        // 1.5 关联事务校验
        checkRelatedTransaction(reqDTO);

        // 2.1 获取或创建库存记录
        MesWmMaterialStockDO materialStock = materialStockService.getOrCreateMaterialStock(
                reqDTO.getItemId(), reqDTO.getWarehouseId(), reqDTO.getLocationId(), reqDTO.getAreaId(),
                reqDTO.getBatchId(), reqDTO.getBatchCode(), reqDTO.getVendorId(), reqDTO.getReceiptTime());
        // 2.2 冻结校验（仓库、库区、库位、库存记录）
        checkFrozen(reqDTO, materialStock);
        // 2.3 更新库存数量
        boolean checkFlag = ObjUtil.defaultIfNull(reqDTO.getCheckFlag(), true);
        materialStockService.updateMaterialStockQuantity(materialStock.getId(), reqDTO.getQuantity(), checkFlag);

        // 3. 插入事务流水
        MesWmTransactionDO transaction = MesWmTransactionDO.builder()
                .type(reqDTO.getType()).quantity(reqDTO.getQuantity()).transactionTime(LocalDateTime.now())
                .itemId(reqDTO.getItemId()).batchId(reqDTO.getBatchId()).batchCode(reqDTO.getBatchCode())
                .warehouseId(reqDTO.getWarehouseId()).locationId(reqDTO.getLocationId()).areaId(reqDTO.getAreaId())
                .bizType(reqDTO.getBizType()).bizId(reqDTO.getBizId()).bizCode(reqDTO.getBizCode()).bizLineId(reqDTO.getBizLineId())
                .materialStockId(materialStock.getId()).relatedTransactionId(reqDTO.getRelatedTransactionId())
                .build();
        transactionMapper.insert(transaction);
        return transaction.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTransactionList(List<MesWmTransactionSaveReqDTO> reqDTOs) {
        if (CollUtil.isEmpty(reqDTOs)) {
            throw exception(WM_TRANSACTION_LIST_EMPTY);
        }
        reqDTOs.forEach(this::createTransaction);
    }

    // ==================== 私有校验方法 ====================

    /**
     * 校验数量正负号与事务方向一致
     */
    private void checkQuantity(MesWmTransactionSaveReqDTO reqDTO, boolean inbound) {
        if (inbound) {
            Assert.isTrue(reqDTO.getQuantity().compareTo(BigDecimal.ZERO) > 0,
                    "入库事务数量必须为正数，实际值: {}", reqDTO.getQuantity());
        } else {
            Assert.isTrue(reqDTO.getQuantity().compareTo(BigDecimal.ZERO) < 0,
                    "出库事务数量必须为负数，实际值: {}", reqDTO.getQuantity());
        }
    }

    /**
     * 批次校验：batchId/batchCode 互补 + 入库时批次号必填检查
     *
     * @param reqDTO  事务请求 DTO
     * @param inbound 是否入库
     */
    private void checkBatch(MesWmTransactionSaveReqDTO reqDTO, boolean inbound) {
        // 1. batchId / batchCode 互补
        if (reqDTO.getBatchId() != null && StrUtil.isEmpty(reqDTO.getBatchCode())) {
            MesWmBatchDO batch = batchService.getBatch(reqDTO.getBatchId());
            if (batch == null) {
                throw exception(WM_TRANSACTION_BATCH_NOT_EXISTS);
            }
            reqDTO.setBatchCode(batch.getCode());
        } else if (StrUtil.isNotEmpty(reqDTO.getBatchCode()) && reqDTO.getBatchId() == null) {
            MesWmBatchDO batch = batchService.getBatchByCode(reqDTO.getBatchCode());
            if (batch == null) {
                throw exception(WM_TRANSACTION_BATCH_NOT_EXISTS);
            }
            reqDTO.setBatchId(batch.getId());
        }
        // 2. 入库时：启用批次管理的物料，必须传递批次号
        if (inbound) {
            MesMdItemDO item = itemService.validateItemExists(reqDTO.getItemId());
            if (Boolean.TRUE.equals(item.getBatchFlag())) {
                if (StrUtil.isEmpty(reqDTO.getBatchCode()) && reqDTO.getBatchId() == null) {
                    throw exception(WM_TRANSACTION_BATCH_REQUIRED);
                }
            }
        }
    }

    /**
     * 关联事务校验：relatedTransactionId 不为空时，关联的事务必须存在
     */
    private void checkRelatedTransaction(MesWmTransactionSaveReqDTO reqDTO) {
        if (reqDTO.getRelatedTransactionId() == null) {
            return;
        }
        MesWmTransactionDO relatedTransaction = transactionMapper.selectById(reqDTO.getRelatedTransactionId());
        if (relatedTransaction == null) {
            throw exception(WM_TRANSACTION_RELATED_NOT_EXISTS);
        }
    }

    /**
     * 冻结校验：检查仓库、库区、库位、库存记录是否被冻结
     *
     * @param reqDTO        事务请求 DTO
     * @param materialStock 库存记录
     */
    private void checkFrozen(MesWmTransactionSaveReqDTO reqDTO, MesWmMaterialStockDO materialStock) {
        // 1.1 检查仓库冻结
        MesWmWarehouseDO warehouse = warehouseService.validateWarehouseExists(reqDTO.getWarehouseId());
        if (Boolean.TRUE.equals(warehouse.getFrozen())) {
            throw exception(WM_TRANSACTION_WAREHOUSE_FROZEN, warehouse.getName());
        }
        // 1.2 检查库区冻结
        MesWmWarehouseLocationDO location = locationService.validateWarehouseLocationExists(reqDTO.getLocationId());
        if (Boolean.TRUE.equals(location.getFrozen())) {
            throw exception(WM_TRANSACTION_LOCATION_FROZEN, location.getName());
        }
        // 1.3 检查库位冻结
        MesWmWarehouseAreaDO area = areaService.validateWarehouseAreaExists(reqDTO.getAreaId());
        if (Boolean.TRUE.equals(area.getFrozen())) {
            throw exception(WM_TRANSACTION_AREA_FROZEN, area.getName());
        }

        // 2. 检查库存记录冻结
        if (Boolean.TRUE.equals(materialStock.getFrozen())) {
            throw exception(WM_TRANSACTION_STOCK_FROZEN, warehouse.getName(), location.getName(), area.getName());
        }
    }

}
