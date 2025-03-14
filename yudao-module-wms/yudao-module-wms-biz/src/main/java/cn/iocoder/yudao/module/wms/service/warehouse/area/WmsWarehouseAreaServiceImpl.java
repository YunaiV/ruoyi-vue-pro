package cn.iocoder.yudao.module.wms.service.warehouse.area;

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
    private WmsWarehouseAreaMapper warehouseAreaMapper;

    /**
     * @sign : E91E47640C63EB3A
     */
    @Override
    public WmsWarehouseAreaDO createWarehouseArea(WmsWarehouseAreaSaveReqVO createReqVO) {
        if (warehouseAreaMapper.getByCode(createReqVO.getCode(), true) != null) {
            throw exception(WAREHOUSE_AREA_CODE_DUPLICATE);
        }
        // 插入
        WmsWarehouseAreaDO warehouseArea = BeanUtils.toBean(createReqVO, WmsWarehouseAreaDO.class);
        warehouseAreaMapper.insert(warehouseArea);
        // 返回
        return warehouseArea;
    }

    /**
     * @sign : 3CB50D670809557D
     */
    @Override
    public WmsWarehouseAreaDO updateWarehouseArea(WmsWarehouseAreaSaveReqVO updateReqVO) {
        // 校验存在
        WmsWarehouseAreaDO exists = validateWarehouseAreaExists(updateReqVO.getId());
        if (!Objects.equals(updateReqVO.getId(), exists.getId()) && Objects.equals(updateReqVO.getCode(), exists.getCode())) {
            throw exception(WAREHOUSE_AREA_CODE_DUPLICATE);
        }
        // 更新
        WmsWarehouseAreaDO warehouseArea = BeanUtils.toBean(updateReqVO, WmsWarehouseAreaDO.class);
        warehouseAreaMapper.updateById(warehouseArea);
        // 返回
        return warehouseArea;
    }

    /**
     * @sign : 84E3D284015CA3CE
     */
    @Override
    public void deleteWarehouseArea(Long id) {
        // 校验存在
        validateWarehouseAreaExists(id);
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
}