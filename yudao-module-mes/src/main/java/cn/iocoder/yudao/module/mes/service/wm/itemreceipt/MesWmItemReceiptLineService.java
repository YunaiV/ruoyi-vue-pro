package cn.iocoder.yudao.module.mes.service.wm.itemreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line.MesWmItemReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.itemreceipt.vo.line.MesWmItemReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.itemreceipt.MesWmItemReceiptLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 采购入库单行 Service 接口
 */
public interface MesWmItemReceiptLineService {

    /**
     * 创建采购入库单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createItemReceiptLine(@Valid MesWmItemReceiptLineSaveReqVO createReqVO);

    /**
     * 修改采购入库单行
     *
     * @param updateReqVO 修改信息
     */
    void updateItemReceiptLine(@Valid MesWmItemReceiptLineSaveReqVO updateReqVO);

    /**
     * 删除采购入库单行（级联删除明细）
     *
     * @param id 编号
     */
    void deleteItemReceiptLine(Long id);

    /**
     * 获得采购入库单行
     *
     * @param id 编号
     * @return 采购入库单行
     */
    MesWmItemReceiptLineDO getItemReceiptLine(Long id);

    /**
     * 获得采购入库单行分页
     *
     * @param pageReqVO 分页参数
     * @return 采购入库单行分页
     */
    PageResult<MesWmItemReceiptLineDO> getItemReceiptLinePage(MesWmItemReceiptLinePageReqVO pageReqVO);

    /**
     * 按入库单编号获得行列表
     *
     * @param receiptId 入库单编号
     * @return 行列表
     */
    List<MesWmItemReceiptLineDO> getItemReceiptLineListByReceiptId(Long receiptId);

    /**
     * 按入库单编号批量删除行
     *
     * @param receiptId 入库单编号
     */
    void deleteItemReceiptLineByReceiptId(Long receiptId);

}
