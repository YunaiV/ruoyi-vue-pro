package cn.iocoder.yudao.module.mes.service.wm.returnsales;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.returnsales.vo.MesWmReturnSalesSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.returnsales.MesWmReturnSalesDO;
import jakarta.validation.Valid;

/**
 * MES 销售退货单 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmReturnSalesService {

    /**
     * 创建销售退货单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createReturnSales(@Valid MesWmReturnSalesSaveReqVO createReqVO);

    /**
     * 修改销售退货单
     *
     * @param updateReqVO 修改信息
     */
    void updateReturnSales(@Valid MesWmReturnSalesSaveReqVO updateReqVO);

    /**
     * 删除销售退货单
     *
     * @param id 编号
     */
    void deleteReturnSales(Long id);

    /**
     * 获得销售退货单
     *
     * @param id 编号
     * @return 销售退货单
     */
    MesWmReturnSalesDO getReturnSales(Long id);

    /**
     * 获得销售退货单分页
     *
     * @param pageReqVO 分页参数
     * @return 销售退货单分页
     */
    PageResult<MesWmReturnSalesDO> getReturnSalesPage(MesWmReturnSalesPageReqVO pageReqVO);

    /**
     * 校验销售退货单是否存在
     *
     * @param id 编号
     * @return 销售退货单
     */
    MesWmReturnSalesDO validateReturnSalesExists(Long id);

    /**
     * 校验销售退货单存在且为草稿状态
     *
     * @param id 编号
     * @return 销售退货单
     */
    MesWmReturnSalesDO validateReturnSalesExistsAndPrepare(Long id);

    /**
     * 提交销售退货单（草稿 -> 待执行）
     *
     * @param id 编号
     */
    void submitReturnSales(Long id);

    /**
     * 执行退货（待执行 -> 待上架）
     *
     * @param id 编号
     */
    void finishReturnSales(Long id);

    /**
     * 执行上架（待上架 -> 已完成），更新库存台账
     *
     * @param id 编号
     */
    void stockReturnSales(Long id);

    /**
     * 取消销售退货单（任意非已完成/已取消状态 -> 已取消）
     *
     * @param id 编号
     */
    void cancelReturnSales(Long id);

    /**
     * 更新销售退货单状态（供外部模块联动调用，如 RQC 检验完成后）
     *
     * @param id 编号
     * @param status 目标状态
     */
    void updateReturnSalesStatus(Long id, Integer status);

    /**
     * 校验销售退货单的数量：每行明细数量之和是否等于行退货数量
     *
     * @param id 编号
     * @return 是否全部一致
     */
    Boolean checkReturnSalesQuantity(Long id);

}
