package cn.iocoder.yudao.module.wms.service.stock.ownership.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.ownership.move.vo.WmsStockOwnershipMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.WmsStockOwnershipMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.move.item.WmsStockOwnershipMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership.move.WmsStockOwnershipMoveMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.ownership.move.item.WmsStockOwnershipMoveItemMapper;
import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsMoveExecuteStatus;
import cn.iocoder.yudao.module.wms.service.quantity.OwnershipMoveExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.OwnershipMoveContext;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_ITEM_REPEATED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_OWNERSHIP_MOVE_NO_DUPLICATE;

/**
 * 所有者库存移动 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockOwnershipMoveServiceImpl implements WmsStockOwnershipMoveService {

    @Resource
    @Lazy
    private WmsStockOwnershipMoveItemMapper stockOwnershipMoveItemMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsStockOwnershipMoveMapper stockOwnershipMoveMapper;

    @Resource
    private OwnershipMoveExecutor ownershipMoveExecutor;

    @Resource
    protected WmsLockRedisDAO lockRedisDAO;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    /**
     * @sign : A2B315CE08CA7A20
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsStockOwnershipMoveDO createStockOwnershipMove(WmsStockOwnershipMoveSaveReqVO createReqVO) {
        WmsStockOwnershipMoveDO stockOwnershipMoveDO = lockRedisDAO.lockByWarehouse(createReqVO.getWarehouseId(), () -> {
            return createStockOwnershipMoveInLock(createReqVO);
        });
        return stockOwnershipMoveDO;
    }

    @Transactional(rollbackFor = Exception.class)
    public WmsStockOwnershipMoveDO createStockOwnershipMoveInLock(WmsStockOwnershipMoveSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.STOCK_OWNERSHIP_MOVE_NO_PREFIX, 3);
        createReqVO.setNo(no);
        // 指定初始状态
        createReqVO.setExecuteStatus(WmsMoveExecuteStatus.DRAFT.getValue());
        // 
        if (stockOwnershipMoveMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(STOCK_OWNERSHIP_MOVE_NO_DUPLICATE);
        }
        // 插入
        WmsStockOwnershipMoveDO stockOwnershipMove = BeanUtils.toBean(createReqVO, WmsStockOwnershipMoveDO.class);
        stockOwnershipMoveMapper.insert(stockOwnershipMove);
        // 保存所有者库存移动详情详情
        if (createReqVO.getItemList() != null) {
            List<WmsStockOwnershipMoveItemDO> toInsetList = new ArrayList<>();
            StreamX.from(createReqVO.getItemList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setOwnershipMoveId(stockOwnershipMove.getId());
                toInsetList.add(BeanUtils.toBean(item, WmsStockOwnershipMoveItemDO.class));
            });
            // 重复校验
            Set<String> uniqueKeys = StreamX.from(toInsetList).toSet(itm -> {
                return StrUtils.join(Arrays.asList(itm.getProductId(), itm.getFromCompanyId(), itm.getFromDeptId(), itm.getToCompanyId(), itm.getToDeptId()));
            });
            if (uniqueKeys.size() != toInsetList.size()) {
                throw exception(STOCK_OWNERSHIP_MOVE_ITEM_REPEATED);
            }
            stockOwnershipMoveItemMapper.insertBatch(toInsetList);
        }
        // 重新读取DO
        WmsStockOwnershipMoveDO newStockBinMove = stockOwnershipMoveMapper.selectById(stockOwnershipMove.getId());
        List<WmsStockOwnershipMoveItemDO> binMoveItemDOS = stockOwnershipMoveItemMapper.selectByOwnershipMoveId(newStockBinMove.getId());
        OwnershipMoveContext ownershipMoveContext = new OwnershipMoveContext();
        ownershipMoveContext.setOwnershipMoveDO(newStockBinMove);
        ownershipMoveContext.setOwnershipMoveItemDOS(binMoveItemDOS);
        ownershipMoveExecutor.execute(ownershipMoveContext);
        // 返回
        return stockOwnershipMove;
    }

    /**
     * @sign : 0F3263313629D9D4
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsStockOwnershipMoveDO updateStockOwnershipMove(WmsStockOwnershipMoveSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockOwnershipMoveDO exists = validateStockOwnershipMoveExists(updateReqVO.getId());
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 保存所有者库存移动详情详情
        if (updateReqVO.getItemList() != null) {
            List<WmsStockOwnershipMoveItemDO> existsInDB = stockOwnershipMoveItemMapper.selectByOwnershipMoveId(updateReqVO.getId());
            StreamX.CompareResult<WmsStockOwnershipMoveItemDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getItemList(), WmsStockOwnershipMoveItemDO.class), WmsStockOwnershipMoveItemDO::getId);
            List<WmsStockOwnershipMoveItemDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsStockOwnershipMoveItemDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsStockOwnershipMoveItemDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsStockOwnershipMoveItemDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 重复校验
            Set<String> uniqueKeys = StreamX.from(finalList).toSet(itm -> {
                return StrUtils.join(Arrays.asList(itm.getProductId(), itm.getFromCompanyId(), itm.getFromDeptId(), itm.getToCompanyId(), itm.getToDeptId()));
            });
            if (uniqueKeys.size() != finalList.size()) {
                throw exception(STOCK_OWNERSHIP_MOVE_ITEM_REPEATED);
            }
            // 设置归属
            finalList.forEach(item -> {
                item.setOwnershipMoveId(updateReqVO.getId());
            });
            // 保存详情
            stockOwnershipMoveItemMapper.insertBatch(toInsetList);
            stockOwnershipMoveItemMapper.updateBatch(toUpdateList);
            stockOwnershipMoveItemMapper.deleteBatchIds(toDeleteList);
        }
        // 更新
        WmsStockOwnershipMoveDO stockOwnershipMove = BeanUtils.toBean(updateReqVO, WmsStockOwnershipMoveDO.class);
        stockOwnershipMoveMapper.updateById(stockOwnershipMove);
        // 返回
        return stockOwnershipMove;
    }

    /**
     * @sign : 28B0CE5244DC7876
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockOwnershipMove(Long id) {
        // 校验存在
        WmsStockOwnershipMoveDO stockOwnershipMove = validateStockOwnershipMoveExists(id);
        // 唯一索引去重
        stockOwnershipMove.setNo(stockOwnershipMoveMapper.flagUKeyAsLogicDelete(stockOwnershipMove.getNo()));
        stockOwnershipMoveMapper.updateById(stockOwnershipMove);
        // 删除
        stockOwnershipMoveMapper.deleteById(id);
    }

    /**
     * @sign : E902C86470C6D000
     */
    private WmsStockOwnershipMoveDO validateStockOwnershipMoveExists(Long id) {
        WmsStockOwnershipMoveDO stockOwnershipMove = stockOwnershipMoveMapper.selectById(id);
        if (stockOwnershipMove == null) {
            throw exception(STOCK_OWNERSHIP_MOVE_NOT_EXISTS);
        }
        return stockOwnershipMove;
    }

    @Override
    public WmsStockOwnershipMoveDO getStockOwnershipMove(Long id) {
        return stockOwnershipMoveMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockOwnershipMoveDO> getStockOwnershipMovePage(WmsStockOwnershipMovePageReqVO pageReqVO) {
        return stockOwnershipMoveMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockOwnershipMoveDO
     */
    public List<WmsStockOwnershipMoveDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockOwnershipMoveMapper.selectByIds(idList);
    }

    @Override
    public void finishMove(WmsStockOwnershipMoveDO ownershipMoveDO, List<WmsStockOwnershipMoveItemDO> ownershipMoveItemDOList) {
        ownershipMoveDO.setExecuteStatus(WmsMoveExecuteStatus.MOVED.getValue());
        stockOwnershipMoveMapper.updateById(ownershipMoveDO);
    }

    @Override
    public void assembleWarehouse(List<WmsStockOwnershipMoveRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockOwnershipMoveRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values())
            .toMap(WmsWarehouseDO::getId, v-> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));

        StreamX.from(list).assemble(warehouseVOMap, WmsStockOwnershipMoveRespVO::getWarehouseId, WmsStockOwnershipMoveRespVO::setWarehouse);
    }
}
