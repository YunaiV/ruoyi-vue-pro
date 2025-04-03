package cn.iocoder.yudao.module.wms.service.stock.flow;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.flow.vo.WmsStockFlowSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.outbound.WmsOutboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.WmsPickupDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.flow.WmsStockFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.ownership.WmsStockOwnershipDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stock.flow.WmsStockFlowMapper;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockFlowDirection;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockReason;
import cn.iocoder.yudao.module.wms.enums.stock.WmsStockType;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.outbound.WmsOutboundService;
import cn.iocoder.yudao.module.wms.service.pickup.WmsPickupService;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.ownership.WmsStockOwnershipService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.STOCK_FLOW_NOT_EXISTS;

/**
 * 库存流水 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsStockFlowServiceImpl implements WmsStockFlowService {

    @Resource
    private WmsStockFlowMapper stockFlowMapper;

    @Resource
    private ErpProductApi productApi;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    @Lazy
    private WmsWarehouseBinService warehouseBinService;

    @Resource
    @Lazy
    private WmsStockBinService stockBinService;

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    @Lazy
    private WmsOutboundService outboundService;

    @Resource
    @Lazy
    private WmsPickupService pickupService;

    @Resource
    @Lazy
    private WmsStockOwnershipService stockOwnershipService;

    @Resource
    private DeptApi deptApi;



    /**
     * @sign : 5CDC0A12A8B023F4
     */
    @Override
    public WmsStockFlowDO createStockFlow(WmsStockFlowSaveReqVO createReqVO) {
        // 插入
        WmsStockFlowDO stockFlow = BeanUtils.toBean(createReqVO, WmsStockFlowDO.class);
        stockFlowMapper.insert(stockFlow);
        // 返回
        return stockFlow;
    }

    /**
     * @sign : 92744785BC7A4404
     */
    @Override
    public WmsStockFlowDO updateStockFlow(WmsStockFlowSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockFlowDO exists = validateStockFlowExists(updateReqVO.getId());
        // 更新
        WmsStockFlowDO stockFlow = BeanUtils.toBean(updateReqVO, WmsStockFlowDO.class);
        stockFlowMapper.updateById(stockFlow);
        // 返回
        return stockFlow;
    }

    /**
     * @sign : 2C67EEBE8FF6B925
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockFlow(Long id) {
        // 校验存在
        WmsStockFlowDO stockFlow = validateStockFlowExists(id);
        // 删除
        stockFlowMapper.deleteById(id);
    }

    /**
     * @sign : 9FD17EF243CE9F52
     */
    private WmsStockFlowDO validateStockFlowExists(Long id) {
        WmsStockFlowDO stockFlow = stockFlowMapper.selectById(id);
        if (stockFlow == null) {
            throw exception(STOCK_FLOW_NOT_EXISTS);
        }
        return stockFlow;
    }

    @Override
    public WmsStockFlowDO getStockFlow(Long id) {
        return stockFlowMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockFlowDO> getStockFlowPage(WmsStockFlowPageReqVO pageReqVO) {
        return stockFlowMapper.selectPage(pageReqVO);
    }

    @Override
    public WmsStockFlowDO getLastFlow(Long warehouseId, Integer stockType, Long stockId) {
        return stockFlowMapper.getLastFlow(warehouseId, stockType, stockId);
    }

    /**
     * 创建仓库库存变化流水
     */
    public void createForStockWarehouse(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockWarehouseDO stockWarehouseDO, Integer quantity, Long reasonId, Long reasonItemId) {
        createFor(reason, WmsStockType.WAREHOUSE, direction, stockWarehouseDO.getId(), stockWarehouseDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, stockFlowDO -> {
            // 采购计划量
            stockFlowDO.setPurchasePlanQty(stockWarehouseDO.getPurchasePlanQty());
            // 采购在途量
            stockFlowDO.setPurchaseTransitQty(stockWarehouseDO.getPurchaseTransitQty());
            // 退货在途量
            stockFlowDO.setReturnTransitQty(stockWarehouseDO.getReturnTransitQty());
            // 可售量，未被单据占用的良品数量
            stockFlowDO.setSellableQty(stockWarehouseDO.getSellableQty());
            // 可用量，在库的良品数量
            stockFlowDO.setAvailableQty(stockWarehouseDO.getAvailableQty());
            // 待上架数量
            stockFlowDO.setShelvingPendingQty(stockWarehouseDO.getShelvingPendingQty() + quantity);
            // 不良品数量
            stockFlowDO.setDefectiveQty(stockWarehouseDO.getDefectiveQty());
            // 待出库量
            stockFlowDO.setOutboundPendingQty(stockWarehouseDO.getOutboundPendingQty());
        });
    }

    /**
     * 创建所有者库存变化流水
     */
    public void createForStockOwner(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockOwnershipDO stockOwnershipDO, Integer quantity, Long reasonId, Long reasonItemId) {
        createFor(reason, WmsStockType.OWNERSHIP, direction, stockOwnershipDO.getId(), stockOwnershipDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, stockFlowDO -> {
            // 采购计划量
            // stockFlowDO.setPurchasePlanQty(stockOwnershipDO.getPurchasePlanQty());
            // 采购在途量
            // stockFlowDO.setPurchaseTransitQty(stockOwnershipDO.getPurchaseTransitQty());
            // 退货在途量
            // stockFlowDO.setReturnTransitQty(stockOwnershipDO.getReturnTransitQty());
            // 可售量，未被单据占用的良品数量
            // stockFlowDO.setSellableQty(stockOwnershipDO.getSellableQty());
            // 可用量，在库的良品数量
            stockFlowDO.setAvailableQty(stockOwnershipDO.getAvailableQty());
            // 待上架数量
            stockFlowDO.setShelvingPendingQty(stockOwnershipDO.getShelvingPendingQty() + quantity);
            // 不良品数量
            // stockFlowDO.setDefectiveQty(stockOwnershipDO.getDefectiveQty());
            // 待出库量
            stockFlowDO.setOutboundPendingQty(stockOwnershipDO.getOutboundPendingQty());
        });
    }

    /**
     * 创建仓位库存变化流水
     */
    public void createForStockBin(WmsStockReason reason, WmsStockFlowDirection direction, Long productId, WmsStockBinDO stockBinDO, Integer quantity, Long reasonId, Long reasonItemId) {
        createFor(reason, WmsStockType.BIN, direction, stockBinDO.getId(), stockBinDO.getWarehouseId(), productId, quantity, reasonId, reasonItemId, stockFlowDO -> {
            // 采购计划量
            // stockFlowDO.setPurchasePlanQty(stockOwnershipDO.getPurchasePlanQty());
            // 采购在途量
            // stockFlowDO.setPurchaseTransitQty(stockOwnershipDO.getPurchaseTransitQty());
            // 退货在途量
            // stockFlowDO.setReturnTransitQty(stockOwnershipDO.getReturnTransitQty());
            // 可售量，未被单据占用的良品数量
            stockFlowDO.setSellableQty(stockBinDO.getSellableQty());
            // 可用量，在库的良品数量
            stockFlowDO.setAvailableQty(stockBinDO.getAvailableQty());
            // 待上架数量
            // stockFlowDO.setShelvingPendingQty(stockBinDO.getShelvingPendingQty() + quantity);
            // 不良品数量
            // stockFlowDO.setDefectiveQty(stockOwnershipDO.getDefectiveQty());
            // 待出库量
            stockFlowDO.setOutboundPendingQty(stockBinDO.getOutboundPendingQty());
        });
    }

    /**
     * 创建仓库库存变化流水
     */
    public void createFor(WmsStockReason reason, WmsStockType stockType, WmsStockFlowDirection direction, Long stockId, Long warehouseId, Long productId, Integer quantity, Long reasonId, Long reasonItemId, Consumer<WmsStockFlowDO> consumer) {
        // 校验本方法在事务中
        JdbcUtils.requireTransaction();
        // 获取上一个流水
        WmsStockFlowDO lastStockFlowDO = this.getLastFlow(warehouseId, stockType.getValue(), stockId);
        // 创建本次流水
        WmsStockFlowDO stockFlowDO = new WmsStockFlowDO();
        stockFlowDO.setWarehouseId(warehouseId);
        stockFlowDO.setProductId(productId);
        stockFlowDO.setStockType(stockType.getValue());
        stockFlowDO.setStockId(stockId);
        stockFlowDO.setReason(reason.getValue());
        stockFlowDO.setReasonBillId(reasonId);
        stockFlowDO.setDirection(direction.getValue());
        stockFlowDO.setReasonItemId(reasonItemId);
        if (lastStockFlowDO != null) {
            stockFlowDO.setPrevFlowId(lastStockFlowDO.getId());
        } else {
            stockFlowDO.setPrevFlowId(0L);
        }
        // 变更量
        stockFlowDO.setDeltaQty(quantity);
        consumer.accept(stockFlowDO);
        // 
        stockFlowDO.setFlowTime(new Timestamp(System.currentTimeMillis()));
        // 保存
        stockFlowMapper.insert(stockFlowDO);
        // 关联前项流水
        if (lastStockFlowDO != null) {
            lastStockFlowDO.setNextFlowId(stockFlowDO.getId());
            this.updateStockFlow(BeanUtils.toBean(lastStockFlowDO, WmsStockFlowSaveReqVO.class));
        }
    }

    @Override
    public List<WmsStockFlowDO> selectStockFlow(Long stockType, Long stockId) {
        return stockFlowMapper.selectStockFlow(stockType, stockId);
    }


    @Override
    public void assembleProducts(List<WmsStockFlowRespVO> list) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(list).map(WmsStockFlowRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(list).assemble(productVOMap, WmsStockFlowRespVO::getProductId, WmsStockFlowRespVO::setProduct);
    }

    @Override
    public void assembleWarehouse(List<WmsStockFlowRespVO> list) {

        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockFlowRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values())
            .toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));

        StreamX.from(list).assemble(warehouseVOMap, WmsStockFlowRespVO::getWarehouseId, WmsStockFlowRespVO::setWarehouse);
    }

    @Override
    public void assembleBin(List<WmsStockFlowRespVO> list) {

        // 过滤库位的动账流水
        List<WmsStockFlowRespVO> binFlowList=StreamX.from(list).filter(v -> Objects.equals(WmsStockType.BIN.getValue(), v.getStockType())).toList();
        List<Long> stockBinIds = StreamX.from(binFlowList).toList(WmsStockFlowRespVO::getStockId).stream().distinct().toList();
        // 查询到对应的库位库存记录
        List<WmsStockBinDO> stockBinDOList = stockBinService.selectStockBinByIds(stockBinIds);
        Map<Long,WmsStockBinDO> stockBinDOMap = StreamX.from(stockBinDOList).toMap(WmsStockBinDO::getId);
        List<Long> binIds = StreamX.from(stockBinDOList).toList(WmsStockBinDO::getBinId);
        // 查询到库位记录
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(binIds);
        Map<Long,WmsWarehouseBinRespVO> binVOMap = StreamX.from(binDOList).toMap(WmsWarehouseBinDO::getId, binDO -> BeanUtils.toBean(binDO, WmsWarehouseBinRespVO.class));

        // 循环库位的动账流水
        for (WmsStockFlowRespVO binRespVO : binFlowList) {
            // 找到流水对应的仓位库存记录
            WmsStockBinDO stockBinDO = stockBinDOMap.get(binRespVO.getStockId());
            // 通过仓位库存记录找到对应的仓位
            WmsWarehouseBinRespVO binVO=binVOMap.get(stockBinDO.getBinId());
            // 设置仓位
            binRespVO.setBin(binVO);
        }
    }

    @Override
    public void assembleInbound(List<WmsStockFlowRespVO> list) {

        List<WmsStockFlowRespVO> inboundFlowList=StreamX.from(list).filter(v -> Objects.equals(WmsStockReason.INBOUND.getValue(), v.getReason())).toList();
        List<WmsInboundDO> inboundDOList = inboundService.selectByIds(StreamX.from(inboundFlowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsInboundSimpleRespVO> inboundMap = StreamX.from(inboundDOList).toMap(WmsInboundDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsInboundSimpleRespVO.class));
        StreamX.from(inboundFlowList).assemble(inboundMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setInbound);

    }

    @Override
    public void assembleOutbound(List<WmsStockFlowRespVO> list) {

        List<WmsStockFlowRespVO> outboundFlowList=StreamX.from(list).filter(v -> Objects.equals(WmsStockReason.OUTBOUND_AGREE.getValue(), v.getReason())).toList();
        List<WmsOutboundDO> outboundDOList = outboundService.selectByIds(StreamX.from(outboundFlowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsInboundSimpleRespVO> outboundMap = StreamX.from(outboundDOList).toMap(WmsOutboundDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsInboundSimpleRespVO.class));
        StreamX.from(outboundFlowList).assemble(outboundMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setInbound);

    }

    @Override
    public void assemblePickup(List<WmsStockFlowRespVO> list) {

        List<WmsStockFlowRespVO> pickupFlowList=StreamX.from(list).filter(v -> Objects.equals(WmsStockReason.PICKUP.getValue(), v.getReason())).toList();
        List<WmsPickupDO> pickupDOList = pickupService.selectByIds(StreamX.from(pickupFlowList).toList(WmsStockFlowRespVO::getReasonBillId));
        Map<Long, WmsInboundSimpleRespVO> pickupMap = StreamX.from(pickupDOList).toMap(WmsPickupDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsInboundSimpleRespVO.class));
        StreamX.from(pickupFlowList).assemble(pickupMap, WmsStockFlowRespVO::getReasonBillId, WmsStockFlowRespVO::setInbound);

    }

    @Override
    public void assembleCompanyAndDept(List<WmsStockFlowRespVO> list) {


        // 过滤所有者库存的动账流水
        List<WmsStockFlowRespVO> ownershioFlowList=StreamX.from(list).filter(v -> Objects.equals(WmsStockType.OWNERSHIP.getValue(), v.getStockType())).toList();
        List<Long> stockOwnershipIds = StreamX.from(ownershioFlowList).toList(WmsStockFlowRespVO::getStockId).stream().distinct().toList();
        // 查询到对应的所有者库存记录
        List<WmsStockOwnershipDO> stockOwnershipDOList = stockOwnershipService.selectByIds(stockOwnershipIds);
        Map<Long,WmsStockOwnershipDO> ownershipDOMap = StreamX.from(stockOwnershipDOList).toMap(WmsStockOwnershipDO::getId);
        List<Long> companyIds = StreamX.from(stockOwnershipDOList).toList(WmsStockOwnershipDO::getCompanyId);
        List<Long> deptIds = StreamX.from(stockOwnershipDOList).toList(WmsStockOwnershipDO::getDeptId);
        // 查询到部门记录
        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(deptIds);
        Map<Long, DeptSimpleRespVO> deptSimpleVOMap = BeanUtils.toBean(deptDTOMap, DeptSimpleRespVO.class);

        // 循环库位的动账流水
        for (WmsStockFlowRespVO binRespVO : ownershioFlowList) {
            // 找到流水对应的所有者库存记录
            WmsStockOwnershipDO stockOwnershipDO = ownershipDOMap.get(binRespVO.getStockId());
            // 通过所有者库存记录找到对应的部门
            DeptSimpleRespVO deptVO=deptSimpleVOMap.get(stockOwnershipDO.getDeptId());
            // 设置仓位
            binRespVO.setDept(deptVO);
            // 通过所有者库存记录找到对应的财务公司
            //todo 待东宇财务公司就绪
        }


    }


}
