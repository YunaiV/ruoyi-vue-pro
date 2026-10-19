package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupMoveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupSaveReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.group.PmsProjectGroupSortReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectGroupDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * PMS 项目分组 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsProjectGroupService {

    /**
     * 创建项目分组
     *
     * @param createReqVO 创建信息
     * @param userId 后台用户编号
     * @return 项目分组编号
     */
    Long createProjectGroup(PmsProjectGroupSaveReqVO createReqVO, Long userId);

    /**
     * 更新项目分组
     *
     * @param updateReqVO 更新信息
     * @param userId 后台用户编号
     */
    void updateProjectGroup(PmsProjectGroupSaveReqVO updateReqVO, Long userId);

    /**
     * 更新项目分组排序
     *
     * @param sortReqVO 排序信息
     * @param userId 后台用户编号
     */
    void updateProjectGroupSort(PmsProjectGroupSortReqVO sortReqVO, Long userId);

    /**
     * 删除项目分组
     *
     * @param id 项目分组编号
     * @param userId 后台用户编号
     */
    void deleteProjectGroup(Long id, Long userId);

    /**
     * 获得当前用户的项目分组列表
     *
     * @param userId 后台用户编号
     * @return 项目分组列表
     */
    List<PmsProjectGroupDO> getProjectGroupList(Long userId);

    /**
     * 获得项目分组的项目数量 Map
     *
     * @param userId 后台用户编号
     * @param groups 项目分组列表
     * @param projectIds 当前用户参与的进行中项目编号集合
     * @return 项目分组编号与项目数量 Map
     */
    Map<Long, Integer> getProjectGroupCountMap(Long userId, Collection<PmsProjectGroupDO> groups, Collection<Long> projectIds);

    /**
     * 移动项目到个人分组
     *
     * @param moveReqVO 移动信息
     * @param userId 后台用户编号
     */
    void moveProjectToGroup(PmsProjectGroupMoveReqVO moveReqVO, Long userId);

    /**
     * 按个人项目分组过滤项目编号
     *
     * @param groupId 项目分组编号
     * @param userId 后台用户编号
     * @param projectIds 待过滤的项目编号集合
     * @return 过滤后的项目编号列表
     */
    List<Long> filterProjectIdListByGroupId(Long groupId, Long userId, Collection<Long> projectIds);

    /**
     * 删除项目的全部个人分组关系
     *
     * @param projectId 项目编号
     */
    void deleteProjectGroupRelationListByProjectId(Long projectId);

}
