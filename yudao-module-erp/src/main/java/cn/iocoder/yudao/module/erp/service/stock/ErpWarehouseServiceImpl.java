package cn.iocoder.yudao.module.erp.service.stock;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehousePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import cn.iocoder.yudao.module.erp.dal.mysql.stock.ErpWarehouseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.*;

/**
 * ERP 仓库 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class ErpWarehouseServiceImpl implements ErpWarehouseService {

    @Resource
    private ErpWarehouseMapper warehouseMapper;

    @Override
    public Long createWarehouse(ErpWarehouseSaveReqVO createReqVO) {
        // 插入
        ErpWarehouseDO warehouse = BeanUtils.toBean(createReqVO, ErpWarehouseDO.class);
        warehouseMapper.insert(warehouse);
        // 返回
        return warehouse.getId();
    }

    @Override
    public void updateWarehouse(ErpWarehouseSaveReqVO updateReqVO) {
        // 校验存在
        validateWarehouseExists(updateReqVO.getId());
        // 更新
        ErpWarehouseDO updateObj = BeanUtils.toBean(updateReqVO, ErpWarehouseDO.class);
        warehouseMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouseDefaultStatus(Long id, Boolean defaultStatus) {
        // 1. 校验存在
        validateWarehouseExists(id);

        // 2.1 如果开启，则需要关闭所有其它的默认
        if (defaultStatus) {
            ErpWarehouseDO warehouse = warehouseMapper.selectByDefaultStatus();
            if (warehouse != null) {
                warehouseMapper.updateById(new ErpWarehouseDO().setId(warehouse.getId()).setDefaultStatus(false));
            }
        }
        // 2.2 更新对应的默认状态
        warehouseMapper.updateById(new ErpWarehouseDO().setId(id).setDefaultStatus(defaultStatus));
    }

    @Override
    public void deleteWarehouse(Long id) {
        // 校验存在
        validateWarehouseExists(id);
        // 删除
        warehouseMapper.deleteById(id);
    }

    private void validateWarehouseExists(Long id) {
        if (warehouseMapper.selectById(id) == null) {
            throw exception(WAREHOUSE_NOT_EXISTS);
        }
    }

    @Override
    public ErpWarehouseDO getWarehouse(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public List<ErpWarehouseDO> validWarehouseList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpWarehouseDO> list = warehouseMapper.selectBatchIds(ids);
        Map<Long, ErpWarehouseDO> warehouseMap = convertMap(list, ErpWarehouseDO::getId);
        for (Long id : ids) {
            ErpWarehouseDO warehouse = warehouseMap.get(id);
            if (warehouseMap.get(id) == null) {
                throw exception(WAREHOUSE_NOT_EXISTS);
            }
            if (CommonStatusEnum.isDisable(warehouse.getStatus())) {
                throw exception(WAREHOUSE_NOT_ENABLE, warehouse.getName());
            }
        }
        return list;
    }

    @Override
    public List<ErpWarehouseDO> getWarehouseListByStatus(Integer status) {
        return warehouseMapper.selectListByStatus(status);
    }

    @Override
    public List<ErpWarehouseDO> getWarehouseList(Collection<Long> ids) {
        return warehouseMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<ErpWarehouseDO> getWarehousePage(ErpWarehousePageReqVO pageReqVO) {
        return warehouseMapper.selectPage(pageReqVO);
    }

}