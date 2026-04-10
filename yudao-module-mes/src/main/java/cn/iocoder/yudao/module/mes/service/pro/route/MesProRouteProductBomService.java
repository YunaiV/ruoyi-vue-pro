package cn.iocoder.yudao.module.mes.service.pro.route;

import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.productbom.MesProRouteProductBomSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProductBomDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 工艺路线产品 BOM Service 接口
 *
 * @author 芋道源码
 */
public interface MesProRouteProductBomService {

    /**
     * 创建工艺路线产品 BOM
     */
    Long createRouteProductBom(@Valid MesProRouteProductBomSaveReqVO createReqVO);

    /**
     * 更新工艺路线产品 BOM
     */
    void updateRouteProductBom(@Valid MesProRouteProductBomSaveReqVO updateReqVO);

    /**
     * 删除工艺路线产品 BOM
     */
    void deleteRouteProductBom(Long id);

    /**
     * 获得工艺路线产品 BOM
     */
    MesProRouteProductBomDO getRouteProductBom(Long id);

    /**
     * 按条件查询工艺路线产品 BOM 列表
     */
    List<MesProRouteProductBomDO> getRouteProductBomList(Long routeId, Long processId, Long productId);

    /**
     * 按工艺路线删除产品 BOM（级联删除使用）
     *
     * @param routeId 工艺路线编号
     */
    void deleteRouteProductBomByRouteId(Long routeId);

    /**
     * 按工艺路线和产品删除 BOM（产品删除时级联使用）
     *
     * @param routeId 工艺路线编号
     * @param productId 产品物料编号
     */
    void deleteRouteProductBomByRouteIdAndProductId(Long routeId, Long productId);

}
