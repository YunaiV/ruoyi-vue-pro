package cn.iocoder.yudao.module.mes.service.wm.outsourcereceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.line.MesWmOutsourceReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.line.MesWmOutsourceReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptLineDO;

import java.util.List;

/**
 * MES 委外收货单行 Service 接口
 */
public interface MesWmOutsourceReceiptLineService {

    /**
     * 创建外协入库单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutsourceReceiptLine(MesWmOutsourceReceiptLineSaveReqVO createReqVO);

    /**
     * 更新外协入库单行
     *
     * @param updateReqVO 更新信息
     */
    void updateOutsourceReceiptLine(MesWmOutsourceReceiptLineSaveReqVO updateReqVO);

    /**
     * 删除外协入库单行
     *
     * @param id 编号
     */
    void deleteOutsourceReceiptLine(Long id);

    /**
     * 获得外协入库单行
     *
     * @param id 编号
     * @return 外协入库单行
     */
    MesWmOutsourceReceiptLineDO getOutsourceReceiptLine(Long id);

    /**
     * 获得外协入库单行分页
     *
     * @param pageReqVO 分页查询
     * @return 外协入库单行分页
     */
    PageResult<MesWmOutsourceReceiptLineDO> getOutsourceReceiptLinePage(MesWmOutsourceReceiptLinePageReqVO pageReqVO);

    /**
     * 获得外协入库单行列表（根据入库单编号）
     *
     * @param receiptId 入库单编号
     * @return 外协入库单行列表
     */
    List<MesWmOutsourceReceiptLineDO> getOutsourceReceiptLineListByReceiptId(Long receiptId);

    /**
     * 删除外协入库单行（根据入库单编号）
     *
     * @param receiptId 入库单编号
     */
    void deleteOutsourceReceiptLineByReceiptId(Long receiptId);

    /**
     * 直接更新外协入库单行 DO（用于 IQC 回写）
     *
     * @param line 行 DO
     */
    void updateOutsourceReceiptLineDO(MesWmOutsourceReceiptLineDO line);

    /**
     * 直接插入外协入库单行 DO（用于 QC 按质量拆分行）
     *
     * @param line 行 DO
     */
    void createOutsourceReceiptLineDO(MesWmOutsourceReceiptLineDO line);

}
