package cn.iocoder.yudao.module.mes.service.md.workstation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.MesMdWorkstationPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.MesMdWorkstationSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 工作站 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdWorkstationService {

    /**
     * 创建工作站
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWorkstation(@Valid MesMdWorkstationSaveReqVO createReqVO);

    /**
     * 更新工作站
     *
     * @param updateReqVO 更新信息
     */
    void updateWorkstation(@Valid MesMdWorkstationSaveReqVO updateReqVO);

    /**
     * 删除工作站
     *
     * @param id 编号
     */
    void deleteWorkstation(Long id);

    /**
     * 校验工作站存在
     *
     * @param id 编号
     * @return 工作站
     */
    MesMdWorkstationDO validateWorkstationExists(Long id);

    /**
     * 获得工作站
     *
     * @param id 编号
     * @return 工作站
     */
    MesMdWorkstationDO getWorkstation(Long id);

    /**
     * 获得工作站分页
     *
     * @param pageReqVO 分页查询
     * @return 工作站分页
     */
    PageResult<MesMdWorkstationDO> getWorkstationPage(MesMdWorkstationPageReqVO pageReqVO);

    /**
     * 获得指定状态的工作站列表
     *
     * @param status 状态
     * @return 工作站列表
     */
    List<MesMdWorkstationDO> getWorkstationListByStatus(Integer status);

    /**
     * 获得指定仓库下的工作站数量
     *
     * @param warehouseId 仓库编号
     * @return 工作站数量
     */
    Long getWorkstationCountByWarehouseId(Long warehouseId);

    /**
     * 获得指定库区下的工作站数量
     *
     * @param locationId 库区编号
     * @return 工作站数量
     */
    Long getWorkstationCountByLocationId(Long locationId);

    /**
     * 获得指定库位下的工作站数量
     *
     * @param areaId 库位编号
     * @return 工作站数量
     */
    Long getWorkstationCountByAreaId(Long areaId);

    /**
     * 获得工作站列表
     *
     * @param ids 编号数组
     * @return 工作站列表
     */
    List<MesMdWorkstationDO> getWorkstationList(Collection<Long> ids);

    /**
     * 获得工作站 Map
     *
     * @param ids 编号数组
     * @return 工作站 Map
     */
    default Map<Long, MesMdWorkstationDO> getWorkstationMap(Collection<Long> ids) {
        return convertMap(getWorkstationList(ids), MesMdWorkstationDO::getId);
    }

}
