package cn.iocoder.yudao.module.pms.service.pm.project;

import java.util.List;

/**
 * PMS 项目收藏 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsProjectFavoriteService {

    /**
     * 收藏项目
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     */
    void createProjectFavorite(Long projectId, Long userId);

    /**
     * 取消收藏项目
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     */
    void deleteProjectFavorite(Long projectId, Long userId);

    /**
     * 获得用户收藏的项目编号列表
     *
     * @param userId 后台用户编号
     * @return 项目编号列表
     */
    List<Long> getFavoriteProjectIdListByUserId(Long userId);

    /**
     * 删除项目的全部收藏关系
     *
     * @param projectId 项目编号
     */
    void deleteProjectFavoriteListByProjectId(Long projectId);

}
