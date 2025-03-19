package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.module.wms.config.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.common.BillType;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.WmsInboundMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemMapper;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 入库单 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsInboundServiceImpl implements WmsInboundService {

    @Resource
    @Lazy
    private WmsInboundItemMapper inboundItemMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsInboundMapper inboundMapper;

    @Resource(name = InboundAction.STATE_MACHINE_NAME)
    private StateMachineWrapper<Integer, InboundStatus.Event, WmsInboundDO> inboundStateMachine;

    /**
     * @sign : 5D2F5734A2A97234
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsInboundDO createInbound(WmsInboundSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.INBOUND_NO_PREFIX, INBOUND_NOT_EXISTS);
        createReqVO.setNo(no);
        createReqVO.setStatus(inboundStateMachine.getInitStatus());
        if (inboundMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(INBOUND_NO_DUPLICATE);
        }
        // 按 wms_inbound.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        if (createReqVO.getWarehouseId() != null) {
            WmsWarehouseDO warehouse = warehouseService.getWarehouse(createReqVO.getWarehouseId());
            if (warehouse == null) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
        }
        // 插入
        WmsInboundDO inbound = BeanUtils.toBean(createReqVO, WmsInboundDO.class);
        inboundMapper.insert(inbound);
        // 保存入库单详情详情
        if (createReqVO.getItemList() != null) {
            List<WmsInboundItemDO> toInsetList = new ArrayList<>();
            StreamX.from(createReqVO.getItemList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setInboundId(inbound.getId());
                toInsetList.add(BeanUtils.toBean(item, WmsInboundItemDO.class));
            });
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInboundItemDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(INBOUND_ITEM_PRODUCT_ID_REPEATED);
            }
            inboundItemMapper.insertBatch(toInsetList);
        }
        // 返回
        return inbound;
    }

    /**
     * @sign : 313B4FDC4F383182
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsInboundDO updateInbound(WmsInboundSaveReqVO updateReqVO) {
        // 校验存在
        WmsInboundDO exists = validateInboundExists(updateReqVO.getId());
        // 判断是否允许编辑
        if (!inboundStateMachine.canEdit(exists.getStatus())) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 按 wms_inbound.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        if (updateReqVO.getWarehouseId() != null) {
            WmsWarehouseDO warehouse = warehouseService.getWarehouse(updateReqVO.getWarehouseId());
            if (warehouse == null) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
        }
        // 保存入库单详情详情
        if (updateReqVO.getItemList() != null) {
            List<WmsInboundItemDO> existsInDB = inboundItemMapper.selectByInboundId(updateReqVO.getId(), Integer.MAX_VALUE);
            StreamX.CompareResult<WmsInboundItemDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getItemList(), WmsInboundItemDO.class), WmsInboundItemDO::getId);
            List<WmsInboundItemDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsInboundItemDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsInboundItemDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsInboundItemDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInboundItemDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(INBOUND_ITEM_PRODUCT_ID_REPEATED);
            }
            // 设置归属
            finalList.forEach(item -> {
                item.setInboundId(updateReqVO.getId());
            });
            // 保存详情
            inboundItemMapper.insertBatch(toInsetList);
            inboundItemMapper.updateBatch(toUpdateList);
            inboundItemMapper.deleteBatchIds(toDeleteList);
        }
        // 更新
        WmsInboundDO inbound = BeanUtils.toBean(updateReqVO, WmsInboundDO.class);
        inboundMapper.updateById(inbound);
        // 返回
        return inbound;
    }

    @Override
    public WmsInboundDO updateInboundStatus(Long id, Integer status) {
        WmsInboundDO inboundDO = validateInboundExists(id);
        inboundDO.setStatus(status);
        inboundMapper.updateById(inboundDO);
        return inboundDO;
    }

    /**
     * @sign : FFFDDAD5269478BB
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInbound(Long id) {
        // 校验存在
        WmsInboundDO inbound = validateInboundExists(id);
        // 判断是否允许删除
        if (!inboundStateMachine.canDelete(inbound.getStatus())) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        // 唯一索引去重
        inbound.setNo(inboundMapper.flagUKeyAsLogicDelete(inbound.getNo()));
        inboundMapper.updateById(inbound);
        // 删除
        inboundMapper.deleteById(id);
    }

    /**
     * @sign : 6549448A5F16EE5E
     */
    private WmsInboundDO validateInboundExists(Long id) {
        WmsInboundDO inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw exception(INBOUND_NOT_EXISTS);
        }
        return inbound;
    }

    @Override
    public WmsInboundDO getInbound(Long id) {
        return inboundMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInboundDO> getInboundPage(WmsInboundPageReqVO pageReqVO) {
        return inboundMapper.selectPage(pageReqVO);
    }

    /**
     * 按 warehouseId 查询 WmsInboundDO
     */
    public List<WmsInboundDO> selectByWarehouseId(Long warehouseId, int limit) {
        return inboundMapper.selectByWarehouseId(warehouseId, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(InboundStatus.Event event, WmsApprovalReqVO approvalReqVO) {
        // 设置业务默认值
        approvalReqVO.setBillType(BillType.INBOUND.getValue());
        approvalReqVO.setStatusType(InboundStatus.getType());
        // 获得业务对象
        WmsInboundDO inbound = validateInboundExists(approvalReqVO.getBillId());
        ColaContext<WmsInboundDO> ctx = ColaContext.from(inbound, approvalReqVO);
        // 触发事件
        inboundStateMachine.fireEvent(event, ctx);
        // 如果未处理，则抛出异常
        if (!ctx.handled()) {
            throw exception(INBOUND_APPROVAL_CONDITION_IS_NOT_MATCH);
        }
    }
}
