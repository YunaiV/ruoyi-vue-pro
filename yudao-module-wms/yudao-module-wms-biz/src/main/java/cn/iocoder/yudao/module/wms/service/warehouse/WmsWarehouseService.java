package cn.iocoder.yudao.module.wms.service.warehouse;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.api.warehouse.dto.vo.WmsWarehouseListReqDTO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehousePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.vo.WmsWarehouseSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.WmsWarehouseDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 验证仓库是否存在
     *
     * @param id 编号
     * @return 仓库
     */
    WmsWarehouseDO validateWarehouseExists(Long id);
    /**
     * 获得仓库分页
     *
     * @param pageReqVO 分页查询
     * @return 仓库分页
     */
    PageResult<WmsWarehouseDO> getWarehousePage(WmsWarehousePageReqVO pageReqVO);

    /**
     * 按 外部存储ID 查询 WmsWarehouseDO
     *
     * @param externalStorageId 外部存储ID
     * @param limit 限制数量
     * @return 仓库列表
     */
    List<WmsWarehouseDO> selectByExternalStorageId(Long externalStorageId, int limit);

    /**
     * 根据ID集合获取仓库Map
     *
     * @param ids 仓库ID集合
     * @return 仓库ID与仓库对象的映射
     */
    Map<Long, WmsWarehouseDO> getWarehouseMap(Set<Long> ids);

    /**
     * 获取简单的仓库列表
     *
     * @param pageReqVO 查询条件
     * @return 仓库列表
     */
    List<WmsWarehouseDO> getSimpleList(@Valid WmsWarehousePageReqVO pageReqVO);

    /**
     * 根据ID列表查询仓库列表
     *
     * @param list 仓库ID列表
     * @return 仓库列表
     */
    List<WmsWarehouseDO> selectByIds(List<Long> list);

    /**
     * 根据仓库编码集合获取仓库Map
     *
     * @param codes 仓库编码集合
     * @return 仓库编码与仓库对象的映射
     */
    Map<String, WmsWarehouseDO> getWarehouseMapByCode(Set<String> codes);

    /**
     * 根据条件查询仓库列表
     *
     * @param reqDTO 查询条件
     * @return 仓库列表
     */
    List<WmsWarehouseDO> selectList(WmsWarehouseListReqDTO reqDTO);

    /**
     * 获得转换单仓库列表
     *
     * @param exchange 转换单类型
     * @return 仓库列表
     */
    List<WmsWarehouseDO> getSimpleListForExchange(Integer exchange);
}
