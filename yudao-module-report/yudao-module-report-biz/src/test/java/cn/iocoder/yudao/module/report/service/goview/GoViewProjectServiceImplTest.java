package cn.iocoder.yudao.module.report.service.goview;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.report.controller.admin.goview.vo.project.GoViewProjectCreateReqVO;
import cn.iocoder.yudao.module.report.controller.admin.goview.vo.project.GoViewProjectUpdateReqVO;
import cn.iocoder.yudao.module.report.dal.dataobject.goview.GoViewProjectDO;
import cn.iocoder.yudao.module.report.dal.mysql.goview.GoViewProjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import jakarta.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.GO_VIEW_PROJECT_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link GoViewProjectServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(GoViewProjectServiceImpl.class)
public class GoViewProjectServiceImplTest extends BaseDbUnitTest {

    @Resource
    private GoViewProjectServiceImpl goViewProjectService;

    @Resource
    private GoViewProjectMapper goViewProjectMapper;

    @Test
    public void testCreateProject_success() {
        // 准备参数
        GoViewProjectCreateReqVO reqVO = randomPojo(GoViewProjectCreateReqVO.class);

        // 调用
        Long goViewProjectId = goViewProjectService.createProject(reqVO);
        // 断言
        assertNotNull(goViewProjectId);
        // 校验记录的属性是否正确
        GoViewProjectDO goViewProject = goViewProjectMapper.selectById(goViewProjectId);
        assertPojoEquals(reqVO, goViewProject);
    }

    @Test
    public void testUpdateProject_success() {
        // mock 数据
        GoViewProjectDO dbGoViewProject = randomPojo(GoViewProjectDO.class);
        goViewProjectMapper.insert(dbGoViewProject);// @Sql: 先插入出一条存在的数据
        // 准备参数
        GoViewProjectUpdateReqVO reqVO = randomPojo(GoViewProjectUpdateReqVO.class, o -> {
            o.setId(dbGoViewProject.getId()); // 设置更新的 ID
            o.setStatus(randomCommonStatus());
        });

        // 调用
        goViewProjectService.updateProject(reqVO);
        // 校验是否更新正确
        GoViewProjectDO goViewProject = goViewProjectMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, goViewProject);
    }

    @Test
    public void testUpdateProject_notExists() {
        // 准备参数
        GoViewProjectUpdateReqVO reqVO = randomPojo(GoViewProjectUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> goViewProjectService.updateProject(reqVO), GO_VIEW_PROJECT_NOT_EXISTS);
    }

    @Test
    public void testDeleteProject_success() {
        // mock 数据
        GoViewProjectDO dbGoViewProject = randomPojo(GoViewProjectDO.class);
        goViewProjectMapper.insert(dbGoViewProject);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbGoViewProject.getId();

        // 调用
        goViewProjectService.deleteProject(id);
        // 校验数据不存在了
        assertNull(goViewProjectMapper.selectById(id));
    }

    @Test
    public void testDeleteProject_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> goViewProjectService.deleteProject(id), GO_VIEW_PROJECT_NOT_EXISTS);
    }

    @Test
    public void testGetProject() {
        // mock 数据
        GoViewProjectDO dbGoViewProject = randomPojo(GoViewProjectDO.class);
        goViewProjectMapper.insert(dbGoViewProject);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbGoViewProject.getId();

        // 调用
        GoViewProjectDO goViewProject = goViewProjectService.getProject(id);
        // 断言
        assertPojoEquals(dbGoViewProject, goViewProject);
    }

    @Test
    public void testGetMyGoViewProjectPage() {
        // mock 数据
        GoViewProjectDO dbGoViewProject = randomPojo(GoViewProjectDO.class, o -> { // 等会查询到
            o.setCreator("1");
        });
        goViewProjectMapper.insert(dbGoViewProject);
        // 测试 userId 不匹配
        goViewProjectMapper.insert(cloneIgnoreId(dbGoViewProject, o -> o.setCreator("2")));
        // 准备参数
        PageParam reqVO = new PageParam();
        Long userId = 1L;

        // 调用
        PageResult<GoViewProjectDO> pageResult = goViewProjectService.getMyProjectPage(reqVO, userId);
        // 断言
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbGoViewProject, pageResult.getList().get(0));
    }

}
