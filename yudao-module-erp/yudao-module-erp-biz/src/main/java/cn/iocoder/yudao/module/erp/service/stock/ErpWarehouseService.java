package cn.iocoder.yudao.module.erp.service.stock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehouseSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.stock.vo.warehouse.ErpWarehousePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.stock.ErpWarehouseDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * ERP 仓库 Service 接口
 *
 * @author 芋道源码
 */
public interface ErpWarehouseService {

    /**
     * 创建仓库
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWarehouse(@Valid ErpWarehouseSaveReqVO createReqVO);

    /**
     * 更新ERP 仓库
     *
     * @param updateReqVO 更新信息
     */
    void updateWarehouse(@Valid ErpWarehouseSaveReqVO updateReqVO);

    /**
     * 更新仓库默认状态
     *
     * @param id     编号
     * @param defaultStatus 默认状态
     */
    void updateWarehouseDefaultStatus(Long id, Boolean defaultStatus);

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
    ErpWarehouseDO getWarehouse(Long id);

    /**
     * 校验仓库列表的有效性
     *
     * @param ids 编号数组
     * @return 仓库列表
     */
    List<ErpWarehouseDO> validWarehouseList(Collection<Long> ids);

    /**
     * 获得指定状态的仓库列表
     *
     * @param status 状态
     * @return 仓库列表
     */
    List<ErpWarehouseDO> getWarehouseListByStatus(Integer status);

    /**
     * 获得仓库列表
     *
     * @param ids 编号数组
     * @return 仓库列表
     */
    List<ErpWarehouseDO> getWarehouseList(Collection<Long> ids);

    /**
     * 获得仓库 Map
     *
     * @param ids 编号数组
     * @return 仓库 Map
     */
    default Map<Long, ErpWarehouseDO> getWarehouseMap(Collection<Long> ids) {
        return convertMap(getWarehouseList(ids), ErpWarehouseDO::getId);
    }

    /**
     * 获得仓库分页
     *
     * @param pageReqVO 分页查询
     * @return 仓库分页
     */
    PageResult<ErpWarehouseDO> getWarehousePage(ErpWarehousePageReqVO pageReqVO);

}