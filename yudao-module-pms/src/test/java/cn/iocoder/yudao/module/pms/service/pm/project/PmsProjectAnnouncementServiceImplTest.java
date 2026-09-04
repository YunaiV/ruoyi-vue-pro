package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.announcement.PmsProjectAnnouncementSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectAnnouncementDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectAnnouncementMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link PmsProjectAnnouncementServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsProjectAnnouncementServiceImpl.class)
public class PmsProjectAnnouncementServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsProjectAnnouncementServiceImpl announcementService;

    @Resource
    private PmsProjectAnnouncementMapper announcementMapper;

    @MockBean
    private PmsProjectMemberService projectMemberService;

    @Test
    public void testCreateProjectAnnouncement_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(new PmsProjectDO().setId(projectId));
        // 准备参数
        PmsProjectAnnouncementSaveReqVO reqVO = new PmsProjectAnnouncementSaveReqVO()
                .setProjectId(projectId).setContent("项目启动")
                .setFileUrls(Collections.singletonList("https://example.com/a.pdf"));

        // 调用
        Long id = announcementService.createProjectAnnouncement(reqVO, userId);

        // 断言
        PmsProjectAnnouncementDO announcement = announcementMapper.selectById(id);
        assertEquals("项目启动", announcement.getContent());
    }

    @Test
    public void testUpdateProjectAnnouncement_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        PmsProjectAnnouncementDO announcement = new PmsProjectAnnouncementDO()
                .setProjectId(projectId).setContent("项目启动");
        announcementMapper.insert(announcement);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(new PmsProjectDO().setId(projectId));
        // 准备参数
        PmsProjectAnnouncementSaveReqVO reqVO = new PmsProjectAnnouncementSaveReqVO()
                .setId(announcement.getId()).setProjectId(projectId).setContent("项目已启动")
                .setFileUrls(Collections.singletonList("https://example.com/a.pdf"));

        // 调用
        announcementService.updateProjectAnnouncement(reqVO, userId);

        // 断言
        announcement = announcementMapper.selectById(announcement.getId());
        assertEquals("项目已启动", announcement.getContent());
    }

    @Test
    public void testDeleteProjectAnnouncement_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        PmsProjectAnnouncementDO announcement = new PmsProjectAnnouncementDO()
                .setProjectId(projectId).setContent("项目启动");
        announcementMapper.insert(announcement);
        when(projectMemberService.validateProjectWritable(projectId, userId))
                .thenReturn(new PmsProjectDO().setId(projectId));

        // 调用
        announcementService.deleteProjectAnnouncement(announcement.getId(), userId);

        // 断言
        assertNull(announcementMapper.selectById(announcement.getId()));
    }

    @Test
    public void testGetProjectAnnouncement_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        PmsProjectAnnouncementDO announcement = new PmsProjectAnnouncementDO()
                .setProjectId(projectId).setContent("项目启动");
        announcementMapper.insert(announcement);
        when(projectMemberService.validateProjectReadable(projectId, userId))
                .thenReturn(new PmsProjectDO().setId(projectId));

        // 调用
        PmsProjectAnnouncementDO result = announcementService
                .getProjectAnnouncement(announcement.getId(), userId);

        // 断言
        assertEquals(announcement.getId(), result.getId());
        assertEquals("项目启动", result.getContent());
    }

    @Test
    public void testDeleteProjectAnnouncementListByProjectId() {
        // mock 数据
        Long projectId = randomLongId();
        announcementMapper.insert(new PmsProjectAnnouncementDO().setProjectId(projectId).setContent("公告"));

        // 调用
        announcementService.deleteProjectAnnouncementListByProjectId(projectId);

        // 断言
        assertTrue(CollUtil.isEmpty(announcementMapper.selectListByProjectId(projectId)));
    }

}
