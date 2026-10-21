package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemUserSortDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectMapper;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemUserSortMapper;
import cn.iocoder.yudao.module.pms.enums.pm.project.PmsProjectStatusEnum;
import cn.iocoder.yudao.module.pms.service.pm.iteration.PmsIterationService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectAnnouncementService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectFavoriteService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectGroupService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectMemberService;
import cn.iocoder.yudao.module.pms.service.pm.project.PmsProjectServiceImpl;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PmsWorkItemUserSortServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PmsWorkItemUserSortServiceImpl.class, PmsProjectServiceImpl.class})
public class PmsWorkItemUserSortServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemUserSortServiceImpl workItemUserSortService;

    @Resource
    private PmsWorkItemUserSortMapper workItemUserSortMapper;
    @Resource
    private PmsProjectMapper projectMapper;

    @MockitoBean
    private PmsProjectMemberService projectMemberService;
    @MockitoBean
    private PmsProjectGroupService projectGroupService;
    @MockitoBean
    private PmsProjectFavoriteService projectFavoriteService;
    @MockitoBean
    private PmsProjectAnnouncementService projectAnnouncementService;
    @MockitoBean
    private PmsIterationService iterationService;
    @MockitoBean
    private PmsWorkItemService workItemService;
    @MockitoBean
    private PmsWorkItemStatusService workItemStatusService;
    @MockitoBean
    private PmsWorkItemWorkLogService workItemWorkLogService;
    @MockitoBean
    private PermissionApi permissionApi;

    @Test
    public void testUpdateWorkItemUserSort_success() {
        // mock 数据
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1);
        projectMapper.insert(project);
        workItemUserSortService.updateWorkItemUserSort(project.getId(), List.of(50L, 51L), 238L);
        workItemUserSortService.updateWorkItemUserSort(project.getId(), List.of(50L, 51L), 239L);

        // 调用：连续调整个人排序，并移除不再位于待规划区域的工作项
        workItemUserSortService.updateWorkItemUserSort(project.getId(), List.of(51L, 50L), 238L);
        workItemUserSortService.updateWorkItemUserSort(project.getId(), List.of(50L), 238L);

        // 断言：仅保留当前顺序，不影响其他用户的个人排序
        List<PmsWorkItemUserSortDO> userSorts = workItemUserSortMapper
                .selectListByProjectIdAndUserId(project.getId(), 238L);
        assertEquals(List.of(50L), convertList(userSorts, PmsWorkItemUserSortDO::getWorkItemId));
        assertEquals(1, userSorts.get(0).getSort());
        assertEquals(List.of(50L, 51L), convertList(workItemUserSortMapper
                .selectListByProjectIdAndUserId(project.getId(), 239L), PmsWorkItemUserSortDO::getWorkItemId));
    }

    @Test
    public void testUpdateWorkItemUserSort_projectNotExists() {
        // 调用，并断言异常
        assertServiceException(() -> workItemUserSortService.updateWorkItemUserSort(999L,
                List.of(50L), 238L), PROJECT_NOT_EXISTS);
        assertEquals(0L, workItemUserSortMapper.selectCount());
    }

    @Test
    public void testUpdateWorkItemUserSort_projectDeleted() {
        // mock 数据
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1);
        projectMapper.insert(project);
        projectMapper.deleteById(project.getId());

        // 调用，并断言异常
        assertServiceException(() -> workItemUserSortService.updateWorkItemUserSort(project.getId(),
                List.of(50L), 238L), PROJECT_NOT_EXISTS);
        assertEquals(0L, workItemUserSortMapper.selectCount());
    }

    @Test
    public void testUpdateWorkItemUserSort_projectStatusInvalid() {
        // mock 数据
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1)
                .setStatus(PmsProjectStatusEnum.RECYCLED.getStatus());
        projectMapper.insert(project);
        workItemUserSortMapper.insert(new PmsWorkItemUserSortDO().setProjectId(project.getId())
                .setWorkItemId(50L).setUserId(238L).setSort(1));

        // 调用，并断言异常
        assertServiceException(() -> workItemUserSortService.updateWorkItemUserSort(project.getId(),
                List.of(51L), 238L), PROJECT_STATUS_INVALID);
        // 断言：拒绝更新时保留原有个人排序
        assertEquals(List.of(50L), convertList(workItemUserSortMapper
                .selectListByProjectIdAndUserId(project.getId(), 238L), PmsWorkItemUserSortDO::getWorkItemId));
    }

    @Test
    public void testUpdateWorkItemUserSort_concurrent() throws Exception {
        // mock 数据
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1);
        projectMapper.insert(project);
        // 准备参数
        CountDownLatch startLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<?>> futures = new ArrayList<>();

        // 调用：同一项目、同一用户并发保存个人排序
        try {
            for (int index = 0; index < 4; index++) {
                futures.add(executor.submit(() -> {
                    startLatch.await();
                    for (int count = 0; count < 5; count++) {
                        workItemUserSortService.updateWorkItemUserSort(project.getId(), List.of(51L, 50L), 238L);
                    }
                    return null;
                }));
            }
            startLatch.countDown();
            for (Future<?> future : futures) {
                future.get(20, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }

        // 断言：没有重复的有效记录，顺序与最后一次保存一致
        List<PmsWorkItemUserSortDO> userSorts = workItemUserSortMapper
                .selectListByProjectIdAndUserId(project.getId(), 238L);
        assertEquals(List.of(51L, 50L), convertList(userSorts, PmsWorkItemUserSortDO::getWorkItemId));
        assertEquals(List.of(1, 2), convertList(userSorts, PmsWorkItemUserSortDO::getSort));
    }

}
