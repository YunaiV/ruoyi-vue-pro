package cn.iocoder.yudao.module.wms.service.stock.logic.move;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.string.StrUtils;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMovePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMoveRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.logic.move.vo.WmsStockLogicMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.WmsStockLogicMoveDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.logic.move.item.WmsStockLogicMoveItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.logic.move.WmsStockLogicMoveMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.logic.move.item.WmsStockLogicMoveItemMapper;
import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.stock.WmsMoveExecuteStatus;
import cn.iocoder.yudao.module.wms.service.quantity.LogicMoveExecutor;
import cn.iocoder.yudao.module.wms.service.quantity.context.LogicMoveContext;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * 逻辑库存移动 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockLogicMoveServiceImpl implements cn.iocoder.yudao.module.wms.service.stock.logic.move.WmsStockLogicMoveService {

    @Resource
    @Lazy
    private WmsStockLogicMoveItemMapper stockLogicMoveItemMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsStockLogicMoveMapper stockLogicMoveMapper;

    @Resource
    private LogicMoveExecutor logicMoveExecutor;

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
    public WmsStockLogicMoveDO createStockLogicMove(WmsStockLogicMoveSaveReqVO createReqVO) {
        return lockRedisDAO.lockByWarehouse(createReqVO.getWarehouseId(), () -> {
            return createStockLogicMoveInLock(createReqVO);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public WmsStockLogicMoveDO createStockLogicMoveInLock(WmsStockLogicMoveSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.STOCK_LOGIC_MOVE_NO_PREFIX, 6);
        createReqVO.setNo(no);
        // 指定初始状态
        createReqVO.setExecuteStatus(WmsMoveExecuteStatus.DRAFT.getValue());
        // 
        if (stockLogicMoveMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(STOCK_LOGIC_MOVE_NO_DUPLICATE);
        }
        // 插入
        WmsStockLogicMoveDO stockLogicMove = BeanUtils.toBean(createReqVO, WmsStockLogicMoveDO.class);
        stockLogicMoveMapper.insert(stockLogicMove);
        // 保存逻辑库存移动详情详情
        if (createReqVO.getItemList() != null) {
            List<WmsStockLogicMoveItemDO> toInsetList = new ArrayList<>();
            StreamX.from(createReqVO.getItemList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setLogicMoveId(stockLogicMove.getId());
                toInsetList.add(BeanUtils.toBean(item, WmsStockLogicMoveItemDO.class));
            });
            // 重复校验
            Set<String> uniqueKeys = StreamX.from(toInsetList).toSet(itm -> {
                return StrUtils.join(Arrays.asList(itm.getProductId(), itm.getFromCompanyId(), itm.getFromDeptId(), itm.getToCompanyId(), itm.getToDeptId()));
            });
            if (uniqueKeys.size() != toInsetList.size()) {
                throw exception(STOCK_LOGIC_MOVE_ITEM_REPEATED);
            }
            stockLogicMoveItemMapper.insertBatch(toInsetList);
        }
        // 重新读取DO
        WmsStockLogicMoveDO newStockBinMove = stockLogicMoveMapper.selectById(stockLogicMove.getId());
        List<WmsStockLogicMoveItemDO> binMoveItemDOS = stockLogicMoveItemMapper.selectByLogicMoveId(newStockBinMove.getId());
        LogicMoveContext logicMoveContext = new LogicMoveContext();
        logicMoveContext.setLogicMoveDO(newStockBinMove);
        logicMoveContext.setLogicMoveItemDOS(binMoveItemDOS);
        logicMoveExecutor.execute(logicMoveContext);
        // 返回
        return stockLogicMove;
    }

    /**
     * @sign : 0F3263313629D9D4
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsStockLogicMoveDO updateStockLogicMove(WmsStockLogicMoveSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockLogicMoveDO exists = validateStockLogicMoveExists(updateReqVO.getId());
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 保存逻辑库存移动详情详情
        if (updateReqVO.getItemList() != null) {
            List<WmsStockLogicMoveItemDO> existsInDB = stockLogicMoveItemMapper.selectByLogicMoveId(updateReqVO.getId());
            StreamX.CompareResult<WmsStockLogicMoveItemDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getItemList(), WmsStockLogicMoveItemDO.class), WmsStockLogicMoveItemDO::getId);
            List<WmsStockLogicMoveItemDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsStockLogicMoveItemDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsStockLogicMoveItemDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsStockLogicMoveItemDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 重复校验
            Set<String> uniqueKeys = StreamX.from(finalList).toSet(itm -> {
                return StrUtils.join(Arrays.asList(itm.getProductId(), itm.getFromCompanyId(), itm.getFromDeptId(), itm.getToCompanyId(), itm.getToDeptId()));
            });
            if (uniqueKeys.size() != finalList.size()) {
                throw exception(STOCK_LOGIC_MOVE_ITEM_REPEATED);
            }
            // 设置归属
            finalList.forEach(item -> {
                item.setLogicMoveId(updateReqVO.getId());
            });
            // 保存详情
            stockLogicMoveItemMapper.insertBatch(toInsetList);
            stockLogicMoveItemMapper.updateBatch(toUpdateList);
            stockLogicMoveItemMapper.deleteByIds(toDeleteList);
        }
        // 更新
        WmsStockLogicMoveDO stockLogicMove = BeanUtils.toBean(updateReqVO, WmsStockLogicMoveDO.class);
        stockLogicMoveMapper.updateById(stockLogicMove);
        // 返回
        return stockLogicMove;
    }

    /**
     * @sign : 28B0CE5244DC7876
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockLogicMove(Long id) {
        // 校验存在
        WmsStockLogicMoveDO stockLogicMove = validateStockLogicMoveExists(id);
        // 唯一索引去重
        stockLogicMove.setNo(stockLogicMoveMapper.flagUKeyAsLogicDelete(stockLogicMove.getNo()));
        stockLogicMoveMapper.updateById(stockLogicMove);
        // 删除
        stockLogicMoveMapper.deleteById(id);
    }

    /**
     * @sign : E902C86470C6D000
     */
    private WmsStockLogicMoveDO validateStockLogicMoveExists(Long id) {
        WmsStockLogicMoveDO stockLogicMove = stockLogicMoveMapper.selectById(id);
        if (stockLogicMove == null) {
            throw exception(STOCK_LOGIC_MOVE_NOT_EXISTS);
        }
        return stockLogicMove;
    }

    @Override
    public WmsStockLogicMoveDO getStockLogicMove(Long id) {
        return stockLogicMoveMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockLogicMoveDO> getStockLogicMovePage(WmsStockLogicMovePageReqVO pageReqVO) {
        return stockLogicMoveMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockLogicMoveDO
     */
    @Override
    public List<WmsStockLogicMoveDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockLogicMoveMapper.selectByIds(idList);
    }

    @Override
    public void finishMove(WmsStockLogicMoveDO logicMoveDO, List<WmsStockLogicMoveItemDO> logicMoveItemDOList) {
        logicMoveDO.setExecuteStatus(WmsMoveExecuteStatus.MOVED.getValue());
        stockLogicMoveMapper.updateById(logicMoveDO);
    }

    @Override
    public void assembleWarehouse(List<WmsStockLogicMoveRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockLogicMoveRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values())
            .toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));

        StreamX.from(list).assemble(warehouseVOMap, WmsStockLogicMoveRespVO::getWarehouseId, WmsStockLogicMoveRespVO::setWarehouse);
    }
}
