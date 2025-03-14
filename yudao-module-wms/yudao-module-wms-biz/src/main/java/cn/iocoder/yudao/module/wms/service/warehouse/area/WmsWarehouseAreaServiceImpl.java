package cn.iocoder.yudao.module.wms.service.warehouse.area;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.location.WmsWarehouseLocationDO;
import cn.iocoder.yudao.module.wms.service.warehouse.WmsWarehouseService;
import cn.iocoder.yudao.module.wms.service.warehouse.location.WmsWarehouseLocationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.area.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.area.WmsWarehouseAreaDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.warehouse.area.WmsWarehouseAreaMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 库区 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsWarehouseAreaServiceImpl implements WmsWarehouseAreaService {

    @Resource
    @Lazy
    private WmsWarehouseLocationService warehouseLocationService;

    @Resource
    @Lazy
    private WmsWarehouseService warehouseService;

    @Resource
    private WmsWarehouseAreaMapper warehouseAreaMapper;

    /**
     * @sign : E30D8E71696EA16B
     */
    @Override
    public WmsWarehouseAreaDO createWarehouseArea(WmsWarehouseAreaSaveReqVO createReqVO) {
        if (warehouseAreaMapper.getByCode(createReqVO.getCode(), true) != null) {
            throw exception(WAREHOUSE_AREA_CODE_DUPLICATE);
        }
        // 按 wms_warehouse_area.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        WmsWarehouseDO warehouse = warehouseService.getWarehouse(createReqVO.getWarehouseId());
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        // 插入
        WmsWarehouseAreaDO warehouseArea = BeanUtils.toBean(createReqVO, WmsWarehouseAreaDO.class);
        warehouseAreaMapper.insert(warehouseArea);
        // 返回
        return warehouseArea;
    }

    /**
     * @sign : 3FCEC40077F85132
     */
    @Override
    public WmsWarehouseAreaDO updateWarehouseArea(WmsWarehouseAreaSaveReqVO updateReqVO) {
        // 校验存在
        WmsWarehouseAreaDO exists = validateWarehouseAreaExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getCode(), exists.getCode())) {
            throw exception(WAREHOUSE_AREA_CODE_DUPLICATE);
        }
        // 按 wms_warehouse_area.warehouse_id -> wms_warehouse.id 的引用关系，校验存在性
        WmsWarehouseDO warehouse = warehouseService.getWarehouse(updateReqVO.getWarehouseId());
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        // 更新
        WmsWarehouseAreaDO warehouseArea = BeanUtils.toBean(updateReqVO, WmsWarehouseAreaDO.class);
        warehouseAreaMapper.updateById(warehouseArea);
        // 返回
        return warehouseArea;
    }

    /**
     * @sign : 61ECDB09556E7AA2
     */
    @Override
    public void deleteWarehouseArea(Long id) {
        // 校验存在
        validateWarehouseAreaExists(id);
        // 校验是否被库位表引用
        List<WmsWarehouseLocationDO> warehouseLocationList = warehouseLocationService.selectByAreaId(id, 1);
        if (!CollectionUtils.isEmpty(warehouseLocationList)) {
            throw exception(WAREHOUSE_AREA_BE_REFERRED);
        }
        // 删除
        warehouseAreaMapper.deleteById(id);
    }

    /**
     * @sign : 8F00B204E9800998
     */
    private WmsWarehouseAreaDO validateWarehouseAreaExists(Long id) {
        WmsWarehouseAreaDO warehouseArea = warehouseAreaMapper.selectById(id);
        if (warehouseArea == null) {
            throw exception(WAREHOUSE_AREA_NOT_EXISTS);
        }
        return warehouseArea;
    }

    @Override
    public WmsWarehouseAreaDO getWarehouseArea(Long id) {
        return warehouseAreaMapper.selectById(id);
    }

    @Override
    public PageResult<WmsWarehouseAreaDO> getWarehouseAreaPage(WmsWarehouseAreaPageReqVO pageReqVO) {
        return warehouseAreaMapper.selectPage(pageReqVO);
    }

    /**
     * 按 warehouseId 查询 WmsWarehouseAreaDO
     */
    public List<WmsWarehouseAreaDO> selectByWarehouseId(Long warehouseId, int limit) {
        return warehouseAreaMapper.selectByWarehouseId(warehouseId, limit);
    }
}
