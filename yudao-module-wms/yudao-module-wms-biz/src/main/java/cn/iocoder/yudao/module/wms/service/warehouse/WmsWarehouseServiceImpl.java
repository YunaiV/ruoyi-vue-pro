package cn.iocoder.yudao.module.wms.service.warehouse;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.wms.dal.dataobject.external.storage.WmsExternalStorageDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.area.WmsWarehouseAreaDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.location.WmsWarehouseLocationDO;
import cn.iocoder.yudao.module.wms.service.external.storage.WmsExternalStorageService;
import cn.iocoder.yudao.module.wms.service.warehouse.area.WmsWarehouseAreaService;
import cn.iocoder.yudao.module.wms.service.warehouse.location.WmsWarehouseLocationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.dal.mysql.warehouse.WmsWarehouseMapper;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.*;

/**
 * 仓库 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsWarehouseServiceImpl implements WmsWarehouseService {

    @Resource
    @Lazy
    private WmsWarehouseLocationService warehouseLocationService;

    @Resource
    @Lazy
    private WmsWarehouseAreaService warehouseAreaService;

    @Resource
    @Lazy
    private WmsExternalStorageService externalStorageService;

    @Resource
    private WmsWarehouseMapper warehouseMapper;

    /**
     * @sign : 6A1033E3C48B0B9A
     */
    @Override
    public WmsWarehouseDO createWarehouse(WmsWarehouseSaveReqVO createReqVO) {
        if (warehouseMapper.getByName(createReqVO.getName()) != null) {
            throw exception(WAREHOUSE_NAME_DUPLICATE);
        }
        if (warehouseMapper.getByCode(createReqVO.getCode()) != null) {
            throw exception(WAREHOUSE_CODE_DUPLICATE);
        }
        // 按 wms_warehouse.external_storage_id -> wms_external_storage.id 的引用关系，校验存在性
        if (createReqVO.getExternalStorageId() != null) {
            WmsExternalStorageDO externalStorage = externalStorageService.getExternalStorage(createReqVO.getExternalStorageId());
            if (externalStorage == null) {
                throw exception(EXTERNAL_STORAGE_NOT_EXISTS);
            }
        }
        // 插入
        WmsWarehouseDO warehouse = BeanUtils.toBean(createReqVO, WmsWarehouseDO.class);
        warehouseMapper.insert(warehouse);
        // 返回
        return warehouse;
    }

    /**
     * @sign : 38B6A9D0484E56C4
     */
    @Override
    public WmsWarehouseDO updateWarehouse(WmsWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        WmsWarehouseDO exists = validateWarehouseExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getName(), exists.getName())) {
            throw exception(WAREHOUSE_NAME_DUPLICATE);
        }
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getCode(), exists.getCode())) {
            throw exception(WAREHOUSE_CODE_DUPLICATE);
        }
        // 按 wms_warehouse.external_storage_id -> wms_external_storage.id 的引用关系，校验存在性
        if (updateReqVO.getExternalStorageId() != null) {
            WmsExternalStorageDO externalStorage = externalStorageService.getExternalStorage(updateReqVO.getExternalStorageId());
            if (externalStorage == null) {
                throw exception(EXTERNAL_STORAGE_NOT_EXISTS);
            }
        }
        // 更新
        WmsWarehouseDO warehouse = BeanUtils.toBean(updateReqVO, WmsWarehouseDO.class);
        warehouseMapper.updateById(warehouse);
        // 返回
        return warehouse;
    }

    /**
     * @sign : 004DC894F8394A48
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouse(Long id) {
        // 校验存在
        WmsWarehouseDO warehouse = validateWarehouseExists(id);
        // 校验是否被库区表引用
        List<WmsWarehouseAreaDO> warehouseAreaList = warehouseAreaService.selectByWarehouseId(id, 1);
        if (!CollectionUtils.isEmpty(warehouseAreaList)) {
            throw exception(WAREHOUSE_BE_REFERRED);
        }
        // 校验是否被库位表引用
        List<WmsWarehouseLocationDO> warehouseLocationList = warehouseLocationService.selectByWarehouseId(id, 1);
        if (!CollectionUtils.isEmpty(warehouseLocationList)) {
            throw exception(WAREHOUSE_BE_REFERRED);
        }
        // 唯一索引去重
        warehouse.setName(warehouseMapper.flagUKeyAsLogicDelete(warehouse.getName()));
        warehouse.setCode(warehouseMapper.flagUKeyAsLogicDelete(warehouse.getCode()));
        warehouseMapper.updateById(warehouse);
        // 删除
        warehouseMapper.deleteById(id);
    }

    /**
     * @sign : 8F00B204E9800998
     */
    private WmsWarehouseDO validateWarehouseExists(Long id) {
        WmsWarehouseDO warehouse = warehouseMapper.selectById(id);
        if (warehouse == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
        return warehouse;
    }

    @Override
    public WmsWarehouseDO getWarehouse(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public PageResult<WmsWarehouseDO> getWarehousePage(WmsWarehousePageReqVO pageReqVO) {
        return warehouseMapper.selectPage(pageReqVO);
    }

    /**
     * 按 externalStorageId 查询 WmsWarehouseDO
     */
    public List<WmsWarehouseDO> selectByExternalStorageId(Long externalStorageId, int limit) {
        return warehouseMapper.selectByExternalStorageId(externalStorageId, limit);
    }
}
