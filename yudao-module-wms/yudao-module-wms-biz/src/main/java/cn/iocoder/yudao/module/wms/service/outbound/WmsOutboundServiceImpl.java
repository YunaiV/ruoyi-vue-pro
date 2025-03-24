package cn.iocoder.yudao.module.wms.service.outbound;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.config.statemachine.ColaContext;
import cn.iocoder.yudao.module.wms.config.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.outbound.WmsOutboundMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.outbound.item.WmsOutboundItemMapper;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.common.BillType;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.InboundStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.OutboundStatus;
import cn.iocoder.yudao.module.wms.service.outbound.item.WmsOutboundItemService;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 出库单 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsOutboundServiceImpl implements WmsOutboundService {

    @Resource
    @Lazy
    private WmsOutboundItemMapper outboundItemMapper;

    @Resource
    @Lazy
    private WmsOutboundItemService outboundItemService;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsOutboundMapper outboundMapper;

    @Resource
    @Lazy
    private WmsWarehouseBinService wmsWarehouseBinService;

    @Resource
    @Lazy
    private WmsStockBinService stockBinService;

    @Resource(name = OutboundAction.STATE_MACHINE_NAME)
    private StateMachineWrapper<Integer, OutboundAuditStatus.Event, WmsOutboundDO> outboundStateMachine;

    /**
     * @sign : A523E13094CD30CE
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsOutboundDO createOutbound(WmsOutboundSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.OUTBOUND_NO_PREFIX, OUTBOUND_NOT_EXISTS);
        createReqVO.setAuditStatus(outboundStateMachine.getInitStatus());
        createReqVO.setOutboundStatus(OutboundStatus.NONE.getValue());
        createReqVO.setNo(no);
        if (outboundMapper.getByNo(createReqVO.getNo()) != null) {
            throw exception(OUTBOUND_NO_DUPLICATE);
        }
        if (CollectionUtils.isEmpty(createReqVO.getItemList())) {
            throw exception(OUTBOUND_ITEM_NOT_EXISTS);
        }
        // 插入
        WmsOutboundDO outbound = BeanUtils.toBean(createReqVO, WmsOutboundDO.class);
        outboundMapper.insert(outbound);

        // 保存出库单详情详情
        List<WmsOutboundItemDO> toInsetList = new ArrayList<>();
        StreamX.from(createReqVO.getItemList()).filter(Objects::nonNull).forEach(item -> {
            item.setId(null);
            // 设置归属
            item.setOutboundId(outbound.getId());
            item.setOutboundStatus(OutboundStatus.NONE.getValue());
            toInsetList.add(BeanUtils.toBean(item, WmsOutboundItemDO.class));
        });
        processAndValidateForOutbound(outbound, toInsetList);
        outboundItemMapper.insertBatch(toInsetList);
        // 返回
        return outbound;
    }

    private void processAndValidateForOutbound(WmsOutboundDO outboundDO, List<WmsOutboundItemDO> itemList) {
        List<Long> binIdList = StreamX.from(itemList).toList(WmsOutboundItemDO::getBinId);
        List<WmsWarehouseBinDO> wmsWarehouseBinDOList = wmsWarehouseBinService.selectByIds(binIdList);
        Set<Long> warehouseIdSetOfBin = StreamX.from(wmsWarehouseBinDOList).toSet(WmsWarehouseBinDO::getWarehouseId);
        // 校验仓库
        if (warehouseIdSetOfBin.size() != 1) {
            throw exception(OUTBOUND_WAREHOUSE_ERROR);
        }

        Long warehouseId = StreamX.from(warehouseIdSetOfBin).first();
        if(!Objects.equals(warehouseId, outboundDO.getWarehouseId())) {
            throw exception(OUTBOUND_WAREHOUSE_ERROR);
        }

        Map<Long,Map<Long,WmsStockBinDO>> binMap = stockBinService.getStockBinMap(binIdList, StreamX.from(itemList).toList(WmsOutboundItemDO::getProductId));
        // 校验仓位库存
        for (WmsOutboundItemDO itemDO : itemList) {
            WmsStockBinDO stockBinDO = null;
            Map<Long,WmsStockBinDO> map= binMap.get(itemDO.getBinId());
            if(map!=null) {
                stockBinDO = map.get(itemDO.getProductId());
            }
            if (stockBinDO == null) {
                throw exception(STOCK_BIN_NOT_EXISTS);
            }
            if (stockBinDO.getAvailableQuantity() < itemDO.getPlanQuantity()) {
                throw exception(STOCK_BIN_NOT_ENOUGH);
            }
        }
    }

    /**
     * @sign : E1DA4E6302BF0EFA
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsOutboundDO updateOutbound(WmsOutboundSaveReqVO updateReqVO) {
        // 校验存在
        WmsOutboundDO exists = validateOutboundExists(updateReqVO.getId());
        // 判断是否允许编辑
        if (!outboundStateMachine.canEdit(exists.getAuditStatus())) {
            throw exception(OUTBOUND_CAN_NOT_EDIT);
        }
        // 单据号不允许被修改
        updateReqVO.setNo(exists.getNo());
        // 更新
        WmsOutboundDO outbound = BeanUtils.toBean(updateReqVO, WmsOutboundDO.class);
        outboundMapper.updateById(outbound);

        // 保存出库单详情详情
        if (updateReqVO.getItemList() != null) {
            List<WmsOutboundItemDO> existsInDB = outboundItemMapper.selectByOutboundId(updateReqVO.getId());
            StreamX.CompareResult<WmsOutboundItemDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getItemList(), WmsOutboundItemDO.class), WmsOutboundItemDO::getId);
            List<WmsOutboundItemDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsOutboundItemDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsOutboundItemDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsOutboundItemDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsOutboundItemDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(OUTBOUND_ITEM_PRODUCT_ID_REPEATED);
            }
            processAndValidateForOutbound(outbound, finalList);
            // 设置归属
            finalList.forEach(item -> {
                item.setOutboundId(updateReqVO.getId());
            });
            // 保存详情
            outboundItemMapper.insertBatch(toInsetList);
            outboundItemMapper.updateBatch(toUpdateList);
            outboundItemMapper.deleteBatchIds(toDeleteList);
        }
        // 返回
        return outbound;
    }

    @Override
    public WmsOutboundDO updateOutboundAuditStatus(Long id, Integer status) {
        WmsOutboundDO inboundDO = validateOutboundExists(id);
        inboundDO.setAuditStatus(status);
        outboundMapper.updateById(inboundDO);
        return inboundDO;
    }

    /**
     * @sign : FE27B31D0FCE6A00
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOutbound(Long id) {
        // 校验存在
        WmsOutboundDO outbound = validateOutboundExists(id);
        // 判断是否允许删除
        if (!outboundStateMachine.canDelete(outbound.getAuditStatus())) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        // 唯一索引去重
        outbound.setNo(outboundMapper.flagUKeyAsLogicDelete(outbound.getNo()));
        outboundMapper.updateById(outbound);
        // 删除
        outboundMapper.deleteById(id);
    }

    /**
     * @sign : 87FB607B65309CC4
     */
    private WmsOutboundDO validateOutboundExists(Long id) {
        WmsOutboundDO outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw exception(OUTBOUND_NOT_EXISTS);
        }
        return outbound;
    }

    @Override
    public WmsOutboundDO getOutbound(Long id) {
        return outboundMapper.selectById(id);
    }

    @Override
    public WmsOutboundRespVO getOutboundWithItemList(Long id) {
        // 查询数据
        WmsOutboundDO outbound = this.getOutbound(id);
        if (outbound == null) {
            throw exception(OUTBOUND_NOT_EXISTS);
        }
        // 转换
        WmsOutboundRespVO outboundVO = BeanUtils.toBean(outbound, WmsOutboundRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(List.of(outboundVO)).mapping(WmsOutboundRespVO::getCreator, WmsOutboundRespVO::setCreatorName).mapping(WmsOutboundRespVO::getCreator, WmsOutboundRespVO::setUpdaterName).fill();
        // 组装出库单详情
        List<WmsOutboundItemDO> outboundItemList = outboundItemService.selectByOutboundId(outboundVO.getId());
        outboundVO.setItemList(BeanUtils.toBean(outboundItemList, WmsOutboundItemRespVO.class));
        outboundItemService.assembleProducts(outboundVO.getItemList());
        return outboundVO;
    }

    @Override
    public void finishOutbound(WmsOutboundRespVO outboundRespVO) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        int countOfNone = 0;
        for (WmsOutboundItemRespVO respVO : outboundRespVO.getItemList()) {
            if (InboundStatus.NONE.matchAny(respVO.getOutboundStatus())) {
                countOfNone++;
            }
        }
        if (countOfNone > 0) {
            throw exception(INBOUND_NOT_COMPLETE);
        }
        // 处理明细的出库状态
        List<WmsOutboundItemDO> itemList = BeanUtils.toBean(outboundRespVO.getItemList(), WmsOutboundItemDO.class);
        outboundItemMapper.updateBatch(itemList);
        // 处理入库单状态
        WmsOutboundDO outboundDO = BeanUtils.toBean(outboundRespVO, WmsOutboundDO.class);
        outboundDO.setOutboundStatus(OutboundStatus.ALL.getValue());
        // outboundDO.setArrivalActualTime(LocalDateTime.now());
        outboundMapper.updateById(outboundDO);
    }

    @Override
    public PageResult<WmsOutboundDO> getOutboundPage(WmsOutboundPageReqVO pageReqVO) {
        return outboundMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(OutboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO) {
        // 设置业务默认值
        approvalReqVO.setBillType(BillType.INBOUND.getValue());
        approvalReqVO.setStatusType(InboundAuditStatus.getType());
        // 获得业务对象
        WmsOutboundDO inbound = validateOutboundExists(approvalReqVO.getBillId());
        ColaContext<WmsOutboundDO> ctx = ColaContext.from(inbound, approvalReqVO);
        // 触发事件
        outboundStateMachine.fireEvent(event, ctx);
        // 如果未处理，则抛出异常
        if (!ctx.handled()) {
            throw exception(INBOUND_APPROVAL_CONDITION_IS_NOT_MATCH);
        }
    }
}
