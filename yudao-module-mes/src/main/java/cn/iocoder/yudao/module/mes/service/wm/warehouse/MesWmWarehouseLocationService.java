package cn.iocoder.yudao.module.mes.service.wm.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.location.MesWmWarehouseLocationPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.location.MesWmWarehouseLocationSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseLocationDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 库区 Service 接口
 */
public interface MesWmWarehouseLocationService {

    /**
     * 创建库区
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWarehouseLocation(@Valid MesWmWarehouseLocationSaveReqVO createReqVO);

    /**
     * 更新库区
     *
     * @param updateReqVO 更新信息
     */
    void updateWarehouseLocation(@Valid MesWmWarehouseLocationSaveReqVO updateReqVO);

    /**
     * 删除库区
     *
     * @param id 编号
     */
    void deleteWarehouseLocation(Long id);

    /**
     * 校验库区存在
     *
     * @param id 编号
     * @return 库区
     */
    MesWmWarehouseLocationDO validateWarehouseLocationExists(Long id);

    /**
     * 获得库区
     *
     * @param id 编号
     * @return 库区
     */
    MesWmWarehouseLocationDO getWarehouseLocation(Long id);

    /**
     * 获得库区分页
     *
     * @param pageReqVO 分页参数
     * @return 库区分页
     */
    PageResult<MesWmWarehouseLocationDO> getWarehouseLocationPage(MesWmWarehouseLocationPageReqVO pageReqVO);

    /**
     * 按仓库获得库区列表
     *
     * @param warehouseId 仓库编号
     * @return 库区列表
     */
    List<MesWmWarehouseLocationDO> getWarehouseLocationList(Long warehouseId);

    /**
     * 按编号集合获得库区列表
     *
     * @param ids 编号集合
     * @return 库区列表
     */
    List<MesWmWarehouseLocationDO> getWarehouseLocationList(Collection<Long> ids);

    default Map<Long, MesWmWarehouseLocationDO> getWarehouseLocationMap(Collection<Long> ids) {
        return convertMap(getWarehouseLocationList(ids), MesWmWarehouseLocationDO::getId);
    }

    /**
     * 获得指定仓库下的库区数量
     *
     * @param warehouseId 仓库编号
     * @return 库区数量
     */
    Long getWarehouseLocationCountByWarehouseId(Long warehouseId);

    /**
     * 按编码获得库区（如果是虚拟线边库区编码且不存在，会自动插入）
     *
     * @param code 编码
     * @return 库区
     */
    MesWmWarehouseLocationDO getWarehouseLocationByCode(String code);

}
