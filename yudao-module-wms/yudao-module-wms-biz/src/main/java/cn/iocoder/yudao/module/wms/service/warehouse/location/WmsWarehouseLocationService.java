package cn.iocoder.yudao.module.wms.service.warehouse.location;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.location.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.location.WmsWarehouseLocationDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 库位 Service 接口
 *
 * @author 李方捷
 */
public interface WmsWarehouseLocationService {

    /**
     * 创建库位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsWarehouseLocationDO createWarehouseLocation(@Valid WmsWarehouseLocationSaveReqVO createReqVO);

    /**
     * 更新库位
     *
     * @param updateReqVO 更新信息
     */
    WmsWarehouseLocationDO updateWarehouseLocation(@Valid WmsWarehouseLocationSaveReqVO updateReqVO);

    /**
     * 删除库位
     *
     * @param id 编号
     */
    void deleteWarehouseLocation(Long id);

    /**
     * 获得库位
     *
     * @param id 编号
     * @return 库位
     */
    WmsWarehouseLocationDO getWarehouseLocation(Long id);

    /**
     * 获得库位分页
     *
     * @param pageReqVO 分页查询
     * @return 库位分页
     */
    PageResult<WmsWarehouseLocationDO> getWarehouseLocationPage(WmsWarehouseLocationPageReqVO pageReqVO);
}
