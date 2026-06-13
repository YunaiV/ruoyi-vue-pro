package cn.iocoder.yudao.module.mes.service.wm.itemreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.MesWmItemReceiptPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.MesWmItemReceiptSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptDO;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 采购入库单 Service 接口
 */
public interface MesWmItemReceiptService {

    /**
     * 创建采购入库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItemReceipt(@Valid MesWmItemReceiptSaveReqVO createReqVO);

    /**
     * 修改采购入库单
     *
     * @param updateReqVO 修改信息
     */
    void updateItemReceipt(@Valid MesWmItemReceiptSaveReqVO updateReqVO);

    /**
     * 删除采购入库单（级联删除行+明细）
     *
     * @param id 编号
     */
    void deleteItemReceipt(Long id);

    /**
     * 获得采购入库单
     *
     * @param id 编号
     * @return 采购入库单
     */
    MesWmItemReceiptDO getItemReceipt(Long id);

    /**
     * 获得采购入库单分页
     *
     * @param pageReqVO 分页参数
     * @return 采购入库单分页
     */
    PageResult<MesWmItemReceiptDO> getItemReceiptPage(MesWmItemReceiptPageReqVO pageReqVO);

    /**
     * 提交采购入库单（草稿 → 待上架）
     *
     * @param id 编号
     */
    void submitItemReceipt(Long id);

    /**
     * 执行上架（待上架 → 待入库）
     *
     * @param id 编号
     */
    void stockItemReceipt(Long id);

    /**
     * 执行入库（待入库 → 已完成），更新库存台账
     *
     * @param id 编号
     */
    void finishItemReceipt(Long id);

    /**
     * 取消采购入库单（任意非已完成/已取消状态 → 已取消）
     *
     * @param id 编号
     */
    void cancelItemReceipt(Long id);

    /**
     * 校验采购入库单存在且处于可编辑状态（草稿或待上架）
     *
     * @param id 编号
     * @return 采购入库单
     */
    MesWmItemReceiptDO validateItemReceiptEditable(Long id);

    /**
     * 查询指定供应商的采购入库单数量
     *
     * @param vendorId 供应商编号
     * @return 数量
     */
    Long getItemReceiptCountByVendorId(Long vendorId);

    /**
     * 查询指定供应商的采购入库单列表
     *
     * @param vendorId 供应商编号
     * @return 入库单列表
     */
    List<MesWmItemReceiptDO> getItemReceiptListByVendorId(Long vendorId);

    /**
     * 批量获得采购入库单列表
     *
     * @param ids 编号数组
     * @return 入库单列表
     */
    List<MesWmItemReceiptDO> getItemReceiptList(Collection<Long> ids);

    /**
     * 批量获得采购入库单 Map
     *
     * @param ids 编号数组
     * @return 入库单 Map
     */
    default Map<Long, MesWmItemReceiptDO> getItemReceiptMap(Collection<Long> ids) {
        return convertMap(getItemReceiptList(ids), MesWmItemReceiptDO::getId);
    }

}
