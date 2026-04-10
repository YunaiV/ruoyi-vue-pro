package cn.iocoder.yudao.module.mes.service.pro.route;

import cn.iocoder.yudao.module.mes.controller.admin.pro.route.vo.process.MesProRouteProcessSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.route.MesProRouteProcessDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * MES 工艺路线工序 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProRouteProcessService {

    /**
     * 创建工艺路线工序
     */
    Long createRouteProcess(@Valid MesProRouteProcessSaveReqVO createReqVO);

    /**
     * 更新工艺路线工序
     */
    void updateRouteProcess(@Valid MesProRouteProcessSaveReqVO updateReqVO);

    /**
     * 删除工艺路线工序
     */
    void deleteRouteProcess(Long id);

    /**
     * 获得工艺路线工序
     */
    MesProRouteProcessDO getRouteProcess(Long id);

    /**
     * 按工艺路线获得工序列表
     */
    List<MesProRouteProcessDO> getRouteProcessListByRouteId(Long routeId);

    /**
     * 按多个工艺路线获得工序列表
     *
     * @param routeIds 工艺路线编号数组
     * @return 工序列表
     */
    List<MesProRouteProcessDO> getRouteProcessListByRouteIds(Collection<Long> routeIds);

    /**
     * 按工艺路线和工序获得工艺路线工序
     *
     * @param routeId   工艺路线编号
     * @param processId 工序编号
     * @return 工艺路线工序
     */
    MesProRouteProcessDO getRouteProcessByRouteIdAndProcessId(Long routeId, Long processId);

    /**
     * 按产品获得工序列表
     *
     * 根据产品查找关联的工艺路线，返回该路线的工序列表
     *
     * @param productId 产品编号
     * @return 工序列表
     */
    List<MesProRouteProcessDO> getRouteProcessListByProductId(Long productId);

    /**
     * 按工序获得工艺路线工序列表
     *
     * @param processId 工序编号
     * @return 工艺路线工序列表
     */
    List<MesProRouteProcessDO> getRouteProcessListByProcessId(Long processId);

    /**
     * 按工艺路线删除工序（级联删除使用）
     *
     * @param routeId 工艺路线编号
     */
    void deleteRouteProcessByRouteId(Long routeId);

}
