package cn.iocoder.yudao.module.mes.service.wm.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.area.MesWmWarehouseAreaPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.area.MesWmWarehouseAreaSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseAreaDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 库位 Service 接口
 */
public interface MesWmWarehouseAreaService {

    /**
     * 创建库位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWarehouseArea(@Valid MesWmWarehouseAreaSaveReqVO createReqVO);

    /**
     * 更新库位
     *
     * @param updateReqVO 更新信息
     */
    void updateWarehouseArea(@Valid MesWmWarehouseAreaSaveReqVO updateReqVO);

    /**
     * 删除库位
     *
     * @param id 编号
     */
    void deleteWarehouseArea(Long id);

    /**
     * 校验库位存在
     *
     * @param id 编号
     * @return 库位
     */
    MesWmWarehouseAreaDO validateWarehouseAreaExists(Long id);

    /**
     * 获得库位
     *
     * @param id 编号
     * @return 库位
     */
    MesWmWarehouseAreaDO getWarehouseArea(Long id);

    /**
     * 获得库位分页
     *
     * @param pageReqVO 分页参数
     * @return 库位分页
     */
    PageResult<MesWmWarehouseAreaDO> getWarehouseAreaPage(MesWmWarehouseAreaPageReqVO pageReqVO);

    /**
     * 按库区获得库位列表
     *
     * @param locationId 库区编号
     * @return 库位列表
     */
    List<MesWmWarehouseAreaDO> getWarehouseAreaList(Long locationId);

    /**
     * 按编号集合获得库位列表
     *
     * @param ids 编号集合
     * @return 库位列表
     */
    List<MesWmWarehouseAreaDO> getWarehouseAreaList(Collection<Long> ids);

    default Map<Long, MesWmWarehouseAreaDO> getWarehouseAreaMap(Collection<Long> ids) {
        return convertMap(getWarehouseAreaList(ids), MesWmWarehouseAreaDO::getId);
    }

    /**
     * 获得指定库区下的库位数量
     *
     * @param locationId 库区编号
     * @return 库位数量
     */
    Long getWarehouseAreaCountByLocationId(Long locationId);

    /**
     * 校验仓库、库区、库位的父子关系
     *
     * @param warehouseId 仓库编号
     * @param locationId 库区编号
     * @param areaId 库位编号
     */
    void validateWarehouseAreaExists(Long warehouseId, Long locationId, Long areaId);

    /**
     * 批量设置指定库区下所有库位的混放规则
     *
     * @param locationId 库区编号
     * @param allowItemMixing 是否允许物料混放（null 表示不修改）
     * @param allowBatchMixing 是否允许批次混放（null 表示不修改）
     */
    void updateByLocationId(Long locationId, Boolean allowItemMixing, Boolean allowBatchMixing);

    /**
     * 按编码获得库位（如果是虚拟线边库位编码且不存在，会自动插入）
     *
     * @param code 编码
     * @return 库位
     */
    MesWmWarehouseAreaDO getWarehouseAreaByCode(String code);

}
