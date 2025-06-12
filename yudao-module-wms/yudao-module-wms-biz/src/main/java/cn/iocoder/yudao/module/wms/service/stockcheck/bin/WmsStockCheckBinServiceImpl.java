package cn.iocoder.yudao.module.wms.service.stockcheck.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stockcheck.bin.vo.WmsStockCheckBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.WmsStockCheckDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stockcheck.bin.WmsStockCheckBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.stockcheck.bin.WmsStockCheckBinMapper;
import cn.iocoder.yudao.module.wms.enums.stock.check.WmsStockCheckAuditStatus;
import cn.iocoder.yudao.module.wms.service.stockcheck.WmsStockCheckService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.WmsErrorCodeConstants.*;

/**
 * 库位盘点 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsStockCheckBinServiceImpl implements WmsStockCheckBinService {

    @Resource
    private WmsStockCheckBinMapper stockCheckBinMapper;

    @Resource
    private ErpProductApi productApi;

    @Autowired
    @Lazy
    private WmsWarehouseBinService warehouseBinService;

    @Autowired
    @Lazy
    private WmsStockCheckService stockCheckService;

    /**
     * @sign : DE344227D83E204E
     */
    @Override
    public WmsStockCheckBinDO createStockCheckBin(WmsStockCheckBinSaveReqVO createReqVO) {
        if (stockCheckBinMapper.getByUkProductId(createReqVO.getStockCheckId(), createReqVO.getBinId(), createReqVO.getProductId()) != null) {
            throw exception(STOCKCHECK_BIN_EXISTS);
        }
        // 插入
        WmsStockCheckBinDO stockCheckBin = BeanUtils.toBean(createReqVO, WmsStockCheckBinDO.class);
        stockCheckBinMapper.insert(stockCheckBin);
        // 返回
        return stockCheckBin;
    }

    @Override
    public Boolean appendStockCheckBin(List<WmsStockCheckBinSaveReqVO> createReqVOList) {
        List<WmsStockCheckBinDO> doList = BeanUtils.toBean(createReqVOList, WmsStockCheckBinDO.class);
        // 确认是否传入了有效的盘点单ID
        Set<Long> stockCheckIds = StreamX.from(createReqVOList).toSet(WmsStockCheckBinSaveReqVO::getStockCheckId);
        if (stockCheckIds.size() != 1) {
            throw exception(STOCKCHECK_BIN_MUST_IN_SAME_STOCKCHECK);
        }
        Long stockCheckId = stockCheckIds.iterator().next();
        WmsStockCheckDO stockCheckDO = stockCheckService.validateStockCheckExists(stockCheckId);
        WmsStockCheckAuditStatus stockCheckAuditStatus = WmsStockCheckAuditStatus.parse(stockCheckDO.getAuditStatus());
        // 不允许追加
        if (stockCheckAuditStatus != WmsStockCheckAuditStatus.AUDITING) {
            throw exception(STOCKCHECK_BIN_CAN_NOT_APPEND, stockCheckAuditStatus.getLabel());
        }
        // 校验仓位有效性
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(StreamX.from(createReqVOList).toSet(WmsStockCheckBinSaveReqVO::getBinId));
        Map<Long, WmsWarehouseBinDO> binDOMap = StreamX.from(binDOList).toMap(WmsWarehouseBinDO::getId);
        Set<Long> warehouseIds = StreamX.from(binDOList).toSet(WmsWarehouseBinDO::getWarehouseId);
        if (warehouseIds.size() != 1) {
            throw exception(STOCKCHECK_BIN_WAREHOUSE_BIN_ERROR);
        }
        Long warehouseId = warehouseIds.iterator().next();
        if (!Objects.equals(warehouseId, stockCheckDO.getWarehouseId())) {
            throw exception(STOCKCHECK_BIN_WAREHOUSE_BIN_ERROR);
        }
        // 准备数据检查存在性
        List<WmsStockCheckBinDO> dosInDB = stockCheckBinMapper.selectByStockCheckId(stockCheckId);
        Map<String, WmsStockCheckBinDO> dosInDBMap = StreamX.from(dosInDB).toMap(e -> e.getBinId() + "-" + e.getProductId());
        // 原始产品范围
        Set<Long> productIds = StreamX.from(dosInDB).toSet(WmsStockCheckBinDO::getProductId);
        for (WmsStockCheckBinDO saveDO : doList) {
            if (!productIds.contains(saveDO.getProductId())) {
                throw exception(STOCKCHECK_BIN_PRODUCT_NOT_ALLOWED);
            }
            // 校验仓位ID有效性
            WmsWarehouseBinDO binDO = binDOMap.get(saveDO.getBinId());
            if (binDO == null) {
                throw exception(STOCKCHECK_BIN_WAREHOUSE_BIN_ERROR);
            }
            WmsStockCheckBinDO dbDO = dosInDBMap.get(saveDO.getBinId() + "-" + saveDO.getProductId());
            if (dbDO != null) {
                throw exception(STOCKCHECK_BIN_EXISTS);
            }
        }
        // 插入
        stockCheckBinMapper.insertBatch(doList);
        return true;
    }

    @Override
    public void updateBatch(List<WmsStockCheckBinDO> wmsStockCheckBinDOList) {
        stockCheckBinMapper.updateBatch(wmsStockCheckBinDOList);
    }

    /**
     * @sign : 62E245F5DCE7AB97
     */
    @Override
    public WmsStockCheckBinDO updateStockCheckBin(WmsStockCheckBinSaveReqVO updateReqVO) {
        // 校验存在
        WmsStockCheckBinDO exists = validateStockCheckBinExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getStockCheckId(), exists.getStockCheckId()) && Objects.equals(updateReqVO.getBinId(), exists.getBinId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(STOCKCHECK_BIN_EXISTS);
        }
        // 更新
        WmsStockCheckBinDO stockCheckBin = BeanUtils.toBean(updateReqVO, WmsStockCheckBinDO.class);
        stockCheckBinMapper.updateById(stockCheckBin);
        // 返回
        return stockCheckBin;
    }

    /**
     * @sign : DD7A82091BC08B56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStockCheckBin(Long id) {
        // 校验存在
        WmsStockCheckBinDO stockCheckBin = validateStockCheckBinExists(id);
        // 唯一索引去重
        stockCheckBin.setStockCheckId(stockCheckBinMapper.flagUKeyAsLogicDelete(stockCheckBin.getStockCheckId()));
        stockCheckBin.setBinId(stockCheckBinMapper.flagUKeyAsLogicDelete(stockCheckBin.getBinId()));
        stockCheckBin.setProductId(stockCheckBinMapper.flagUKeyAsLogicDelete(stockCheckBin.getProductId()));
        stockCheckBinMapper.updateById(stockCheckBin);
        // 删除
        stockCheckBinMapper.deleteById(id);
    }

    /**
     * @sign : D836A97E979452F4
     */
    private WmsStockCheckBinDO validateStockCheckBinExists(Long id) {
        WmsStockCheckBinDO stockCheckBin = stockCheckBinMapper.selectById(id);
        if (stockCheckBin == null) {
            throw exception(STOCKCHECK_BIN_NOT_EXISTS);
        }
        return stockCheckBin;
    }

    @Override
    public WmsStockCheckBinDO getStockCheckBin(Long id) {
        return stockCheckBinMapper.selectById(id);
    }

    @Override
    public PageResult<WmsStockCheckBinDO> getStockCheckBinPage(WmsStockCheckBinPageReqVO pageReqVO) {
        return stockCheckBinMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsStockCheckBinDO
     */
    @Override
    public List<WmsStockCheckBinDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return stockCheckBinMapper.selectByIds(idList);
    }

    @Override
    public List<WmsStockCheckBinDO> selectByStockCheckId(Long id) {
        return stockCheckBinMapper.selectByStockCheckId(id);
    }

    @Override
    public void assembleProduct(List<WmsStockCheckBinRespVO> binItemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(binItemList).map(WmsStockCheckBinRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(binItemList).assemble(productVOMap, WmsStockCheckBinRespVO::getProductId, WmsStockCheckBinRespVO::setProduct);
    }

    @Override
    public void assembleBin(List<WmsStockCheckBinRespVO> binItemList) {
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(StreamX.from(binItemList).toSet(WmsStockCheckBinRespVO::getBinId));
        List<WmsWarehouseBinRespVO> binVOList = BeanUtils.toBean(binDOList, WmsWarehouseBinRespVO.class);
        StreamX.from(binItemList).assemble(binVOList, WmsWarehouseBinRespVO::getId, WmsStockCheckBinRespVO::getBinId, WmsStockCheckBinRespVO::setBin);
    }

    @Override
    public void updateActualQuantity(List<WmsStockCheckBinSaveReqVO> updateReqVOList) {
        if (CollectionUtils.isEmpty(updateReqVOList)) {
            return;
        }
        Set<Long> stockCheckIds = StreamX.from(updateReqVOList).toSet(WmsStockCheckBinSaveReqVO::getStockCheckId);
        if (stockCheckIds.size() > 1) {
            throw exception(STOCKCHECK_BIN_STOCKCHECK_ID_DUPLICATE);
        }
        Long stockCheckId = stockCheckIds.stream().findFirst().get();
        WmsStockCheckDO stockCheckDO = stockCheckService.validateStockCheckExists(stockCheckId);
        WmsStockCheckAuditStatus auditStatus = WmsStockCheckAuditStatus.parse(stockCheckDO.getAuditStatus());
        // 除了审批中的情况，其它情况不允许修改实际盘点量
        assert auditStatus != null;
        if (!auditStatus.matchAny(WmsStockCheckAuditStatus.AUDITING)) {
            throw exception(STOCKCHECK_CAN_NOT_EDIT);
        }
        Map<Long, WmsStockCheckBinSaveReqVO> updateReqVOMap = StreamX.from(updateReqVOList).toMap(WmsStockCheckBinSaveReqVO::getId);
        List<WmsStockCheckBinDO> stockCheckBinDOSInDB = stockCheckBinMapper.selectByIds(StreamX.from(updateReqVOList).toList(WmsStockCheckBinSaveReqVO::getId));
        for (WmsStockCheckBinDO itemDO : stockCheckBinDOSInDB) {
            WmsStockCheckBinSaveReqVO updateReqVO = updateReqVOMap.get(itemDO.getId());
            if (updateReqVO.getActualQty() == null || updateReqVO.getActualQty() < 0) {
                throw exception(STOCKCHECK_BIN_QUANTITY_ERROR);
            }
            itemDO.setActualQty(updateReqVO.getActualQty());
        }
        // 保存
        stockCheckBinMapper.updateBatch(stockCheckBinDOSInDB);
    }

    /**
     * 保存导入的盘点结果
     */
    @Override
    public void saveStockCheckBinList(WmsStockCheckDO stockCheck, List<WmsStockCheckBinDO> impDOList) {
        List<WmsStockCheckBinDO> dosInDB = stockCheckBinMapper.selectByStockCheckId(stockCheck.getId());
        Map<Long, WmsStockCheckBinDO> dosInDBMap = StreamX.from(dosInDB).toMap(WmsStockCheckBinDO::getId);
        List<WmsStockCheckBinDO> insertList = new ArrayList<>();
        List<WmsStockCheckBinDO> updateList = new ArrayList<>();
        // 原始产品范围
        Set<Long> productIds = StreamX.from(dosInDB).toSet(WmsStockCheckBinDO::getProductId);
        // 循环导入的清单
        for (WmsStockCheckBinDO impDO : impDOList) {
            WmsStockCheckBinDO doInDB = dosInDBMap.get(impDO.getId());
            if (doInDB != null) {
                // 如果在数据库中存在，则更新
                doInDB.setActualQty(impDO.getActualQty());
                doInDB.setRemark(impDO.getRemark());
                updateList.add(doInDB);
            } else {
                // 超出原始产品范围时，提示错误
                if (!productIds.contains(impDO.getProductId())) {
                    throw exception(STOCKCHECK_BIN_PRODUCT_NOT_ALLOWED);
                }
                // 如果不存在就插入
                impDO.setId(null);
                impDO.setStockCheckId(stockCheck.getId());
                insertList.add(impDO);
            }
        }
        if (!insertList.isEmpty()) {
            stockCheckBinMapper.insertBatch(insertList);
        }
        if (!updateList.isEmpty()) {
            stockCheckBinMapper.updateBatch(updateList);
        }
    }
}
