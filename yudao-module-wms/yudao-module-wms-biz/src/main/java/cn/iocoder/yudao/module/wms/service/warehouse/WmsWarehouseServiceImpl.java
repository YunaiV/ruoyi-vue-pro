package cn.iocoder.yudao.module.wms.service.warehouse;

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
    private WmsWarehouseMapper warehouseMapper;

    /**
     * @sign : 2B1DE640C85EEDD2
     */
    @Override
    public WmsWarehouseDO createWarehouse(WmsWarehouseSaveReqVO createReqVO) {
        if (warehouseMapper.getByName(createReqVO.getName(), true) != null) {
            throw exception(WAREHOUSE_NAME_DUPLICATE);
        }
        if (warehouseMapper.getByCode(createReqVO.getCode(), true) != null) {
            throw exception(WAREHOUSE_CODE_DUPLICATE);
        }
        // 插入
        WmsWarehouseDO warehouse = BeanUtils.toBean(createReqVO, WmsWarehouseDO.class);
        warehouseMapper.insert(warehouse);
        // 返回
        return warehouse;
    }

    /**
     * @sign : 5824B44D6C7D7CEB
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
        // 插入
        WmsWarehouseDO warehouse = BeanUtils.toBean(updateReqVO, WmsWarehouseDO.class);
        warehouseMapper.updateById(warehouse);
        // 返回
        return warehouse;
    }

    @Override
    public void deleteWarehouse(Long id) {
        // 校验存在
        validateWarehouseExists(id);
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
}
