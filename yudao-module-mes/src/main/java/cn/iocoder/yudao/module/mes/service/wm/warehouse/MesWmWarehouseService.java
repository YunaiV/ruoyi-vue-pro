package cn.iocoder.yudao.module.mes.service.wm.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.MesWmWarehousePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.warehouse.vo.MesWmWarehouseSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.warehouse.MesWmWarehouseDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 仓库 Service 接口
 */
public interface MesWmWarehouseService {

    /**
     * 创建仓库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWarehouse(@Valid MesWmWarehouseSaveReqVO createReqVO);

    /**
     * 更新仓库
     *
     * @param updateReqVO 更新信息
     */
    void updateWarehouse(@Valid MesWmWarehouseSaveReqVO updateReqVO);

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
    MesWmWarehouseDO validateWarehouseExists(Long id);

    /**
     * 获得仓库
     *
     * @param id 编号
     * @return 仓库
     */
    MesWmWarehouseDO getWarehouse(Long id);

    /**
     * 获得仓库分页
     *
     * @param pageReqVO 分页参数
     * @return 仓库分页
     */
    PageResult<MesWmWarehouseDO> getWarehousePage(MesWmWarehousePageReqVO pageReqVO);

    /**
     * 获得仓库列表
     *
     * @return 仓库列表
     */
    List<MesWmWarehouseDO> getWarehouseList();

    /**
     * 按编号集合获得仓库列表
     *
     * @param ids 编号集合
     * @return 仓库列表
     */
    List<MesWmWarehouseDO> getWarehouseList(Collection<Long> ids);

    default Map<Long, MesWmWarehouseDO> getWarehouseMap(Collection<Long> ids) {
        return convertMap(getWarehouseList(ids), MesWmWarehouseDO::getId);
    }

    /**
     * 按编码获得仓库（如果是虚拟线边库编码且不存在，会自动插入）
     *
     * @param code 编码
     * @return 仓库
     */
    MesWmWarehouseDO getWarehouseByCode(String code);

}
