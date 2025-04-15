package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.config.InventoryStateMachineConfigure;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventorySaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.product.WmsInventoryProductDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.WmsInventoryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.bin.WmsInventoryBinMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.product.WmsInventoryProductMapper;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.WmsConstants;
import cn.iocoder.yudao.module.wms.enums.common.WmsBillType;
import cn.iocoder.yudao.module.wms.enums.inventory.WmsInventoryAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_CAN_NOT_DELETE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_CAN_NOT_EDIT;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_NO_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_PRODUCT_EXISTS;

/**
 * 盘点 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInventoryServiceImpl implements WmsInventoryService {

    @Resource
    @Lazy
    private WmsInventoryBinMapper inventoryBinMapper;

    @Resource
    @Lazy
    private WmsInventoryProductMapper inventoryProductMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsInventoryMapper inventoryMapper;

    @Resource
    private WmsStockWarehouseService stockWarehouseService;

    @Resource
    private WmsStockBinService stockBinService;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource(name = InventoryStateMachineConfigure.STATE_MACHINE_NAME)
    private StateMachineWrapper<Integer, WmsInventoryAuditStatus.Event, WmsInventoryDO> inventoryStateMachine;


    /**
     * @sign : A9D51C9E0E654C80
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsInventoryDO createInventory(WmsInventorySaveReqVO createReqVO) {
        // 设置单据号等初始值
        String no = noRedisDAO.generate(WmsNoRedisDAO.INVENTORY_NO_PREFIX, 3);
        createReqVO.setNo(no);
        createReqVO.setAuditStatus(WmsInventoryAuditStatus.DRAFT.getValue());
        // 
        if (inventoryMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(INVENTORY_NO_DUPLICATE);
        }
        // 插入
        WmsInventoryDO inventory = BeanUtils.toBean(createReqVO, WmsInventoryDO.class);
        inventoryMapper.insert(inventory);
        // 保存库存盘点产品详情
        List<WmsInventoryProductDO> toInsetList = new ArrayList<>();
        if (createReqVO.getProductItemList() != null) {
            StreamX.from(createReqVO.getProductItemList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setInventoryId(inventory.getId());
                item.setExpectedQty(0);
                item.setActualQty(0);
                toInsetList.add(BeanUtils.toBean(item, WmsInventoryProductDO.class));
            });
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInventoryProductDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(INVENTORY_PRODUCT_EXISTS);
            }
            inventoryProductMapper.insertBatch(toInsetList);
        }
        // 保存盘点的其它详情信息
        this.saveInventoryExtra(inventory);
        // 返回
        return inventory;
    }

    private void saveInventoryExtra(WmsInventoryDO inventory) {
        // 处理盘点的产品详情，保存预期库存
        List<WmsInventoryProductDO> inventoryProductDOList = inventoryProductMapper.selectByInventoryId(inventory.getId());
        List<WmsStockWarehouseDO> wmsStockWarehouseDOS = stockWarehouseService.selectByWarehouse(inventory.getWarehouseId());
        Map<Long, WmsStockWarehouseDO> wmsStockWarehouseDOMap = StreamX.from(wmsStockWarehouseDOS).toMap(WmsStockWarehouseDO::getId);
        for (WmsInventoryProductDO inventoryProductDO : inventoryProductDOList) {
            WmsStockWarehouseDO wmsStockWarehouseDO = wmsStockWarehouseDOMap.get(inventoryProductDO.getProductId());
            if (wmsStockWarehouseDO == null) {
                inventoryProductDO.setExpectedQty(0);
            } else {
                inventoryProductDO.setExpectedQty(wmsStockWarehouseDO.getAvailableQty());
            }
        }
        if(!CollectionUtils.isEmpty(inventoryProductDOList)) {
            inventoryProductMapper.updateBatch(inventoryProductDOList);
        }
        // 分解库存到仓位
        List<WmsWarehouseProductVO> wmsWarehouseProductVOList = new ArrayList<>();
        for (WmsInventoryProductDO inventoryProductDO : inventoryProductDOList) {
            wmsWarehouseProductVOList.add(WmsWarehouseProductVO.builder().warehouseId(inventory.getWarehouseId()).productId(inventoryProductDO.getProductId()).build());
        }
        // 数据库里已经有的
        List<WmsInventoryBinDO> allInventoryBinDOListInDB = inventoryBinMapper.selectByInventoryId(inventory.getId());
        Set<String> binProductIdSet = StreamX.from(allInventoryBinDOListInDB).toSet(e -> e.getBinId() + "-" + e.getProductId());
        // 仓位库存清单
        List<WmsStockBinRespVO> stockBinList = stockBinService.selectStockBinList(wmsWarehouseProductVOList, false);
        // 获得需要插入的部分：仓位库存里有，但数据库里没有的
        List<WmsInventoryBinDO> toInsertInventoryBinDOList = new ArrayList<>();
        for (WmsStockBinRespVO stockBinRespVO : stockBinList) {
            // 已经存在的不需要加入清单
            if (binProductIdSet.contains(stockBinRespVO.getBinId() + "-" + stockBinRespVO.getProductId())) {
                continue;
            }
            WmsInventoryBinDO inventoryBinDO = new WmsInventoryBinDO();
            inventoryBinDO.setInventoryId(inventory.getId());
            inventoryBinDO.setProductId(stockBinRespVO.getProductId());
            inventoryBinDO.setBinId(stockBinRespVO.getBinId());
            inventoryBinDO.setExpectedQty(stockBinRespVO.getAvailableQty());
            inventoryBinDO.setActualQuantity(0);
            toInsertInventoryBinDOList.add(inventoryBinDO);
        }
        // 需要删除的部分：数据库里有，但仓位库存里面没有的
        List<WmsInventoryBinDO> toDeleteInventoryBinDOList = new ArrayList<>();
        binProductIdSet = StreamX.from(stockBinList).toSet(e -> e.getBinId() + "-" + e.getProductId());
        for (WmsInventoryBinDO stockBinRespVO : allInventoryBinDOListInDB) {
            if (!binProductIdSet.contains(stockBinRespVO.getBinId() + "-" + stockBinRespVO.getProductId())) {
                toDeleteInventoryBinDOList.add(stockBinRespVO);
            }
        }
        // 保存库存盘点产品详情
        inventoryBinMapper.insertBatch(toInsertInventoryBinDOList);
        inventoryBinMapper.deleteBatchIds(toDeleteInventoryBinDOList);
    }

    /**
     * @sign : 2710B20EC7D9E031
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsInventoryDO updateInventory(WmsInventorySaveReqVO updateReqVO) {
        // 校验存在
        WmsInventoryDO exists = validateInventoryExists(updateReqVO.getId());
        // 校验可否编辑
        WmsInventoryAuditStatus inventoryAuditStatus = WmsInventoryAuditStatus.parse(exists.getAuditStatus());
        if (inventoryAuditStatus.matchAny(WmsInventoryAuditStatus.AUDITING, WmsInventoryAuditStatus.PASS)) {
            throw exception(INVENTORY_CAN_NOT_EDIT);
        }
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 保存库存盘点产品详情
        if (updateReqVO.getProductItemList() != null) {
            List<WmsInventoryProductDO> existsInDB = inventoryProductMapper.selectByInventoryId(updateReqVO.getId());
            StreamX.CompareResult<WmsInventoryProductDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getProductItemList(), WmsInventoryProductDO.class), WmsInventoryProductDO::getId);
            List<WmsInventoryProductDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsInventoryProductDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsInventoryProductDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsInventoryProductDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInventoryProductDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(INVENTORY_PRODUCT_EXISTS);
            }
            // 设置归属
            finalList.forEach(item -> {
                if(item.getExpectedQty()==null) {
                    item.setExpectedQty(0);
                }
                if(item.getActualQty()==null) {
                    item.setActualQty(0);
                }
                item.setInventoryId(updateReqVO.getId());
            });
            // 保存详情
            inventoryProductMapper.insertBatch(toInsetList);
            inventoryProductMapper.updateBatch(toUpdateList);
            inventoryProductMapper.deleteBatchIds(toDeleteList);
        }
        // 更新
        WmsInventoryDO inventory = BeanUtils.toBean(updateReqVO, WmsInventoryDO.class);
        inventoryMapper.updateById(inventory);
        // 保存盘点的其它详情信息
        this.saveInventoryExtra(inventory);
        // 返回
        return inventory;
    }

    /**
     * @sign : 159065E285D50040
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInventory(Long id) {
        // 校验存在
        WmsInventoryDO inventory = validateInventoryExists(id);
        // 校验可否编辑
        WmsInventoryAuditStatus inventoryAuditStatus = WmsInventoryAuditStatus.parse(inventory.getAuditStatus());
        if (inventoryAuditStatus.matchAny(WmsInventoryAuditStatus.AUDITING, WmsInventoryAuditStatus.PASS)) {
            throw exception(INVENTORY_CAN_NOT_DELETE);
        }
        // 唯一索引去重
        inventory.setNo(inventoryMapper.flagUKeyAsLogicDelete(inventory.getNo()));
        inventoryMapper.updateById(inventory);
        // 删除
        inventoryMapper.deleteById(id);
    }

    /**
     * @sign : CCF673C00F6357F0
     */
    public WmsInventoryDO validateInventoryExists(Long id) {
        WmsInventoryDO inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw exception(INVENTORY_NOT_EXISTS);
        }
        return inventory;
    }

    @Override
    public WmsInventoryDO getInventory(Long id) {
        return inventoryMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryDO> getInventoryPage(WmsInventoryPageReqVO pageReqVO) {
        return inventoryMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsInventoryDO
     */
    public List<WmsInventoryDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return inventoryMapper.selectByIds(idList);
    }

    @Override
    public void assembleWarehouse(List<WmsInventoryRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsInventoryRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsInventoryRespVO::getWarehouseId, WmsInventoryRespVO::setWarehouse);
    }

    @Override
    public WmsInventoryDO updateOutboundAuditStatus(Long id, Integer status) {
        WmsInventoryDO inventoryDO = validateInventoryExists(id);
        inventoryDO.setAuditStatus(status);
        inventoryMapper.updateById(inventoryDO);
        return inventoryDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(WmsInventoryAuditStatus.Event event, WmsApprovalReqVO approvalReqVO) {
        // 设置业务默认值
        approvalReqVO.setBillType(WmsBillType.OUTBOUND.getValue());
        approvalReqVO.setStatusType(WmsOutboundAuditStatus.getType());
        // 获得业务对象
        WmsInventoryDO inbound = validateInventoryExists(approvalReqVO.getBillId());

        TransitionContext<WmsInventoryDO> ctx = inventoryStateMachine.createContext(inbound);
        ctx.setExtra(WmsConstants.APPROVAL_REQ_VO_KEY, approvalReqVO);
        // 触发事件
        inventoryStateMachine.fireEvent(event, ctx);
    }



}
