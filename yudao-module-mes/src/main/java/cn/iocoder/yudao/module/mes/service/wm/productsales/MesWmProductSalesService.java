package cn.iocoder.yudao.module.mes.service.wm.productsales;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.MesWmProductSalesPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.MesWmProductSalesSaveReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.productsales.vo.MesWmProductSalesShippingReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.productsales.MesWmProductSalesDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 销售出库单 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmProductSalesService {

    /**
     * 创建销售出库单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProductSales(@Valid MesWmProductSalesSaveReqVO createReqVO);

    /**
     * 修改销售出库单
     *
     * @param updateReqVO 修改信息
     */
    void updateProductSales(@Valid MesWmProductSalesSaveReqVO updateReqVO);

    /**
     * 删除销售出库单（级联删除行+明细）
     *
     * @param id 编号
     */
    void deleteProductSales(Long id);

    /**
     * 获得销售出库单
     *
     * @param id 编号
     * @return 销售出库单
     */
    MesWmProductSalesDO getProductSales(Long id);

    /**
     * 获得销售出库单分页
     *
     * @param pageReqVO 分页参数
     * @return 销售出库单分页
     */
    PageResult<MesWmProductSalesDO> getProductSalesPage(MesWmProductSalesPageReqVO pageReqVO);

    /**
     * 提交销售出库单
     * <ul>
     *     <li>有 OQC 检验行：草稿 → 待检测</li>
     *     <li>无 OQC 检验行：草稿 → 待拣货</li>
     * </ul>
     *
     * @param id 编号
     */
    void submitProductSales(Long id);

    /**
     * 校验销售出库单数量是否匹配
     *
     * @param id 编号
     * @return 是否匹配
     */
    boolean checkProductSalesQuantity(Long id);

    /**
     * 执行拣货（待拣货 → 待填写运单）
     *
     * @param id 编号
     */
    void stockProductSales(Long id);

    /**
     * 填写运单（待填写运单 → 待执行出库）
     *
     * @param reqVO 运单信息
     */
    void shippingProductSales(@Valid MesWmProductSalesShippingReqVO reqVO);

    /**
     * 执行出库（待出库 → 已完成），扣减库存
     *
     * @param id 编号
     */
    void finishProductSales(Long id);

    /**
     * 取消销售出库单（任意非已完成/已取消状态 → 已取消）
     *
     * @param id 编号
     */
    void cancelProductSales(Long id);

    /**
     * OQC 检验完成后，自动流转（待检测 → 待拣货）
     *
     * @param id 编号
     */
    void confirmProductSales(Long id);

    /**
     * 按客户编号获得销售出库单列表
     *
     * @param clientId 客户编号
     * @return 出库单列表
     */
    List<MesWmProductSalesDO> getProductSalesListByClientId(Long clientId);

    /**
     * 校验销售出库单存在且为草稿状态
     *
     * @param id 编号
     * @return 销售出库单
     */
    MesWmProductSalesDO validateProductSalesExistsAndDraft(Long id);

}

