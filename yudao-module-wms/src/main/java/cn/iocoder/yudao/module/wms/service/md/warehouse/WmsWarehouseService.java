package cn.iocoder.yudao.module.wms.service.md.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.md.warehouse.vo.WmsWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.md.warehouse.vo.WmsWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.md.warehouse.WmsWarehouseDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * WMS 仓库 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsWarehouseService {

    /**
     * 创建仓库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWarehouse(@Valid WmsWarehouseSaveReqVO createReqVO);

    /**
     * 更新仓库
     *
     * @param updateReqVO 更新信息
     */
    void updateWarehouse(@Valid WmsWarehouseSaveReqVO updateReqVO);

    /**
     * 删除仓库
     *
     * @param id 编号
     */
    void deleteWarehouse(Long id);

    /**
     * 校验仓库存在
     *
     * @param id 编号
     * @return 仓库
     */
    WmsWarehouseDO validateWarehouseExists(Long id);

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
     * 获得仓库列表
     *
     * @return 仓库列表
     */
    List<WmsWarehouseDO> getWarehouseList();

    /**
     * 按编号集合获得仓库列表
     *
     * @param ids 编号集合
     * @return 仓库列表
     */
    List<WmsWarehouseDO> getWarehouseList(Collection<Long> ids);

    default Map<Long, WmsWarehouseDO> getWarehouseMap(Collection<Long> ids) {
        return convertMap(getWarehouseList(ids), WmsWarehouseDO::getId);
    }

}
