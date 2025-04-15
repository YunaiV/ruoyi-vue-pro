package cn.iocoder.yudao.module.wms.service.inventory.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.ErpProductApi;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.bin.vo.WmsInventoryBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespSimpleVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinRespVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.bin.WmsInventoryBinDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.module.wms.dal.mysql.inventory.bin.WmsInventoryBinMapper;
import cn.iocoder.yudao.module.wms.enums.inventory.WmsInventoryAuditStatus;
import cn.iocoder.yudao.module.wms.service.inventory.WmsInventoryService;
import cn.iocoder.yudao.module.wms.service.warehouse.bin.WmsWarehouseBinService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_BIN_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_BIN_INVENTORY_ID_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_BIN_NOT_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_BIN_QUANTITY_ERROR;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.INVENTORY_CAN_NOT_EDIT;

/**
 * 库位盘点 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsInventoryBinServiceImpl implements WmsInventoryBinService {

    @Resource
    private WmsInventoryBinMapper inventoryBinMapper;

    @Resource
    private ErpProductApi productApi;

    @Autowired
    @Lazy
    private WmsWarehouseBinService warehouseBinService;

    @Autowired
    @Lazy
    private WmsInventoryService inventoryService;

    /**
     * @sign : DE344227D83E204E
     */
    @Override
    public WmsInventoryBinDO createInventoryBin(WmsInventoryBinSaveReqVO createReqVO) {
        if (inventoryBinMapper.getByUkProductId(createReqVO.getInventoryId(), createReqVO.getBinId(), createReqVO.getProductId()) != null) {
            throw exception(INVENTORY_BIN_EXISTS);
        }
        // 插入
        WmsInventoryBinDO inventoryBin = BeanUtils.toBean(createReqVO, WmsInventoryBinDO.class);
        inventoryBinMapper.insert(inventoryBin);
        // 返回
        return inventoryBin;
    }

    /**
     * @sign : 62E245F5DCE7AB97
     */
    @Override
    public WmsInventoryBinDO updateInventoryBin(WmsInventoryBinSaveReqVO updateReqVO) {
        // 校验存在
        WmsInventoryBinDO exists = validateInventoryBinExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getInventoryId(), exists.getInventoryId()) && Objects.equals(updateReqVO.getBinId(), exists.getBinId()) && Objects.equals(updateReqVO.getProductId(), exists.getProductId())) {
            throw exception(INVENTORY_BIN_EXISTS);
        }
        // 更新
        WmsInventoryBinDO inventoryBin = BeanUtils.toBean(updateReqVO, WmsInventoryBinDO.class);
        inventoryBinMapper.updateById(inventoryBin);
        // 返回
        return inventoryBin;
    }

    /**
     * @sign : DD7A82091BC08B56
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteInventoryBin(Long id) {
        // 校验存在
        WmsInventoryBinDO inventoryBin = validateInventoryBinExists(id);
        // 唯一索引去重
        inventoryBin.setInventoryId(inventoryBinMapper.flagUKeyAsLogicDelete(inventoryBin.getInventoryId()));
        inventoryBin.setBinId(inventoryBinMapper.flagUKeyAsLogicDelete(inventoryBin.getBinId()));
        inventoryBin.setProductId(inventoryBinMapper.flagUKeyAsLogicDelete(inventoryBin.getProductId()));
        inventoryBinMapper.updateById(inventoryBin);
        // 删除
        inventoryBinMapper.deleteById(id);
    }

    /**
     * @sign : D836A97E979452F4
     */
    private WmsInventoryBinDO validateInventoryBinExists(Long id) {
        WmsInventoryBinDO inventoryBin = inventoryBinMapper.selectById(id);
        if (inventoryBin == null) {
            throw exception(INVENTORY_BIN_NOT_EXISTS);
        }
        return inventoryBin;
    }

    @Override
    public WmsInventoryBinDO getInventoryBin(Long id) {
        return inventoryBinMapper.selectById(id);
    }

    @Override
    public PageResult<WmsInventoryBinDO> getInventoryBinPage(WmsInventoryBinPageReqVO pageReqVO) {
        return inventoryBinMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsInventoryBinDO
     */
    public List<WmsInventoryBinDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return inventoryBinMapper.selectByIds(idList);
    }

    @Override
    public List<WmsInventoryBinDO> selectByInventoryId(Long id) {
        return inventoryBinMapper.selectByInventoryId(id);
    }

    @Override
    public void assembleProduct(List<WmsInventoryBinRespVO> binItemList) {
        Map<Long, ErpProductDTO> productDTOMap = productApi.getProductMap(StreamX.from(binItemList).map(WmsInventoryBinRespVO::getProductId).toList());
        Map<Long, WmsProductRespSimpleVO> productVOMap = new HashMap<>();
        for (ErpProductDTO productDTO : productDTOMap.values()) {
            WmsProductRespSimpleVO productVO = BeanUtils.toBean(productDTO, WmsProductRespSimpleVO.class);
            productVOMap.put(productDTO.getId(), productVO);
        }
        StreamX.from(binItemList).assemble(productVOMap, WmsInventoryBinRespVO::getProductId, WmsInventoryBinRespVO::setProduct);
    }

    @Override
    public void assembleBin(List<WmsInventoryBinRespVO> binItemList) {
        List<WmsWarehouseBinDO> binDOList = warehouseBinService.selectByIds(StreamX.from(binItemList).toList(WmsInventoryBinRespVO::getBinId).stream().distinct().toList());
        List<WmsWarehouseBinRespVO> binVOList = BeanUtils.toBean(binDOList, WmsWarehouseBinRespVO.class);
        StreamX.from(binItemList).assemble(binVOList, WmsWarehouseBinRespVO::getId, WmsInventoryBinRespVO::getBinId,WmsInventoryBinRespVO::setBin);
    }

    @Override
    public void updateActualQuantity(List<WmsInventoryBinSaveReqVO> updateReqVOList) {

        if (CollectionUtils.isEmpty(updateReqVOList)) {
            return;
        }
        Set<Long> inventoryIds = StreamX.from(updateReqVOList).toSet(WmsInventoryBinSaveReqVO::getInventoryId);
        if (inventoryIds.size() > 1) {
            throw exception(INVENTORY_BIN_INVENTORY_ID_DUPLICATE);
        }
        Long inventoryId = inventoryIds.stream().findFirst().get();
        WmsInventoryDO inventoryDO = inventoryService.validateInventoryExists(inventoryId);
        WmsInventoryAuditStatus auditStatus = WmsInventoryAuditStatus.parse(inventoryDO.getAuditStatus());
        // 除了审批中的情况，其它情况不允许修改实际盘点量
        if (!auditStatus.matchAny(WmsInventoryAuditStatus.AUDITING)) {
            throw exception(INVENTORY_CAN_NOT_EDIT);
        }

        Map<Long, WmsInventoryBinSaveReqVO> updateReqVOMap = StreamX.from(updateReqVOList).toMap(WmsInventoryBinSaveReqVO::getId);
        List<WmsInventoryBinDO> inventoryBinDOSInDB = inventoryBinMapper.selectByIds(StreamX.from(updateReqVOList).toList(WmsInventoryBinSaveReqVO::getId));
        for (WmsInventoryBinDO itemDO : inventoryBinDOSInDB) {
            WmsInventoryBinSaveReqVO updateReqVO = updateReqVOMap.get(itemDO.getId());
            if (updateReqVO.getActualQty() == null || updateReqVO.getActualQty() < 0) {
                throw exception(INVENTORY_BIN_QUANTITY_ERROR);
            }
            itemDO.setActualQty(updateReqVO.getActualQty());
        }
        // 保存
        inventoryBinMapper.updateBatch(inventoryBinDOSInDB);

    }
}
