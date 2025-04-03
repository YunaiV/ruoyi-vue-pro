package cn.iocoder.yudao.module.wms.service.warehouse.zone;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZonePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.zone.vo.WmsWarehouseZoneSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.zone.WmsWarehouseZoneDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 库区 Service 接口
 *
 * @author 李方捷
 */
public interface WmsWarehouseZoneService {

    /**
     * 创建库区
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsWarehouseZoneDO createWarehouseZone(@Valid WmsWarehouseZoneSaveReqVO createReqVO);

    /**
     * 更新库区
     *
     * @param updateReqVO 更新信息
     */
    WmsWarehouseZoneDO updateWarehouseZone(@Valid WmsWarehouseZoneSaveReqVO updateReqVO);

    /**
     * 删除库区
     *
     * @param id 编号
     */
    void deleteWarehouseZone(Long id);

    /**
     * 获得库区
     *
     * @param id 编号
     * @return 库区
     */
    WmsWarehouseZoneDO getWarehouseZone(Long id);

    /**
     * 获得库区分页
     *
     * @param pageReqVO 分页查询
     * @return 库区分页
     */
    PageResult<WmsWarehouseZoneDO> getWarehouseZonePage(WmsWarehouseZonePageReqVO pageReqVO);

    /**
     * 按 warehouseId 查询 WmsWarehouseZoneDO
     */
    List<WmsWarehouseZoneDO> selectByWarehouseId(Long warehouseId, int limit);

    List<WmsWarehouseZoneDO> getSimpleList(@Valid WmsWarehouseZonePageReqVO pageReqVO);

    List<WmsWarehouseZoneDO> selectByIds(List<Long> list);
}
