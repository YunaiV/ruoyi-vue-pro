package cn.iocoder.yudao.module.mes.service.wm.materialstock;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockFreezeReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.materialstock.vo.MesWmMaterialStockPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.materialstock.MesWmMaterialStockDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import cn.iocoder.yudao.module.mes.dal.mysql.wm.materialstock.MesWmMaterialStockMapper;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemService;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemTypeService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseAreaService;
import cn.iocoder.yudao.module.mes.service.wm.warehouse.MesWmWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 库存台账 Service 实现类
 */
@Service
@Validated
public class MesWmMaterialStockServiceImpl implements MesWmMaterialStockService {

    @Resource
    private MesWmMaterialStockMapper materialStockMapper;

    @Resource
    private MesMdItemService itemService;
    @Resource
    private MesMdItemTypeService itemTypeService;
    @Resource
    private MesWmWarehouseAreaService areaService;
    @Resource
    @Lazy
    private MesWmWarehouseService warehouseService;

    @Override
    public MesWmMaterialStockDO getMaterialStock(Long id) {
        return materialStockMapper.selectById(id);
    }

    @Override
    public List<MesWmMaterialStockDO> getMaterialStockListByAreaId(Long areaId) {
        return materialStockMapper.selectList(MesWmMaterialStockDO::getAreaId, areaId);
    }

    public MesWmMaterialStockDO validateMaterialStockExists(Long id) {
        MesWmMaterialStockDO stock = materialStockMapper.selectById(id);
        if (stock == null) {
            throw exception(WM_MATERIAL_STOCK_NOT_EXISTS);
        }
        return stock;
    }

    @Override
    public PageResult<MesWmMaterialStockDO> getMaterialStockPage(MesWmMaterialStockPageReqVO pageReqVO) {
        // 1.1 解析 itemTypeId：包含子分类
        Set<Long> itemTypeIds = null;
        if (pageReqVO.getItemTypeId() != null) {
            itemTypeIds = new HashSet<>();
            itemTypeIds.add(pageReqVO.getItemTypeId());
            List<MesMdItemTypeDO> children = itemTypeService.getItemTypeChildrenList(pageReqVO.getItemTypeId());
            itemTypeIds.addAll(convertSet(children, MesMdItemTypeDO::getId));
        }
        // 1.2 解析 itemId
        Set<Long> itemIds = null;
        if (pageReqVO.getItemId() != null) {
            itemIds = SetUtils.asSet(pageReqVO.getItemId());
        }
        // 1.3 解析 virtualFilter：转换为虚拟仓 warehouseId
        Long virtualWarehouseId = null;
        String virtualFilter = pageReqVO.getVirtualFilter();
        if (MesWmMaterialStockPageReqVO.VIRTUAL_FILTER_EXCLUDE.equals(virtualFilter)
                || MesWmMaterialStockPageReqVO.VIRTUAL_FILTER_ONLY.equals(virtualFilter)) {
            MesWmWarehouseDO virtualWarehouse = warehouseService.getWarehouseByCode(
                    MesWmWarehouseDO.WIP_VIRTUAL_WAREHOUSE);
            Assert.notNull(virtualWarehouse, "虚拟仓库（WIP_VIRTUAL_WAREHOUSE）不存在");
            virtualWarehouseId = virtualWarehouse.getId();
        }

        // 2. 分页查询
        return materialStockMapper.selectPage(pageReqVO, itemTypeIds, itemIds, virtualWarehouseId);
    }

    @Override
    public void updateMaterialStockFrozen(MesWmMaterialStockFreezeReqVO updateReqVO) {
        // 校验存在
        validateMaterialStockExists(updateReqVO.getId());

        // 更新冻结状态
        materialStockMapper.updateById(new MesWmMaterialStockDO()
                .setId(updateReqVO.getId()).setFrozen(updateReqVO.getFrozen()));
    }

