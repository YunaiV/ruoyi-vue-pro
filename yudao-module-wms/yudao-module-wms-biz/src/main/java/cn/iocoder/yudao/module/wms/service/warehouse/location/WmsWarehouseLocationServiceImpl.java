package cn.iocoder.yudao.module.wms.service.warehouse.location;

import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.area.WmsWarehouseAreaDO;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.area.WmsWarehouseAreaService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.location.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.location.WmsWarehouseLocationDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.warehouse.location.WmsWarehouseLocationMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 库位 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsWarehouseLocationServiceImpl implements WmsWarehouseLocationService {

    @Resource
    @Lazy
    private WmsWarehouseAreaService warehouseAreaService;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsWarehouseLocationMapper warehouseLocationMapper;

    /**
     * @sign : 1F1E8314BF9384E7
     */
    @Override
    public WmsWarehouseLocationDO createWarehouseLocation(WmsWarehouseLocationSaveReqVO createReqVO) {
        if (warehouseLocationMapper.getByCode(createReqVO.getCode(), true) != null) {
            throw exception(WAREHOUSE_LOCATION_CODE_DUPLICATE);
        }
        // 按 wms_warehouse_location.area_id -> wms_warehouse_area.id 的引用关系，校验存在性
        WmsWarehouseAreaDO warehouseArea = warehouseAreaService.getWarehouseArea(createReqVO.getAreaId());
        if (warehouseArea == null) {
            throw exception(WAREHOUSE_AREA_NOT_EXISTS);
        }
        // 按 wms_warehouse_location.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        WmsWarehouseDO warehouse = warehouseService.getWarehouse(createReqVO.getWarehouseId());
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        // 插入
        WmsWarehouseLocationDO warehouseLocation = BeanUtils.toBean(createReqVO, WmsWarehouseLocationDO.class);
        warehouseLocationMapper.insert(warehouseLocation);
        // 返回
        return warehouseLocation;
    }

    /**
     * @sign : B6C73DA2201D7423
     */
    @Override
    public WmsWarehouseLocationDO updateWarehouseLocation(WmsWarehouseLocationSaveReqVO updateReqVO) {
        // 校验存在
        WmsWarehouseLocationDO exists = validateWarehouseLocationExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getCode(), exists.getCode())) {
            throw exception(WAREHOUSE_LOCATION_CODE_DUPLICATE);
        }
        // 按 wms_warehouse_location.area_id -> wms_warehouse_area.id 的引用关系，校验存在性
        WmsWarehouseAreaDO warehouseArea = warehouseAreaService.getWarehouseArea(updateReqVO.getAreaId());
        if (warehouseArea == null) {
            throw exception(WAREHOUSE_AREA_NOT_EXISTS);
        }
        // 按 wms_warehouse_location.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        WmsWarehouseDO warehouse = warehouseService.getWarehouse(updateReqVO.getWarehouseId());
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        // 更新
        WmsWarehouseLocationDO warehouseLocation = BeanUtils.toBean(updateReqVO, WmsWarehouseLocationDO.class);
        warehouseLocationMapper.updateById(warehouseLocation);
        // 返回
        return warehouseLocation;
    }

    /**
     * @sign : F21210E01DBAE953
     */
    @Override
    public void deleteWarehouseLocation(Long id) {
        // 校验存在
        validateWarehouseLocationExists(id);
        // 删除
        warehouseLocationMapper.deleteById(id);
    }

    /**
     * @sign : 8F00B204E9800998
     */
    private WmsWarehouseLocationDO validateWarehouseLocationExists(Long id) {
        WmsWarehouseLocationDO warehouseLocation = warehouseLocationMapper.selectById(id);
        if (warehouseLocation == null) {
            throw exception(WAREHOUSE_LOCATION_NOT_EXISTS);
        }
        return warehouseLocation;
    }

    @Override
    public WmsWarehouseLocationDO getWarehouseLocation(Long id) {
        return warehouseLocationMapper.selectById(id);
    }

    @Override
    public PageResult<WmsWarehouseLocationDO> getWarehouseLocationPage(WmsWarehouseLocationPageReqVO pageReqVO) {
        return warehouseLocationMapper.selectPage(pageReqVO);
    }

    /**
     * 按 warehouseId 查询 WmsWarehouseLocationDO
     */
    public List<WmsWarehouseLocationDO> selectByWarehouseId(Long warehouseId, int limit) {
        return warehouseLocationMapper.selectByWarehouseId(warehouseId, limit);
    }

    /**
     * 按 areaId 查询 WmsWarehouseLocationDO
     */
    public List<WmsWarehouseLocationDO> selectByAreaId(Long areaId, int limit) {
        return warehouseLocationMapper.selectByAreaId(areaId, limit);
    }
}