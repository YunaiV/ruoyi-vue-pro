package cn.iocoder.yudao.module.mes.service.wm.productreceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.line.MesWmProductReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productreceipt.vo.line.MesWmProductReceiptLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productreceipt.MesWmProductReceiptLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 产品收货单行 Service 接口
 */
public interface MesWmProductReceiptLineService {

    /**
     * 创建产品收货单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductReceiptLine(@Valid MesWmProductReceiptLineSaveReqVO createReqVO);

    /**
     * 修改产品收货单行
     *
     * @param updateReqVO 修改信息
     */
    void updateProductReceiptLine(@Valid MesWmProductReceiptLineSaveReqVO updateReqVO);

    /**
     * 删除产品收货单行（级联删除明细）
     *
     * @param id 编号
     */
    void deleteProductReceiptLine(Long id);

    /**
     * 获得产品收货单行
     *
     * @param id 编号
     * @return 产品收货单行
     */
    MesWmProductReceiptLineDO getProductReceiptLine(Long id);

    /**
     * 获得产品收货单行分页
     *
     * @param pageReqVO 分页参数
     * @return 产品收货单行分页
     */
    PageResult<MesWmProductReceiptLineDO> getProductReceiptLinePage(MesWmProductReceiptLinePageReqVO pageReqVO);

    /**
     * 按收货单编号获得行列表
     *
     * @param receiptId 收货单编号
     * @return 行列表
     */
    List<MesWmProductReceiptLineDO> getProductReceiptLineListByRecptId(Long receiptId);

    /**
     * 按收货单编号批量删除行
     *
     * @param receiptId 收货单编号
     */
    void deleteProductReceiptLineByRecptId(Long receiptId);

}
