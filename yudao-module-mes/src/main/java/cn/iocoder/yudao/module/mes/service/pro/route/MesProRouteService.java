package cn.iocoder.yudao.module.mes.service.pro.route;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.MesProRoutePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.MesProRouteSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 工艺路线 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProRouteService {

    /**
     * 创建工艺路线
     */
    Long createRoute(@Valid MesProRouteSaveReqVO createReqVO);

    /**
     * 更新工艺路线
     */
    void updateRoute(@Valid MesProRouteSaveReqVO updateReqVO);

    /**
     * 更新工艺路线状态（启用/禁用）
     *
     * @param id 编号
     * @param status 状态
     */
    void updateRouteStatus(Long id, Integer status);

    /**
     * 删除工艺路线
     */
    void deleteRoute(Long id);

    /**
     * 获得工艺路线
     */
    MesProRouteDO getRoute(Long id);

    /**
     * 获得工艺路线分页
     */
    PageResult<MesProRouteDO> getRoutePage(MesProRoutePageReqVO pageReqVO);

    /**
     * 获得启用状态的工艺路线列表
     */
    List<MesProRouteDO> getRouteListByStatus(Integer status);

    /**
     * 校验工艺路线未启用（已启用则抛异常）
     *
     * @param routeId 工艺路线编号
     */
    void validateRouteNotEnable(Long routeId);

    /**
     * 校验工艺路线存在
     *
     * @param id 工艺路线编号
     * @return 工艺路线
     */
    MesProRouteDO validateRouteExists(Long id);

    /**
     * 获得工艺路线列表
     *
     * @param ids 编号数组
     * @return 工艺路线列表
     */
    List<MesProRouteDO> getRouteList(Collection<Long> ids);

    /**
     * 获得工艺路线 Map
     *
     * @param ids 编号数组
     * @return 工艺路线 Map
     */
    default Map<Long, MesProRouteDO> getRouteMap(Collection<Long> ids) {
        return convertMap(getRouteList(ids), MesProRouteDO::getId);
    }

}
