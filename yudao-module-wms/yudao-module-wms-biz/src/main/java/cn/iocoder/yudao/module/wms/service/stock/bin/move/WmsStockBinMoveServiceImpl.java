package cn.iocoder.yudao.module.wms.service.stock.bin.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.WmsStockBinMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.move.item.WmsStockBinMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.WmsStockBinMoveMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.bin.move.item.WmsStockBinMoveItemMapper;
import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsMoveExecuteStatus;
import cn.iocoder.yudao.module.wms.service.quantity.BinMoveExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.BinMoveContext;
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
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_ITEM_REPEATED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_MOVE_NO_DUPLICATE;

/**
 * 库位移动 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockBinMoveServiceImpl implements WmsStockBinMoveService {

    @Resource
    @Lazy
    private WmsStockBinMoveItemMapper stockBinMoveItemMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsStockBinMoveMapper stockBinMoveMapper;

    @Resource
    private BinMoveExecutor binMoveExecutor;

    @Resource
    protected WmsLockRedisDAO lockRedisDAO;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    /**
     * @sign : 9EAFE43CD7993903
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsStockBinMoveDO createStockBinMove(WmsStockBinMoveSaveReqVO createReqVO) {

        WmsStockBinMoveDO stockBinMoveDO = lockRedisDAO.lockByWarehouse(createReqVO.getWarehouseId(),()->{
            return createStockBinMoveInLock(createReqVO);
        });

        return stockBinMoveDO;
    }

    private WmsStockBinMoveDO createStockBinMoveInLock(WmsStockBinMoveSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.STOCK_BIN_MOVE_NO_PREFIX, 3);
        createReqVO.setNo(no);
        // 指定初始状态
        createReqVO.setExecuteStatus(WmsMoveExecuteStatus.DRAFT.getValue());
        if (stockBinMoveMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(STOCK_BIN_MOVE_NO_DUPLICATE);
        }
        // 插入
        final WmsStockBinMoveDO stockBinMove = BeanUtils.toBean(createReqVO, WmsStockBinMoveDO.class);
        stockBinMoveMapper.insert(stockBinMove);
        // 保存库位移动详情详情
        if (createReqVO.getItemList() != null) {
            List<WmsStockBinMoveItemDO> toInsetList = new ArrayList<>();
            StreamX.from(createReqVO.getItemList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setBinMoveId(stockBinMove.getId());
                toInsetList.add(BeanUtils.toBean(item, WmsStockBinMoveItemDO.class));
            });
            Set<String> uniqueKeys = StreamX.from(toInsetList).toSet(itm -> {
                return StrUtils.join(Arrays.asList(itm.getProductId(), itm.getFromBinId(), itm.getToBinId()));
            });
            // 校验有重复的清单
            if (uniqueKeys.size() != toInsetList.size()) {
                throw exception(STOCK_BIN_MOVE_ITEM_REPEATED);
            }
            stockBinMoveItemMapper.insertBatch(toInsetList);
        }

        // 重新读取DO
        WmsStockBinMoveDO newStockBinMove = stockBinMoveMapper.selectById(stockBinMove.getId());
        List<WmsStockBinMoveItemDO> binMoveItemDOS = stockBinMoveItemMapper.selectByBinMoveId(newStockBinMove.getId());

        // 执行库位移动
        BinMoveContext context = new BinMoveContext();
        context.setBinMoveDO(newStockBinMove);
        context.setBinMoveItemDOList(binMoveItemDOS);
        binMoveExecutor.execute(context);

        // 返回
        return stockBinMove;
    }

    /**
     * @sign : CF76F2A3D4230C8A
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsStockBinMoveDO updateStockBinMove(WmsStockBinMoveSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockBinMoveDO exists = validateStockBinMoveExists(updateReqVO.getId());
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 保存库位移动详情详情
        if (updateReqVO.getItemList() != null) {
            List<WmsStockBinMoveItemDO> existsInDB = stockBinMoveItemMapper.selectByBinMoveId(updateReqVO.getId());
            StreamX.CompareResult<WmsStockBinMoveItemDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getItemList(), WmsStockBinMoveItemDO.class), WmsStockBinMoveItemDO::getId);
            List<WmsStockBinMoveItemDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsStockBinMoveItemDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsStockBinMoveItemDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsStockBinMoveItemDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 校验 toInsetList 中是否有重复的 productId
            Set<String> uniqueKeys = StreamX.from(finalList).toSet(itm -> {
                return StrUtils.join(Arrays.asList(itm.getProductId(), itm.getFromBinId(), itm.getToBinId()));
            });
            // 校验有重复的清单
            if (uniqueKeys.size() != toInsetList.size()) {
                throw exception(STOCK_BIN_MOVE_ITEM_REPEATED);
            }
            // 设置归属
            finalList.forEach(item -> {
                item.setBinMoveId(updateReqVO.getId());
            });
            // 保存详情
            stockBinMoveItemMapper.insertBatch(toInsetList);
            stockBinMoveItemMapper.updateBatch(toUpdateList);
            stockBinMoveItemMapper.deleteBatchIds(toDeleteList);
        }
        // 更新
        WmsStockBinMoveDO stockBinMove = BeanUtils.toBean(updateReqVO, WmsStockBinMoveDO.class);
        stockBinMoveMapper.updateById(stockBinMove);
        // 返回
        return stockBinMove;
    }

    /**
     * @sign : D02CAA96D65F5B67
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockBinMove(Long id) {
        // 校验存在
        WmsStockBinMoveDO stockBinMove = validateStockBinMoveExists(id);
        // 唯一索引去重
        stockBinMove.setNo(stockBinMoveMapper.flagUKeyAsLogicDelete(stockBinMove.getNo()));
        stockBinMoveMapper.updateById(stockBinMove);
        // 删除
        stockBinMoveMapper.deleteById(id);
    }

    /**
     * @sign : BE64F6298385B449
     */
    private WmsStockBinMoveDO validateStockBinMoveExists(Long id) {
        WmsStockBinMoveDO stockBinMove = stockBinMoveMapper.selectById(id);
        if (stockBinMove == null) {
            throw exception(STOCK_BIN_MOVE_NOT_EXISTS);
        }
        return stockBinMove;
    }

    @Override
    public WmsStockBinMoveDO getStockBinMove(Long id) {
        return stockBinMoveMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockBinMoveDO> getStockBinMovePage(WmsStockBinMovePageReqVO pageReqVO) {
        return stockBinMoveMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockBinMoveDO
     */
    public List<WmsStockBinMoveDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockBinMoveMapper.selectByIds(idList);
    }

    /**
     * 按 ID 集合查询 WmsStockBinMoveDO
     */
    public List<WmsStockBinMoveDO> selectSimpleList(WmsStockBinMovePageReqVO reqVO) {
        return stockBinMoveMapper.selectSimpleList(reqVO);
    }

    /**
     * 完成库位移动
     **/
    @Override
    public void finishMove(WmsStockBinMoveDO binMoveDO, List<WmsStockBinMoveItemDO> binMoveItemDOList) {
        binMoveDO.setExecuteStatus(WmsMoveExecuteStatus.MOVED.getValue());
        stockBinMoveMapper.updateById(binMoveDO);
    }

    @Override
    public void assembleWarehouse(List<WmsStockBinMoveRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockBinMoveRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values())
            .toMap(WmsWarehouseDO::getId, v-> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));

        StreamX.from(list).assemble(warehouseVOMap, WmsStockBinMoveRespVO::getWarehouseId, WmsStockBinMoveRespVO::setWarehouse);
    }
}
