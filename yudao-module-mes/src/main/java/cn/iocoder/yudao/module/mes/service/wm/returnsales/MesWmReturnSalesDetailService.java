package cn.iocoder.yudao.module.mes.service.wm.returnsales;

import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.detail.MesWmReturnSalesDetailSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDetailDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 销售退货明细 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmReturnSalesDetailService {

    /**
     * 创建销售退货明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReturnSalesDetail(@Valid MesWmReturnSalesDetailSaveReqVO createReqVO);

    /**
     * 更新销售退货明细
     *
     * @param updateReqVO 更新信息
     */
    void updateReturnSalesDetail(@Valid MesWmReturnSalesDetailSaveReqVO updateReqVO);

    /**
     * 删除销售退货明细
     *
     * @param id 编号
     */
    void deleteReturnSalesDetail(Long id);

    /**
     * 获得销售退货明细
     *
     * @param id 编号
     * @return 销售退货明细
     */
    MesWmReturnSalesDetailDO getReturnSalesDetail(Long id);

    /**
     * 根据行ID获取明细列表
     *
     * @param lineId 行ID
     * @return 明细列表
     */
    List<MesWmReturnSalesDetailDO> getReturnSalesDetailListByLineId(Long lineId);

    /**
     * 根据退货单ID获取明细列表
     *
     * @param returnId 退货单ID
     * @return 明细列表
     */
    List<MesWmReturnSalesDetailDO> getReturnSalesDetailListByReturnId(Long returnId);

    /**
     * 根据退货单ID删除明细
     *
     * @param returnId 退货单ID
     */
    void deleteReturnSalesDetailByReturnId(Long returnId);

    /**
     * 根据行ID删除明细（行删除时级联）
     *
     * @param lineId 行ID
     */
    void deleteReturnSalesDetailByLineId(Long lineId);

}
