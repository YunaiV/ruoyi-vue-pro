package cn.iocoder.yudao.module.wms.service.stockcheck;

import cn.iocoder.yudao.framework.cola.statemachine.StateMachine;
import cn.iocoder.yudao.framework.cola.statemachine.builder.TransitionContext;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import cn.iocoder.yudao.module.wms.config.StockCheckStateMachineConfigure;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistoryRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckProductExcelVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.vo.WmsStockCheckSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSimpleRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.bin.WmsStockCheckBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stockcheck.WmsStockCheckMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.stockcheck.bin.WmsStockCheckBinMapper;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.WmsConstants;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundAuditStatus;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import cn.iocoder.yudao.module.wms.service.approval.history.WmsApprovalHistoryService;
import cn.iocoder.yudao.module.wms.service.stock.bin.WmsStockBinService;
import cn.iocoder.yudao.module.wms.service.stock.warehouse.WmsStockWarehouseService;
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
 * 盘点 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockCheckServiceImpl implements WmsStockCheckService {

    @Resource
    @Lazy
    private WmsStockCheckBinMapper stockCheckBinMapper;


    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsStockCheckMapper stockCheckMapper;

    @Resource
    private WmsStockWarehouseService stockWarehouseService;

    @Resource
    private WmsStockBinService stockBinService;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsApprovalHistoryService approvalHistoryService;

    @Resource
    private ErpProductApi productApi;

    @Resource(name = StockCheckStateMachineConfigure.STATE_MACHINE_NAME)
    private StateMachine<Integer, WmsStockCheckAuditStatus.Event, TransitionContext<WmsStockCheckDO>> stockCheckStateMachine;

    /**
     * @sign : A9D51C9E0E654C80
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsStockCheckDO createStockCheck(WmsStockCheckSaveReqVO createReqVO) {
        // 设置单据号等初始值
        String no = noRedisDAO.generate(WmsNoRedisDAO.STOCKCHECK_NO_PREFIX, 6);
        createReqVO.setCode(no);
        createReqVO.setAuditStatus(WmsStockCheckAuditStatus.DRAFT.getValue());
        // 
        if (stockCheckMapper.getByNo(createReqVO.getCode()) != null) {
            throw exception(STOCKCHECK_NO_DUPLICATE);
        }
        // 插入
        WmsStockCheckDO stockCheck = BeanUtils.toBean(createReqVO, WmsStockCheckDO.class);
        stockCheckMapper.insert(stockCheck);
        // 保存库存盘点产品详情
        List<WmsStockCheckBinDO> toInsetList = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        if (createReqVO.getBinItemList() != null) {
            StreamX.from(createReqVO.getBinItemList()).filter(Objects::nonNull).forEach(item -> {
                keys.add(item.getProductId() + "-" + item.getBinId());
                WmsStockCheckBinDO stockCheckBinDO = BeanUtils.toBean(item, WmsStockCheckBinDO.class);
                stockCheckBinDO.setId(null);
                // 设置归属
                stockCheckBinDO.setStockCheckId(stockCheck.getId());
                stockCheckBinDO.setActualQty(0);
                toInsetList.add(stockCheckBinDO);
            });
            // 校验
            if (keys.size() != toInsetList.size()) {
                throw exception(STOCKCHECK_BIN_DUPLICATE);
            }
            stockCheckBinMapper.insertBatch(toInsetList);
        }
        // 返回
        return stockCheck;
    }


//    /**
//     * @sign : 2710B20EC7D9E031
//     */
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public WmsStockCheckDO updateStockCheck(WmsStockCheckSaveReqVO updateReqVO) {
//        // 校验存在
//        WmsStockCheckDO exists = validateStockCheckExists(updateReqVO.getId());
//        // 校验可否编辑
//        WmsStockCheckAuditStatus stockCheckAuditStatus = WmsStockCheckAuditStatus.parse(exists.getAuditStatus());
//        if (stockCheckAuditStatus.matchAny(WmsStockCheckAuditStatus.AUDITING, WmsStockCheckAuditStatus.PASS)) {
//            throw exception(STOCKCHECK_CAN_NOT_EDIT);
//        }
//        // 单据号不允许被修改
//        updateReqVO.setCode(exists.getCode());
//
//        // 全部删除
//        List<WmsStockCheckBinDO> stockCheckBinDOList = stockCheckBinMapper.selectByStockCheckId(updateReqVO.getId());
//        for (WmsStockCheckBinDO stockCheckBinDO : stockCheckBinDOList) {
//            stockCheckBinMapper.deleteAbsoluteById(stockCheckBinDO.getId());
//        }
//
//        // 保存库存盘点产品详情
//        List<WmsStockCheckBinDO> toInsetList = new ArrayList<>();
//        Set<String> keys = new HashSet<>();
//        if (updateReqVO.getBinItemList() != null) {
//            StreamX.from(updateReqVO.getBinItemList()).filter(Objects::nonNull).forEach(item -> {
//                keys.add(item.getProductId()+"-"+item.getBinId());
//                WmsStockCheckBinDO stockCheckBinDO = BeanUtils.toBean(item, WmsStockCheckBinDO.class);
//                stockCheckBinDO.setId(null);
//                // 设置归属
//                stockCheckBinDO.setStockCheckId(exists.getId());
//                stockCheckBinDO.setActualQty(0);
//                toInsetList.add(stockCheckBinDO);
//            });
//            // 校验
//            if (keys.size()!= toInsetList.size()) {
//                throw exception(STOCKCHECK_BIN_DUPLICATE);
//            }
//            stockCheckBinMapper.insertBatch(toInsetList);
//        }
//        // 更新
//        WmsStockCheckDO stockCheck = BeanUtils.toBean(updateReqVO, WmsStockCheckDO.class);
//        stockCheckMapper.updateById(stockCheck);
//        // 返回
//        return stockCheck;
//    }

    /**
     * @sign : 159065E285D50040
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockCheck(Long id) {
        // 校验存在
        WmsStockCheckDO stockCheck = validateStockCheckExists(id);
        // 校验可否编辑
        WmsStockCheckAuditStatus stockCheckAuditStatus = WmsStockCheckAuditStatus.parse(stockCheck.getAuditStatus());
        if (stockCheckAuditStatus.matchAny(WmsStockCheckAuditStatus.AUDITING, WmsStockCheckAuditStatus.PASS)) {
            throw exception(STOCKCHECK_CAN_NOT_DELETE);
        }
        // 唯一索引去重
        stockCheck.setCode(stockCheckMapper.flagUKeyAsLogicDelete(stockCheck.getCode()));
        stockCheckMapper.updateById(stockCheck);
        // 删除
        stockCheckMapper.deleteById(id);
    }

    /**
     * @sign : CCF673C00F6357F0
     */
    @Override
    public WmsStockCheckDO validateStockCheckExists(Long id) {
        WmsStockCheckDO stockCheck = stockCheckMapper.selectById(id);
        if (stockCheck == null) {
            throw exception(STOCKCHECK_NOT_EXISTS);
        }
        return stockCheck;
    }

    @Override
    public WmsStockCheckDO getStockCheck(Long id) {
        return stockCheckMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockCheckDO> getStockCheckPage(WmsStockCheckPageReqVO pageReqVO) {
        return stockCheckMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockCheckDO
     */
    @Override
    public List<WmsStockCheckDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockCheckMapper.selectByIds(idList);
    }

    @Override
    public void assembleWarehouse(List<WmsStockCheckRespVO> list) {
        Map<Long, WmsWarehouseDO> warehouseDOMap = warehouseService.getWarehouseMap(StreamX.from(list).toSet(WmsStockCheckRespVO::getWarehouseId));
        Map<Long, WmsWarehouseSimpleRespVO> warehouseVOMap = StreamX.from(warehouseDOMap.values()).toMap(WmsWarehouseDO::getId, v -> BeanUtils.toBean(v, WmsWarehouseSimpleRespVO.class));
        StreamX.from(list).assemble(warehouseVOMap, WmsStockCheckRespVO::getWarehouseId, WmsStockCheckRespVO::setWarehouse);
    }

    @Override
    public WmsStockCheckDO updateOutboundAuditStatus(Long id, Integer status) {
        WmsStockCheckDO stockCheckDO = validateStockCheckExists(id);
        stockCheckDO.setAuditStatus(status);
        stockCheckMapper.updateById(stockCheckDO);
        return stockCheckDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(WmsStockCheckAuditStatus.Event event, WmsApprovalReqVO approvalReqVO) {
        // 设置业务默认值
        approvalReqVO.setBillType(BillType.WMS_STOCKCHECK.getValue());
        approvalReqVO.setStatusType(WmsOutboundAuditStatus.getType());
        // 获得业务对象
        WmsStockCheckDO stockCheckDO = validateStockCheckExists(approvalReqVO.getBillId());
        TransitionContext<WmsStockCheckDO> ctx = TransitionContext.from(stockCheckDO);
        ctx.setExtra(WmsConstants.APPROVAL_REQ_VO_KEY, approvalReqVO);
        // 触发事件
        stockCheckStateMachine.fireEvent(event, ctx);
    }

    /**
     * 装配审批历史信息
     *
     * @param list 入库单集合
     */
    @Override
    public void assembleApprovalHistory(List<WmsStockCheckRespVO> list) {
        Map<Long, List<WmsApprovalHistoryRespVO>> groupedApprovalHistory = approvalHistoryService.selectGroupedApprovalHistory(BillType.WMS_STOCKCHECK, StreamX.from(list).toList(WmsStockCheckRespVO::getId));
        StreamX.from(list).assemble(groupedApprovalHistory, WmsStockCheckRespVO::getId, WmsStockCheckRespVO::setApprovalHistoryList);
    }

    @Override
    public List<WmsStockBinRespVO> parseProductExcel(WmsWarehouseDO wmsWarehouseDO, List<WmsStockCheckProductExcelVO> impVOList) {

        Map<String, ErpProductDTO> productMapByCode = productApi.getProductMapByCode(StreamX.from(impVOList).toSet(WmsStockCheckProductExcelVO::getProductCode));
        StreamX.from(impVOList).assemble(productMapByCode, WmsStockCheckProductExcelVO::getProductCode, (p, v) -> {
            if (v != null) {
                p.setProductId(v.getId());
            }
        });

        List<WmsWarehouseProductVO> wmsWarehouseProductVOS = new ArrayList<>();
        for (WmsStockCheckProductExcelVO excelVO : impVOList) {
            wmsWarehouseProductVOS.add(WmsWarehouseProductVO.builder().productId(excelVO.getProductId()).warehouseId(wmsWarehouseDO.getId()).build());
        }

        List<WmsStockBinRespVO> stockBinList = stockBinService.selectStockBinList(wmsWarehouseProductVOS, true);
        StreamX.from(stockBinList).assemble(productMapByCode.values(), ErpProductDTO::getId, WmsStockBinRespVO::getProductId, (e, v) -> {
            if (v != null) {
                e.setProduct(BeanUtils.toBean(v, WmsProductRespSimpleVO.class));
            }
        });
        return stockBinList;

    }
}
