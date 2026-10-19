package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.member.PmsProjectMemberRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.member.PmsProjectMemberSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * PMS 项目成员 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsProjectMemberService {

    /**
     * 获得用户参与的进行中项目编号
     *
     * @param userId 后台用户编号
     * @return 项目编号列表
     */
    List<Long> getActiveProjectIdListByUserId(Long userId);

    /**
     * 校验用户是指定项目的成员
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     * @return 项目
     */
    PmsProjectDO validateProjectMember(Long projectId, Long userId);

    /**
     * 校验用户可以查看指定项目
     *
     * 公开项目允许所有后台用户查看，私有项目只允许项目成员或超级管理员查看
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     * @return 项目
     */
    PmsProjectDO validateProjectReadable(Long projectId, Long userId);

    /**
     * 校验用户可以编辑指定项目的业务数据
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     * @return 项目
     */
    PmsProjectDO validateProjectWritable(Long projectId, Long userId);

    /**
     * 初始化项目成员
     *
     * @param projectId 项目编号
     * @param creatorId 创建人编号
     * @param memberUserIds 初始成员编号列表
     */
    void createProjectMemberList(Long projectId, Long creatorId, Collection<Long> memberUserIds);

    /**
     * 保存项目成员及其权限级别
     *
     * @param saveReqVO 保存信息
     * @param operatorUserId 操作人编号
     */
    void updateProjectMemberList(PmsProjectMemberSaveReqVO saveReqVO, Long operatorUserId);

    /**
     * 移除项目成员
     *
     * @param projectId 项目编号
     * @param userId 成员编号
     * @param operatorUserId 操作人编号
     */
    void deleteProjectMember(Long projectId, Long userId, Long operatorUserId);

    /**
     * 主动退出项目
     *
     * @param projectId 项目编号
     * @param userId 当前用户编号
     */
    void exitProject(Long projectId, Long userId);

    /**
     * 删除项目的全部成员关系
     *
     * @param projectId 项目编号
     */
    void deleteProjectMemberListByProjectId(Long projectId);

    /**
     * 获得项目成员列表
     *
     * @param projectId 项目编号
     * @param userId 当前用户编号
     * @return 项目成员列表
     */
    List<PmsProjectMemberRespVO> getProjectMemberList(Long projectId, Long userId);

    /**
     * 获得用户参与的项目编号列表
     *
     * @param userId 后台用户编号
     * @return 项目编号列表
     */
    List<Long> getProjectIdListByUserId(Long userId);

    /**
     * 获得用户拥有的项目编号列表
     *
     * @param userId 后台用户编号
     * @return 项目编号列表
     */
    List<Long> getOwnerProjectIdListByUserId(Long userId);

    /**
     * 获得用户管理的项目编号列表
     *
     * @param userId 后台用户编号
     * @return 项目编号列表
     */
    List<Long> getManagedProjectIdListByUserId(Long userId);

    /**
     * 获得用户可编辑的项目编号列表
     *
     * @param userId 后台用户编号
     * @return 项目编号列表
     */
    List<Long> getWritableProjectIdListByUserId(Long userId);

    /**
     * 获得项目拥有者和管理员用户编号 Map
     *
     * @param projectIds 项目编号集合
     * @return 项目管理员用户编号 Map
     */
    Map<Long, List<Long>> getProjectManagerUserIdListMap(Collection<Long> projectIds);

    /**
     * 获得项目成员数量 Map
     *
     * @param projectIds 项目编号集合
     * @return 项目成员数量 Map
     */
    Map<Long, Integer> getProjectMemberCountMap(Collection<Long> projectIds);

    /**
     * 判断用户是否拥有项目拥有者权限
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     * @return 是否拥有项目拥有者权限
     */
    boolean hasProjectOwnerPermission(Long projectId, Long userId);

    /**
     * 判断用户是否拥有项目管理权限
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     * @return 是否拥有项目管理权限
     */
    boolean hasProjectManagerPermission(Long projectId, Long userId);

    /**
     * 校验用户是项目成员
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     */
    void validateProjectMemberExists(Long projectId, Long userId);

    /**
     * 校验用户列表都是项目成员
     *
     * @param projectId 项目编号
     * @param userIds 后台用户编号集合
     */
    void validateProjectMemberList(Long projectId, Collection<Long> userIds);

}
