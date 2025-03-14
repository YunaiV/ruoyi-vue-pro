package cn.iocoder.yudao.module.wms.service.warehouse.location;

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
    private WmsWarehouseLocationMapper warehouseLocationMapper;

    /**
     * @sign : 60C0FAB32DC32C3F
     */
    @Override
    public WmsWarehouseLocationDO createWarehouseLocation(WmsWarehouseLocationSaveReqVO createReqVO) {
        if (warehouseLocationMapper.getByCode(createReqVO.getCode(), true) != null) {
            throw exception(WAREHOUSE_LOCATION_CODE_DUPLICATE);
        }
        // 插入
        WmsWarehouseLocationDO warehouseLocation = BeanUtils.toBean(createReqVO, WmsWarehouseLocationDO.class);
        warehouseLocationMapper.insert(warehouseLocation);
        // 返回
        return warehouseLocation;
    }

    /**
     * @sign : B458D3D556DAAEDC
     */
    @Override
    public WmsWarehouseLocationDO updateWarehouseLocation(WmsWarehouseLocationSaveReqVO updateReqVO) {
        // 校验存在
        WmsWarehouseLocationDO exists = validateWarehouseLocationExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getCode(), exists.getCode())) {
            throw exception(WAREHOUSE_LOCATION_CODE_DUPLICATE);
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
}