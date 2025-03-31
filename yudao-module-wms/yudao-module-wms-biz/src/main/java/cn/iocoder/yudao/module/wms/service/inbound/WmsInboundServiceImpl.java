package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow.WmsInboundItemFlowMapper;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.common.WmsBillType;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.WmsInboundMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemMapper;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;

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
    protected WmsLockRedisDAO lockRedisDAO;

    @Resource
    @Lazy
    private WmsInboundItemFlowMapper inboundItemFlowMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsInboundMapper inboundMapper;

    @Resource
    @Lazy
    private WmsInboundItemService inboundItemService;

    @Resource(name = InboundActions.STATE_MACHINE_NAME)
    private StateMachineWrapper<Integer, WmsInboundAuditStatus.Event, WmsInboundDO> inboundStateMachine;

    /**
     * @sign : 5D2F5734A2A97234
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsInboundDO createInbound(WmsInboundSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.INBOUND_NO_PREFIX, INBOUND_NOT_EXISTS);
        createReqVO.setNo(no);
        createReqVO.setAuditStatus(inboundStateMachine.getInitStatus());
        createReqVO.setInboundStatus(WmsInboundStatus.NONE.getValue());
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
                if(item.getPlanQty()==null || item.getPlanQty()<=0) {
                    throw exception(INBOUND_ITEM_PLAN_QTY_ERROR);
                }
                item.setId(null);
                // 设置归属
                item.setInboundId(inbound.getId());
                item.setInboundStatus(WmsInboundStatus.NONE.getValue());
                item.setActualQty(0);
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
        if (!inboundStateMachine.canEdit(exists.getAuditStatus())) {
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
                if(item.getPlanQty()==null || item.getPlanQty()<=0) {
                    throw exception(INBOUND_ITEM_PLAN_QTY_ERROR);
                }
                item.setInboundId(updateReqVO.getId());
                item.setInboundStatus(WmsInboundStatus.NONE.getValue());
                item.setActualQty(0);
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
    public WmsInboundDO updateInboundAuditStatus(Long id, Integer status) {
        WmsInboundDO inboundDO = validateInboundExists(id);
        inboundDO.setAuditStatus(status);
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
        if (!inboundStateMachine.canDelete(inbound.getAuditStatus())) {
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
    public WmsInboundDO validateInboundExists(Long id) {
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
    public void approve(WmsInboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO) {
        // 设置业务默认值
        approvalReqVO.setBillType(WmsBillType.INBOUND.getValue());
        approvalReqVO.setStatusType(WmsInboundAuditStatus.getType());
        // 获得业务对象
        WmsInboundDO inbound = validateInboundExists(approvalReqVO.getBillId());
        // 锁在外，事务在锁内
        WmsInboundServiceImpl proxy = SpringUtils.getBeanByExactType(WmsInboundServiceImpl.class);
        lockRedisDAO.lockByWarehouse(inbound.getWarehouseId(),()->{
            proxy.fireEvent(event,approvalReqVO,inbound);
        });

    }

    @Transactional(rollbackFor = Exception.class)
    protected void  fireEvent(WmsInboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO, WmsInboundDO inbound) {
        ColaContext<WmsInboundDO> ctx =  inboundStateMachine.createContext(approvalReqVO,inbound);
        // 触发事件
        inboundStateMachine.fireEvent(event, ctx);
    }

    @Override
    public WmsInboundRespVO getInboundWithItemList(Long id) {
        // 查询数据
        WmsInboundDO inbound = this.getInbound(id);
        if (inbound == null) {
            throw exception(INBOUND_NOT_EXISTS);
        }
        // 转换
        WmsInboundRespVO inboundVO = BeanUtils.toBean(inbound, WmsInboundRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(inboundVO)).mapping(WmsInboundRespVO::getCreator, WmsInboundRespVO::setCreatorName).mapping(WmsInboundRespVO::getCreator, WmsInboundRespVO::setUpdaterName).fill();
        // 组装入库单详情
        List<WmsInboundItemDO> inboundItemList = inboundItemService.selectByInboundId(inboundVO.getId());
        inboundVO.setItemList(BeanUtils.toBean(inboundItemList, WmsInboundItemRespVO.class));
        inboundItemService.assembleProducts(inboundVO.getItemList());
        return inboundVO;
    }

    @Override
    public void finishInbound(WmsInboundRespVO inboundRespVO) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        int total= inboundRespVO.getItemList().size();
        int countOfNone = 0;
        int countOfPart = 0;
        int countOfAll = 0;
        for (WmsInboundItemRespVO respVO : inboundRespVO.getItemList()) {

            if (WmsInboundStatus.NONE.matchAny(respVO.getInboundStatus())) {
                countOfNone++;
            }
            if (WmsInboundStatus.PART.matchAny(respVO.getInboundStatus())) {
                countOfPart++;
            }
            if (WmsInboundStatus.ALL.matchAny(respVO.getInboundStatus())) {
                countOfAll++;
            }

            if(respVO.getOutboundAvailableQty()==null) {
                respVO.setOutboundAvailableQty(0);
            }
            if(respVO.getShelvedQty()==null) {
                respVO.setShelvedQty(0);
            }
        }
        if (countOfNone > 0) {
            throw exception(INBOUND_NOT_COMPLETE);
        }

        WmsInboundStatus inboundStatus = WmsInboundStatus.NONE;
        if(countOfPart>0) {
            inboundStatus = WmsInboundStatus.PART;
        }
        if(total==countOfAll) {
            inboundStatus = WmsInboundStatus.ALL;
        }


        // 处理明细的入库状态
        List<WmsInboundItemDO> itemList = BeanUtils.toBean(inboundRespVO.getItemList(), WmsInboundItemDO.class);
        inboundItemMapper.updateBatch(itemList);
        // 处理入库单状态
        WmsInboundDO inboundDO = BeanUtils.toBean(inboundRespVO, WmsInboundDO.class);
        inboundDO.setInboundStatus(inboundStatus.getValue());
        if (inboundDO.getArrivalActualTime() == null) {
            inboundDO.setArrivalActualTime(LocalDateTime.now());
        }
        inboundDO.setInboundTime(LocalDateTime.now());
        inboundMapper.updateById(inboundDO);
    }

    @Override
    public List<WmsInboundDO> selectByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return inboundMapper.selectByIds(ids);
    }
}
