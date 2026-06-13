package cn.iocoder.yudao.module.wms.service.inventory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Tuple;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.MoneyUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryHistoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.item.WmsItemSkuDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryCheckReqDTO;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemService;
import cn.iocoder.yudao.module.wms.service.md.item.WmsItemSkuService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * WMS 库存 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class WmsInventoryServiceImpl implements WmsInventoryService {

    @Resource
    private WmsInventoryMapper inventoryMapper;

    @Resource
    private WmsInventoryHistoryService inventoryHistoryService;

    @Resource
    private WmsItemSkuService itemSkuService;
    @Resource
    private WmsItemService itemService;

    @Override
    public PageResult<WmsInventoryDO> getInventoryPage(WmsInventoryPageReqVO pageReqVO) {
        return inventoryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsInventoryDO> getInventoryList(WmsInventoryListReqVO listReqVO) {
        return inventoryMapper.selectList(listReqVO);
    }

    @Override
    public long getInventoryCountBySkuId(Long skuId) {
        return inventoryMapper.selectCountBySkuId(skuId);
    }

    @Override
    public long getInventoryCountByWarehouseId(Long warehouseId) {
        return inventoryMapper.selectCountByWarehouseId(warehouseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkInventory(WmsInventoryCheckReqDTO reqDTO) {
        if (reqDTO == null || CollUtil.isEmpty(reqDTO.getItems())) {
            return;
        }

        // 1. 逐条复核账面库存，并计算库存调整和库存流水
        List<WmsInventoryDO> updateInventories = new ArrayList<>(reqDTO.getItems().size());
        List<WmsInventoryHistoryDO> histories = new ArrayList<>(reqDTO.getItems().size());
        for (WmsInventoryCheckReqDTO.Item item : reqDTO.getItems()) {
            // 1.1 锁定或创建库存余额行
            WmsInventoryDO inventory = getOrCreateCheckInventory(item);
            // 1.2 无盈亏时不更新库存，也不生成库存流水
            BigDecimal beforeQuantity = inventory.getQuantity();
            BigDecimal afterQuantity = item.getCheckQuantity();
            if (beforeQuantity.compareTo(afterQuantity) == 0) {
                continue;
            }
            updateInventories.add(new WmsInventoryDO().setId(inventory.getId()).setQuantity(afterQuantity));
            histories.add(buildInventoryHistory(reqDTO, item, beforeQuantity, afterQuantity));
        }

        // 2.1 批量更新库存余额
        if (CollUtil.isNotEmpty(updateInventories)) {
            inventoryMapper.updateBatch(updateInventories);
        }
        // 2.2 批量写入库存流水
        if (CollUtil.isNotEmpty(histories)) {
            inventoryHistoryService.createInventoryHistoryList(histories);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeInventory(WmsInventoryChangeReqDTO reqDTO) {
        if (reqDTO == null || CollUtil.isEmpty(reqDTO.getItems())) {
            return;
        }

        // 1. 补齐并锁定本次涉及的库存余额行，再计算库存变更
        Map<WmsInventoryChangeReqDTO.Item, Tuple> resultMap = changeInventoryList(reqDTO.getItems());

        // 2. 批量写入库存流水
        List<WmsInventoryHistoryDO> histories = new ArrayList<>(reqDTO.getItems().size());
        for (WmsInventoryChangeReqDTO.Item item : reqDTO.getItems()) {
            histories.add(buildInventoryHistory(reqDTO, item, resultMap.get(item)));
        }
        inventoryHistoryService.createInventoryHistoryList(histories);
    }

    /**
     * 批量变更库存余额
     *
     * 1. 库存行按 ID 批量加锁
     * 2. 库存变更先在内存计算并校验，全部通过后批量覆盖库存数量。
     *
     * @param items 库存变更明细列表
     * @return 每条变更明细对应的变更前数量、变更后数量
     */
    private Map<WmsInventoryChangeReqDTO.Item, Tuple> changeInventoryList(List<WmsInventoryChangeReqDTO.Item> items) {
        // 1.1 创建或锁定库存行
        List<WmsInventoryDO> inventories = getOrCreateInventoryList(items);
        // 1.2 锁定库存：避免 quantity 变更时的并发问题，导致 quantity 前后计算不对
        inventories = inventoryMapper.selectListByIdsForUpdate(convertSet(inventories, WmsInventoryDO::getId));

        // 2.1 校验库存充足
        Map<WmsInventoryChangeReqDTO.Item, Tuple> resultMap = new IdentityHashMap<>(items.size());
        for (WmsInventoryChangeReqDTO.Item item : items) {
            WmsInventoryDO inventory = findInventory(inventories, item);
            if (inventory == null) {
                throw new IllegalStateException("库存行不存在，skuId=" + item.getSkuId()
                        + ", warehouseId=" + item.getWarehouseId());
            }
            BigDecimal beforeQuantity = inventory.getQuantity();
            BigDecimal afterQuantity = beforeQuantity.add(item.getQuantity());
            if (afterQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw buildInventoryQuantityNotEnoughException(item, beforeQuantity);
            }
            inventory.setQuantity(afterQuantity);
            resultMap.put(item, new Tuple(beforeQuantity, afterQuantity));
        }
        // 2.2 批量更新库存数量（加锁安全）
        if (CollUtil.isNotEmpty(inventories)) {
            inventoryMapper.updateBatch(convertList(inventories, inventory ->
                    new WmsInventoryDO().setId(inventory.getId()).setQuantity(inventory.getQuantity())));
        }
        return resultMap;
    }

    private List<WmsInventoryDO> getOrCreateInventoryList(List<WmsInventoryChangeReqDTO.Item> items) {
        // 1.1 先按库存维度在内存去重，避免同一批明细重复查询或重复补行
        List<WmsInventoryDO> inventoryDimensions = new ArrayList<>(items.size());
        for (WmsInventoryChangeReqDTO.Item item : items) {
            if (findInventory(inventoryDimensions, item) == null) {
                inventoryDimensions.add(new WmsInventoryDO().setSkuId(item.getSkuId())
                        .setWarehouseId(item.getWarehouseId()));
            }
        }
        // 1.2 批量查询已存在的库存行（这里不加锁，后续统一按库存 ID 批量加锁）
        List<WmsInventoryDO> inventories = inventoryMapper.selectListByKeys(inventoryDimensions);

        // 2.1 对比库存维度，找出数据库中还不存在的库存行
        List<WmsInventoryDO> missingInventories = new ArrayList<>(inventoryDimensions.size());
        for (WmsInventoryDO inventoryDimension : inventoryDimensions) {
            if (findInventory(inventories, inventoryDimension) == null) {
                missingInventories.add(inventoryDimension);
            }
        }
        // 2.2 如果库存行都已存在，直接返回待加锁库存列表
        if (CollUtil.isEmpty(missingInventories)) {
            return inventories;
        }
        // 2.3 对缺失库存行执行创建；并发冲突时，内部会按唯一索引回查已创建的库存行
        inventories.addAll(createMissingInventoryList(missingInventories));
        return inventories;
    }

    private List<WmsInventoryDO> createMissingInventoryList(List<WmsInventoryDO> missingInventories) {
        List<WmsInventoryDO> createdInventories = new ArrayList<>(missingInventories.size());
        for (WmsInventoryDO missingInventory : missingInventories) {
            WmsInventoryDO inventory = new WmsInventoryDO().setSkuId(missingInventory.getSkuId())
                    .setWarehouseId(missingInventory.getWarehouseId())
                    .setQuantity(BigDecimal.ZERO);
            try {
                inventoryMapper.insert(inventory);
            } catch (DuplicateKeyException ex) {
                inventory = inventoryMapper.selectBySkuIdAndWarehouseId(
                        missingInventory.getSkuId(), missingInventory.getWarehouseId());
                log.warn("[createMissingInventoryList][missingInventory({}) 插入库存行冲突，回查已有库存行]", missingInventory);
                if (inventory == null) {
                    throw ex;
                }
            }
            createdInventories.add(inventory);
        }
        return createdInventories;
    }

    private WmsInventoryHistoryDO buildInventoryHistory(WmsInventoryChangeReqDTO reqDTO,
                                                       WmsInventoryChangeReqDTO.Item item,
                                                       Tuple result) {
        return new WmsInventoryHistoryDO()
                .setWarehouseId(item.getWarehouseId()).setSkuId(item.getSkuId())
                .setQuantity(item.getQuantity()).setBeforeQuantity(result.get(0)).setAfterQuantity(result.get(1))
                .setPrice(item.getPrice()).setTotalPrice(item.getTotalPrice()).setRemark(item.getRemark())
                .setOrderId(reqDTO.getOrderId()).setOrderNo(reqDTO.getOrderNo()).setOrderType(reqDTO.getOrderType());
    }

    private WmsInventoryHistoryDO buildInventoryHistory(WmsInventoryCheckReqDTO reqDTO,
                                                       WmsInventoryCheckReqDTO.Item item,
                                                       BigDecimal beforeQuantity,
                                                       BigDecimal afterQuantity) {
        BigDecimal quantity = afterQuantity.subtract(beforeQuantity);
        return new WmsInventoryHistoryDO().setWarehouseId(item.getWarehouseId()).setSkuId(item.getSkuId())
                .setQuantity(quantity).setBeforeQuantity(beforeQuantity).setAfterQuantity(afterQuantity)
                .setPrice(item.getPrice()).setTotalPrice(MoneyUtils.priceMultiply(item.getPrice(), quantity))
                .setOrderId(reqDTO.getOrderId()).setOrderNo(reqDTO.getOrderNo()).setOrderType(reqDTO.getOrderType())
                .setRemark(item.getRemark());
    }

    private WmsInventoryDO getOrCreateCheckInventory(WmsInventoryCheckReqDTO.Item item) {
        if (item.getInventoryId() == null) {
            return createCheckInventory(item);
        }
        WmsInventoryDO inventory = inventoryMapper.selectByIdForUpdate(item.getInventoryId());
        if (inventory == null || !isSameInventory(inventory, item)
                || inventory.getQuantity().compareTo(item.getQuantity()) != 0) {
            throw exception(CHECK_ORDER_INVENTORY_CHANGED);
        }
        return inventory;
    }

    private WmsInventoryDO createCheckInventory(WmsInventoryCheckReqDTO.Item item) {
        WmsInventoryDO inventory = new WmsInventoryDO()
                .setSkuId(item.getSkuId()).setWarehouseId(item.getWarehouseId())
                .setQuantity(item.getCheckQuantity());
        try {
            inventoryMapper.insert(inventory);
        } catch (DuplicateKeyException ex) {
            throw exception(CHECK_ORDER_INVENTORY_CHANGED);
        }
        // 内存中重置为 0，让主循环按 0 -> checkQuantity 生成盘库流水
        inventory.setQuantity(BigDecimal.ZERO);
        return inventory;
    }

    private static WmsInventoryDO findInventory(List<WmsInventoryDO> inventories, WmsInventoryChangeReqDTO.Item item) {
        return CollUtil.findOne(inventories, inventory -> isSameInventory(inventory, item));
    }

    private static WmsInventoryDO findInventory(List<WmsInventoryDO> inventories, WmsInventoryDO key) {
        return CollUtil.findOne(inventories, inventory -> isSameInventory(inventory, key));
    }

    private static boolean isSameInventory(WmsInventoryDO inventory, WmsInventoryChangeReqDTO.Item item) {
        return ObjectUtil.equal(inventory.getSkuId(), item.getSkuId())
                && ObjectUtil.equal(inventory.getWarehouseId(), item.getWarehouseId());
    }

    private static boolean isSameInventory(WmsInventoryDO inventory, WmsInventoryCheckReqDTO.Item item) {
        return ObjectUtil.equal(inventory.getSkuId(), item.getSkuId())
                && ObjectUtil.equal(inventory.getWarehouseId(), item.getWarehouseId());
    }

    private static boolean isSameInventory(WmsInventoryDO inventory, WmsInventoryDO key) {
        return ObjectUtil.equal(inventory.getSkuId(), key.getSkuId())
                && ObjectUtil.equal(inventory.getWarehouseId(), key.getWarehouseId());
    }

    private ServiceException buildInventoryQuantityNotEnoughException(WmsInventoryChangeReqDTO.Item item,
                                                                      BigDecimal beforeQuantity) {
        WmsItemSkuDO skuDO = itemSkuService.validateItemSkuExists(item.getSkuId());
        WmsItemDO itemDO = itemService.validateItemExists(skuDO.getItemId());
        return exception(INVENTORY_QUANTITY_NOT_ENOUGH, itemDO.getName(), skuDO.getName(),
                item.getWarehouseId(), beforeQuantity, item.getQuantity());
    }

}
