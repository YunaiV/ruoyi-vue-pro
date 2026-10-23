package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;
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
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import javax.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.pms.enums.ErrorCodeConstants.PROJECT_STATUS_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PmsWorkItemUserSortServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PmsWorkItemUserSortServiceImpl.class, PmsProjectServiceImpl.class,
        PmsWorkItemUserSortServiceImplTest.TenantTestConfiguration.class})
public class PmsWorkItemUserSortServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemUserSortServiceImpl workItemUserSortService;

    @Resource
    private PmsWorkItemUserSortMapper workItemUserSortMapper;
    @Resource
    private PmsProjectMapper projectMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private TenantLineInnerInterceptor tenantLineInnerInterceptor;

    @MockBean
    private PmsProjectMemberService projectMemberService;
    @MockBean
    private PmsProjectGroupService projectGroupService;
    @MockBean
    private PmsProjectFavoriteService projectFavoriteService;
    @MockBean
    private PmsProjectAnnouncementService projectAnnouncementService;
    @MockBean
    private PmsIterationService iterationService;
    @MockBean
    private PmsWorkItemService workItemService;
    @MockBean
    private PmsWorkItemStatusService workItemStatusService;
    @MockBean
    private PmsWorkItemWorkLogService workItemWorkLogService;
    @MockBean
    private PermissionApi permissionApi;

    @BeforeEach
    public void before() {
        TenantContextHolder.setTenantId(0L);
    }

    @AfterEach
    public void after() {
        TenantContextHolder.clear();
    }

    @Test
    public void testUpdateWorkItemUserSort_success() {
        // mock 数据
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1);
        projectMapper.insert(project);
        workItemUserSortService.updateWorkItemUserSort(project.getId(), java.util.Arrays.asList(50L, 51L), 238L);
        workItemUserSortService.updateWorkItemUserSort(project.getId(), java.util.Arrays.asList(50L, 51L), 239L);

        // 调用：连续调整个人排序，并移除不再位于待规划区域的工作项
        workItemUserSortService.updateWorkItemUserSort(project.getId(), java.util.Arrays.asList(51L, 50L), 238L);
        workItemUserSortService.updateWorkItemUserSort(project.getId(), java.util.Arrays.asList(50L), 238L);

        // 断言：仅保留当前顺序，不影响其他用户的个人排序
        List<PmsWorkItemUserSortDO> userSorts = workItemUserSortMapper
                .selectListByProjectIdAndUserId(project.getId(), 238L);
        assertEquals(java.util.Arrays.asList(50L), convertList(userSorts, PmsWorkItemUserSortDO::getWorkItemId));
        assertEquals(1, userSorts.get(0).getSort());
        assertEquals(java.util.Arrays.asList(50L, 51L), convertList(workItemUserSortMapper
                .selectListByProjectIdAndUserId(project.getId(), 239L), PmsWorkItemUserSortDO::getWorkItemId));
    }

    @Test
    public void testUpdateWorkItemUserSort_repeated() {
        // mock 数据
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1);
        projectMapper.insert(project);
        // 准备参数
        Long projectId = project.getId();
        Long userId = randomLongId();
        Long firstId = randomLongId();
        Long secondId = randomLongId();

        // 调用：连续保存三次，覆盖同一排序记录重复删除的情况
        workItemUserSortService.updateWorkItemUserSort(projectId, Arrays.asList(firstId, secondId), userId);
        workItemUserSortService.updateWorkItemUserSort(projectId, Arrays.asList(secondId, firstId), userId);
        workItemUserSortService.updateWorkItemUserSort(projectId, Arrays.asList(firstId, secondId), userId);

        // 断言
        List<PmsWorkItemUserSortDO> userSorts = workItemUserSortMapper.selectListByProjectIdAndUserId(projectId, userId);
        assertEquals(Arrays.asList(firstId, secondId), convertList(userSorts, PmsWorkItemUserSortDO::getWorkItemId));
        assertEquals(Arrays.asList(1, 2), convertList(userSorts, PmsWorkItemUserSortDO::getSort));
        assertEquals(2, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pms_work_item_user_sort", Integer.class));
    }

    @Test
    public void testUpdateWorkItemUserSort_deletedRecords() {
        // mock 数据
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1);
        projectMapper.insert(project);
        Long projectId = project.getId();
        Long userId = randomLongId();
        Long workItemId = randomLongId();
        insertUserSort(projectId, workItemId, userId, false);
        insertUserSort(projectId, workItemId, userId, true);
        PmsWorkItemUserSortDO otherUser = insertUserSort(projectId, workItemId, randomLongId(), false);
        PmsWorkItemUserSortDO otherProject = insertUserSort(randomLongId(), workItemId, userId, false);

        // 调用
        workItemUserSortService.updateWorkItemUserSort(projectId, Collections.singletonList(workItemId), userId);

        // 断言：历史记录被清理，其他用户和项目的排序不受影响
        assertEquals(1, workItemUserSortMapper.selectListByProjectIdAndUserId(projectId, userId).size());
        assertPojoEquals(otherUser, workItemUserSortMapper.selectById(otherUser.getId()), "createTime", "updateTime");
        assertPojoEquals(otherProject, workItemUserSortMapper.selectById(otherProject.getId()), "createTime", "updateTime");
        assertEquals(3, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pms_work_item_user_sort", Integer.class));
    }

    @Test
    public void testUpdateWorkItemUserSort_empty() {
        // mock 数据
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1);
        projectMapper.insert(project);
        Long projectId = project.getId();
        Long userId = randomLongId();
        Long workItemId = randomLongId();
        insertUserSort(projectId, workItemId, userId, false);
        insertUserSort(projectId, workItemId, userId, true);

        // 调用
        workItemUserSortService.updateWorkItemUserSort(projectId, Collections.emptyList(), userId);

        // 断言
        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pms_work_item_user_sort", Integer.class));
    }

    @Test
    public void testUpdateWorkItemUserSort_projectNotExists() {
        // 调用，并断言异常
        assertServiceException(() -> workItemUserSortService.updateWorkItemUserSort(999L,
                java.util.Arrays.asList(50L), 238L), PROJECT_NOT_EXISTS);
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
                java.util.Arrays.asList(50L), 238L), PROJECT_NOT_EXISTS);
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
                java.util.Arrays.asList(51L), 238L), PROJECT_STATUS_INVALID);
        // 断言：拒绝更新时保留原有个人排序
        assertEquals(java.util.Arrays.asList(50L), convertList(workItemUserSortMapper
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
                    // 工作线程使用与测试线程相同的租户，结束后清理，避免租户上下文残留
                    TenantContextHolder.setTenantId(0L);
                    try {
                        startLatch.await();
                        for (int count = 0; count < 5; count++) {
                            workItemUserSortService.updateWorkItemUserSort(project.getId(), java.util.Arrays.asList(51L, 50L), 238L);
                        }
                        return null;
                    } finally {
                        TenantContextHolder.clear();
                    }
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
        assertEquals(java.util.Arrays.asList(51L, 50L), convertList(userSorts, PmsWorkItemUserSortDO::getWorkItemId));
        assertEquals(java.util.Arrays.asList(1, 2), convertList(userSorts, PmsWorkItemUserSortDO::getSort));
    }

    @Test
    public void testDeleteWorkItemUserSortByWorkItemId_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        Long workItemId = randomLongId();
        insertUserSort(projectId, workItemId, userId, false);
        insertUserSort(projectId, workItemId, userId, true);
        PmsWorkItemUserSortDO other = insertUserSort(projectId, randomLongId(), userId, false);

        // 调用
        workItemUserSortService.deleteWorkItemUserSortByWorkItemId(workItemId);

        // 断言
        assertPojoEquals(other, workItemUserSortMapper.selectById(other.getId()), "createTime", "updateTime");
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pms_work_item_user_sort", Integer.class));
    }

    @Test
    public void testDeleteWorkItemUserSortByProjectId_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        Long workItemId = randomLongId();
        insertUserSort(projectId, workItemId, userId, false);
        insertUserSort(projectId, workItemId, userId, true);
        PmsWorkItemUserSortDO other = insertUserSort(randomLongId(), workItemId, userId, false);

        // 调用
        workItemUserSortService.deleteWorkItemUserSortByProjectId(projectId);

        // 断言
        assertPojoEquals(other, workItemUserSortMapper.selectById(other.getId()), "createTime", "updateTime");
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pms_work_item_user_sort", Integer.class));
    }

    @Test
    public void testDeleteWorkItemUserSort_tenantIsolation() {
        // mock 数据：两个租户使用相同的业务编号
        PmsProjectDO project = new PmsProjectDO().setName("测试项目").setType(1).setLevel(1);
        projectMapper.insert(project);
        Long projectId = project.getId();
        Long userId = randomLongId();
        Long workItemId = randomLongId();
        insertUserSort(projectId, workItemId, userId, false);
        TenantContextHolder.setTenantId(1L);
        insertUserSort(projectId, workItemId, userId, false);
        TenantContextHolder.setTenantId(0L);

        // 调用，并断言：三个清理入口均保留其他租户的记录
        workItemUserSortService.updateWorkItemUserSort(projectId, Collections.emptyList(), userId);
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pms_work_item_user_sort", Integer.class));
        insertUserSort(projectId, workItemId, userId, false);
        workItemUserSortService.deleteWorkItemUserSortByWorkItemId(workItemId);
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pms_work_item_user_sort", Integer.class));
        insertUserSort(projectId, workItemId, userId, false);
        workItemUserSortService.deleteWorkItemUserSortByProjectId(projectId);
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM pms_work_item_user_sort", Integer.class));
        assertEquals(1L, jdbcTemplate.queryForObject("SELECT tenant_id FROM pms_work_item_user_sort", Long.class));
    }

    private PmsWorkItemUserSortDO insertUserSort(Long projectId, Long workItemId, Long userId, boolean deleted) {
        PmsWorkItemUserSortDO userSort = new PmsWorkItemUserSortDO().setProjectId(projectId)
                .setWorkItemId(workItemId).setUserId(userId).setSort(1);
        userSort.setDeleted(deleted);
        workItemUserSortMapper.insert(userSort);
        return userSort;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TenantTestConfiguration {

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public TenantLineInnerInterceptor tenantLineInnerInterceptor(MybatisPlusInterceptor interceptor) {
            TenantLineInnerInterceptor inner = new TenantLineInnerInterceptor(
                    new TenantDatabaseInterceptor(new TenantProperties()));
            MyBatisUtils.addInterceptor(interceptor, inner, 0);
            return inner;
        }

    }

}
