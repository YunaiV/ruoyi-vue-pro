package cn.iocoder.yudao.module.wms.service.outbound;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachineWrapper;
import cn.iocoder.yudao.framework.cola.statemachine.TransitionContext;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.wms.config.OutboundStateMachineConfigure;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.item.vo.WmsOutboundItemRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.outbound.vo.WmsOutboundSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.item.WmsOutboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.outbound.WmsOutboundMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.outbound.item.WmsOutboundItemMapper;
import cn.iocoder.yudao.module.wms.dal.redis.lock.WmsLockRedisDAO;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.WmsConstants;
import cn.iocoder.yudao.module.wms.enums.common.WmsBillType;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.WmsApprovalHistoryService;
import cn.iocoder.yudao.module.wms.service.outbound.item.WmsOutboundItemService;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INBOUND_CAN_NOT_EDIT;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_CAN_NOT_EDIT;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_PLAN_QTY_ERROR;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_ITEM_PRODUCT_ID_REPEATED;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_NO_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.OUTBOUND_WAREHOUSE_ERROR;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_ENOUGH;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_BIN_NOT_EXISTS;

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
    protected WmsLockRedisDAO lockRedisDAO;

    @Resource
    private WmsOutboundMapper outboundMapper;

    @Resource
    @Lazy
    private WmsWarehouseBinService wmsWarehouseBinService;

    @Resource
    @Lazy
    private WmsStockBinService stockBinService;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private DeptApi deptApi;

    @Resource
    private WmsApprovalHistoryService approvalHistoryService;

    @Resource(name = OutboundStateMachineConfigure.STATE_MACHINE_NAME)
    private StateMachineWrapper<Integer, WmsOutboundAuditStatus.Event, WmsOutboundDO> outboundStateMachine;

    /**
     * @sign : A523E13094CD30CE
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsOutboundDO createOutbound(WmsOutboundSaveReqVO createReqVO) {
        // 设置单据号
        String no = noRedisDAO.generate(WmsNoRedisDAO.OUTBOUND_NO_PREFIX, 3);
        createReqVO.setAuditStatus(WmsOutboundAuditStatus.DRAFT.getValue());
        createReqVO.setOutboundStatus(WmsOutboundStatus.NONE.getValue());
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
            if (item.getPlanQty() == null || item.getPlanQty() <= 0) {
                throw exception(OUTBOUND_ITEM_PLAN_QTY_ERROR);
            }
            item.setId(null);
            // 设置归属
            item.setOutboundId(outbound.getId());
            item.setOutboundStatus(WmsOutboundStatus.NONE.getValue());
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
        if (!Objects.equals(warehouseId, outboundDO.getWarehouseId())) {
            throw exception(OUTBOUND_WAREHOUSE_ERROR);
        }
        Map<Long, Map<Long, WmsStockBinDO>> binMap = stockBinService.getStockBinMap(binIdList, StreamX.from(itemList).toList(WmsOutboundItemDO::getProductId));
        // 校验仓位库存
        for (WmsOutboundItemDO itemDO : itemList) {
            WmsStockBinDO stockBinDO = null;
            Map<Long, WmsStockBinDO> map = binMap.get(itemDO.getBinId());
            if (map != null) {
                stockBinDO = map.get(itemDO.getProductId());
            }
            if (stockBinDO == null) {
                throw exception(STOCK_BIN_NOT_EXISTS);
            }
            if (stockBinDO.getSellableQty() < itemDO.getPlanQty()) {
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
        WmsOutboundAuditStatus auditStatus = WmsOutboundAuditStatus.parse(exists.getAuditStatus());
        if (!auditStatus.matchAny(WmsOutboundAuditStatus.DRAFT, WmsOutboundAuditStatus.REJECT)) {
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
                if (item.getPlanQty() == null || item.getPlanQty() <= 0) {
                    throw exception(OUTBOUND_ITEM_PLAN_QTY_ERROR);
                }
                item.setOutboundStatus(WmsOutboundStatus.NONE.getValue());
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
        WmsOutboundAuditStatus auditStatus = WmsOutboundAuditStatus.parse(outbound.getAuditStatus());
        if (!auditStatus.matchAny(WmsOutboundAuditStatus.DRAFT, WmsOutboundAuditStatus.REJECT)) {
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
    public WmsOutboundDO validateOutboundExists(Long id) {
        WmsOutboundDO outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw exception(OUTBOUND_NOT_EXISTS);
        }
        return outbound;
    }

    @Override
    public List<WmsOutboundDO> selectByIds(List<Long> list) {
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        return outboundMapper.selectByIds(list);
    }

    @Override
    public List<WmsOutboundDO> getSimpleList(WmsOutboundPageReqVO pageReqVO) {
        return outboundMapper.getSimpleList(pageReqVO);
    }

    @Override
    public void assembleWarehouse(List<WmsOutboundRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsOutboundRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsOutboundRespVO::getWarehouseId, WmsOutboundRespVO::setWarehouse);
    }

    @Override
    public void assembleDept(List<WmsOutboundRespVO> list) {
        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(StreamX.from(list).map(WmsOutboundRespVO::getDeptId).toList());
        Map<Long, DeptSimpleRespVO> deptVOMap = new HashMap<>();
        for (DeptRespDTO productDTO : deptDTOMap.values()) {
            DeptSimpleRespVO deptVO = BeanUtils.toBean(productDTO, DeptSimpleRespVO.class);
            deptVOMap.put(productDTO.getId(), deptVO);
        }
        StreamX.from(list).assemble(deptVOMap, WmsOutboundRespVO::getDeptId, WmsOutboundRespVO::setDept);
    }

    @Override
    public void assembleCompany(List<WmsOutboundRespVO> list) {
        // todo 待东宇财务模块支持
    }

    @Override
    public void assembleApprovalHistory(List<WmsOutboundRespVO> list) {
        Map<Long, List<WmsApprovalHistoryRespVO>> groupedApprovalHistory = approvalHistoryService.selectGroupedApprovalHistory(WmsBillType.OUTBOUND, StreamX.from(list).toList(WmsOutboundRespVO::getId));
        StreamX.from(list).assemble(groupedApprovalHistory, WmsOutboundRespVO::getId, WmsOutboundRespVO::setApprovalHistoryList);
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
        // 处理明细的出库状态
        List<WmsOutboundItemDO> itemList = BeanUtils.toBean(outboundRespVO.getItemList(), WmsOutboundItemDO.class);
        outboundItemMapper.updateBatch(itemList);
        // 处理入库单状态
        WmsOutboundDO outboundDO = BeanUtils.toBean(outboundRespVO, WmsOutboundDO.class);
        outboundMapper.updateById(outboundDO);
    }

    @Override
    public PageResult<WmsOutboundDO> getOutboundPage(WmsOutboundPageReqVO pageReqVO) {
        return outboundMapper.selectPage(pageReqVO);
    }

    @Override
    public void approve(WmsOutboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO) {
        // 设置业务默认值
        approvalReqVO.setBillType(WmsBillType.OUTBOUND.getValue());
        approvalReqVO.setStatusType(WmsOutboundAuditStatus.getType());
        // 获得业务对象
        WmsOutboundDO inbound = validateOutboundExists(approvalReqVO.getBillId());
        // 锁在外，事务在锁内
        WmsOutboundServiceImpl proxy = SpringUtils.getBeanByExactType(WmsOutboundServiceImpl.class);
        lockRedisDAO.lockByWarehouse(inbound.getWarehouseId(), () -> {
            proxy.fireEvent(event, approvalReqVO, inbound);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    protected void fireEvent(WmsOutboundAuditStatus.Event event, WmsApprovalReqVO approvalReqVO, WmsOutboundDO inbound) {
        TransitionContext<WmsOutboundDO> ctx = outboundStateMachine.createContext(inbound);
        ctx.setExtra(WmsConstants.APPROVAL_REQ_VO_KEY, approvalReqVO);
        // 触发事件
        outboundStateMachine.fireEvent(event, ctx);
    }
}