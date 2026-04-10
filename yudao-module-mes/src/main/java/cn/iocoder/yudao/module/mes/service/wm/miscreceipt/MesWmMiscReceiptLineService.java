package cn.iocoder.yudao.module.mes.service.wm.miscreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.miscreceipt.vo.line.MesWmMiscReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 杂项入库单行 Service 接口
 */
public interface MesWmMiscReceiptLineService {

    /**
     * 创建杂项入库单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMiscReceiptLine(@Valid MesWmMiscReceiptLineSaveReqVO createReqVO);

    /**
     * 修改杂项入库单行
     *
     * @param updateReqVO 修改信息
     */
    void updateMiscReceiptLine(@Valid MesWmMiscReceiptLineSaveReqVO updateReqVO);

    /**
     * 删除杂项入库单行
     *
     * @param id 编号
     */
    void deleteMiscReceiptLine(Long id);

    /**
     * 获得杂项入库单行
     *
     * @param id 编号
     * @return 杂项入库单行
     */
    MesWmMiscReceiptLineDO getMiscReceiptLine(Long id);

    /**
     * 获得杂项入库单行列表
     *
     * @param receiptId 入库单编号
     * @return 杂项入库单行列表
     */
    List<MesWmMiscReceiptLineDO> getMiscReceiptLineListByReceiptId(Long receiptId);

    /**
     * 获得杂项入库单行分页
     *
     * @param pageReqVO 分页参数
     * @return 杂项入库单行分页
     */
    PageResult<MesWmMiscReceiptLineDO> getMiscReceiptLinePage(MesWmMiscReceiptLinePageReqVO pageReqVO);

    /**
     * 删除指定入库单的所有行（级联删除）
     *
     * @param receiptId 入库单编号
     */
    void deleteByReceiptId(Long receiptId);

    /**
     * 校验杂项入库单行存在
     *
     * @param id 编号
     * @return 杂项入库单行
     */
    MesWmMiscReceiptLineDO validateMiscReceiptLineExists(Long id);

}
