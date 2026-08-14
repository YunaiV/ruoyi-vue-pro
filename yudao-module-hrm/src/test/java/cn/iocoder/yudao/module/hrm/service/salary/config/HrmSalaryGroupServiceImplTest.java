package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryGroupDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryGroupMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalaryGroupServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalaryGroupServiceImpl.class)
public class HrmSalaryGroupServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalaryGroupServiceImpl salaryGroupService;

    @Resource
    private HrmSalaryGroupMapper salaryGroupMapper;

    @MockitoBean
    private HrmSalaryTaxRuleService salaryTaxRuleService;
    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private DeptApi deptApi;

    @Test
    public void testCreateSalaryGroup_success() {
        // 准备参数
        HrmSalaryGroupSaveReqVO reqVO = randomSalaryGroupSaveReqVO();
        // mock 方法
        mockSalaryTaxRule(reqVO.getTaxRuleId());

        // 调用
        Long salaryGroupId = salaryGroupService.createSalaryGroup(reqVO);

        // 断言
        assertNotNull(salaryGroupId);
        HrmSalaryGroupDO salaryGroup = salaryGroupMapper.selectById(salaryGroupId);
        assertPojoEquals(reqVO, salaryGroup, "id");
        assertEquals(HrmSalaryGroupDO.DEFAULT_SALARY_STANDARD, salaryGroup.getSalaryStandard());
        assertEquals(HrmSalaryGroupDO.DEFAULT_CHANGE_RULE, salaryGroup.getChangeRule());
    }

    @Test
    public void testCreateSalaryGroup_nameDuplicate() {
        // mock 数据
        HrmSalaryGroupDO dbSalaryGroup = randomSalaryGroupDO();
        salaryGroupMapper.insert(dbSalaryGroup);
        // 准备参数
        HrmSalaryGroupSaveReqVO reqVO = randomSalaryGroupSaveReqVO(o -> o.setName(dbSalaryGroup.getName()));

        // 调用，并断言异常
        assertServiceException(() -> salaryGroupService.createSalaryGroup(reqVO),
                SALARY_GROUP_NAME_DUPLICATE);
    }

    @Test
    public void testCreateSalaryGroup_employeeBelongsToOtherGroup() {
        // mock 数据
        Long employeeId = randomLongId();
        HrmSalaryGroupDO dbSalaryGroup = randomSalaryGroupDO(
                o -> o.setEmployeeIds(singletonList(employeeId)));
        salaryGroupMapper.insert(dbSalaryGroup);
        // 准备参数
        HrmSalaryGroupSaveReqVO reqVO = randomSalaryGroupSaveReqVO(o -> o
                .setDeptIds(emptyList()).setEmployeeIds(singletonList(employeeId)));
        // mock 方法
        mockSalaryTaxRule(reqVO.getTaxRuleId());

        // 调用，并断言异常
        assertServiceException(() -> salaryGroupService.createSalaryGroup(reqVO),
                SALARY_GROUP_EMPLOYEE_CONFLICT);
    }

    @Test
    public void testCreateSalaryGroup_deptHierarchyBelongsToOtherGroup() {
        // mock 数据
        Long parentDeptId = randomLongId();
        Long childDeptId = randomLongId();
        HrmSalaryGroupDO dbSalaryGroup = randomSalaryGroupDO(o -> o.setDeptIds(singletonList(parentDeptId)));
        salaryGroupMapper.insert(dbSalaryGroup);
        // 准备参数
        HrmSalaryGroupSaveReqVO reqVO = randomSalaryGroupSaveReqVO(o -> o
                .setDeptIds(singletonList(childDeptId)).setEmployeeIds(emptyList()));
        // mock 方法
        mockSalaryTaxRule(reqVO.getTaxRuleId());
        when(deptApi.getChildDeptList(Collections.singleton(parentDeptId)))
                .thenReturn(singletonList(new DeptRespDTO().setId(childDeptId)));

        // 调用，并断言异常
        assertServiceException(() -> salaryGroupService.createSalaryGroup(reqVO),
                SALARY_GROUP_DEPT_CONFLICT);
    }

    @Test
    public void testUpdateSalaryGroup_success() {
        // mock 数据
        HrmSalaryGroupDO dbSalaryGroup = randomSalaryGroupDO();
        salaryGroupMapper.insert(dbSalaryGroup);
        // 准备参数
        HrmSalaryGroupSaveReqVO reqVO = randomSalaryGroupSaveReqVO(o -> o.setId(dbSalaryGroup.getId()));
        // mock 方法
        mockSalaryTaxRule(reqVO.getTaxRuleId());

        // 调用
        salaryGroupService.updateSalaryGroup(reqVO);

        // 断言
        HrmSalaryGroupDO salaryGroup = salaryGroupMapper.selectById(dbSalaryGroup.getId());
        assertPojoEquals(reqVO, salaryGroup);
        assertEquals(HrmSalaryGroupDO.DEFAULT_SALARY_STANDARD, salaryGroup.getSalaryStandard());
        assertEquals(HrmSalaryGroupDO.DEFAULT_CHANGE_RULE, salaryGroup.getChangeRule());
    }

    @Test
    public void testUpdateSalaryGroup_notExists() {
        // 准备参数
        HrmSalaryGroupSaveReqVO reqVO = randomSalaryGroupSaveReqVO(o -> o.setId(randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> salaryGroupService.updateSalaryGroup(reqVO),
                SALARY_GROUP_NOT_EXISTS);
    }

    @Test
    public void testDeleteSalaryGroup_success() {
        // mock 数据
        HrmSalaryGroupDO dbSalaryGroup = randomSalaryGroupDO();
        salaryGroupMapper.insert(dbSalaryGroup);

        // 调用
        salaryGroupService.deleteSalaryGroup(dbSalaryGroup.getId());

        // 断言
        assertNull(salaryGroupMapper.selectById(dbSalaryGroup.getId()));
    }

    @Test
    public void testDeleteSalaryGroup_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> salaryGroupService.deleteSalaryGroup(id),
                SALARY_GROUP_NOT_EXISTS);
    }

    @Test
    public void testGetSalaryGroupPage() {
        // mock 数据
        HrmSalaryGroupDO dbSalaryGroup = randomSalaryGroupDO(o -> o
                .setName("总部薪资组").setTaxRuleId(10L));
        salaryGroupMapper.insert(dbSalaryGroup);
        // 测试 name 不匹配
        salaryGroupMapper.insert(cloneIgnoreId(dbSalaryGroup, o -> o.setName("分公司薪资组")));
        // 测试 taxRuleId 不匹配
        salaryGroupMapper.insert(cloneIgnoreId(dbSalaryGroup, o -> o.setTaxRuleId(20L)));
        // 准备参数
        HrmSalaryGroupPageReqVO reqVO = new HrmSalaryGroupPageReqVO().setName("总部").setTaxRuleId(10L);

        // 调用
        PageResult<HrmSalaryGroupDO> pageResult = salaryGroupService.getSalaryGroupPage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbSalaryGroup, pageResult.getList().get(0));
    }

    @Test
    public void testGetEmployeeSalaryGroupMap_employeeAndDeptPriority() {
        // mock 数据
        Long employeeId = randomLongId();
        Long deptEmployeeId = randomLongId();
        Long deptId = randomLongId();
        HrmSalaryGroupDO deptSalaryGroup = randomSalaryGroupDO(o -> o
                .setDeptIds(singletonList(deptId)).setEmployeeIds(emptyList()));
        salaryGroupMapper.insert(deptSalaryGroup);
        HrmSalaryGroupDO employeeSalaryGroup = randomSalaryGroupDO(o -> o
                .setDeptIds(emptyList()).setEmployeeIds(singletonList(employeeId)));
        salaryGroupMapper.insert(employeeSalaryGroup);
        List<HrmEmployeeDO> employees = Arrays.asList(
                new HrmEmployeeDO().setId(employeeId).setDeptId(deptId),
                new HrmEmployeeDO().setId(deptEmployeeId).setDeptId(deptId));

        // 调用
        Map<Long, HrmSalaryGroupDO> result = salaryGroupService.getEmployeeSalaryGroupMap(employees);

        // 断言
        assertEquals(employeeSalaryGroup.getId(), result.get(employeeId).getId());
        assertEquals(deptSalaryGroup.getId(), result.get(deptEmployeeId).getId());
    }

    @Test
    public void testGetEmployeeSalaryGroupMap_nearestParentDeptPriority() {
        // mock 数据
        Long employeeId = randomLongId();
        Long employeeDeptId = randomLongId();
        Long parentDeptId = randomLongId();
        Long rootDeptId = randomLongId();
        HrmSalaryGroupDO rootSalaryGroup = randomSalaryGroupDO(o -> o
                .setDeptIds(singletonList(rootDeptId)).setEmployeeIds(emptyList()));
        salaryGroupMapper.insert(rootSalaryGroup);
        HrmSalaryGroupDO parentSalaryGroup = randomSalaryGroupDO(o -> o
                .setDeptIds(singletonList(parentDeptId)).setEmployeeIds(emptyList()));
        salaryGroupMapper.insert(parentSalaryGroup);
        when(deptApi.getParentDeptList(employeeDeptId)).thenReturn(Arrays.asList(
                new DeptRespDTO().setId(parentDeptId), new DeptRespDTO().setId(rootDeptId)));

        // 调用
        Map<Long, HrmSalaryGroupDO> result = salaryGroupService.getEmployeeSalaryGroupMap(
                singletonList(new HrmEmployeeDO().setId(employeeId).setDeptId(employeeDeptId)));

        // 断言
        assertEquals(parentSalaryGroup.getId(), result.get(employeeId).getId());
    }

    @Test
    public void testGetSalaryGroupCountMapByTaxRuleIds() {
        // mock 数据
        Long taxRuleId = randomLongId();
        salaryGroupMapper.insert(randomSalaryGroupDO(o -> o.setTaxRuleId(taxRuleId)));
        salaryGroupMapper.insert(randomSalaryGroupDO(o -> o.setTaxRuleId(taxRuleId)));
        salaryGroupMapper.insert(randomSalaryGroupDO());

        // 调用
        Map<Long, Long> result = salaryGroupService.getSalaryGroupCountMapByTaxRuleIds(
                singletonList(taxRuleId));

        // 断言
        assertEquals(2L, result.get(taxRuleId));
    }

    @Test
    public void testGetSalaryGroupCountMapByTaxRuleIds_empty() {
        // 调用
        Map<Long, Long> result = salaryGroupService.getSalaryGroupCountMapByTaxRuleIds(emptyList());

        // 断言
        assertTrue(result.isEmpty());
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static HrmSalaryGroupDO randomSalaryGroupDO(Consumer<HrmSalaryGroupDO>... consumers) {
        Consumer<HrmSalaryGroupDO> consumer = o -> o.setId(null).setName(randomString())
                .setSalaryStandard(HrmSalaryGroupDO.DEFAULT_SALARY_STANDARD)
                .setChangeRule(HrmSalaryGroupDO.DEFAULT_CHANGE_RULE)
                .setDeptIds(singletonList(randomLongId())).setEmployeeIds(emptyList())
                .setTaxRuleId(randomLongId());
        return randomPojo(HrmSalaryGroupDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmSalaryGroupSaveReqVO randomSalaryGroupSaveReqVO(
            Consumer<HrmSalaryGroupSaveReqVO>... consumers) {
        Consumer<HrmSalaryGroupSaveReqVO> consumer = o -> o.setId(null).setName(randomString())
                .setTaxRuleId(randomLongId()).setDeptIds(singletonList(randomLongId()))
                .setEmployeeIds(emptyList());
        return randomPojo(HrmSalaryGroupSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

    private void mockSalaryTaxRule(Long taxRuleId) {
        when(salaryTaxRuleService.validateSalaryTaxRuleExists(taxRuleId))
                .thenReturn(new HrmSalaryTaxRuleDO().setId(taxRuleId));
    }

}
