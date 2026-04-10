package cn.iocoder.yudao.module.mes.service.cal.team;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.member.MesCalTeamMemberPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.cal.team.vo.member.MesCalTeamMemberSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.cal.team.MesCalTeamMemberDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * MES 班组成员 Service 接口
 *
 * @author 芋道源码
 */
public interface MesCalTeamMemberService {

    /**
     * 创建班组成员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTeamMember(@Valid MesCalTeamMemberSaveReqVO createReqVO);

    /**
     * 删除班组成员
     *
     * @param id 编号
     */
    void deleteTeamMember(Long id);

    /**
     * 获得班组成员
     *
     * @param id 编号
     * @return 班组成员
     */
    MesCalTeamMemberDO getTeamMember(Long id);

    /**
     * 获得班组成员分页
     *
     * @param pageReqVO 分页查询
     * @return 班组成员分页
     */
    PageResult<MesCalTeamMemberDO> getTeamMemberPage(MesCalTeamMemberPageReqVO pageReqVO);

    /**
     * 获得指定班组的成员列表
     *
     * @param teamId 班组编号
     * @return 班组成员列表
     */
    List<MesCalTeamMemberDO> getTeamMemberListByTeamId(Long teamId);

    /**
     * 获得指定多个班组的成员列表
     *
     * @param teamIds 班组编号列表
     * @return 班组成员列表
     */
    List<MesCalTeamMemberDO> getTeamMemberListByTeamIds(Collection<Long> teamIds);

    /**
     * 根据用户编号获得班组成员
     *
     * @param userId 用户编号
     * @return 班组成员，不存在则返回 null
     */
    MesCalTeamMemberDO getTeamMemberByUserId(Long userId);

    /**
     * 根据班组编号删除所有成员
     *
     * @param teamId 班组编号
     */
    void deleteByTeamId(Long teamId);

}
