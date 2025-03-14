package cn.iocoder.yudao.module.wms.service.warehouse.area;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.area.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.area.WmsWarehouseAreaDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 库区 Service 接口
 *
 * @author 李方捷
 */
public interface WmsWarehouseAreaService {

    /**
     * 创建库区
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsWarehouseAreaDO createWarehouseArea(@Valid WmsWarehouseAreaSaveReqVO createReqVO);

    /**
     * 更新库区
     *
     * @param updateReqVO 更新信息
     */
    WmsWarehouseAreaDO updateWarehouseArea(@Valid WmsWarehouseAreaSaveReqVO updateReqVO);

    /**
     * 删除库区
     *
     * @param id 编号
     */
    void deleteWarehouseArea(Long id);

    /**
     * 获得库区
     *
     * @param id 编号
     * @return 库区
     */
    WmsWarehouseAreaDO getWarehouseArea(Long id);

    /**
     * 获得库区分页
     *
     * @param pageReqVO 分页查询
     * @return 库区分页
     */
    PageResult<WmsWarehouseAreaDO> getWarehouseAreaPage(WmsWarehouseAreaPageReqVO pageReqVO);
}
