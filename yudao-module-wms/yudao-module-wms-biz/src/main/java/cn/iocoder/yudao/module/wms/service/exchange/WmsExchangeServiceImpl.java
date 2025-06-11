package cn.iocoder.yudao.module.wms.service.exchange;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.config.ExchangeStateMachineConfigure;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.WmsInboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.item.vo.WmsStockBinMoveItemSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.move.vo.WmsStockBinMoveSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.item.WmsExchangeItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemLogicDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.exchange.WmsExchangeMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.exchange.item.WmsExchangeItemMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.warehouse.WmsStockWarehouseMapper;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.WmsConstants;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundType;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundType;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.stock.bin.move.WmsStockBinMoveService;
import cn.iocoder.yudao.module.wms.service.stock.flow.WmsStockFlowService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;
import static java.lang.Boolean.TRUE;

/**
 * 换货单 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
@Slf4j
public class WmsExchangeServiceImpl implements WmsExchangeService {

    @Getter
    private WmsStockReason reason;

    @Resource
    @Lazy
    private WmsExchangeItemMapper exchangeItemMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsExchangeMapper exchangeMapper;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    @Lazy
    protected WmsStockFlowService stockFlowService;

    @Resource
    private WmsOutboundService outboundService;

    @Resource
    private WmsStockBinMoveService stockBinMoveService;

    @Resource(name = ExchangeStateMachineConfigure.STATE_MACHINE_NAME)
    private StateMachine<Integer, WmsExchangeAuditStatus.Event, TransitionContext<WmsExchangeDO>> exchangeStateMachine;

    @Resource
    private WmsStockWarehouseMapper wmsStockWarehouseMapper;

    @Resource
    private WmsInboundService wmsInboundService;


    /**
     * @sign : 48FA5E8619B15D35
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsExchangeDO createExchange(WmsExchangeSaveReqVO createReqVO) {
        // 设置单据号
        String code = noRedisDAO.generate(WmsNoRedisDAO.EXCHANGE_NO_PREFIX, 6);
        createReqVO.setCode(code);
        createReqVO.setAuditStatus(WmsExchangeAuditStatus.DRAFT.getValue());
        if (exchangeMapper.getByCode(createReqVO.getCode()) != null) {
            throw exception(EXCHANGE_CODE_DUPLICATE);
        }
        // 插入
        WmsExchangeDO exchange = BeanUtils.toBean(createReqVO, WmsExchangeDO.class);
        exchangeMapper.insert(exchange);
        // 保存良次换货详情详情
        if (createReqVO.getItemList() != null) {
            List<WmsExchangeItemDO> toInsetList = new ArrayList<>();
            StreamX.from(createReqVO.getItemList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setExchangeId(exchange.getId());
                toInsetList.add(BeanUtils.toBean(item, WmsExchangeItemDO.class));
            });
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsExchangeItemDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(EXCHANGE_ITEM_EXISTS);
            }
            exchangeItemMapper.insertBatch(toInsetList);
        }
        // 返回
        return exchange;
    }

    /**
     * @sign : E0A7EE421ACD0093
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsExchangeDO updateExchange(WmsExchangeSaveReqVO updateReqVO) {
        // 校验存在
        WmsExchangeDO exists = validateExchangeExists(updateReqVO.getId());
        // 校验状态
        WmsExchangeAuditStatus auditStatus = WmsExchangeAuditStatus.parse(exists.getAuditStatus());
        if(auditStatus.matchAny(WmsExchangeAuditStatus.AUDITING,WmsExchangeAuditStatus.PASS)) {
           throw exception(EXCHANGE_CAN_NOT_EDIT);
        }
        // 单据号不允许被修改
        updateReqVO.setCode(exists.getCode());
        // 保存良次换货详情详情
        if (updateReqVO.getItemList() != null) {
            List<WmsExchangeItemDO> existsInDB = exchangeItemMapper.selectByExchangeId(updateReqVO.getId());
            StreamX.CompareResult<WmsExchangeItemDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getItemList(), WmsExchangeItemDO.class), WmsExchangeItemDO::getId);
            List<WmsExchangeItemDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsExchangeItemDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsExchangeItemDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsExchangeItemDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsExchangeItemDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(EXCHANGE_ITEM_EXISTS);
            }
            // 设置归属
            finalList.forEach(item -> {
                item.setExchangeId(updateReqVO.getId());
            });
            // 保存详情
            exchangeItemMapper.insertBatch(toInsetList);
            exchangeItemMapper.updateBatch(toUpdateList);
            exchangeItemMapper.deleteByIds(toDeleteList);
        }
        // 更新
        WmsExchangeDO exchange = BeanUtils.toBean(updateReqVO, WmsExchangeDO.class);
        exchangeMapper.updateById(exchange);
        // 返回
        return exchange;
    }

    /**
     * @sign : CA8438BC1760D4D2
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExchange(Long id) {
        // 校验存在
        WmsExchangeDO exchange = validateExchangeExists(id);
        // 校验状态
        WmsExchangeAuditStatus auditStatus = WmsExchangeAuditStatus.parse(exchange.getAuditStatus());
        if(auditStatus.matchAny(WmsExchangeAuditStatus.AUDITING,WmsExchangeAuditStatus.PASS)) {
            throw exception(EXCHANGE_CAN_NOT_DELETE);
        }
        // 唯一索引去重
        exchange.setCode(exchangeMapper.flagUKeyAsLogicDelete(exchange.getCode()));
        exchangeMapper.updateById(exchange);
        // 删除
        exchangeMapper.deleteById(id);
    }

    /**
     * @sign : B9AD7AEF52B150BB
     */
    private WmsExchangeDO validateExchangeExists(Long id) {
        WmsExchangeDO exchange = exchangeMapper.selectById(id);
        if (exchange == null) {
            throw exception(EXCHANGE_NOT_EXISTS);
        }
        return exchange;
    }

    @Override
    public WmsExchangeDO getExchange(Long id) {
        return exchangeMapper.selectById(id);
    }

    @Override
    public PageResult<WmsExchangeDO> getExchangePage(WmsExchangePageReqVO pageReqVO) {
        return exchangeMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsExchangeDO
     */
    @Override
    public List<WmsExchangeDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return exchangeMapper.selectByIds(idList);
    }

    /**
     * 更新换货审批状态
     **/
    @Override
    public WmsExchangeDO updateExchangeAuditStatus(Long id, Integer status) {
        WmsExchangeDO exchangeDO = validateExchangeExists(id);
        exchangeDO.setAuditStatus(status);
        exchangeMapper.updateById(exchangeDO);
        return exchangeDO;
    }

    /**
     * 审批
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(WmsExchangeAuditStatus.Event event, WmsApprovalReqVO approvalReqVO) {
        // 设置业务默认值
        approvalReqVO.setBillType(BillType.WMS_EXCHANGE.getValue());
        approvalReqVO.setStatusType(WmsExchangeAuditStatus.getType());
        // 获得业务对象
        WmsExchangeDO exchangeDO = validateExchangeExists(approvalReqVO.getBillId());
        TransitionContext<WmsExchangeDO> ctx = TransitionContext.from(exchangeDO);
        ctx.setExtra(WmsConstants.APPROVAL_REQ_VO_KEY, approvalReqVO);
        // 触发事件
        exchangeStateMachine.fireEvent(event, ctx);
    }

    /**
     * 完成换货
     **/
    @Override
    public void finishExchange(WmsExchangeDO exchangeDO, List<WmsExchangeItemDO> exchangeItemDOList) {
        //校验本方法在事务中
        JdbcUtils.requireTransaction();
        log.info("[finishExchange][完成换货单开始，exchangeDO({}) exchangeItemDOList({})]", exchangeDO, exchangeItemDOList);
        int type = exchangeDO.getType();
        if (type == 1) {
            //换货单类型为良品转不良品
            this.performBinMove(exchangeDO, exchangeItemDOList);
        } else {
            //换货单类型为不良品转良品
            this.performOutboundInbound(exchangeDO, exchangeItemDOList);
        }

        log.info("[finishExchange][完成换货单结束，exchangeDO({}) exchangeItemDOList({})]", exchangeDO, exchangeItemDOList);
    }

    private void performOutboundInbound(WmsExchangeDO exchangeDO, List<WmsExchangeItemDO> exchangeItemDOList) {
        this.performInbound(exchangeDO, exchangeItemDOList);
        this.performOutbound(exchangeDO, exchangeItemDOList);
    }

    //执行出库动作
    private void performOutbound(WmsExchangeDO exchangeDO, List<WmsExchangeItemDO> exchangeItemDOList) {
        //1.新建入库单
        WmsInboundSaveReqVO createInboundVO = new WmsInboundSaveReqVO();
        createInboundVO.setType(WmsInboundType.EXCHANGE.getValue());
        createInboundVO.setWarehouseId(exchangeDO.getWarehouseId());
        createInboundVO.setAuditStatus(WmsInboundAuditStatus.DRAFT.getValue());
        createInboundVO.setInboundStatus(WmsInboundStatus.NONE.getValue());
        createInboundVO.setInboundTime(LocalDateTime.now());
        List<WmsInboundItemSaveReqVO> inboundItemDOList = new ArrayList<>();
        for (WmsExchangeItemDO itemDO : exchangeItemDOList) {
            WmsInboundItemSaveReqVO inboundItemDO = BeanUtils.toBean(itemDO, WmsInboundItemSaveReqVO.class);
            inboundItemDO.setPlanQty(itemDO.getQty());
            inboundItemDO.setActualQty(itemDO.getQty());
            inboundItemDOList.add(inboundItemDO);
        }
        createInboundVO.setItemList(inboundItemDOList);
        WmsInboundDO inboundDO = wmsInboundService.createInbound(createInboundVO);
        WmsApprovalReqVO approvalInboundReqVO = new WmsApprovalReqVO();
        approvalInboundReqVO.setBillType(BillType.WMS_INBOUND.getValue());
        approvalInboundReqVO.setBillId(inboundDO.getId());
        approvalInboundReqVO.setStatusType(WmsOutboundAuditStatus.getType());
        //2.提交审核并通过
        wmsInboundService.approve(WmsInboundAuditStatus.Event.SUBMIT, approvalInboundReqVO);
        wmsInboundService.approve(WmsInboundAuditStatus.Event.AGREE, approvalInboundReqVO);

        //记录流水
        Integer reason = exchangeDO.getType() == 1 ? WmsStockReason.EXCHANGE_GOOD_TO_BAD.getValue() : WmsStockReason.EXCHANGE_BAD_TO_GOOD.getValue();
//        stockFlowService.createForStockWarehouse(reason,wmsStockFlowDirection, productId, stockWarehouseDO, quantity, outboundId, outboundItemId);
    }

    //执行入库动作
    private void performInbound(WmsExchangeDO exchangeDO, List<WmsExchangeItemDO> exchangeItemDOList) {
        //1.新建出库单
        WmsOutboundSaveReqVO createReqVO = new WmsOutboundSaveReqVO();
        createReqVO.setWarehouseId(exchangeDO.getWarehouseId());
        createReqVO.setType(WmsOutboundType.EXCHANGE.getValue());
        createReqVO.setOutboundStatus(WmsOutboundStatus.NONE.getValue());
        List<WmsOutboundItemSaveReqVO> outboundItemDOList = new ArrayList<>();
        for (WmsExchangeItemDO itemDO : exchangeItemDOList) {
            WmsOutboundItemSaveReqVO outboundItemDO = BeanUtils.toBean(itemDO, WmsOutboundItemSaveReqVO.class);
            outboundItemDO.setPlanQty(itemDO.getQty());
            outboundItemDO.setActualQty(itemDO.getQty());
            outboundItemDO.setBinId(itemDO.getFromBinId());
            outboundItemDOList.add(outboundItemDO);
        }
        createReqVO.setItemList(outboundItemDOList);
        WmsOutboundDO outboundDO = outboundService.createOutbound(createReqVO);
        WmsApprovalReqVO approvalReqVO = new WmsApprovalReqVO();
        approvalReqVO.setBillType(BillType.WMS_OUTBOUND.getValue());
        approvalReqVO.setBillId(outboundDO.getId());
        approvalReqVO.setStatusType(WmsOutboundAuditStatus.getType());
        //2.提交审核并通过
        outboundService.approve(WmsOutboundAuditStatus.Event.SUBMIT, approvalReqVO);
        outboundService.approve(WmsOutboundAuditStatus.Event.AGREE, approvalReqVO);
        outboundService.approve(WmsOutboundAuditStatus.Event.FINISH, approvalReqVO);
    }

    //执行移库位操作
    private void performBinMove(WmsExchangeDO exchangeDO, List<WmsExchangeItemDO> exchangeItemDOList) {
        for (WmsExchangeItemDO itemDO : exchangeItemDOList) {
            WmsStockBinMoveSaveReqVO createReqVO = new WmsStockBinMoveSaveReqVO();
            createReqVO.setItemList(Collections.singletonList(BeanUtils.toBean(itemDO, WmsStockBinMoveItemSaveReqVO.class)));
            WmsInboundItemLogicDO inboundItemDetail = wmsInboundService.selectInboundItemLogicList(exchangeDO.getWarehouseId(), itemDO.getProductId(), TRUE).get(0);
            createReqVO.setInboundId(inboundItemDetail.getId());
            createReqVO.setWarehouseId(exchangeDO.getWarehouseId());
            stockBinMoveService.createStockBinMove(createReqVO);
        }
    }

    /**
     * 组装仓库
     **/
    @Override
    public void assembleWarehouse(List<WmsExchangeRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsExchangeRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsExchangeRespVO::getWarehouseId, WmsExchangeRespVO::setWarehouse);
    }
}
