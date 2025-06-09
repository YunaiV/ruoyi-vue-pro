package cn.iocoder.yudao.module.tms.service.transfer.item;

import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item.TmsTransferItemDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 调拨单明细 Service 接口
 *
 * @author wdy
 */
@Validated
public interface TmsTransferItemService {

    /**
     * 创建调拨单明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTransferItem(@Valid TmsTransferItemSaveReqVO createReqVO);

    /**
     * 更新调拨单明细
     *
     * @param updateReqVO 更新信息
     */
    void updateTransferItem(@Valid TmsTransferItemSaveReqVO updateReqVO);

    /**
     * 删除调拨单明细
     *
     * @param id 编号
     */
    void deleteTransferItem(Long id);

    /**
     * 获得调拨单明细
     *
     * @param id 编号
     * @return 调拨单明细
     */
    TmsTransferItemDO getTransferItem(Long id);

    /**
     * 根据调拨单ID获取明细列表
     *
     * @param transferId 调拨单编号
     * @return 明细列表
     */
    List<TmsTransferItemDO> getTransferItemListByTransferId(Long transferId);

    /**
     * 根据ID列表获取明细列表
     *
     * @param ids ID列表
     * @return 明细列表
     */
    List<TmsTransferItemDO> getTransferItemListByIds(List<Long> ids);

    /**
     * 校验调拨单明细项是否存在
     *
     * @param ids 调拨单明细项编号列表
     * @return 调拨单明细项列表
     */
    List<TmsTransferItemDO> validateTransferItemExists(List<Long> ids);

    /**
     * 批量创建调拨单明细
     *
     * @param list 创建信息列表
     */
    void createTransferItemList(List<TmsTransferItemDO> list);

    /**
     * 批量更新调拨单明细
     *
     * @param list 更新信息列表
     */
    void updateTransferItemList(List<TmsTransferItemDO> list);

    /**
     * 批量删除调拨单明细
     *
     * @param ids 编号列表
     */
    void deleteTransferItemList(List<Long> ids);

    /**
     * 根据调拨单ID删除明细
     *
     * @param transferId 调拨单编号
     */
    void deleteTransferItemByTransferId(Long transferId);

    /**
     * 更新调拨单明细的出库信息
     *
     * @param id          调拨单明细编号
     * @param outboundQty 出库数量
     */
    void updateTransferItemOutbound(@NotNull(message = "调拨单明细ID不能为空") Long id, Integer outboundQty);

    void updateTransferItemInbound(@NotNull(message = "调拨单明细ID不能为空") Long itemId, int i);
}