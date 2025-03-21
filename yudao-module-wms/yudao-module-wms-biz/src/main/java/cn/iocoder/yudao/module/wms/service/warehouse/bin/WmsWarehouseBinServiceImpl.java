package cn.iocoder.yudao.module.wms.service.warehouse.bin;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.zone.WmsWarehouseZoneDO;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.zone.WmsWarehouseZoneService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.warehouse.bin.WmsWarehouseBinMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;
import org.springframework.context.annotation.Lazy;

/**
 * 库位 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsWarehouseBinServiceImpl implements WmsWarehouseBinService {

    @Resource
    @Lazy
    private WmsWarehouseZoneService warehouseZoneService;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsWarehouseBinMapper warehouseBinMapper;

    /**
     * @sign : 7C7A5946A6985FD8
     */
    @Override
    public WmsWarehouseBinDO createWarehouseBin(WmsWarehouseBinSaveReqVO createReqVO) {
        if (warehouseBinMapper.getByCode(createReqVO.getCode()) != null) {
            throw exception(WAREHOUSE_BIN_CODE_DUPLICATE);
        }
        // 按 wms_warehouse_bin.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        if (createReqVO.getWarehouseId() != null) {
            WmsWarehouseDO warehouse = warehouseService.getWarehouse(createReqVO.getWarehouseId());
            if (warehouse == null) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
        }
        // 按 wms_warehouse_bin.zone_id -> wms_warehouse_zone.id 的引用关系，校验存在性
        if (createReqVO.getZoneId() != null) {
            WmsWarehouseZoneDO warehouseZone = warehouseZoneService.getWarehouseZone(createReqVO.getZoneId());
            if (warehouseZone == null) {
                throw exception(WAREHOUSE_ZONE_NOT_EXISTS);
            }
        }
        // 插入
        WmsWarehouseBinDO warehouseBin = BeanUtils.toBean(createReqVO, WmsWarehouseBinDO.class);
        warehouseBinMapper.insert(warehouseBin);
        // 返回
        return warehouseBin;
    }

    /**
     * @sign : 7061C64B648E5252
     */
    @Override
    public WmsWarehouseBinDO updateWarehouseBin(WmsWarehouseBinSaveReqVO updateReqVO) {
        // 校验存在
        WmsWarehouseBinDO exists = validateWarehouseBinExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getCode(), exists.getCode())) {
            throw exception(WAREHOUSE_BIN_CODE_DUPLICATE);
        }
        // 按 wms_warehouse_bin.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        if (updateReqVO.getWarehouseId() != null) {
            WmsWarehouseDO warehouse = warehouseService.getWarehouse(updateReqVO.getWarehouseId());
            if (warehouse == null) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
        }
        // 按 wms_warehouse_bin.zone_id -> wms_warehouse_zone.id 的引用关系，校验存在性
        if (updateReqVO.getZoneId() != null) {
            WmsWarehouseZoneDO warehouseZone = warehouseZoneService.getWarehouseZone(updateReqVO.getZoneId());
            if (warehouseZone == null) {
                throw exception(WAREHOUSE_ZONE_NOT_EXISTS);
            }
        }
        // 更新
        WmsWarehouseBinDO warehouseBin = BeanUtils.toBean(updateReqVO, WmsWarehouseBinDO.class);
        warehouseBinMapper.updateById(warehouseBin);
        // 返回
        return warehouseBin;
    }

    /**
     * @sign : 7C20460B6C4953A7
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouseBin(Long id) {
        // 校验存在
        WmsWarehouseBinDO warehouseBin = validateWarehouseBinExists(id);
        // 唯一索引去重
        warehouseBin.setCode(warehouseBinMapper.flagUKeyAsLogicDelete(warehouseBin.getCode()));
        warehouseBinMapper.updateById(warehouseBin);
        // 删除
        warehouseBinMapper.deleteById(id);
    }

    /**
     * @sign : B03B72B6D2D098BC
     */
    private WmsWarehouseBinDO validateWarehouseBinExists(Long id) {
        WmsWarehouseBinDO warehouseBin = warehouseBinMapper.selectById(id);
        if (warehouseBin == null) {
            throw exception(WAREHOUSE_BIN_NOT_EXISTS);
        }
        return warehouseBin;
    }

    @Override
    public WmsWarehouseBinDO getWarehouseBin(Long id) {
        return warehouseBinMapper.selectById(id);
    }

    @Override
    public PageResult<WmsWarehouseBinDO> getWarehouseBinPage(WmsWarehouseBinPageReqVO pageReqVO) {
        return warehouseBinMapper.selectPage(pageReqVO);
    }

    /**
     * 按 warehouseId 查询 WmsWarehouseBinDO
     */
    public List<WmsWarehouseBinDO> selectByWarehouseId(Long warehouseId, int limit) {
        return warehouseBinMapper.selectByWarehouseId(warehouseId, limit);
    }

    /**
     * 按 zoneId 查询 WmsWarehouseBinDO
     */
    public List<WmsWarehouseBinDO> selectByZoneId(Long zoneId, int limit) {
        return warehouseBinMapper.selectByZoneId(zoneId, limit);
    }

    @Override
    public List<WmsWarehouseBinDO> selectByIds(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        return warehouseBinMapper.selectByIds(ids);
    }
}
