package cn.iocoder.yudao.module.oa.service.plan;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.plan.vo.OaPlanSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.plan.OaPlanDO;
import cn.iocoder.yudao.module.oa.dal.mysql.plan.OaPlanMapper;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanStatusEnum;
import cn.iocoder.yudao.module.oa.enums.plan.OaPlanTypeEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.PLAN_ACCESS_DENIED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * {@link OaPlanServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaPlanServiceImpl.class)
public class OaPlanServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaPlanServiceImpl planService;

    @Resource
    private OaPlanMapper planMapper;

    @MockitoBean
    private AdminUserApi adminUserApi;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreatePlan_success() {
        // 准备参数
        Long userId = randomLongId();
        OaPlanSaveReqVO reqVO = buildPlanSaveReqVO();

        // 调用
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());
        Long planId = planService.createPlan(reqVO);

        // 断言
        OaPlanDO plan = planMapper.selectById(planId);
        assertEquals(userId.toString(), plan.getCreator());
        assertEquals(reqVO.getTitle(), plan.getTitle());
    }

    @Test
    public void testUpdatePlan_ignoreNullFields() {
        // mock 数据
        Long userId = randomLongId();
        OaPlanDO plan = buildPlanDO(userId).setLabel("重点").setSummary("已完成")
                .setFileUrls(Collections.singletonList("https://example.com/a.txt"));
        planMapper.insert(plan);
        // 准备参数
        OaPlanSaveReqVO reqVO = buildPlanSaveReqVO().setId(plan.getId())
                .setLabel(null).setSummary(null).setFileUrls(null);

        // 调用
        planService.updatePlan(reqVO, userId);

        // 断言
        OaPlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals(plan.getLabel(), updatedPlan.getLabel());
        assertEquals(plan.getSummary(), updatedPlan.getSummary());
        assertEquals(plan.getFileUrls(), updatedPlan.getFileUrls());
    }

    @Test
    public void testUpdatePlan_preserveCreator() {
        // mock 数据
        Long userId = randomLongId();
        OaPlanDO plan = buildPlanDO(userId);
        planMapper.insert(plan);
        // 准备参数
        OaPlanSaveReqVO reqVO = buildPlanSaveReqVO().setId(plan.getId());
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(userId), new MockHttpServletRequest());

        // 调用
        planService.updatePlan(reqVO, userId);

        // 断言
        OaPlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals(userId.toString(), updatedPlan.getCreator());
        assertEquals(reqVO.getTitle(), updatedPlan.getTitle());
        assertEquals(plan.getId(), planService.getPlan(plan.getId(), userId).getId());
    }

    @Test
    public void testUpdatePlan_notOwner() {
        // mock 数据
        OaPlanDO plan = buildPlanDO(randomLongId());
        planMapper.insert(plan);
        // 准备参数
        OaPlanSaveReqVO reqVO = buildPlanSaveReqVO().setId(plan.getId());

        // 调用，并断言异常
        assertServiceException(() -> planService.updatePlan(reqVO, randomLongId()), PLAN_ACCESS_DENIED);
    }

    @Test
    public void testGetLatestPlanMap_emptyUsers() {

        // 调用
        Map<Long, OaPlanDO> result = planService.getLatestPlanMap(Collections.emptyList(),
                OaPlanTypeEnum.WEEK.getType(), new LocalDateTime[]{LocalDateTime.now(), LocalDateTime.now()});

        // 断言
        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    public void testDeletePlan_notOwner() {
        // mock 数据
        OaPlanDO plan = buildPlanDO(randomLongId());
        planMapper.insert(plan);

        // 调用，并断言异常
        assertServiceException(() -> planService.deletePlan(plan.getId(), randomLongId()), PLAN_ACCESS_DENIED);
    }

    @Test
    public void testGetPlan_notOwner() {
        // mock 数据
        OaPlanDO plan = buildPlanDO(randomLongId());
        planMapper.insert(plan);

        // 调用，并断言异常
        assertServiceException(() -> planService.getPlan(plan.getId(), randomLongId()), PLAN_ACCESS_DENIED);
    }

    @Test
    public void testAddPlanComment_success() {
        // mock 数据
        Long managerUserId = randomLongId();
        Long subordinateUserId = randomLongId();
        OaPlanDO plan = buildPlanDO(subordinateUserId).setLabel("重点").setSummary("原总结")
                .setComment("第一次点评")
                .setFileUrls(Collections.singletonList("https://example.com/a.txt"));
        planMapper.insert(plan);
        when(adminUserApi.getUserListBySubordinate(managerUserId)).thenReturn(
                Collections.singletonList(new AdminUserRespDTO().setId(subordinateUserId)));

        // 调用
        planService.addPlanComment(plan.getId(), "验收通过", managerUserId);

        // 断言
        OaPlanDO updatedPlan = planMapper.selectById(plan.getId());
        assertEquals("第一次点评" + System.lineSeparator() + "验收通过", updatedPlan.getComment());
        assertEquals(plan.getLabel(), updatedPlan.getLabel());
        assertEquals(plan.getSummary(), updatedPlan.getSummary());
        assertEquals(plan.getFileUrls(), updatedPlan.getFileUrls());
    }

    @Test
    public void testAddPlanComment_atMaxLength() {
        // mock 数据：已有点评与本次追加内容按字符数计算
        Long ownerId = randomLongId();
        Long managerId = randomLongId();
        OaPlanDO plan = buildPlanDO(ownerId).setComment(StrUtil.repeat("a", 65533 - System.lineSeparator().length()));
        planMapper.insert(plan);
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(managerId)).thenReturn(
                Collections.singletonList(new AdminUserRespDTO().setId(ownerId)));

        // 调用：包含换行后恰好达到 65535 个字符
        planService.addPlanComment(plan.getId(), "ab", managerId);

        // 断言
        assertEquals(plan.getComment() + System.lineSeparator() + "ab",
                planMapper.selectById(plan.getId()).getComment());
    }

    @Test
    public void testAddPlanComment_exceedsMaxLength() {
        // mock 数据：已有点评与本次追加内容按字符数计算
        Long ownerId = randomLongId();
        Long managerId = randomLongId();
        OaPlanDO plan = buildPlanDO(ownerId).setComment(StrUtil.repeat("a", 65533 - System.lineSeparator().length()));
        planMapper.insert(plan);
        // mock 方法
        when(adminUserApi.getUserListBySubordinate(managerId)).thenReturn(
                Collections.singletonList(new AdminUserRespDTO().setId(ownerId)));

        // 调用，并断言：累计字符数超限时保留原点评
        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
                () -> planService.addPlanComment(plan.getId(), "abc", managerId));
        assertTrue(exception.getConstraintViolations().stream().anyMatch(violation ->
                "comment".equals(violation.getPropertyPath().toString())
                        && "点评内容长度不能超过 65535 个字符".equals(violation.getMessage())));
        assertEquals(plan.getComment(), planMapper.selectById(plan.getId()).getComment());
    }

    @Test
    public void testAddPlanComment_notSubordinate() {
        // mock 数据
        Long managerUserId = randomLongId();
        OaPlanDO plan = buildPlanDO(randomLongId());
        planMapper.insert(plan);
        when(adminUserApi.getUserListBySubordinate(managerUserId)).thenReturn(Collections.emptyList());

        // 调用，并断言异常
        assertServiceException(() -> planService.addPlanComment(plan.getId(), "验收通过", managerUserId),
                PLAN_ACCESS_DENIED);
    }

    @Test
    public void testGetLatestPlanMap() {
        // mock 数据
        Long firstUserId = randomLongId();
        Long secondUserId = randomLongId();
        LocalDateTime beginTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);
        OaPlanDO oldPlan = buildPlanDO(firstUserId);
        oldPlan.setCreateTime(beginTime.plusHours(1));
        planMapper.insert(oldPlan);
        OaPlanDO latestPlan = buildPlanDO(firstUserId).setTitle("最新计划");
        latestPlan.setCreateTime(beginTime.plusHours(2));
        planMapper.insert(latestPlan);
        OaPlanDO secondUserPlan = buildPlanDO(secondUserId);
        secondUserPlan.setCreateTime(beginTime.plusHours(1));
        planMapper.insert(secondUserPlan);

        // 调用
        Map<Long, OaPlanDO> result = planService.getLatestPlanMap(Arrays.asList(firstUserId, secondUserId),
                OaPlanTypeEnum.WEEK.getType(), new LocalDateTime[]{beginTime, endTime});

        // 断言
        assertEquals(2, result.size());
        assertEquals(latestPlan.getId(), result.get(firstUserId).getId());
        assertEquals(secondUserPlan.getId(), result.get(secondUserId).getId());
    }

    @Test
    public void testGetPlanPage_onlyMineAndSortByCreateTime() {
        // mock 数据
        Long userId = randomLongId();
        OaPlanDO dayPlan = buildPlanDO(userId).setType(OaPlanTypeEnum.DAY.getType());
        dayPlan.setCreateTime(LocalDateTime.now().minusDays(1));
        planMapper.insert(dayPlan);
        OaPlanDO monthPlan = buildPlanDO(userId).setType(OaPlanTypeEnum.MONTH.getType());
        monthPlan.setCreateTime(LocalDateTime.now());
        planMapper.insert(monthPlan);
        planMapper.insert(buildPlanDO(randomLongId()).setType(OaPlanTypeEnum.MONTH.getType()));
        // 准备参数
        OaPlanPageReqVO pageReqVO = new OaPlanPageReqVO();

        // 调用
        PageResult<OaPlanDO> result = planService.getPlanPage(pageReqVO, userId);

        // 断言
        assertEquals(2, result.getTotal());
        assertEquals(Arrays.asList(monthPlan.getId(), dayPlan.getId()),
                convertList(result.getList(), OaPlanDO::getId));
    }

    @Test
    public void testGetLatestPlanMap_sameCreateTimeAndRangeBoundary() {
        // mock 数据
        Long userId = randomLongId();
        LocalDateTime beginTime = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 9, 30, 23, 59, 59);
        OaPlanDO firstPlan = buildPlanDO(userId).setId(101L);
        firstPlan.setCreateTime(endTime);
        planMapper.insert(firstPlan);
        OaPlanDO latestPlan = buildPlanDO(userId).setId(102L);
        latestPlan.setCreateTime(endTime);
        planMapper.insert(latestPlan);
        OaPlanDO nextMonthPlan = buildPlanDO(userId).setId(103L);
        nextMonthPlan.setCreateTime(endTime.plusSeconds(1));
        planMapper.insert(nextMonthPlan);

        // 调用
        Map<Long, OaPlanDO> result = planService.getLatestPlanMap(Collections.singletonList(userId),
                OaPlanTypeEnum.WEEK.getType(), new LocalDateTime[]{beginTime, endTime});

        // 断言：包含结束边界，相同创建时间取编号较大的记录，不包含下一个周期
        assertEquals(1, result.size());
        assertEquals(latestPlan.getId(), result.get(userId).getId());
    }

    // ========== 随机对象 ==========

    /**
     * 构造开始时间晚于当前时间的未完成周计划参数。
     *
     * @return 保存参数
     */
    private static OaPlanSaveReqVO buildPlanSaveReqVO() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        return new OaPlanSaveReqVO().setType(OaPlanTypeEnum.WEEK.getType())
                .setStatus(OaPlanStatusEnum.UNFINISHED.getStatus()).setTitle("本周工作计划")
                .setContent("完成 OA 工作计划管理功能迁移与自动化测试")
                .setStartTime(startTime).setEndTime(startTime.plusDays(7));
    }

    /**
     * 构造开始时间晚于当前时间的未完成周计划。
     *
     * @param userId 用户编号
     * @return 未入库的测试对象
     */
    private static OaPlanDO buildPlanDO(Long userId) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        OaPlanDO plan = new OaPlanDO().setType(OaPlanTypeEnum.WEEK.getType())
                .setStatus(OaPlanStatusEnum.UNFINISHED.getStatus()).setTitle("旧计划")
                .setContent("完成 OA 工作计划管理功能迁移与自动化测试")
                .setStartTime(startTime).setEndTime(startTime.plusDays(7));
        plan.setCreator(userId.toString());
        return plan;
    }

}
