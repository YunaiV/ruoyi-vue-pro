package cn.iocoder.yudao.module.wms.service.inbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.fms.api.finance.FmsCompanyApi;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.wms.controller.admin.company.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.dept.DeptSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.vo.WmsInboundSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsStockWarehouseSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZoneSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.WmsInboundDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemBinQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.pickup.item.WmsPickupItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.warehouse.WmsStockWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.zone.WmsWarehouseZoneDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemBinQueryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.WmsInboundItemQueryMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.inbound.item.flow.WmsInboundItemFlowMapper;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.inbound.WmsInboundStatus;
import cn.iocoder.yudao.module.wms.service.inbound.WmsInboundService;
import cn.iocoder.yudao.module.wms.service.pickup.item.WmsPickupItemService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import cn.iocoder.yudao.module.wms.service.warehouse.zone.WmsWarehouseZoneService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * 入库单详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
public class WmsInboundItemServiceImpl implements WmsInboundItemService {

    @Resource
    @Lazy
    private WmsInboundService inboundService;

    @Resource
    private WmsInboundItemMapper inboundItemMapper;

    @Resource
    private WmsInboundItemQueryMapper inboundItemQueryMapper;

    @Resource
    private WmsInboundItemBinQueryMapper inboundItemBinQueryMapper;

    @Resource
    private ErpProductApi productApi;

    @Resource
    @Lazy
    private WmsInboundItemFlowMapper inboundItemFlowMapper;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    @Lazy
    private WmsWarehouseZoneService warehouseZoneService;

    @Resource
    @Lazy
    private WmsWarehouseBinService warehouseBinService;

    @Resource
    @Lazy
    private WmsStockWarehouseService stockWarehouseService;

    @Resource
    @Lazy
    private WmsPickupItemService pickupItemService;


    @Resource
    private DeptApi deptApi;

    @Resource
    private FmsCompanyApi companyApi;

    /**
     * @sign : 22513AF8FE74E202
     */
    @Override
    public WmsInboundItemDO createInboundItem(WmsInboundItemSaveReqVO createReqVO) {
        // 按 wms_inbound_item.inbound_id -> wms_inbound.id 的引用关系，校验存在性
        if (createReqVO.getInboundId() != null) {
            WmsInboundDO inbound = inboundService.getInbound(createReqVO.getInboundId());
            if (inbound == null) {
                throw exception(INBOUND_NOT_EXISTS);
            }
        }
        // 插入
        WmsInboundItemDO inboundItem = BeanUtils.toBean(createReqVO, WmsInboundItemDO.class);
        inboundItemMapper.insert(inboundItem);
        // 返回
        return inboundItem;
    }

    /**
     * @sign : 7D86D27C4FFD8AFE
     */
    @Override
    public WmsInboundItemDO updateInboundItem(WmsInboundItemSaveReqVO updateReqVO) {
        // 校验存在
        WmsInboundItemDO exists = validateInboundItemExists(updateReqVO.getId());
        // 按 wms_inbound_item.inbound_id -> wms_inbound.id 的引用关系，校验存在性
        if (updateReqVO.getInboundId() != null) {
            WmsInboundDO inbound = inboundService.getInbound(updateReqVO.getInboundId());
            if (inbound == null) {
                throw exception(INBOUND_NOT_EXISTS);
            }
        }
        // 更新
        WmsInboundItemDO inboundItem = BeanUtils.toBean(updateReqVO, WmsInboundItemDO.class);
        inboundItemMapper.updateById(inboundItem);
        // 返回
        return inboundItem;
    }

    /**
     * @sign : 412E589DBBC092B4
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInboundItem(Long id) {
        // 校验存在
        WmsInboundItemDO inboundItem = validateInboundItemExists(id);
        // 删除
        inboundItemMapper.deleteById(id);
    }

    /**
     * @sign : 1E3323E02C6F15FA
     */
    private WmsInboundItemDO validateInboundItemExists(Long id) {
        WmsInboundItemDO inboundItem = inboundItemMapper.selectById(id);
        if (inboundItem == null) {
            throw exception(INBOUND_ITEM_NOT_EXISTS);
        }
        return inboundItem;
    }