    @Override
    public void updateMaterialStockFrozen(Collection<Long> ids, boolean frozen) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        materialStockMapper.updateByIds(ids, new MesWmMaterialStockDO().setFrozen(frozen));
    }

    @Override
    public Long getMaterialStockCountByWarehouseId(Long warehouseId) {
        return materialStockMapper.selectCountByWarehouseId(warehouseId);
    }

    @Override
    public Long getMaterialStockCountByLocationId(Long locationId) {
        return materialStockMapper.selectCountByLocationId(locationId);
    }

    @Override
    public Long getMaterialStockCountByAreaId(Long areaId) {
        return materialStockMapper.selectCountByAreaId(areaId);
    }

    @Override
    public List<MesWmMaterialStockDO> getMaterialStockList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return materialStockMapper.selectListByIds(ids);
    }

    @Override
    public List<MesWmMaterialStockDO> getMaterialStockList(MesWmMaterialStockListReqVO reqVO) {
        return materialStockMapper.selectList(reqVO);
    }

    @Override
    public MesWmMaterialStockDO getOrCreateMaterialStock(Long itemId, Long warehouseId, Long locationId, Long areaId,
                                                         Long batchId, String batchCode, Long vendorId, LocalDateTime receiptTime) {
        // 1. 查找已有库存记录
        MesWmMaterialStockDO stock = materialStockMapper.selectByCompositeKey(
                itemId, warehouseId, locationId, areaId, batchId);
        if (stock != null) {
            return stock;
        }

        // 2. 不存在则新建
        MesMdItemDO item = itemService.validateItemExists(itemId);
        MesWmMaterialStockDO newStock = MesWmMaterialStockDO.builder()
                .itemId(itemId).itemTypeId(item.getItemTypeId())
                .warehouseId(warehouseId).locationId(locationId).areaId(areaId)
                .batchId(batchId).batchCode(batchCode).vendorId(vendorId)
                .quantity(BigDecimal.ZERO) // 初始数量为 0，由 updateMaterialStockQuantity 更新
                .receiptTime(receiptTime != null ? receiptTime : LocalDateTime.now())
                .frozen(false)
                .build();
        materialStockMapper.insert(newStock);
        return newStock;
    }

    @Override
    public void updateMaterialStockQuantity(Long id, BigDecimal quantity, boolean checkFlag) {
        // 更新库存数量（Mapper 层 CAS 防负库存；checkFlag=false 时允许负数）
        int updatedRows = materialStockMapper.updateQuantity(id, quantity, checkFlag);
        if (updatedRows == 0) {
            throw exception(WM_MATERIAL_STOCK_INSUFFICIENT);
        }
    }

    @Override
    public void checkAreaMixingRule(Long areaId, Long itemId, Long batchId) {
        // 1.1 校验库位必须存在
        MesWmWarehouseAreaDO area = areaService.validateWarehouseAreaExists(areaId);

        // 2. 查询当前库位上已有的库存记录
        List<MesWmMaterialStockDO> stockList = getMaterialStockListByAreaId(areaId);
        if (CollUtil.isEmpty(stockList)) {
            return;
        }
        // 2.1 检查物料混放
        if (Boolean.FALSE.equals(area.getAllowItemMixing()) && itemId != null) {
            if (CollUtil.anyMatch(stockList, item -> ObjUtil.notEqual(itemId, item.getItemId()))) {
                throw exception(WM_MATERIAL_STOCK_AREA_ITEM_MIXING_NOT_ALLOWED, area.getName());
            }
        }
        // 2.2 检查批次混放
        if (Boolean.FALSE.equals(area.getAllowBatchMixing()) && batchId != null) {
            if (CollUtil.anyMatch(stockList, item -> ObjUtil.notEqual(batchId, item.getBatchId()))) {
                throw exception(WM_MATERIAL_STOCK_AREA_BATCH_MIXING_NOT_ALLOWED, area.getName());
            }
        }
    }

    @Override
    public MesWmMaterialStockDO validateSelectedStock(Long materialStockId,
                                                      Long itemId,
                                                      Long batchId,
                                                      String batchCode,
                                                      Long warehouseId,
                                                      Long locationId,
                                                      Long areaId,
                                                      BigDecimal quantity) {
        // 1. 校验库存记录必须存在
        if (materialStockId == null) {
            throw exception(WM_MATERIAL_STOCK_REQUIRED);
        }
        MesWmMaterialStockDO stock = validateMaterialStockExists(materialStockId);
        // 2. 校验库存记录的物料、批次、仓库、库区、库位等信息与前端选择的一致，避免串单或越权提交
        if (ObjUtil.notEqual(stock.getItemId(), itemId)
                || ObjUtil.notEqual(stock.getBatchId(), batchId)
                || (batchCode != null && ObjUtil.notEqual(stock.getBatchCode(), batchCode))
                || ObjUtil.notEqual(stock.getWarehouseId(), warehouseId)
                || ObjUtil.notEqual(stock.getLocationId(), locationId)
                || ObjUtil.notEqual(stock.getAreaId(), areaId)) {
            throw exception(WM_MATERIAL_STOCK_SELECTION_MISMATCH);
        }
        // 3. 校验库存数量充足（如果前端传了 quantity，则必须保证库存数量 >= quantity）
        if (quantity != null && stock.getQuantity() != null && stock.getQuantity().compareTo(quantity) < 0) {
            throw exception(WM_MATERIAL_STOCK_INSUFFICIENT);
        }
        return stock;
    }

}
