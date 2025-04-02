package cn.iocoder.yudao.module.wms.service.warehouse.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.warehouse.bin.vo.WmsWarehouseBinSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.warehouse.bin.WmsWarehouseBinDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 库位 Service 接口
 *
 * @author 李方捷
 */
public interface WmsWarehouseBinService {

    /**
     * 创建库位
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsWarehouseBinDO createWarehouseBin(@Valid WmsWarehouseBinSaveReqVO createReqVO);

    /**
     * 更新库位
     *
     * @param updateReqVO 更新信息
     */
    WmsWarehouseBinDO updateWarehouseBin(@Valid WmsWarehouseBinSaveReqVO updateReqVO);

    /**
     * 删除库位
     *
     * @param id 编号
     */
    void deleteWarehouseBin(Long id);

    /**
     * 获得库位
     *
     * @param id 编号
     * @return 库位
     */
    WmsWarehouseBinDO getWarehouseBin(Long id);

    /**
     * 获得库位分页
     *
     * @param pageReqVO 分页查询
     * @return 库位分页
     */
    PageResult<WmsWarehouseBinDO> getWarehouseBinPage(WmsWarehouseBinPageReqVO pageReqVO);

    /**
     * 按 warehouseId 查询 WmsWarehouseBinDO
     */
    List<WmsWarehouseBinDO> selectByWarehouseId(Long warehouseId, int limit);

    /**
     * 按 zoneId 查询 WmsWarehouseBinDO
     */
    List<WmsWarehouseBinDO> selectByZoneId(Long zoneId, int limit);

    List<WmsWarehouseBinDO> selectByIds(List<Long> ids);


}
