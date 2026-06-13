package cn.iocoder.yudao.module.wms.service.inventory;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryListReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.inventory.vo.WmsInventoryPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inventory.WmsInventoryDO;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryChangeReqDTO;
import cn.iocoder.yudao.module.wms.service.inventory.dto.WmsInventoryCheckReqDTO;

import java.util.List;

/**
 * WMS 库存 Service 接口
 *
 * @author 芋道源码
 */
public interface WmsInventoryService {

    /**
     * 获得库存统计分页
     *
     * @param pageReqVO 分页查询
     * @return 库存统计分页
     */
    PageResult<WmsInventoryDO> getInventoryPage(WmsInventoryPageReqVO pageReqVO);

    /**
     * 获得库存余额列表
     *
     * @param listReqVO 列表查询
     * @return 库存余额列表
     */
    List<WmsInventoryDO> getInventoryList(WmsInventoryListReqVO listReqVO);

    /**
     * 获得指定 SKU 的库存数量
     *
     * @param skuId SKU 编号
     * @return 库存数量
     */
    long getInventoryCountBySkuId(Long skuId);

    /**
     * 获得指定仓库的库存数量
     *
     * @param warehouseId 仓库编号
     * @return 库存数量
     */
    long getInventoryCountByWarehouseId(Long warehouseId);

    /**
     * 盘点库存
     *
     * @param reqDTO 盘点库存请求
     */
    void checkInventory(WmsInventoryCheckReqDTO reqDTO);

    /**
     * 变更库存
     *
     * @param reqDTO 库存变更请求
     */
    void changeInventory(WmsInventoryChangeReqDTO reqDTO);

}
