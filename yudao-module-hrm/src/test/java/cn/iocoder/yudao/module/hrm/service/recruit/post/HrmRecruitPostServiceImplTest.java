package cn.iocoder.yudao.module.hrm.service.recruit.post;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.recruit.post.HrmRecruitPostMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.post.HrmRecruitPostStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDateTimeRange;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_POST_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_POST_TYPE_NOT_EXISTS;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.DEPT_NOT_FOUND;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link HrmRecruitPostServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmRecruitPostServiceImpl.class)
public class HrmRecruitPostServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmRecruitPostServiceImpl recruitPostService;

    @Resource
    private HrmRecruitPostMapper recruitPostMapper;

    @MockitoBean
    private HrmRecruitPostTypeService recruitPostTypeService;
    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private DeptApi deptApi;

    @Test
    public void testCreateRecruitPost_success() {
        // 准备参数
        HrmRecruitPostSaveReqVO reqVO = randomRecruitPostSaveReqVO(o -> o
                .setDeptId(100L).setOwnerEmployeeId(200L)
                .setInterviewEmployeeIds(asList(201L, 202L)));

        // 调用
        Long postId = recruitPostService.createRecruitPost(reqVO);

        // 断言
        assertNotNull(postId);
        HrmRecruitPostDO recruitPost = recruitPostMapper.selectById(postId);
        assertPojoEquals(reqVO, recruitPost, "id", "status");
        assertEquals(HrmRecruitPostStatusEnum.RECRUITING.getStatus(), recruitPost.getStatus());
        verify(recruitPostTypeService).validateRecruitPostTypeExists(reqVO.getPostTypeId());
        verify(deptApi).validateDeptList(singleton(reqVO.getDeptId()));
        verify(employeeService).validateEmployeeListByEntryStatus(
                new HashSet<>(asList(reqVO.getOwnerEmployeeId(), 201L, 202L)),
                HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
    }

    @Test
    public void testCreateRecruitPost_postTypeNotExists() {
        // 准备参数
        HrmRecruitPostSaveReqVO reqVO = randomRecruitPostSaveReqVO();
        // mock 方法
        doThrow(exception(RECRUIT_POST_TYPE_NOT_EXISTS)).when(recruitPostTypeService)
                .validateRecruitPostTypeExists(reqVO.getPostTypeId());

        // 调用，并断言异常
        assertServiceException(() -> recruitPostService.createRecruitPost(reqVO), RECRUIT_POST_TYPE_NOT_EXISTS);
        assertEquals(0L, recruitPostMapper.selectCount());
    }

    @Test
    public void testCreateRecruitPost_employeeNotExists() {
        // 准备参数
        HrmRecruitPostSaveReqVO reqVO = randomRecruitPostSaveReqVO();
        // mock 方法
        doThrow(exception(EMPLOYEE_NOT_EXISTS)).when(employeeService)
                .validateEmployeeListByEntryStatus(anyCollection(),
                        eq(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> recruitPostService.createRecruitPost(reqVO), EMPLOYEE_NOT_EXISTS);
        assertEquals(0L, recruitPostMapper.selectCount());
    }

    @Test
    public void testCreateRecruitPost_deptNotExists() {
        // 准备参数
        HrmRecruitPostSaveReqVO reqVO = randomRecruitPostSaveReqVO();
        // mock 方法
        doThrow(exception(DEPT_NOT_FOUND)).when(deptApi)
                .validateDeptList(singleton(reqVO.getDeptId()));

        // 调用，并断言异常
        assertServiceException(() -> recruitPostService.createRecruitPost(reqVO), DEPT_NOT_FOUND);
        assertEquals(0L, recruitPostMapper.selectCount());
    }

    @Test
    public void testUpdateRecruitPost_success() {
        // mock 数据
        HrmRecruitPostDO dbRecruitPost = randomRecruitPostDO(o -> o
                .setStatus(HrmRecruitPostStatusEnum.STOPPED.getStatus()));
        recruitPostMapper.insert(dbRecruitPost);
        // 准备参数
        HrmRecruitPostSaveReqVO reqVO = randomRecruitPostSaveReqVO(o -> o.setId(dbRecruitPost.getId()));

        // 调用
        recruitPostService.updateRecruitPost(reqVO);

        // 断言
        HrmRecruitPostDO recruitPost = recruitPostMapper.selectById(dbRecruitPost.getId());
        assertPojoEquals(reqVO, recruitPost);
        assertEquals(HrmRecruitPostStatusEnum.STOPPED.getStatus(), recruitPost.getStatus());
        verify(recruitPostTypeService).validateRecruitPostTypeExists(reqVO.getPostTypeId());
    }

    @Test
    public void testUpdateRecruitPost_clearOptionalFields() {
        // mock 数据
        HrmRecruitPostDO dbRecruitPost = randomRecruitPostDO();
        recruitPostMapper.insert(dbRecruitPost);
        // 准备参数
        HrmRecruitPostSaveReqVO reqVO = randomRecruitPostSaveReqVO(o -> o.setId(dbRecruitPost.getId())
                .setDeptId(null).setAreaId(null).setRecruitNum(null).setReason(null)
                .setWorkTime(null).setEducationRequire(null).setMinSalary(null).setMaxSalary(null)
                .setSalaryUnit(null).setMinAge(null).setMaxAge(null).setLatestEntryTime(null)
                .setOwnerEmployeeId(null).setInterviewEmployeeIds(null).setDescription(null)
                .setEmergencyLevel(null).setPostTypeId(null));

        // 调用
        recruitPostService.updateRecruitPost(reqVO);

        // 断言
        HrmRecruitPostDO recruitPost = recruitPostMapper.selectById(dbRecruitPost.getId());
        assertNull(recruitPost.getDeptId());
        assertNull(recruitPost.getAreaId());
        assertNull(recruitPost.getRecruitNum());
        assertNull(recruitPost.getLatestEntryTime());
        assertNull(recruitPost.getOwnerEmployeeId());
        assertNull(recruitPost.getInterviewEmployeeIds());
        assertNull(recruitPost.getPostTypeId());
        assertEquals(dbRecruitPost.getStatus(), recruitPost.getStatus());
    }

    @Test
    public void testUpdateRecruitPost_notExists() {
        // 准备参数
        HrmRecruitPostSaveReqVO reqVO = randomRecruitPostSaveReqVO(o -> o.setId(randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> recruitPostService.updateRecruitPost(reqVO), RECRUIT_POST_NOT_EXISTS);
        verifyNoInteractions(recruitPostTypeService);
    }

    @Test
    public void testGetRecruitPost() {
        // mock 数据
        HrmRecruitPostDO dbRecruitPost = randomRecruitPostDO();
        recruitPostMapper.insert(dbRecruitPost);

        // 调用
        HrmRecruitPostDO recruitPost = recruitPostService.getRecruitPost(dbRecruitPost.getId());

        // 断言
        assertPojoEquals(dbRecruitPost, recruitPost);
    }

    @Test
    public void testValidateRecruitPostExists_success() {
        // mock 数据
        HrmRecruitPostDO dbRecruitPost = randomRecruitPostDO();
        recruitPostMapper.insert(dbRecruitPost);

        // 调用
        HrmRecruitPostDO recruitPost = recruitPostService.validateRecruitPostExists(dbRecruitPost.getId());

        // 断言
        assertPojoEquals(dbRecruitPost, recruitPost);
    }

    @Test
    public void testValidateRecruitPostExists_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> recruitPostService.validateRecruitPostExists(id), RECRUIT_POST_NOT_EXISTS);
    }

    @Test
    public void testGetRecruitPostList() {
        // mock 数据
        HrmRecruitPostDO firstPost = randomRecruitPostDO();
        recruitPostMapper.insert(firstPost);
        HrmRecruitPostDO secondPost = randomRecruitPostDO();
        recruitPostMapper.insert(secondPost);
        // 测试 id 不匹配
        recruitPostMapper.insert(randomRecruitPostDO());
        // 准备参数
        List<Long> ids = asList(firstPost.getId(), secondPost.getId());

        // 调用
        List<HrmRecruitPostDO> posts = recruitPostService.getRecruitPostList(ids);

        // 断言
        assertEquals(2, posts.size());
        assertEquals(new HashSet<>(ids), convertSet(posts, HrmRecruitPostDO::getId));
    }

    @Test
    public void testGetRecruitPostList_empty() {
        // 调用
        List<HrmRecruitPostDO> posts = recruitPostService.getRecruitPostList(emptyList());

        // 断言
        assertTrue(posts.isEmpty());
    }

    @Test
    public void testGetRecruitPostPage() {
        // mock 数据
        HrmRecruitPostDO dbRecruitPost = randomRecruitPostDO(o -> o.setPostName("Java 开发工程师")
                .setJobNature(1).setAreaId(310100).setDeptId(100L).setOwnerEmployeeId(200L)
                .setPostTypeId(300L).setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus())
                .setCreateTime(buildTime(2026, 1, 20).plusHours(12)));
        recruitPostMapper.insert(dbRecruitPost);
        // 测试 postName 不匹配
        recruitPostMapper.insert(cloneIgnoreId(dbRecruitPost, o -> o.setPostName("产品经理")));
        // 测试 jobNature 不匹配
        recruitPostMapper.insert(cloneIgnoreId(dbRecruitPost, o -> o.setJobNature(2)));
        // 测试 areaId 不匹配
        recruitPostMapper.insert(cloneIgnoreId(dbRecruitPost, o -> o.setAreaId(110100)));
        // 测试 deptId 不匹配
        recruitPostMapper.insert(cloneIgnoreId(dbRecruitPost, o -> o.setDeptId(101L)));
        // 测试 ownerEmployeeId 不匹配
        recruitPostMapper.insert(cloneIgnoreId(dbRecruitPost, o -> o.setOwnerEmployeeId(201L)));
        // 测试 postTypeId 不匹配
        recruitPostMapper.insert(cloneIgnoreId(dbRecruitPost, o -> o.setPostTypeId(301L)));
        // 测试 status 不匹配
        recruitPostMapper.insert(cloneIgnoreId(dbRecruitPost,
                o -> o.setStatus(HrmRecruitPostStatusEnum.STOPPED.getStatus())));
        // 测试 createTime 不匹配
        recruitPostMapper.insert(cloneIgnoreId(dbRecruitPost, o -> o.setCreateTime(buildTime(2026, 1, 1))));
        // 准备参数
        HrmRecruitPostPageReqVO reqVO = new HrmRecruitPostPageReqVO();
        reqVO.setPostName("Java");
        reqVO.setJobNature(1);
        reqVO.setAreaId(310100);
        reqVO.setDeptId(100L);
        reqVO.setOwnerEmployeeId(200L);
        reqVO.setPostTypeId(300L);
        reqVO.setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus());
        reqVO.setCreateTime(getDateTimeRange(
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20)));

        // 调用
        PageResult<HrmRecruitPostDO> pageResult = recruitPostService.getRecruitPostPage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbRecruitPost, pageResult.getList().get(0));
    }

    @Test
    public void testGetRecruitPostSimpleList() {
        // mock 数据
        HrmRecruitPostDO firstPost = randomRecruitPostDO(o -> o
                .setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus()));
        recruitPostMapper.insert(firstPost);
        HrmRecruitPostDO secondPost = randomRecruitPostDO(o -> o
                .setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus()));
        recruitPostMapper.insert(secondPost);
        // 测试 status 不匹配
        recruitPostMapper.insert(randomRecruitPostDO(o -> o
                .setStatus(HrmRecruitPostStatusEnum.STOPPED.getStatus())));

        // 调用
        List<HrmRecruitPostDO> posts = recruitPostService.getRecruitPostSimpleList();

        // 断言
        assertEquals(new HashSet<>(asList(firstPost.getId(), secondPost.getId())),
                convertSet(posts, HrmRecruitPostDO::getId));
        assertTrue(posts.get(0).getId() > posts.get(1).getId());
    }

    @Test
    public void testUpdateRecruitPostStatus_stopped() {
        // mock 数据
        HrmRecruitPostDO dbRecruitPost = randomRecruitPostDO(o -> o
                .setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus()).setStopReason(null));
        recruitPostMapper.insert(dbRecruitPost);
        // 准备参数
        HrmRecruitPostStatusReqVO reqVO = randomPojo(HrmRecruitPostStatusReqVO.class, o -> o
                .setId(dbRecruitPost.getId()).setStatus(HrmRecruitPostStatusEnum.STOPPED.getStatus())
                .setStopReason(randomString()));

        // 调用
        recruitPostService.updateRecruitPostStatus(reqVO);

        // 断言
        HrmRecruitPostDO recruitPost = recruitPostMapper.selectById(dbRecruitPost.getId());
        assertEquals(reqVO.getStatus(), recruitPost.getStatus());
        assertEquals(reqVO.getStopReason(), recruitPost.getStopReason());
    }

    @Test
    public void testUpdateRecruitPostStatus_recruiting() {
        // mock 数据
        HrmRecruitPostDO dbRecruitPost = randomRecruitPostDO(o -> o
                .setStatus(HrmRecruitPostStatusEnum.STOPPED.getStatus()).setStopReason(randomString()));
        recruitPostMapper.insert(dbRecruitPost);
        // 准备参数
        HrmRecruitPostStatusReqVO reqVO = randomPojo(HrmRecruitPostStatusReqVO.class, o -> o
                .setId(dbRecruitPost.getId()).setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus()));

        // 调用
        recruitPostService.updateRecruitPostStatus(reqVO);

        // 断言
        HrmRecruitPostDO recruitPost = recruitPostMapper.selectById(dbRecruitPost.getId());
        assertEquals(reqVO.getStatus(), recruitPost.getStatus());
        assertEquals("", recruitPost.getStopReason());
    }

    @Test
    public void testUpdateRecruitPostStatus_notExists() {
        // 准备参数
        HrmRecruitPostStatusReqVO reqVO = randomPojo(HrmRecruitPostStatusReqVO.class, o -> o
                .setId(randomLongId()).setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus()));

        // 调用，并断言异常
        assertServiceException(() -> recruitPostService.updateRecruitPostStatus(reqVO), RECRUIT_POST_NOT_EXISTS);
    }

    @Test
    public void testGetRecruitPostStatusCount() {
        // mock 数据
        HrmRecruitPostDO recruitingPost = randomRecruitPostDO(o -> o.setPostName("Java 开发工程师")
                .setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus()));
        recruitPostMapper.insert(recruitingPost);
        recruitPostMapper.insert(cloneIgnoreId(recruitingPost, o -> {}));
        HrmRecruitPostDO stoppedPost = cloneIgnoreId(recruitingPost, o -> o
                .setStatus(HrmRecruitPostStatusEnum.STOPPED.getStatus()));
        recruitPostMapper.insert(stoppedPost);
        // 测试 postName 不匹配
        recruitPostMapper.insert(cloneIgnoreId(recruitingPost, o -> o.setPostName("产品经理")));
        // 准备参数
        HrmRecruitPostPageReqVO reqVO = new HrmRecruitPostPageReqVO();
        reqVO.setPostName("开发工程师");
        reqVO.setStatus(HrmRecruitPostStatusEnum.STOPPED.getStatus());

        // 调用
        Map<Integer, Long> countMap = recruitPostService.getRecruitPostStatusCount(reqVO);

        // 断言
        assertEquals(2L, countMap.get(HrmRecruitPostStatusEnum.RECRUITING.getStatus()));
        assertEquals(1L, countMap.get(HrmRecruitPostStatusEnum.STOPPED.getStatus()));
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static HrmRecruitPostDO randomRecruitPostDO(Consumer<HrmRecruitPostDO>... consumers) {
        Consumer<HrmRecruitPostDO> consumer = o -> o.setPostName(randomString()).setRecruitNum(randomInteger())
                .setJobNature(1).setWorkTime(1).setEducationRequire(1)
                .setMinSalary(new BigDecimal("10000.00")).setMaxSalary(new BigDecimal("20000.00")).setSalaryUnit(2)
                .setMinAge(22).setMaxAge(35).setInterviewEmployeeIds(singletonList(randomLongId()))
                .setEmergencyLevel(2).setStatus(HrmRecruitPostStatusEnum.RECRUITING.getStatus());
        return randomPojo(HrmRecruitPostDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmRecruitPostSaveReqVO randomRecruitPostSaveReqVO(
            Consumer<HrmRecruitPostSaveReqVO>... consumers) {
        Consumer<HrmRecruitPostSaveReqVO> consumer = o -> o.setId(null).setPostName(randomString())
                .setRecruitNum(randomInteger()).setJobNature(1).setWorkTime(1).setEducationRequire(1)
                .setMinSalary(new BigDecimal("10000.00")).setMaxSalary(new BigDecimal("20000.00")).setSalaryUnit(2)
                .setMinAge(22).setMaxAge(35).setInterviewEmployeeIds(singletonList(randomLongId()))
                .setEmergencyLevel(2).setPostTypeId(randomLongId());
        return randomPojo(HrmRecruitPostSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

}
