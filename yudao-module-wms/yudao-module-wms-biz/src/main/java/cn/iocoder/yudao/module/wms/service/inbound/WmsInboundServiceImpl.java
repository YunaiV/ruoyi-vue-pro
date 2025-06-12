package cn.iocoder.yudao.module.wms.service.inbound;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.config.InboundStateMachineConfigure;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.company.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.WmsInboundMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemLogicQueryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow.WmsInboundItemFlowMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.flow.WmsStockFlowMapper;
import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.WmsConstants;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundShelvingStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundType;
import cn.iocoder.yudao.module.wms.service.approval.history.WmsApprovalHistoryService;
import cn.iocoder.yudao.module.wms.service.inbound.item.WmsInboundItemService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;
import static cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus.*;
import static com.fhs.common.constant.Constant.ONE;
import static java.lang.Boolean.FALSE;

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
    @Lazy
    private WmsInboundItemLogicQueryMapper inboundItemLogicQueryMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsStockWarehouseService stockWarehouseService;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsInboundMapper inboundMapper;

    @Resource
    private WmsStockFlowMapper stockFlowMapper;

    @Resource
    @Lazy
    private WmsInboundItemService inboundItemService;

    @Resource
    private WmsApprovalHistoryService approvalHistoryService;

    @Resource
    private FmsCompanyApi companyApi;

    @Resource(name = InboundStateMachineConfigure.STATE_MACHINE_NAME)
    private StateMachine<Integer, WmsInboundAuditStatus.Event, TransitionContext<WmsInboundDO>> inboundStateMachine;


    /**
     * @sign : 5D2F5734A2A97234
     */
    @Override
    public WmsInboundDO createInbound(WmsInboundSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.INBOUND_NO_PREFIX, 6);
        createReqVO.setCode(no);
        createReqVO.setAuditStatus(DRAFT.getValue());
        createReqVO.setInboundStatus(WmsInboundStatus.NONE.getValue());
        createReqVO.setShelveStatus(WmsInboundShelvingStatus.NONE.getValue());
        if (inboundMapper.getByCode(createReqVO.getCode()) != null) {
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
                if (item.getPlanQty() == null || item.getPlanQty() <= 0) {
                    throw exception(INBOUND_ITEM_PLAN_QTY_ERROR);
                }
                item.setId(null);
                // 设置归属
                item.setInboundId(inbound.getId());
                item.setInboundStatus(WmsInboundStatus.NONE.getValue());
                item.setActualQty(item.getActualQty() == null ? 0 : item.getActualQty());
                item.setCompanyId(createReqVO.getCompanyId());
                toInsetList.add(BeanUtils.toBean(item, WmsInboundItemDO.class));
            });
            // 校验 toInsetList 中是否有重复的 productId
            // boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInboundItemDO::getProductId);
            // if (isProductIdRepeated) {
            //    throw exception(INBOUND_ITEM_PRODUCT_ID_REPEATED);
            // }
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
        WmsInboundAuditStatus auditStatus = WmsInboundAuditStatus.parse(exists.getAuditStatus());
        if (!auditStatus.matchAny(DRAFT, WmsInboundAuditStatus.REJECT)) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        // 单据号不允许被修改
        updateReqVO.setCode(exists.getCode());
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
            // boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsInboundItemDO::getProductId);
            // if (isProductIdRepeated) {
            //     throw exception(INBOUND_ITEM_PRODUCT_ID_REPEATED);
            // }
            // 设置归属
            finalList.forEach(item -> {
                if (item.getPlanQty() == null || item.getPlanQty() <= 0) {
                    throw exception(INBOUND_ITEM_PLAN_QTY_ERROR);
                }
                item.setInboundId(updateReqVO.getId());
                item.setInboundStatus(WmsInboundStatus.NONE.getValue());
                item.setActualQty(0);
                item.setCompanyId(updateReqVO.getCompanyId());
            });
            // 保存详情
            if(!toInsetList.isEmpty()) {
                inboundItemMapper.insertBatch(toInsetList);
            }
            if(!toUpdateList.isEmpty()) {
                inboundItemMapper.updateBatch(toUpdateList);
            }
