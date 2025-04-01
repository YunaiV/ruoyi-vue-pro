package cn.iocoder.yudao.module.wms.service.warehouse;

import java.util.*;
import jakarta.validation.*;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;

/**
 * 仓库 Service 接口
 *
 * @author 李方捷
 */
public interface WmsWarehouseService {

    /**
     * 创建仓库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsWarehouseDO createWarehouse(@Valid WmsWarehouseSaveReqVO createReqVO);

    /**
     * 更新仓库
     *
     * @param updateReqVO 更新信息
     */
    WmsWarehouseDO updateWarehouse(@Valid WmsWarehouseSaveReqVO updateReqVO);

    /**
     * 删除仓库
     *
     * @param id 编号
     */
    void deleteWarehouse(Long id);

    /**
     * 获得仓库
     *
     * @param id 编号
     * @return 仓库
     */
    WmsWarehouseDO getWarehouse(Long id);

    /**
     * 获得仓库分页
     *
     * @param pageReqVO 分页查询
     * @return 仓库分页
     */
    PageResult<WmsWarehouseDO> getWarehousePage(WmsWarehousePageReqVO pageReqVO);

    /**
     * 按 externalStorageId 查询 WmsWarehouseDO
     */
    List<WmsWarehouseDO> selectByExternalStorageId(Long externalStorageId, int limit);

    Map<Long, WmsWarehouseDO> getWarehouseMap(Set<Long> ids);
}
