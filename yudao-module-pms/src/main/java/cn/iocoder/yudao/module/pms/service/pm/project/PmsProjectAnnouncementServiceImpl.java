package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.announcement.PmsProjectAnnouncementSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectAnnouncementDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectAnnouncementMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_ANNOUNCEMENT_NOT_EXISTS;

/**
 * PMS 项目公告 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class PmsProjectAnnouncementServiceImpl implements PmsProjectAnnouncementService {

    @Resource
    private PmsProjectAnnouncementMapper announcementMapper;

    @Resource
    private PmsProjectMemberService projectMemberService;

    @Override
    public Long createProjectAnnouncement(PmsProjectAnnouncementSaveReqVO saveReqVO, Long userId) {
        // 1. 校验项目编辑权限
        projectMemberService.validateProjectWritable(saveReqVO.getProjectId(), userId);

        // 2. 创建公告
        PmsProjectAnnouncementDO announcement = BeanUtils.toBean(saveReqVO, PmsProjectAnnouncementDO.class);
        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    @Override
    public void updateProjectAnnouncement(PmsProjectAnnouncementSaveReqVO saveReqVO, Long userId) {
        // 1.1 校验公告存在
        PmsProjectAnnouncementDO announcement = validateAnnouncementExists(saveReqVO.getId());
        // 1.2 校验公告属于请求项目
        if (ObjectUtil.notEqual(announcement.getProjectId(), saveReqVO.getProjectId())) {
            throw exception(PROJECT_ANNOUNCEMENT_NOT_EXISTS);
        }
        // 1.3 校验项目编辑权限
        projectMemberService.validateProjectWritable(announcement.getProjectId(), userId);

        // 2. 更新公告
        announcementMapper.updateById(BeanUtils.toBean(saveReqVO, PmsProjectAnnouncementDO.class));
    }

    @Override
    public void deleteProjectAnnouncement(Long id, Long userId) {
        // 1.1 校验公告存在
        PmsProjectAnnouncementDO announcement = validateAnnouncementExists(id);
        // 1.2 校验项目编辑权限
        projectMemberService.validateProjectWritable(announcement.getProjectId(), userId);

        // 2. 删除公告
        announcementMapper.deleteById(id);
    }

    @Override
    public PmsProjectAnnouncementDO getProjectAnnouncement(Long id, Long userId) {
        // 1. 校验公告存在
        PmsProjectAnnouncementDO announcement = validateAnnouncementExists(id);
        // 2. 校验项目读取权限
        projectMemberService.validateProjectReadable(announcement.getProjectId(), userId);
        return announcement;
    }

    @Override
    public List<PmsProjectAnnouncementDO> getProjectAnnouncementList(Long projectId, Long userId) {
        // 1. 校验项目读取权限
        projectMemberService.validateProjectReadable(projectId, userId);

        // 2. 查询项目公告
        return announcementMapper.selectListByProjectId(projectId);
    }

    @Override
    public void deleteProjectAnnouncementListByProjectId(Long projectId) {
        announcementMapper.deleteByProjectId(projectId);
    }

    /**
     * 校验项目公告存在
     *
     * @param id 公告编号
     * @return 项目公告
     */
    private PmsProjectAnnouncementDO validateAnnouncementExists(Long id) {
        PmsProjectAnnouncementDO announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw exception(PROJECT_ANNOUNCEMENT_NOT_EXISTS);
        }
        return announcement;
    }

}
