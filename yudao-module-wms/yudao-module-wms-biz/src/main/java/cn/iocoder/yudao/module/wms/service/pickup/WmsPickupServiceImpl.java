package cn.iocoder.yudao.module.wms.service.pickup;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.pickup.vo.WmsPickupSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.pickup.WmsPickupMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.pickup.item.WmsPickupItemMapper;
import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.quantity.PickupExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.PickupContext;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
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
import java.util.concurrent.atomic.AtomicReference;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_ITEM_INBOUND_ITEM_ID_NOT_SAME;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_ITEM_INBOUND_ITEM_ID_REPEATED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_ITEM_INBOUND_ITEM_ID_WAREHOUSE_ID_NOT_SAME;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_ITEM_QTY_ERROR;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.PICKUP_NO_DUPLICATE;

/**
 * 拣货单 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsPickupServiceImpl implements WmsPickupService {

    @Resource
    @Lazy
    private WmsPickupItemMapper pickupItemMapper;

    @Resource
    protected WmsLockRedisDAO lockRedisDAO;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsPickupMapper pickupMapper;

    @Resource
    @Lazy
    private WmsInboundItemService inboundItemService;

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    @Lazy
    private WmsWarehouseBinService wmsWarehouseBinService;

    @Resource
    @Lazy
    private PickupExecutor pickupExecutor;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    /**
     * @sign : E7A4B1135281D8DB
     */
    @Override
    public WmsPickupDO createPickup(WmsPickupSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.PICKUP_NO_PREFIX, 3);
        createReqVO.setNo(no);
        if (pickupMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(PICKUP_NO_DUPLICATE);
        }
        if (CollectionUtils.isEmpty(createReqVO.getItemList())) {
            throw exception(PICKUP_ITEM_NOT_EXISTS);
        }
        // 保存拣货单详情详情
        List<WmsPickupItemDO> toInsetList = new ArrayList<>();
        StreamX.from(createReqVO.getItemList()).filter(Objects::nonNull).forEach(item -> {
            item.setId(null);
            // 设置归属
            toInsetList.add(BeanUtils.toBean(item, WmsPickupItemDO.class));
        });
        // 校验 toInsetList 中是否有重复的 inboundItemId
        boolean isInboundItemIdRepeated = StreamX.isRepeated(toInsetList, WmsPickupItemDO::getInboundItemId);
        if (isInboundItemIdRepeated) {
            throw exception(PICKUP_ITEM_INBOUND_ITEM_ID_REPEATED);
        }
        WmsPickupDO pickup = BeanUtils.toBean(createReqVO, WmsPickupDO.class);
        // 校验入库单与仓位的仓库必须是同一个仓库
        List<WmsInboundItemDO> inboundItemDOList = processAndValidateForPickIn(pickup, toInsetList);
        WmsPickupServiceImpl proxy = SpringUtils.getBeanByExactType(WmsPickupServiceImpl.class);
        AtomicReference<WmsPickupDO> pickupDO = new AtomicReference<>();
        lockRedisDAO.lockByWarehouse(pickup.getWarehouseId(), () -> {
            pickupDO.set(proxy.createPickupInLock(pickup, toInsetList));
        });
        return pickupDO.get();
    }

    @Transactional(rollbackFor = Exception.class)
    protected WmsPickupDO createPickupInLock(WmsPickupDO pickup, List<WmsPickupItemDO> toInsetList) {
        // 取得锁之后需要重新取数
        List<Long> inboundItemIdList = StreamX.from(toInsetList).toList(WmsPickupItemDO::getInboundItemId);
        List<WmsInboundItemDO> inboundItemDOList = inboundItemService.selectByIds(inboundItemIdList);
        // 插入
        pickupMapper.insert(pickup);
        toInsetList.forEach(wmsPickupItemDO -> {
            wmsPickupItemDO.setPickupId(pickup.getId());
        });
        // 保存单据，使产生ID
        pickupItemMapper.insertBatch(toInsetList);
        // 拣货到仓位
        this.pickup(pickup, toInsetList, BeanUtils.toBean(inboundItemDOList, WmsInboundItemRespVO.class));
        // 再次保存
        pickupItemMapper.updateBatch(toInsetList);
        // 返回
        return pickup;
    }

    private void pickup(WmsPickupDO pickup, List<WmsPickupItemDO> wmsPickupItemDOList, List<WmsInboundItemRespVO> inboundItemVOList) {
        PickupContext context = new PickupContext();
        context.setWmsPickupItemDOList(wmsPickupItemDOList);
        context.setInboundItemVOList(inboundItemVOList);
        context.setPickup(pickup);
        pickupExecutor.execute(context);
    }

    private List<WmsInboundItemDO> processAndValidateForPickIn(WmsPickupDO pickup, List<WmsPickupItemDO> toInsetList) {
        // 准备数据
        List<Long> inboundItemIdList = StreamX.from(toInsetList).toList(WmsPickupItemDO::getInboundItemId);
        List<WmsInboundItemDO> inboundItemDOList = inboundItemService.selectByIds(inboundItemIdList);
        List<Long> inboundIdList = StreamX.from(inboundItemDOList).toList(WmsInboundItemDO::getInboundId);
        List<WmsInboundDO> inboundDOList = inboundService.selectByIds(inboundIdList);
        Set<Long> warehouseIdSetOfInboundItem = StreamX.from(inboundDOList).toSet(WmsInboundDO::getWarehouseId);
        List<Long> binIdList = StreamX.from(toInsetList).toList(WmsPickupItemDO::getBinId);
        List<WmsWarehouseBinDO> wmsWarehouseBinDOList = wmsWarehouseBinService.selectByIds(binIdList);
        Set<Long> warehouseIdSetOfBin = StreamX.from(wmsWarehouseBinDOList).toSet(WmsWarehouseBinDO::getWarehouseId);
        // 校验仓库
        if (warehouseIdSetOfInboundItem.size() != 1) {
            throw exception(PICKUP_ITEM_INBOUND_ITEM_ID_WAREHOUSE_ID_NOT_SAME);
        }
        if (warehouseIdSetOfBin.size() != 1) {
            throw exception(PICKUP_ITEM_INBOUND_ITEM_ID_WAREHOUSE_ID_NOT_SAME);
        }
        Long warehouseIdOfInboundItem = StreamX.from(warehouseIdSetOfInboundItem).first();
        Long warehouseIdOfBin = StreamX.from(warehouseIdSetOfBin).first();
        if (!Objects.equals(warehouseIdOfInboundItem, warehouseIdOfBin)) {
            throw exception(PICKUP_ITEM_INBOUND_ITEM_ID_WAREHOUSE_ID_NOT_SAME);
        }
        // 校验拣货单明细是否对应
        if (toInsetList.size() != inboundItemDOList.size()) {
            throw exception(PICKUP_ITEM_INBOUND_ITEM_ID_NOT_SAME);
        }
        // 校验数量
        for (WmsPickupItemDO itemDO : toInsetList) {
            if (itemDO.getQty() == null || itemDO.getQty() <= 0) {
                throw exception(PICKUP_ITEM_QTY_ERROR);
            }
        }
        // 设置仓库ID
        pickup.setWarehouseId(warehouseIdOfInboundItem);
        return inboundItemDOList;
    }

    /**
     * @sign : 2E601EBDA1614D4E
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsPickupDO updatePickup(WmsPickupSaveReqVO updateReqVO) {
        // 校验存在
        WmsPickupDO exists = validatePickupExists(updateReqVO.getId());
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 保存拣货单详情详情
        if (updateReqVO.getItemList() != null) {
            List<WmsPickupItemDO> existsInDB = pickupItemMapper.selectByPickupId(updateReqVO.getId());
            StreamX.CompareResult<WmsPickupItemDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getItemList(), WmsPickupItemDO.class), WmsPickupItemDO::getId);
            List<WmsPickupItemDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsPickupItemDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsPickupItemDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsPickupItemDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 设置归属
            finalList.forEach(item -> {
                item.setPickupId(updateReqVO.getId());
            });
            // 保存详情
            pickupItemMapper.insertBatch(toInsetList);
            pickupItemMapper.updateBatch(toUpdateList);
            pickupItemMapper.deleteBatchIds(toDeleteList);
        }
        // 更新
        WmsPickupDO pickup = BeanUtils.toBean(updateReqVO, WmsPickupDO.class);
        pickupMapper.updateById(pickup);
        // 返回
        return pickup;
    }

    /**
     * @sign : E178F96C953E3D97
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePickup(Long id) {
        // 校验存在
        WmsPickupDO pickup = validatePickupExists(id);
        // 唯一索引去重
        pickup.setNo(pickupMapper.flagUKeyAsLogicDelete(pickup.getNo()));
        pickupMapper.updateById(pickup);
        // 删除
        pickupMapper.deleteById(id);
    }

    /**
     * @sign : ACE5C762DE808407
     */
    private WmsPickupDO validatePickupExists(Long id) {
        WmsPickupDO pickup = pickupMapper.selectById(id);
        if (pickup == null) {
            throw exception(PICKUP_NOT_EXISTS);
        }
        return pickup;
    }

    @Override
    public WmsPickupDO getPickup(Long id) {
        return pickupMapper.selectById(id);
    }

    @Override
    public PageResult<WmsPickupDO> getPickupPage(WmsPickupPageReqVO pageReqVO) {
        return pickupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsPickupDO> selectByIds(List<Long> list) {
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        return pickupMapper.selectByIds(list);
    }

    @Override
    public void assembleWarehouse(List<WmsPickupRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsPickupRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsPickupRespVO::getWarehouseId, WmsPickupRespVO::setWarehouse);
    }

    @Override
    public void createForInventory(WmsPickupSaveReqVO pickupSaveReqVO) {
        this.createPickup(pickupSaveReqVO);
    }
}