//            if(!toDeleteList.isEmpty()) {
//                inboundItemMapper.deleteBatchIds(toDeleteList);
//            }
            if (!toDeleteList.isEmpty()) {
                List<Long> idList = toDeleteList.stream().map(WmsInboundItemDO::getId).collect(Collectors.toList());
                inboundItemMapper.delete(new QueryWrapper<WmsInboundItemDO>().in("id", idList));
            }
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
        WmsInboundAuditStatus auditStatus = WmsInboundAuditStatus.parse(inbound.getAuditStatus());
        if (!auditStatus.matchAny(DRAFT, WmsInboundAuditStatus.REJECT)) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        // 唯一索引去重
        inbound.setCode(inboundMapper.flagUKeyAsLogicDelete(inbound.getCode()));
        inboundMapper.updateById(inbound);
        // 删除
        inboundMapper.deleteById(id);
    }

    /**
     * @sign : 6549448A5F16EE5E
     */
    @Override
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
    @Override
    public List<WmsInboundDO> selectByWarehouseId(Long warehouseId, int limit) {
        return inboundMapper.selectByWarehouseId(warehouseId, limit);
    }

    @Override
    public void approve(WmsInboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO) {
        // 设置业务默认值
        approvalReqVO.setBillType(BillType.WMS_INBOUND.getValue());
        approvalReqVO.setStatusType(WmsInboundAuditStatus.getType());
        // 获得业务对象
        WmsInboundDO inbound = validateInboundExists(approvalReqVO.getBillId());
        // 锁在外，事务在锁内
        WmsInboundServiceImpl proxy = SpringUtils.getBeanByExactType(WmsInboundServiceImpl.class);
        lockRedisDAO.lockByWarehouse(inbound.getWarehouseId(), () -> {
            proxy.fireEvent(event, approvalReqVO, inbound);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    protected void fireEvent(WmsInboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO, WmsInboundDO inbound) {
        TransitionContext<WmsInboundDO> ctx = TransitionContext.from(inbound);
        ctx.setExtra(WmsConstants.APPROVAL_REQ_VO_KEY, approvalReqVO);
        // 触发事件
        inboundStateMachine.fireEvent(event, ctx);
    }

    /**
     * 获得入库单详情
     *
     * @param id 编号
     * @return 入库单
     */
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
        AdminUserApi.inst().prepareFill(List.of(inboundVO)).mapping(WmsInboundRespVO::getCreator, WmsInboundRespVO::setCreatorName).mapping(WmsInboundRespVO::getUpdater, WmsInboundRespVO::setUpdaterName).fill();
        // 组装入库单详情
        List<WmsInboundItemDO> inboundItemList = inboundItemService.selectByInboundId(inboundVO.getId());
        inboundVO.setItemList(BeanUtils.toBean(inboundItemList, WmsInboundItemRespVO.class));
        inboundItemService.assembleProducts(inboundVO.getItemList());
        return inboundVO;
    }

    /**
     * 完成入库单
     *
     * @param inboundRespVO 入库单
     */
    @Override
    public void finishInbound(WmsInboundRespVO inboundRespVO) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        int total = inboundRespVO.getItemList().size();
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
            if (respVO.getOutboundAvailableQty() == null) {
                respVO.setOutboundAvailableQty(0);
            }
            if (respVO.getShelveClosedQty() == null) {
                respVO.setShelveClosedQty(0);
            }
        }
        if (countOfNone > 0) {
            throw exception(INBOUND_NOT_COMPLETE);
        }
        WmsInboundStatus inboundStatus = WmsInboundStatus.NONE;
        if (countOfPart > 0) {
            inboundStatus = WmsInboundStatus.PART;
        }
        if (total == countOfAll) {
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
        updateStockFlow(inboundRespVO, inboundDO);
        //针对外部生成单据，更新在途数
        if (inboundDO.getUpstreamType() != null) {
            updateTransitQty(inboundDO, itemList);
        }

    }

    private void updateTransitQty(WmsInboundDO inbound, List<WmsInboundItemDO> itemList) {
        for(WmsInboundItemDO item : itemList) {
            WmsStockWarehouseDO stockWarehouseDO = stockWarehouseService.getStockWarehouse(inbound.getWarehouseId(), item.getProductId(), FALSE);
            stockWarehouseDO.setTransitQty(Math.max(stockWarehouseDO.getTransitQty() - item.getActualQty(), 0));
            stockWarehouseService.updateStockWarehouse(BeanUtils.toBean(stockWarehouseDO, WmsStockWarehouseSaveReqVO.class));
        }
    }

    //生成批次可用库存流水，并把id更新到库存流水表
    private void updateStockFlow(WmsInboundRespVO inboundRespVO, WmsInboundDO inboundDO) {
        List<WmsInboundItemRespVO> itemList = inboundRespVO.getItemList();
        if(CollectionUtils.isEmpty(itemList)) {
            return;
        }
        for (WmsInboundItemRespVO respVO : itemList) {
            WmsItemFlowDO inboundItemFlow = WmsItemFlowDO.builder()
                    .inboundId(respVO.getInboundId())
                    .inboundItemId(respVO.getId())
                    .productId(respVO.getProductId())
                    .actualQty(respVO.getActualQty())
                    .billType(inboundDO.getType())
                    .direction(ONE)
                    .outboundAvailableQty(respVO.getActualQty())
                    .outboundAvailableDeltaQty(respVO.getActualQty())
                .shelveClosedQty(respVO.getShelveClosedQty())
                    .build();
            inboundItemFlowMapper.insert(inboundItemFlow);
            List<WmsStockFlowDO> wmsStockFlowDOList = stockFlowMapper.selectByReasonItemIdAndReasonBillId(respVO.getId(), respVO.getInboundId());
            if(wmsStockFlowDOList == null){
                throw exception(INBOUND_ITEM_FLOW_NOT_EXISTS);
            }
            for (WmsStockFlowDO stockFlow : wmsStockFlowDOList) {
                stockFlow.setInboundItemFlowId(inboundItemFlow.getId());
                stockFlowMapper.updateById(stockFlow);
            }
        }

    }

    /**
     * 批量查询
     *
     * @param ids 入库单id集合
     * @return 入库单集合
     */
    @Override
    public List<WmsInboundDO> selectByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return inboundMapper.selectByIds(ids);
    }

    /**
     * 获得入库单列表
     *
     * @param pageReqVO 查询条件
     * @return 入库单列表
     */
    @Override
    public List<WmsInboundDO> getSimpleList(WmsInboundPageReqVO pageReqVO) {
        return inboundMapper.getSimpleList(pageReqVO);
    }

    /**
     * 装配仓库信息
     *
     * @param list 入库单集合
     */
    @Override
    public void assembleWarehouse(List<WmsInboundRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsInboundRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsInboundRespVO::getWarehouseId, WmsInboundRespVO::setWarehouse);
    }

    /**
     * 装配公司信息
     *
     * @param list 入库单集合
     */
    @Override
    public void assembleCompany(List<WmsInboundRespVO> list) {
        Map<Long, FmsCompanyDTO> companyMap = companyApi.getCompanyMap(StreamX.from(list).toSet(WmsInboundRespVO::getCompanyId));
        Map<Long, FmsCompanySimpleRespVO> companyVOMap = StreamX.from(companyMap.values()).toMap(FmsCompanyDTO::getId, v -> BeanUtils.toBean(v, FmsCompanySimpleRespVO.class));
        StreamX.from(list).assemble(companyVOMap, WmsInboundRespVO::getCompanyId, WmsInboundRespVO::setCompany);
    }

    /**
     * 装配审批历史信息
     *
     * @param list 入库单集合
     */
    @Override
    public void assembleApprovalHistory(List<WmsInboundRespVO> list) {
        Map<Long, List<WmsApprovalHistoryRespVO>> groupedApprovalHistory = approvalHistoryService.selectGroupedApprovalHistory(BillType.WMS_INBOUND, StreamX.from(list).toList(WmsInboundRespVO::getId));
        StreamX.from(list).assemble(groupedApprovalHistory, WmsInboundRespVO::getId, WmsInboundRespVO::setApprovalHistoryList);
    }

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId 仓库ID
     * @param productId 产品ID
     * @param olderFirst 是否按入库时间升序
     */
    @Override
    public WmsInboundItemLogicDO getInboundItemLogic(Long warehouseId, Long productId, boolean olderFirst) {
        return inboundItemLogicQueryMapper.getInboundItemLogic(warehouseId, productId, olderFirst);
    }

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId 仓库ID
     * @param productIds 产品ID
     * @param olderFirst 是否按入库时间升序
     */
    @Override
    public Map<Long, WmsInboundItemLogicDO> getInboundItemLogicMap(Long warehouseId, List<Long> productIds, boolean olderFirst) {
        return inboundItemLogicQueryMapper.selectInboundItemLogicMap(warehouseId, productIds, olderFirst);
    }

    /**
     * 按入库顺序获得入库批次列表
     *
     * @param warehouseId 仓库id
     * @param productId   产品id
     * @param olderFirst  是否按入库时间升序
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WmsInboundItemLogicDO> getInboundItemLogicList(Long warehouseId, Long productId, boolean olderFirst) {
        return inboundItemLogicQueryMapper.getInboundItemLogicList(warehouseId, productId, olderFirst);
    }

    /**
     * 按入库顺序获得第一个入库批次
     * @param warehouseId 仓库id
     * @param productId 产品id
     * @param olderFirst 是否按入库时间升序
     */
    @Override
    public List<WmsInboundItemLogicDO> selectInboundItemLogicList(Long warehouseId, Long productId, boolean olderFirst) {
        Map<Long, List<WmsInboundItemLogicDO>> longListMap = inboundItemLogicQueryMapper.selectInboundItemLogicGroupedMap(warehouseId, Collections.singletonList(productId), olderFirst);
        return longListMap.get(productId);
    }

    /**
     * 创建盘点入库单
     */
    @Override
    public WmsInboundDO createForStockCheck(WmsInboundSaveReqVO inboundSaveReqVO) {
        JdbcUtils.requireTransaction();
        Map<Long, Integer> actualQtyMap = StreamX.from(inboundSaveReqVO.getItemList()).toMap(WmsInboundItemSaveReqVO::getProductId, WmsInboundItemSaveReqVO::getActualQty);
        // 创建
        WmsInboundDO inbound = this.createInbound(inboundSaveReqVO);
        // 保存
        inbound.setUpstreamType(BillType.WMS_STOCKCHECK.getValue());
        inbound.setType(WmsInboundType.STOCKCHECK.getValue());
        inboundMapper.updateById(inbound);
        // 
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(inbound.getId());
        approvalReqVO.setComment("盘点入库");
        this.approve(WmsInboundAuditStatus.Event.SUBMIT, approvalReqVO);
        // 拉取明细
        List<WmsInboundItemDO> inboundItemDOS = inboundItemService.selectByInboundId(inbound.getId());
        // 设置实际入库量
        StreamX.from(inboundItemDOS).assemble(actualQtyMap, WmsInboundItemDO::getProductId, WmsInboundItemDO::setActualQty);
        // 保存实际入库量
        inboundItemService.updateActualQuantity(BeanUtils.toBean(inboundItemDOS, WmsInboundItemSaveReqVO.class));
        // 同意确认收货
        this.approve(WmsInboundAuditStatus.Event.AGREE, approvalReqVO);
        // 
        return this.getInbound(inbound.getId());
    }

    /**
     * 更新上架状态
     */
    @Override
    public void updateShelvingStatus(Set<Long> ids) {
        for (Long id : ids) {
            List<WmsInboundItemDO> inboundItemDOList = inboundItemMapper.selectByInboundId(id, Integer.MAX_VALUE);
            int none = 0;
            int part = 0;
            int full = 0;
            for (WmsInboundItemDO itemDO : inboundItemDOList) {
                Integer actualQty = itemDO.getActualQty();
                Integer ShelveClosedQty = itemDO.getShelveClosedQty();
                // 如果存在已上架数量不为0，部分上架
                if (ShelveClosedQty == 0) {
                    none++;
                }
                if (ShelveClosedQty >= actualQty) {
                    full++;
                }
                if (ShelveClosedQty > 0 && ShelveClosedQty < actualQty) {
                    part++;
                }
            }
            WmsInboundDO inbound = this.getInbound(id);
            if (none == inboundItemDOList.size()) {
                inbound.setShelveStatus(WmsInboundShelvingStatus.NONE.getValue());
            } else if (full == inboundItemDOList.size()) {
                inbound.setShelveStatus(WmsInboundShelvingStatus.ALL.getValue());
            } else {
                inbound.setShelveStatus(WmsInboundShelvingStatus.PARTLY.getValue());
            }
            inboundMapper.updateById(inbound);
        }
    }

    @Override
    public List<WmsInboundDO> getInboundList(Integer upstreamType, Long upstreamId) {
        return inboundMapper.getInboundList(upstreamType, upstreamId);
    }

    @Override
    public WmsInboundDO createForTransfer(WmsInboundSaveReqVO inboundSaveReqVO) {

        JdbcUtils.requireTransaction();
        Map<Long, Integer> actualQtyMap = StreamX.from(inboundSaveReqVO.getItemList()).toMap(WmsInboundItemSaveReqVO::getProductId, WmsInboundItemSaveReqVO::getActualQty);
        // 创建
        WmsInboundDO inbound = this.createInbound(inboundSaveReqVO);
        // 保存
        inbound.setUpstreamType(BillType.TMS_TRANSFER.getValue());
        inbound.setType(WmsInboundType.TRANSFER.getValue());
        inboundMapper.updateById(inbound);
        //
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillId(inbound.getId());
        approvalReqVO.setComment("调拨入库");
        this.approve(WmsInboundAuditStatus.Event.SUBMIT, approvalReqVO);
        // 拉取明细
        List<WmsInboundItemDO> inboundItemDOS = inboundItemService.selectByInboundId(inbound.getId());
        // 设置实际入库量
        StreamX.from(inboundItemDOS).assemble(actualQtyMap, WmsInboundItemDO::getProductId, WmsInboundItemDO::setActualQty);
        // 保存实际入库量
        inboundItemService.updateActualQuantity(BeanUtils.toBean(inboundItemDOS, WmsInboundItemSaveReqVO.class));
        //
        return this.getInbound(inbound.getId());
    }

    /**
    * 强制作废入库单
     */
    @Override
    public void forceAbandon(WmsApprovalReqVO approvalReqVO) {
        // 获得业务对象
        WmsInboundDO inbound = validateInboundExists(approvalReqVO.getBillId());
        if(inbound.getAuditStatus().equals(AUDITING.getValue())){
            approve(WmsInboundAuditStatus.Event.REJECT, approvalReqVO);
        }
        if(inbound.getAuditStatus().equals(DRAFT.getValue())||inbound.getAuditStatus().equals(REJECT.getValue())){
            approve(WmsInboundAuditStatus.Event.ABANDON, approvalReqVO);
        }else{
            throw exception(INBOUND_ABANDON_NOT_ALLOWED);
        }
    }

    @Override
    public WmsInboundDO getByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        return inboundMapper.getByWarehouseIdAndProductId(warehouseId, productId);
    }
}
