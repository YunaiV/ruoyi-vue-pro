package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;
import cn.iocoder.yudao.module.pms.dal.mysql.pm.project.PmsProjectFavoriteMapper;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link PmsProjectFavoriteServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsProjectFavoriteServiceImpl.class)
public class PmsProjectFavoriteServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsProjectFavoriteServiceImpl favoriteService;

    @Resource
    private PmsProjectFavoriteMapper favoriteMapper;

    @MockBean
    private PmsProjectMemberService projectMemberService;

    @Test
    public void testCreateProjectFavorite_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        when(projectMemberService.validateProjectMember(projectId, userId))
                .thenReturn(new PmsProjectDO().setId(projectId));

        // 调用
        favoriteService.createProjectFavorite(projectId, userId);

        // 断言
        assertNotNull(favoriteMapper.selectByProjectIdAndUserId(projectId, userId));
    }

    @Test
    public void testDeleteProjectFavorite_success() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        when(projectMemberService.validateProjectMember(projectId, userId))
                .thenReturn(new PmsProjectDO().setId(projectId));
        favoriteService.createProjectFavorite(projectId, userId);

        // 调用
        favoriteService.deleteProjectFavorite(projectId, userId);

        // 断言
        assertNull(favoriteMapper.selectByProjectIdAndUserId(projectId, userId));
    }

    @Test
    public void testDeleteProjectFavoriteListByProjectId() {
        // mock 数据
        Long projectId = randomLongId();
        Long userId = randomLongId();
        when(projectMemberService.validateProjectMember(projectId, userId))
                .thenReturn(new PmsProjectDO().setId(projectId));
        favoriteService.createProjectFavorite(projectId, userId);

        // 调用
        favoriteService.deleteProjectFavoriteListByProjectId(projectId);

        // 断言
        assertTrue(CollUtil.isEmpty(favoriteMapper.selectProjectIdListByUserId(userId)));
    }

}
