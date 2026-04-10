package cn.iocoder.yudao.module.mes.service.cal.team;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.MesCalTeamPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.MesCalTeamSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * MES 班组 Service 接口
 *
 * @author 芋道源码
 */
public interface MesCalTeamService {

    /**
     * 创建班组
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTeam(@Valid MesCalTeamSaveReqVO createReqVO);

    /**
     * 更新班组
     *
     * @param updateReqVO 更新信息
     */
    void updateTeam(@Valid MesCalTeamSaveReqVO updateReqVO);

    /**
     * 删除班组
     *
     * @param id 编号
     */
    void deleteTeam(Long id);

    /**
     * 获得班组
     *
     * @param id 编号
     * @return 班组
     */
    MesCalTeamDO getTeam(Long id);

    /**
     * 获得班组分页
     *
     * @param pageReqVO 分页查询
     * @return 班组分页
     */
    PageResult<MesCalTeamDO> getTeamPage(MesCalTeamPageReqVO pageReqVO);

    /**
     * 获得班组列表（全量，用于下拉选择）
     *
     * @return 班组列表
     */
    List<MesCalTeamDO> getTeamList();

    /**
     * 获得班组列表
     *
     * @param ids 班组编号集合
     * @return 班组列表
     */
    List<MesCalTeamDO> getTeamList(Collection<Long> ids);

    /**
     * 获得班组 Map
     *
     * @param ids 班组编号集合
     * @return 班组 Map，key 为编号
     */
    default Map<Long, MesCalTeamDO> getTeamMap(Collection<Long> ids) {
        return convertMap(getTeamList(ids), MesCalTeamDO::getId);
    }

    /**
     * 校验班组存在
     *
     * @param id 编号
     * @return 班组
     */
    MesCalTeamDO validateTeamExists(Long id);

}
