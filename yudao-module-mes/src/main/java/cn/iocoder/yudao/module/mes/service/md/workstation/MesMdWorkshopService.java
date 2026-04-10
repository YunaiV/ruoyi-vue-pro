package cn.iocoder.yudao.module.mes.service.md.workstation;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop.MesMdWorkshopPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop.MesMdWorkshopSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkshopDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 车间 Service 接口
 *
 * @author 芋道源码
 */
public interface MesMdWorkshopService {

    /**
     * 创建车间
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWorkshop(@Valid MesMdWorkshopSaveReqVO createReqVO);

    /**
     * 更新车间
     *
     * @param updateReqVO 更新信息
     */
    void updateWorkshop(@Valid MesMdWorkshopSaveReqVO updateReqVO);

    /**
     * 删除车间
     *
     * @param id 编号
     */
    void deleteWorkshop(Long id);

    /**
     * 获得车间
     *
     * @param id 编号
     * @return 车间
     */
    MesMdWorkshopDO getWorkshop(Long id);

    /**
     * 获得车间分页
     *
     * @param pageReqVO 分页查询
     * @return 车间分页
     */
    PageResult<MesMdWorkshopDO> getWorkshopPage(MesMdWorkshopPageReqVO pageReqVO);

    /**
     * 获得车间列表
     *
     * @param ids 编号列表
     * @return 车间列表
     */
    List<MesMdWorkshopDO> getWorkshopList(Collection<Long> ids);

    /**
     * 获得车间 Map
     *
     * @param ids 编号列表
     * @return 车间 Map
     */
    default Map<Long, MesMdWorkshopDO> getWorkshopMap(Collection<Long> ids) {
        return convertMap(getWorkshopList(ids), MesMdWorkshopDO::getId);
    }

    /**
     * 获得指定状态的车间列表
     *
     * @param status 状态
     * @return 车间列表
     */
    List<MesMdWorkshopDO> getWorkshopListByStatus(Integer status);

}
