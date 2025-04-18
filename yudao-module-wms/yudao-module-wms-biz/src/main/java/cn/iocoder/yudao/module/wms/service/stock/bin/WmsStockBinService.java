package cn.iocoder.yudao.module.wms.service.stock.bin;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.product.WmsProductRespBinVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinPageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinRespVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.bin.vo.WmsStockBinSaveReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.stock.warehouse.vo.WmsWarehouseProductVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.stock.bin.WmsStockBinDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 仓位库存 Service 接口
 *
 * @author 李方捷
 */
public interface WmsStockBinService {

    /**
     * 创建仓位库存
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsStockBinDO createStockBin(@Valid WmsStockBinSaveReqVO createReqVO);

    /**
     * 更新仓位库存
     *
     * @param updateReqVO 更新信息
     */
    WmsStockBinDO updateStockBin(@Valid WmsStockBinSaveReqVO updateReqVO);

    /**
     * 删除仓位库存
     *
     * @param id 编号
     */
    void deleteStockBin(Long id);

    /**
     * 获得仓位库存
     *
     * @param id 编号
     * @return 仓位库存
     */
    WmsStockBinDO getStockBin(Long id);

    /**
     * 获得仓位库存分页
     *
     * @param pageReqVO 分页查询
     * @return 仓位库存分页
     */
    PageResult<WmsStockBinDO> getStockBinPage(WmsStockBinPageReqVO pageReqVO);

    List<WmsStockBinDO> selectStockBin(Long warehouseId,Long binId, Long productId);

    default List<WmsStockBinDO> selectStockBin(Long warehouseId, Long productId){
        return selectStockBin(warehouseId, null, productId);
    }

    WmsStockBinDO getStockBin(Long binId, Long productId, boolean createNew);

    Map<Long, Map<Long, WmsStockBinDO>> getStockBinMap(Collection<Long> binIds, Collection<Long> productIds);

    void insertOrUpdate(WmsStockBinDO stockBinDO);

    List<WmsStockBinRespVO> selectStockBinList(List<WmsWarehouseProductVO> warehouseProductList, Boolean withBin);

    Map<String, List<WmsStockBinRespVO>> selectStockBinGroup(List<WmsWarehouseProductVO> warehouseProductList, Boolean withBin);

    void assembleProducts(List<WmsStockBinRespVO> list);

    void assembleWarehouse(List<WmsStockBinRespVO> list);

    void assembleBin(List<WmsStockBinRespVO> list,boolean withZone);

    List<WmsStockBinDO> selectStockBinByIds(List<Long> stockBinIds);

    /**
     * 按库存批次详情ID 查询仓位库存清单
     **/
    List<WmsStockBinDO> selectBinsByInboundItemId(Long warehouseId, Long productId, Long inboundItemId);


    PageResult<WmsProductRespBinVO> getGroupedStockBinPage(@Valid WmsStockBinPageReqVO pageReqVO);

    void assembleDept(List<WmsProductRespBinVO> list);

    void assembleStockWarehouseList(Long warehouseId,List<WmsProductRespBinVO> list);

}
