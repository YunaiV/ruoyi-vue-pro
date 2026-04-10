package cn.iocoder.yudao.module.mes.service.pro.route;

import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.product.MesProRouteProductSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 工艺路线产品 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProRouteProductService {

    /**
     * 创建工艺路线产品
     */
    Long createRouteProduct(@Valid MesProRouteProductSaveReqVO createReqVO);

    /**
     * 更新工艺路线产品
     */
    void updateRouteProduct(@Valid MesProRouteProductSaveReqVO updateReqVO);

    /**
     * 删除工艺路线产品
     */
    void deleteRouteProduct(Long id);

    /**
     * 获得工艺路线产品
     */
    MesProRouteProductDO getRouteProduct(Long id);

    /**
     * 按产品（物料）获得工艺路线产品
     *
     * @param itemId 产品编号
     * @return 工艺路线产品，如果未配置则返回 null
     */
    MesProRouteProductDO getRouteProductByItemId(Long itemId);

    /**
     * 按工艺路线获得产品列表
     */
    List<MesProRouteProductDO> getRouteProductListByRouteId(Long routeId);

    /**
     * 按工艺路线删除产品（级联删除使用）
     *
     * @param routeId 工艺路线编号
     */
    void deleteRouteProductByRouteId(Long routeId);

}
