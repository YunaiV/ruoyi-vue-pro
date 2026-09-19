package cn.iocoder.yudao.module.oa.service.announcement;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementReceiverDO;
import cn.iocoder.yudao.module.oa.dal.mysql.announcement.OaAnnouncementMapper;
import cn.iocoder.yudao.module.oa.dal.mysql.announcement.OaAnnouncementReceiverMapper;
import cn.iocoder.yudao.module.oa.enums.announcement.OaAnnouncementTypeEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_ACCESS_DENIED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_ALREADY_FORWARDED;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_RECEIVER_NOT_EXISTS;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.ANNOUNCEMENT_UNREAD_DELETE_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link OaAnnouncementServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaAnnouncementServiceImpl.class)
public class OaAnnouncementServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaAnnouncementServiceImpl announcementService;

    @Resource
    private OaAnnouncementMapper announcementMapper;

    @Resource
    private OaAnnouncementReceiverMapper announcementReceiverMapper;

    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private NotifyMessageSendApi notifyMessageSendApi;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateAnnouncement_success() {
        // 准备参数
        Long userId = randomLongId();
        Long firstReceiverUserId = randomLongId();
        Long secondReceiverUserId = randomLongId();
        OaAnnouncementSaveReqVO reqVO = buildAnnouncementSaveReqVO();
        when(adminUserApi.getUserListBySubordinate(userId)).thenReturn(Arrays.asList(
                new AdminUserRespDTO().setId(firstReceiverUserId),
                new AdminUserRespDTO().setId(secondReceiverUserId)));

        // 调用
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());
        Long announcementId = announcementService.createAnnouncement(reqVO, userId);

        // 断言公告和去重后的接收关系
        OaAnnouncementDO announcement = announcementMapper.selectById(announcementId);
        assertEquals(userId.toString(), announcement.getCreator());
        assertEquals(userId.toString(), announcement.getUpdater());
        assertEquals(reqVO.getTitle(), announcement.getTitle());
        assertEquals(2, announcementReceiverMapper.selectListByAnnouncementId(announcementId).size());
        verify(adminUserApi).getUserListBySubordinate(userId);
        verifyNoInteractions(notifyMessageSendApi);
    }

    @Test
    public void testUpdateAnnouncement_preservePublisherAndCreateTimeAndNullFields() {
        // mock 数据
        Long userId = randomLongId();
        LocalDateTime createTime = LocalDateTime.now().minusDays(1);
        OaAnnouncementDO announcement = buildAnnouncementDO(userId)
                .setContent("原内容").setUrl("https://old.example.com");
        announcement.setCreateTime(createTime);
        announcementMapper.insert(announcement);
        Long existingReceiverUserId = randomLongId();
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), existingReceiverUserId, true));
        // 准备参数
        OaAnnouncementSaveReqVO reqVO = buildAnnouncementSaveReqVO().setId(announcement.getId())
                .setTitle("更新后的公告").setContent(null).setUrl(null);

        // 调用
        announcementService.updateAnnouncement(reqVO, userId);

        // 断言
        OaAnnouncementDO updatedAnnouncement = announcementMapper.selectById(announcement.getId());
        assertEquals(userId.toString(), updatedAnnouncement.getCreator());
        assertEquals(createTime, updatedAnnouncement.getCreateTime());
        assertEquals(reqVO.getTitle(), updatedAnnouncement.getTitle());
        // 可选字段传 null 时保留原值，不提供显式清空能力
        assertEquals(announcement.getContent(), updatedAnnouncement.getContent());
        assertEquals(announcement.getUrl(), updatedAnnouncement.getUrl());
        List<OaAnnouncementReceiverDO> receivers = announcementReceiverMapper
                .selectListByAnnouncementId(announcement.getId());
        assertEquals(1, receivers.size());
        assertTrue(announcementReceiverMapper.selectByAnnouncementIdAndReceiverUserId(
                announcement.getId(), existingReceiverUserId).getReadStatus());
    }

    @Test
    public void testUpdateAnnouncement_notPublisher() {
        // mock 数据
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        // 准备参数
        OaAnnouncementSaveReqVO reqVO = buildAnnouncementSaveReqVO().setId(announcement.getId());

        // 调用，并断言异常
        assertServiceException(() -> announcementService.updateAnnouncement(reqVO, randomLongId()),
                ANNOUNCEMENT_ACCESS_DENIED);
    }

    @Test
    public void testDeletePublishedAnnouncement_success() {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(userId);
        announcementMapper.insert(announcement);
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), randomLongId(), false));

        // 调用
        announcementService.deleteAnnouncement(announcement.getId(), userId);

        // 断言
        assertNull(announcementMapper.selectById(announcement.getId()));
        assertTrue(announcementReceiverMapper.selectListByAnnouncementId(announcement.getId()).isEmpty());
    }

    @Test
    public void testDeleteReceivedAnnouncement_success() {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), userId, true));
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), randomLongId(), false));

        // 调用
        announcementService.deleteReceivedAnnouncement(announcement.getId(), userId);

        // 断言仅删除当前用户的接收关系
        assertNull(announcementReceiverMapper.selectByAnnouncementIdAndReceiverUserId(
                announcement.getId(), userId));
        assertEquals(1, announcementReceiverMapper.selectListByAnnouncementId(announcement.getId()).size());
        assertEquals(announcement.getId(), announcementMapper.selectById(announcement.getId()).getId());
    }

    @Test
    public void testDeleteReceivedAnnouncement_unread() {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), userId, false));

        // 调用，并断言未读公告不能移除
        assertServiceException(() -> announcementService.deleteReceivedAnnouncement(announcement.getId(), userId),
                ANNOUNCEMENT_UNREAD_DELETE_DENIED);
        assertEquals(1, announcementReceiverMapper.selectListByAnnouncementId(announcement.getId()).size());
    }

    @Test
    public void testDeleteReceivedAnnouncement_notReceiver() {
        // mock 数据
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        Long receiverUserId = randomLongId();
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), receiverUserId, false));

        // 调用，并断言无权移除其他人的接收关系
        assertServiceException(() -> announcementService.deleteReceivedAnnouncement(
                announcement.getId(), randomLongId()), ANNOUNCEMENT_RECEIVER_NOT_EXISTS);
        assertEquals(1, announcementReceiverMapper.selectListByAnnouncementId(announcement.getId()).size());
    }

    @Test
    public void testUpdateAnnouncementReadStatus_notReceiver() {
        // mock 数据
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        Long receiverUserId = randomLongId();
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), receiverUserId, false));

        // 调用，并断言无权修改其他人的阅读状态
        assertServiceException(() -> announcementService.updateAnnouncementReadStatus(
                announcement.getId(), randomLongId()), ANNOUNCEMENT_RECEIVER_NOT_EXISTS);
        assertEquals(false, announcementReceiverMapper.selectByAnnouncementIdAndReceiverUserId(
                announcement.getId(), receiverUserId).getReadStatus());
    }

    @Test
    public void testGetAnnouncement_accessDenied() {
        // mock 数据
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);

        // 调用，并断言异常
        assertServiceException(() -> announcementService.getAnnouncement(announcement.getId(), randomLongId()),
                ANNOUNCEMENT_ACCESS_DENIED);
    }

    @Test
    public void testUpdateAnnouncementReadStatus_success() {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), userId, false));

        // 调用
        announcementService.updateAnnouncementReadStatus(announcement.getId(), userId);

        // 断言
        assertTrue(announcementReceiverMapper.selectByAnnouncementIdAndReceiverUserId(
                announcement.getId(), userId).getReadStatus());
    }

    @Test
    public void testUpdateAnnouncementReadStatus_alreadyRead() {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        OaAnnouncementReceiverDO receiver = buildReceiverDO(announcement.getId(), userId, true);
        receiver.setUpdateTime(LocalDateTime.of(2026, 1, 1, 0, 0));
        announcementReceiverMapper.insert(receiver);

        // 调用
        announcementService.updateAnnouncementReadStatus(announcement.getId(), userId);

        // 断言
        OaAnnouncementReceiverDO updatedReceiver = announcementReceiverMapper.selectById(receiver.getId());
        assertTrue(updatedReceiver.getReadStatus());
        assertEquals(receiver.getUpdateTime(), updatedReceiver.getUpdateTime());
    }

    @Test
    public void testGetAnnouncementReceiverListMap() {
        // mock 数据
        announcementReceiverMapper.insert(buildReceiverDO(1L, 10L, false));
        announcementReceiverMapper.insert(buildReceiverDO(1L, 11L, true));
        announcementReceiverMapper.insert(buildReceiverDO(2L, 12L, false));

        // 调用
        Map<Long, List<OaAnnouncementReceiverDO>> receiverListMap =
                announcementService.getAnnouncementReceiverListMap(Collections.singletonList(1L));

        // 断言
        assertEquals(1, receiverListMap.size());
        assertEquals(2, receiverListMap.get(1L).size());
    }

    @Test
    public void testGetAnnouncementReceiverListMap_empty() {

        // 调用
        Map<Long, List<OaAnnouncementReceiverDO>> receiverListMap =
                announcementService.getAnnouncementReceiverListMap(Collections.emptyList());

        // 断言
        assertTrue(CollUtil.isEmpty(receiverListMap));
    }

    @Test
    public void testForwardAnnouncement_alreadyForwarded() {
        // mock 数据
        Long userId = randomLongId();
        Long existingSubordinateUserId = randomLongId();
        Long newSubordinateUserId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), userId, false));
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), existingSubordinateUserId, false));
        when(adminUserApi.getUserListBySubordinate(userId)).thenReturn(Arrays.asList(
                new AdminUserRespDTO().setId(existingSubordinateUserId),
                new AdminUserRespDTO().setId(newSubordinateUserId)));

        // 调用，并断言任一下属已接收时拒绝转发，也不补发给新增下属
        assertServiceException(() -> announcementService.forwardAnnouncement(announcement.getId(), userId),
                ANNOUNCEMENT_ALREADY_FORWARDED);
        assertEquals(2, announcementReceiverMapper.selectListByAnnouncementId(announcement.getId()).size());
        verifyNoInteractions(notifyMessageSendApi);
    }

    @Test
    public void testForwardAnnouncement_successThenRejectRepeat() {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), userId, false));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(userId)).thenReturn(Arrays.asList(
                new AdminUserRespDTO().setId(randomLongId()),
                new AdminUserRespDTO().setId(randomLongId())));

        // 调用
        assertEquals(2, announcementService.forwardAnnouncement(announcement.getId(), userId));

        // 断言重复请求被拒绝，接收关系没有增加
        assertServiceException(() -> announcementService.forwardAnnouncement(announcement.getId(), userId),
                ANNOUNCEMENT_ALREADY_FORWARDED);
        assertEquals(3, announcementReceiverMapper.selectListByAnnouncementId(announcement.getId()).size());
        verifyNoInteractions(notifyMessageSendApi);
    }

    @Test
    public void testForwardAnnouncement_concurrentRequests() throws Exception {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), userId, false));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(userId)).thenReturn(Collections.singletonList(
                new AdminUserRespDTO().setId(randomLongId())));

        // 调用：两个独立线程同时转发同一公告
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        try {
            Callable<Integer> forwardTask = () -> {
                startLatch.await();
                try {
                    return announcementService.forwardAnnouncement(announcement.getId(), userId);
                } catch (ServiceException ex) {
                    assertEquals(ANNOUNCEMENT_ALREADY_FORWARDED.getCode(), ex.getCode());
                    return 0;
                }
            };
            Future<Integer> firstResult = executor.submit(forwardTask);
            Future<Integer> secondResult = executor.submit(forwardTask);
            startLatch.countDown();

            // 断言仅一个请求成功，另一个请求返回已转发业务错误
            assertEquals(1, firstResult.get(10, TimeUnit.SECONDS) + secondResult.get(10, TimeUnit.SECONDS));
            assertEquals(2, announcementReceiverMapper.selectListByAnnouncementId(announcement.getId()).size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void testForwardAnnouncement_noSubordinates() {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);
        announcementReceiverMapper.insert(buildReceiverDO(announcement.getId(), userId, false));
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(userId)).thenReturn(Collections.emptyList());

        // 调用，并断言没有新增接收关系
        assertEquals(0, announcementService.forwardAnnouncement(announcement.getId(), userId));
        assertEquals(1, announcementReceiverMapper.selectListByAnnouncementId(announcement.getId()).size());
        verifyNoInteractions(notifyMessageSendApi);
    }

    @Test
    public void testForwardAnnouncement_notReceiver() {
        // mock 数据
        OaAnnouncementDO announcement = buildAnnouncementDO(randomLongId());
        announcementMapper.insert(announcement);

        // 调用，并断言异常
        assertServiceException(() -> announcementService.forwardAnnouncement(announcement.getId(), randomLongId()),
                ANNOUNCEMENT_RECEIVER_NOT_EXISTS);
    }

    @Test
    public void testGetReceivedAnnouncementPage_filterReadStatusAndDefaultOrder() {
        // mock 数据
        Long userId = randomLongId();
        OaAnnouncementDO readAnnouncement = buildAnnouncementDO(randomLongId()).setTitle("已读公告")
                .setPriority(OaPriorityEnum.URGENT.getPriority()).setTop(false);
        readAnnouncement.setCreateTime(LocalDateTime.now().minusDays(2));
        announcementMapper.insert(readAnnouncement);
        announcementReceiverMapper.insert(buildReceiverDO(readAnnouncement.getId(), userId, true));
        OaAnnouncementDO unreadAnnouncement = buildAnnouncementDO(randomLongId()).setTitle("未读置顶公告")
                .setTop(true);
        unreadAnnouncement.setCreateTime(LocalDateTime.now().minusDays(1));
        announcementMapper.insert(unreadAnnouncement);
        announcementReceiverMapper.insert(buildReceiverDO(unreadAnnouncement.getId(), userId, false));

        // 调用默认排序
        PageResult<OaAnnouncementDO> allResult = announcementService.getReceivedAnnouncementPage(
                new OaAnnouncementPageReqVO(), userId);

        // 断言置顶公告优先
        assertEquals(2, allResult.getTotal());
        assertEquals(unreadAnnouncement.getId(), allResult.getList().get(0).getId());

        // 调用未读筛选，并断言
        OaAnnouncementPageReqVO pageReqVO = new OaAnnouncementPageReqVO().setReadStatus(false);
        PageResult<OaAnnouncementDO> unreadResult = announcementService
                .getReceivedAnnouncementPage(pageReqVO, userId);
        assertEquals(1, unreadResult.getTotal());
        assertEquals(unreadAnnouncement.getId(), unreadResult.getList().get(0).getId());

    }

    @Test
    public void testUpdateAnnouncement_preserveCreateTime() {
        // mock 数据
        Long userId = randomLongId();
        LocalDateTime createTime = LocalDateTime.now().minusDays(3).withNano(0);
        OaAnnouncementDO announcement = buildAnnouncementDO(userId);
        announcement.setCreateTime(createTime);
        announcementMapper.insert(announcement);

        // 准备参数
        OaAnnouncementSaveReqVO reqVO = buildAnnouncementSaveReqVO().setId(announcement.getId())
                .setTitle("修改标题但不改变发布时间");

        // 调用
        announcementService.updateAnnouncement(reqVO, userId);

        // 断言
        assertEquals(createTime, announcementMapper.selectById(announcement.getId()).getCreateTime());
        assertEquals(userId.toString(), announcementMapper.selectById(announcement.getId()).getCreator());
    }

    @Test
    public void testGetPublishedAnnouncementPage_filterCreateTime() {
        // mock 数据
        Long userId = randomLongId();
        LocalDateTime createTime = LocalDateTime.now().minusDays(3).withNano(0);
        OaAnnouncementDO announcement = buildAnnouncementDO(userId);
        announcement.setCreateTime(createTime);
        announcementMapper.insert(announcement);
        OaAnnouncementDO otherAnnouncement = buildAnnouncementDO(userId);
        otherAnnouncement.setCreateTime(createTime.plusDays(1));
        announcementMapper.insert(otherAnnouncement);
        OaAnnouncementDO otherPublisherAnnouncement = buildAnnouncementDO(randomLongId());
        otherPublisherAnnouncement.setCreateTime(createTime);
        announcementMapper.insert(otherPublisherAnnouncement);

        // 准备参数
        OaAnnouncementPageReqVO reqVO = new OaAnnouncementPageReqVO()
                .setCreateTime(new LocalDateTime[]{createTime, createTime});

        // 调用
        PageResult<OaAnnouncementDO> page = announcementService.getPublishedAnnouncementPage(reqVO, userId);

        // 断言
        assertEquals(Collections.singletonList(announcement.getId()),
                convertList(page.getList(), OaAnnouncementDO::getId));
    }

    // ========== 随机对象 ==========

    /**
     * 构造普通优先级的公告保存参数。
     *
     * @return 保存参数
     */
    private static OaAnnouncementSaveReqVO buildAnnouncementSaveReqVO() {
        return new OaAnnouncementSaveReqVO().setType(OaAnnouncementTypeEnum.ANNOUNCEMENT.getType())
                .setPriority(OaPriorityEnum.NORMAL.getPriority()).setTitle("国庆节放假通知")
                .setContent("请各部门提前安排好假期值班工作").setUrl("https://example.com/announcement")
                .setTop(false);
    }

    /**
     * 构造普通优先级、未置顶的公告。
     *
     * @param publisherUserId 发布人编号
     * @return 未入库的测试对象
     */
    private static OaAnnouncementDO buildAnnouncementDO(Long publisherUserId) {
        OaAnnouncementDO announcement = new OaAnnouncementDO()
                .setType(OaAnnouncementTypeEnum.ANNOUNCEMENT.getType())
                .setPriority(OaPriorityEnum.NORMAL.getPriority()).setTitle("国庆节放假通知")
                .setContent("请各部门提前安排好假期值班工作").setTop(false);
        announcement.setCreator(publisherUserId.toString());
        announcement.setCreateTime(LocalDateTime.now());
        return announcement;
    }

    /**
     * 构造具有指定阅读状态的公告接收关系。
     *
     * @param announcementId 公告编号
     * @param receiverUserId 接收人编号
     * @param readStatus 是否已读
     * @return 未入库的测试对象
     */
    private static OaAnnouncementReceiverDO buildReceiverDO(Long announcementId, Long receiverUserId,
                                                              Boolean readStatus) {
        return new OaAnnouncementReceiverDO().setAnnouncementId(announcementId)
                .setReceiverUserId(receiverUserId).setReadStatus(readStatus);
    }

}
