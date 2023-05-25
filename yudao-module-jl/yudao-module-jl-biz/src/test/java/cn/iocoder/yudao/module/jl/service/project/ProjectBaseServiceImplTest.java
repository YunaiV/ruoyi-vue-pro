package cn.iocoder.yudao.module.jl.service.project;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.annotation.Resource;

import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;

import cn.iocoder.yudao.module.jl.controller.admin.project.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;
import cn.iocoder.yudao.module.jl.dal.mysql.project.ProjectBaseMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import javax.annotation.Resource;
import org.springframework.context.annotation.Import;
import java.util.*;
import java.time.LocalDateTime;

import static cn.hutool.core.util.RandomUtil.*;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.*;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.*;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.*;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ProjectBaseServiceImpl} 的单元测试类
 *
 * @author 惟象科技
 */
@Import(ProjectBaseServiceImpl.class)
public class ProjectBaseServiceImplTest extends BaseDbUnitTest {

    @Resource
    private ProjectBaseServiceImpl projectBaseService;

    @Resource
    private ProjectBaseMapper projectBaseMapper;

    @Test
    public void testCreateProjectBase_success() {
        // 准备参数
        ProjectBaseCreateReqVO reqVO = randomPojo(ProjectBaseCreateReqVO.class);

        // 调用
        Long projectBaseId = projectBaseService.createProjectBase(reqVO);
        // 断言
        assertNotNull(projectBaseId);
        // 校验记录的属性是否正确
        ProjectBaseDO projectBase = projectBaseMapper.selectById(projectBaseId);
        assertPojoEquals(reqVO, projectBase);
    }

    @Test
    public void testUpdateProjectBase_success() {
        // mock 数据
        ProjectBaseDO dbProjectBase = randomPojo(ProjectBaseDO.class);
        projectBaseMapper.insert(dbProjectBase);// @Sql: 先插入出一条存在的数据
        // 准备参数
        ProjectBaseUpdateReqVO reqVO = randomPojo(ProjectBaseUpdateReqVO.class, o -> {
            o.setId(dbProjectBase.getId()); // 设置更新的 ID
        });

        // 调用
        projectBaseService.updateProjectBase(reqVO);
        // 校验是否更新正确
        ProjectBaseDO projectBase = projectBaseMapper.selectById(reqVO.getId()); // 获取最新的
        assertPojoEquals(reqVO, projectBase);
    }

    @Test
    public void testUpdateProjectBase_notExists() {
        // 准备参数
        ProjectBaseUpdateReqVO reqVO = randomPojo(ProjectBaseUpdateReqVO.class);

        // 调用, 并断言异常
        assertServiceException(() -> projectBaseService.updateProjectBase(reqVO), PROJECT_BASE_NOT_EXISTS);
    }

    @Test
    public void testDeleteProjectBase_success() {
        // mock 数据
        ProjectBaseDO dbProjectBase = randomPojo(ProjectBaseDO.class);
        projectBaseMapper.insert(dbProjectBase);// @Sql: 先插入出一条存在的数据
        // 准备参数
        Long id = dbProjectBase.getId();

        // 调用
        projectBaseService.deleteProjectBase(id);
       // 校验数据不存在了
       assertNull(projectBaseMapper.selectById(id));
    }

    @Test
    public void testDeleteProjectBase_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用, 并断言异常
        assertServiceException(() -> projectBaseService.deleteProjectBase(id), PROJECT_BASE_NOT_EXISTS);
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProjectBasePage() {
       // mock 数据
       ProjectBaseDO dbProjectBase = randomPojo(ProjectBaseDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setName(null);
           o.setStage(null);
           o.setStatus(null);
           o.setType(null);
           o.setStartDate(null);
           o.setEndDate(null);
           o.setManagerId(null);
           o.setParticipants(null);
       });
       projectBaseMapper.insert(dbProjectBase);
       // 测试 createTime 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setSalesleadId(null)));
       // 测试 name 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setName(null)));
       // 测试 stage 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setStage(null)));
       // 测试 status 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setStatus(null)));
       // 测试 type 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setType(null)));
       // 测试 startDate 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setStartDate(null)));
       // 测试 endDate 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setEndDate(null)));
       // 测试 managerId 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setManagerId(null)));
       // 测试 participants 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setParticipants(null)));
       // 准备参数
       ProjectBasePageReqVO reqVO = new ProjectBasePageReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setName(null);
       reqVO.setStage(null);
       reqVO.setStatus(null);
       reqVO.setType(null);
       reqVO.setStartDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setManagerId(null);
       reqVO.setParticipants(null);

       // 调用
       PageResult<ProjectBaseDO> pageResult = projectBaseService.getProjectBasePage(reqVO);
       // 断言
       assertEquals(1, pageResult.getTotal());
       assertEquals(1, pageResult.getList().size());
       assertPojoEquals(dbProjectBase, pageResult.getList().get(0));
    }

    @Test
    @Disabled  // TODO 请修改 null 为需要的值，然后删除 @Disabled 注解
    public void testGetProjectBaseList() {
       // mock 数据
       ProjectBaseDO dbProjectBase = randomPojo(ProjectBaseDO.class, o -> { // 等会查询到
           o.setCreateTime(null);
           o.setSalesleadId(null);
           o.setName(null);
           o.setStage(null);
           o.setStatus(null);
           o.setType(null);
           o.setStartDate(null);
           o.setEndDate(null);
           o.setManagerId(null);
           o.setParticipants(null);
       });
       projectBaseMapper.insert(dbProjectBase);
       // 测试 createTime 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setCreateTime(null)));
       // 测试 salesleadId 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setSalesleadId(null)));
       // 测试 name 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setName(null)));
       // 测试 stage 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setStage(null)));
       // 测试 status 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setStatus(null)));
       // 测试 type 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setType(null)));
       // 测试 startDate 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setStartDate(null)));
       // 测试 endDate 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setEndDate(null)));
       // 测试 managerId 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setManagerId(null)));
       // 测试 participants 不匹配
       projectBaseMapper.insert(cloneIgnoreId(dbProjectBase, o -> o.setParticipants(null)));
       // 准备参数
       ProjectBaseExportReqVO reqVO = new ProjectBaseExportReqVO();
       reqVO.setCreateTime(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setSalesleadId(null);
       reqVO.setName(null);
       reqVO.setStage(null);
       reqVO.setStatus(null);
       reqVO.setType(null);
       reqVO.setStartDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setEndDate(buildBetweenTime(2023, 2, 1, 2023, 2, 28));
       reqVO.setManagerId(null);
       reqVO.setParticipants(null);

       // 调用
       List<ProjectBaseDO> list = projectBaseService.getProjectBaseList(reqVO);
       // 断言
       assertEquals(1, list.size());
       assertPojoEquals(dbProjectBase, list.get(0));
    }

}
