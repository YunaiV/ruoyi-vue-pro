package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.announcement.PmsProjectAnnouncementSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectAnnouncementDO;

import java.util.List;

/**
 * PMS 项目公告 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsProjectAnnouncementService {

    /**
     * 创建项目公告
     *
     * @param saveReqVO 保存信息
     * @param userId 后台用户编号
     * @return 公告编号
     */
    Long createProjectAnnouncement(PmsProjectAnnouncementSaveReqVO saveReqVO, Long userId);

    /**
     * 更新项目公告
     *
     * @param saveReqVO 保存信息
     * @param userId 后台用户编号
     */
    void updateProjectAnnouncement(PmsProjectAnnouncementSaveReqVO saveReqVO, Long userId);

    /**
     * 删除项目公告
     *
     * @param id 公告编号
     * @param userId 后台用户编号
     */
    void deleteProjectAnnouncement(Long id, Long userId);

    /**
     * 获得项目公告详情
     *
     * @param id 公告编号
     * @param userId 后台用户编号
     * @return 项目公告
     */
    PmsProjectAnnouncementDO getProjectAnnouncement(Long id, Long userId);

    /**
     * 获得项目公告列表
     *
     * @param projectId 项目编号
     * @param userId 后台用户编号
     * @return 公告列表
     */
    List<PmsProjectAnnouncementDO> getProjectAnnouncementList(Long projectId, Long userId);

    /**
     * 删除项目的全部公告
     *
     * @param projectId 项目编号
     */
    void deleteProjectAnnouncementListByProjectId(Long projectId);

}
