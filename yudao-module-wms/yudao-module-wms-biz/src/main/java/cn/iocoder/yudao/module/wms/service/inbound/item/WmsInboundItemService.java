package cn.iocoder.yudao.module.wms.service.inbound.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.wms.controller.admin.inbound.item.vo.*;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemBinQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.WmsInboundItemQueryDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.inbound.item.flow.WmsInboundItemFlowDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 入库单详情 Service 接口
 *
 * @author 李方捷
 */
public interface WmsInboundItemService {

    /**
     * 创建入库单详情
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    WmsInboundItemDO createInboundItem(@Valid WmsInboundItemSaveReqVO createReqVO);

    /**
     * 更新入库单详情
     *
     * @param updateReqVO 更新信息
     */
    WmsInboundItemDO updateInboundItem(@Valid WmsInboundItemSaveReqVO updateReqVO);

    /**
     * 删除入库单详情
     *
     * @param id 编号
     */
    void deleteInboundItem(Long id);

    /**
     * 获得入库单详情
     *
     * @param id 编号
     * @return 入库单详情
     */
    WmsInboundItemDO getInboundItem(Long id);

    /**
     * 获得入库单详情分页
     *
     * @param pageReqVO 分页查询
     * @return 入库单详情分页
     */
    PageResult<WmsInboundItemQueryDO> getInboundItemPage(WmsInboundItemPageReqVO pageReqVO);

    /**
     * 获得入库单详情列表
     *
     * @param companyId 公司ID
     * @param productIds 商品ID
     * @return 入库单详情分页
     */
    List<WmsInboundItemQueryDO> getInboundItemList(Long companyId, List<Long> productIds);
    /**
     * 按 inboundId 查询 WmsInboundItemDO
     */
    List<WmsInboundItemDO> selectByInboundId(Long inboundId, int limit);

    /**
     * 按 inboundId 查询 WmsInboundItemDO
     */
    default List<WmsInboundItemDO> selectByInboundId(Long inboundId) {
        return selectByInboundId(inboundId, Integer.MAX_VALUE);
    }

    /**
     * 更新实际入库量
     *
     * @param updateReqVOList 更新信息
     */
    void updateActualQuantity(List<WmsInboundItemSaveReqVO> updateReqVOList);

    /**
     * 按 id 查询 WmsInboundItemDO
     */
    List<WmsInboundItemDO> selectByIds(List<Long> ids);

    /**
     * 更新 WmsInboundItemDO
     */
    void updateById(WmsInboundItemDO inboundItemDO);

    /**
     * 获取待上架清单
     */
    PageResult<WmsInboundItemQueryDO> getPickupPending(WmsPickupPendingPageReqVO pageReqVO);

    /**
     * 装配产品
     */
    void assembleProducts(List<? extends WmsInboundItemRespVO> itemList);

    /**
     * 装配入库单
     */
    void assembleInbound(List<? extends WmsInboundItemRespVO> itemList);

    /**
     * 按仓库id和商品id查询
     */
    List<WmsInboundItemDO> selectItemListHasAvailableQty(Long warehouseId, Long productId);

    /**
     * 保存入库单详情
     */
    void saveItems(List<WmsInboundItemDO> itemsToUpdate, List<WmsInboundItemFlowDO> inboundItemFlowList);

    /**
     * 装配仓库
     */
    void assembleWarehouse(List<? extends WmsInboundItemRespVO> list);

    /**
     * 装配仓库货位
     */
    void assembleWarehouseBin(List<WmsInboundItemBinRespVO> list);

    /**
     * 装配部门
     */
    void assembleDept(List<? extends WmsInboundItemRespVO> list);

    /**
     * 装配公司
     */
    void assembleCompany(List<? extends WmsInboundItemRespVO> list);

    /**
     * 装配商品id
     */
    void assembleProductIds(List<WmsInboundItemImportExcelVO> impVOList);

    /**
     * 批次库存关联仓位查询
     */
    PageResult<WmsInboundItemBinQueryDO> getInboundItemBinPage(@Valid WmsInboundItemPageReqVO pageReqVO,boolean withPickupDetail);

    /**
     * 仓库内产品的批次库存查询，先进先出排序
     */
    Map<Long,List<WmsInboundItemBinQueryDO>> selectInboundItemBinMap(Long warehouseId, Set<Long> productIds, boolean olderFirst);

    void assembleStockWarehouse(List<? extends WmsInboundItemRespVO> list);

    void assembleStockType(List<WmsInboundItemRespVO> list);

    WmsInboundItemDO getByInboundIdAndProductId(Long inboundId, Long productId);

    /**
     * 获取入库单详情列表 tms查询用
     *
     * @param listForTmsReqVO 入库单详情列表查询条件
     * @return 入库单详情列表
     */
    List<WmsInboundItemQueryDO> getInboundItemListForTms(WmsInboundItemListForTmsReqVO listForTmsReqVO);
}
