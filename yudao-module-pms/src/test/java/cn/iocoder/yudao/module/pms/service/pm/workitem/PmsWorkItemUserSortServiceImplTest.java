package cn.iocoder.yudao.module.pms.service.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.db.TenantDatabaseInterceptor;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemUserSortDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.workitem.PmsWorkItemUserSortMapper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PmsWorkItemUserSortServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({PmsWorkItemUserSortServiceImpl.class, PmsWorkItemUserSortServiceImplTest.TenantTestConfiguration.class})
public class PmsWorkItemUserSortServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemUserSortServiceImpl workItemUserSortService;
    @Resource
    private PmsWorkItemUserSortMapper workItemUserSortMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private TenantLineInnerInterceptor tenantLineInnerInterceptor;

    @BeforeEach
    public void before() {
        TenantContextHolder.setTenantId(0L);
    }

    @AfterEach
    public void after() {
        TenantContextHolder.clear();
    }

    @Test
    public void testUpdateWorkItemUserSort_repeated() {
        // 准备参数
        Long projectId = randomLongId();
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
        Long projectId = randomLongId();
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
        Long projectId = randomLongId();
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
        Long projectId = randomLongId();
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