    /**
     * 获得入库单详情
     *
     * @param id 编号
     * @return 入库单详情
     */
    @Override
    public WmsInboundItemDO getInboundItem(Long id) {
        return inboundItemMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInboundItemQueryDO> getInboundItemPage(WmsInboundItemPageReqVO pageReqVO) {
        return inboundItemQueryMapper.selectPage(pageReqVO);
    }

    @Override
    public List<WmsInboundItemQueryDO> getInboundItemList(Long companyId, List<Long> productIds) {
        return inboundItemQueryMapper.selectListByCompany(companyId, productIds);
    }

    /**
     * 按 inboundId 查询 WmsInboundItemDO
     */
    @Override
    public List<WmsInboundItemDO> selectByInboundId(Long inboundId, int limit) {
        return inboundItemMapper.selectByInboundId(inboundId, limit);
    }

    /**
     * 更新实际入库量
     *
     * @param updateReqVOList 更新信息
     */
    @Override
    public void updateActualQuantity(List<WmsInboundItemSaveReqVO> updateReqVOList) {
        if (CollectionUtils.isEmpty(updateReqVOList)) {
            return;
        }
        Set<Long> inboundIds = StreamX.from(updateReqVOList).toSet(WmsInboundItemSaveReqVO::getInboundId);
        if (inboundIds.size() > 1) {
            throw exception(INBOUND_ITEM_INBOUND_ID_DUPLICATE);
        }
        Long inboundId = inboundIds.stream().findFirst().get();
        WmsInboundDO inboundDO = inboundService.validateInboundExists(inboundId);
        WmsInboundAuditStatus auditStatus = WmsInboundAuditStatus.parse(inboundDO.getAuditStatus());
        WmsInboundStatus inboundStatus = WmsInboundStatus.parse(inboundDO.getInboundStatus());
        // 除了审批中的情况，其它情况不允许修改实际入库量
        if (!auditStatus.matchAny(WmsInboundAuditStatus.AUDITING)) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        // 除了未入库的情况，其它情况不允许修改实际入库量
        if (!inboundStatus.matchAny(WmsInboundStatus.NONE)) {
            throw exception(INBOUND_CAN_NOT_EDIT);
        }
        Map<Long, WmsInboundItemSaveReqVO> updateReqVOMap = StreamX.from(updateReqVOList).toMap(WmsInboundItemSaveReqVO::getId);
        List<WmsInboundItemDO> inboundItemDOSInDB = inboundItemMapper.selectByIds(StreamX.from(updateReqVOList).toList(WmsInboundItemSaveReqVO::getId));
        for (WmsInboundItemDO itemDO : inboundItemDOSInDB) {
            WmsInboundItemSaveReqVO updateReqVO = updateReqVOMap.get(itemDO.getId());
            if (updateReqVO.getActualQty() == null || updateReqVO.getActualQty() <= 0) {
                throw exception(INBOUND_ITEM_ACTUAL_QTY_ERROR);
            }
            itemDO.setActualQty(updateReqVO.getActualQty());
        }
        // 保存
        inboundItemMapper.updateBatch(inboundItemDOSInDB);
    }

    /**
     * 按 id 查询 WmsInboundItemDO
     */
    @Override
    public List<WmsInboundItemDO> selectByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return inboundItemMapper.selectByIds(ids);
    }

    /**
     * 更新 WmsInboundItemDO
     */
    @Override
    public void updateById(WmsInboundItemDO inboundItemDO) {
        inboundItemMapper.updateById(inboundItemDO);
    }

    /**
     * 获取待上架清单
     */
    @Override
    public PageResult<WmsInboundItemQueryDO> getPickupPending(WmsPickupPendingPageReqVO pageReqVO) {
        return inboundItemQueryMapper.getPickupPending(pageReqVO);
    }

    /**
     * 装配入库单
     */
    @Override
    public void assembleInbound(List<? extends WmsInboundItemRespVO> itemList) {
        List<WmsInboundDO> inboundDOList = inboundService.selectByIds(StreamX.from(itemList).toList(WmsInboundItemRespVO::getInboundId));
        Map<Long, WmsInboundSimpleRespVO> inboundMap = StreamX.from(inboundDOList).toMap(WmsInboundDO::getId, inboundDO -> BeanUtils.toBean(inboundDO, WmsInboundSimpleRespVO.class));
        StreamX.from(itemList).assemble(inboundMap, WmsInboundItemRespVO::getInboundId, WmsInboundItemRespVO::setInbound);
    }

    /**
     * 按仓库id和商品id查询
     */
    @Override
    public List<WmsInboundItemDO> selectItemListHasAvailableQty(Long warehouseId, Long productId) {
        return inboundItemMapper.selectItemListHasAvailableQty(warehouseId, productId);
    }

    /**
     * 保存入库单详情
     */
    @Override
    public void saveItems(List<WmsInboundItemDO> itemsToUpdate, List<WmsInboundItemFlowDO> inboundItemFlowList) {
        // 保存流水
        inboundItemFlowMapper.insertBatch(inboundItemFlowList);
        Map<Long, WmsInboundItemFlowDO> flowDOMap = StreamX.from(inboundItemFlowList).toMap(WmsInboundItemFlowDO::getInboundItemId);
        for (WmsInboundItemDO itemDO : itemsToUpdate) {
            WmsInboundItemFlowDO flowDO = flowDOMap.get(itemDO.getId());
            itemDO.setLatestFlowId(flowDO.getId());
        }
        // 保存余量
        inboundItemMapper.updateBatch(itemsToUpdate);
    }

    /**
     * 装配仓库
     */
    @Override
    public void assembleWarehouse(List<? extends WmsInboundItemRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsInboundItemRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsInboundItemRespVO::getWarehouseId, WmsInboundItemRespVO::setWarehouse);
    }

    /**
     * 装配仓库货位
     */
    @Override
    public void assembleWarehouseBin(List<WmsInboundItemBinRespVO> list) {
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(StreamX.from(list).toSet(WmsInboundItemBinRespVO::getBinId));
        List<WmsWarehouseBinRespVO> binVOList = BeanUtils.toBean(binDOList, WmsWarehouseBinRespVO.class);
        Set<Long> zoneIds = StreamX.from(binDOList).map(WmsWarehouseBinDO::getZoneId).toSet();
        List<WmsWarehouseZoneDO> zoneDOList = warehouseZoneService.selectByIds(zoneIds);
        Map<Long, WmsWarehouseZoneDO> zoneVOMap = StreamX.from(zoneDOList).toMap(WmsWarehouseZoneDO::getId);
        StreamX.from(list).assemble(binVOList, WmsWarehouseBinRespVO::getId, WmsInboundItemBinRespVO::getBinId,(e,v)->{
            if(v!=null) {
                e.setBinName(v.getName());
                WmsWarehouseZoneDO zoneDO=zoneVOMap.get(v.getZoneId());
                if(zoneDO!=null) {
                    e.setStockType(zoneDO.getStockType());
                }
            }
        });



    }

    /**
     * 装配产品
     */
    @Override
    public void assembleProducts(List<? extends WmsInboundItemRespVO> itemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(itemList).map(WmsInboundItemRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(itemList).assemble(productVOMap, WmsInboundItemRespVO::getProductId, WmsInboundItemRespVO::setProduct);
    }

    /**
     * 装配部门
     */
    @Override
    public void assembleDept(List<? extends WmsInboundItemRespVO> list) {

        Set<Long> deptIds=new HashSet<>();
        deptIds.addAll(StreamX.from(list).map(WmsInboundItemRespVO::getDeptId).toList());
        deptIds.addAll(StreamX.from(list).map(WmsInboundItemRespVO::getInboundDeptId).toList());

        Map<Long, DeptRespDTO> deptDTOMap = deptApi.getDeptMap(deptIds);
        Map<Long, DeptSimpleRespVO> deptVOMap = new HashMap<>();
        for (DeptRespDTO productDTO : deptDTOMap.values()) {
            DeptSimpleRespVO deptVO = BeanUtils.toBean(productDTO, DeptSimpleRespVO.class);
            deptVOMap.put(productDTO.getId(), deptVO);
        }
        StreamX.from(list).assemble(deptVOMap, WmsInboundItemRespVO::getDeptId, WmsInboundItemRespVO::setDept);
        StreamX.from(list).assemble(deptVOMap, WmsInboundItemRespVO::getInboundDeptId, WmsInboundItemRespVO::setInboundDept);
    }

    /**
     * 装配公司
     */
    @Override
    public void assembleCompany(List<? extends WmsInboundItemRespVO> list) {

        Set<Long> companyIds=new HashSet<>();
        companyIds.addAll(StreamX.from(list).map(WmsInboundItemRespVO::getCompanyId).toList());
        companyIds.addAll(StreamX.from(list).map(WmsInboundItemRespVO::getInboundCompanyId).toList());
        Map<Long, FmsCompanyDTO> companyMap = companyApi.getCompanyMap(companyIds);
        Map<Long, FmsCompanySimpleRespVO> companyVOMap = StreamX.from(companyMap.values()).toMap(FmsCompanyDTO::getId, v -> BeanUtils.toBean(v, FmsCompanySimpleRespVO.class));
        //
        StreamX.from(list).assemble(companyVOMap, WmsInboundItemRespVO::getCompanyId, WmsInboundItemRespVO::setCompany);
        StreamX.from(list).assemble(companyVOMap, WmsInboundItemRespVO::getInboundCompanyId, WmsInboundItemRespVO::setInboundCompany);

    }

    /**
     * 装配商品id
     */
    @Override
    public void assembleProductIds(List<WmsInboundItemImportExcelVO> impVOList) {
        List<String> productCodes = StreamX.from(impVOList).toList(WmsInboundItemImportExcelVO::getProductCode);
        Map<String, ErpProductDTO> productMap = productApi.getProductMapByCode(productCodes);
        StreamX.from(impVOList).assemble(productMap, WmsInboundItemImportExcelVO::getProductCode, (p, v) -> {
            if (v != null) {
                p.setProductId(v.getId());
            }
        });
    }

    @Override
    public PageResult<WmsInboundItemBinQueryDO> getInboundItemBinPage(WmsInboundItemPageReqVO pageReqVO,boolean withPickupDetail) {
        return inboundItemBinQueryMapper.selectPage(pageReqVO,withPickupDetail);
    }



    @Override
    public Map<Long,List<WmsInboundItemBinQueryDO>> selectInboundItemBinMap(Long warehouseId, Set<Long> productIds, boolean olderFirst) {
        return inboundItemBinQueryMapper.selectInboundItemBinMap(warehouseId,productIds,olderFirst);
    }

    @Override
    public void assembleStockWarehouse(List<? extends WmsInboundItemRespVO> list) {

        List<WmsWarehouseProductVO> wmsWarehouseProductVOList = new ArrayList<>();
        for (WmsInboundItemRespVO flowRespVO : list) {
            wmsWarehouseProductVOList.add(WmsWarehouseProductVO.builder().warehouseId(flowRespVO.getWarehouseId()).productId(flowRespVO.getProductId()).build());
        }
        List<WmsStockWarehouseDO> stockWarehouseDOList = stockWarehouseService.selectStockWarehouse(wmsWarehouseProductVOList);
        Map<String, WmsStockWarehouseSimpleVO> stockWarehouseDOMap = StreamX.from(stockWarehouseDOList).toMap(e -> e.getProductId() + "-" + e.getWarehouseId(), e -> BeanUtils.toBean(e, WmsStockWarehouseSimpleVO.class));
        for (WmsStockWarehouseSimpleVO simpleVO : stockWarehouseDOMap.values()) {
            simpleVO.setTotalQty(simpleVO.getAvailableQty() + simpleVO.getShelvingPendingQty());
        }
        StreamX.from(list).assemble(stockWarehouseDOMap, e -> e.getProductId() + "-" + e.getWarehouseId(), WmsInboundItemRespVO::setStockWarehouse);



    }

    @Override
    public void assembleStockType(List<WmsInboundItemRespVO> list) {
        List<WmsPickupItemDO> pickupItemDOList = pickupItemService.getPickupItemListByInboundItemIds(StreamX.from(list).toSet(WmsInboundItemRespVO::getId));
        Map<Long,List<WmsPickupItemDO>> pickupItemMap = StreamX.from(pickupItemDOList).groupBy(WmsPickupItemDO::getInboundItemId);
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(StreamX.from(pickupItemDOList).toSet(WmsPickupItemDO::getBinId));
        List<WmsWarehouseBinSimpleRespVO> binVOList = BeanUtils.toBean(binDOList, WmsWarehouseBinSimpleRespVO.class);

        List<WmsWarehouseZoneDO> zoneDOList = warehouseZoneService.selectByIds(StreamX.from(binDOList).toSet(WmsWarehouseBinDO::getZoneId));


        StreamX.from(binVOList).assemble(BeanUtils.toBean(zoneDOList, WmsWarehouseZoneSimpleRespVO.class),WmsWarehouseZoneSimpleRespVO::getId, WmsWarehouseBinSimpleRespVO::getZoneId, WmsWarehouseBinSimpleRespVO::setZone);

        for (WmsInboundItemRespVO itemRespVO : list) {

            List<WmsPickupItemDO> pickupItems = pickupItemMap.get(itemRespVO.getId());
            if(pickupItems!=null) {
                System.out.println();
                Set<Long> binIds = StreamX.from(pickupItems).map(WmsPickupItemDO::getBinId).toSet();
                List<WmsWarehouseBinSimpleRespVO> binVOS = StreamX.from(binVOList).filter(e -> binIds.contains(e.getId())).toList();
                itemRespVO.setWarehouseBinList(binVOS);
                if(itemRespVO.getStockType()==null) {
                    itemRespVO.setStockType(binVOS.get(0).getZone().getStockType());
                }
            }
        }



        Set<Integer> stockTypes = StreamX.from(zoneDOList).map(WmsWarehouseZoneDO::getStockType).toSet();

    }

    @Override
    public WmsInboundItemDO getByInboundIdAndProductId(Long inboundId, Long productId) {
        return inboundItemMapper.getByInboundIdAndProductId(inboundId, productId);
    }

    @Override
    public List<WmsInboundItemQueryDO> getInboundItemListForTms(WmsInboundItemListForTmsReqVO listForTmsReqVO) {
        return inboundItemQueryMapper.getInboundItemListForTms(listForTmsReqVO);
    }
}
