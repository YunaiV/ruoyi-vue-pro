package cn.iocoder.yudao.module.hrm.service.salary.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.group.HrmSalaryGroupSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryGroupDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.config.HrmSalaryGroupMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_GROUP_DEPT_CONFLICT;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_GROUP_EMPLOYEE_CONFLICT;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_GROUP_NAME_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_GROUP_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * HRM 薪资组 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalaryGroupServiceImpl implements HrmSalaryGroupService {

    @Resource
    private HrmSalaryGroupMapper salaryGroupMapper;

    @Resource
    private HrmSalaryTaxRuleService salaryTaxRuleService;
    @Resource
    private HrmEmployeeService employeeService;

    @Resource
    private DeptApi deptApi;

    @Override
    @LogRecord(type = HRM_SALARY_GROUP_TYPE, subType = HRM_SALARY_GROUP_CREATE_SUB_TYPE,
            bizNo = "{{#salaryGroup.id}}", success = HRM_SALARY_GROUP_CREATE_SUCCESS)
    public Long createSalaryGroup(HrmSalaryGroupSaveReqVO createReqVO) {
        // 1. 校验薪资组
        validateSalaryGroupForCreateOrUpdate(null, createReqVO);

        // 2. 创建薪资组
        HrmSalaryGroupDO salaryGroup = BeanUtils.toBean(createReqVO, HrmSalaryGroupDO.class);
        fillSalaryGroupDefaultValue(salaryGroup);
        salaryGroupMapper.insert(salaryGroup);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("salaryGroup", salaryGroup);
        return salaryGroup.getId();
    }

    @Override
    @LogRecord(type = HRM_SALARY_GROUP_TYPE, subType = HRM_SALARY_GROUP_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = HRM_SALARY_GROUP_UPDATE_SUCCESS)
    public void updateSalaryGroup(HrmSalaryGroupSaveReqVO updateReqVO) {
        // 1. 校验薪资组
        HrmSalaryGroupDO oldSalaryGroup = validateSalaryGroupForCreateOrUpdate(
                updateReqVO.getId(), updateReqVO);

        // 2. 更新薪资组
        HrmSalaryGroupDO updateObj = BeanUtils.toBean(updateReqVO, HrmSalaryGroupDO.class);
        fillSalaryGroupDefaultValue(updateObj);
        salaryGroupMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(oldSalaryGroup, HrmSalaryGroupSaveReqVO.class));
    }

    @Override
    @LogRecord(type = HRM_SALARY_GROUP_TYPE, subType = HRM_SALARY_GROUP_DELETE_SUB_TYPE,
            bizNo = "{{#salaryGroup.id}}", success = HRM_SALARY_GROUP_DELETE_SUCCESS)
    public void deleteSalaryGroup(Long id) {
        // 1. 校验薪资组存在
        HrmSalaryGroupDO salaryGroup = validateSalaryGroupExists(id);

        // 2. 删除薪资组
        salaryGroupMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("salaryGroup", salaryGroup);
    }

    @Override
    public HrmSalaryGroupDO getSalaryGroup(Long id) {
        return salaryGroupMapper.selectById(id);
    }

    @Override
    public PageResult<HrmSalaryGroupDO> getSalaryGroupPage(HrmSalaryGroupPageReqVO pageReqVO) {
        return salaryGroupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HrmSalaryGroupDO> getSalaryGroupList() {
        return salaryGroupMapper.selectListByIdDesc();
    }

    @Override
    public Map<Long, HrmSalaryGroupDO> getEmployeeSalaryGroupMap(Collection<HrmEmployeeDO> employees) {
        if (CollUtil.isEmpty(employees)) {
            return Collections.emptyMap();
        }
        // 1. 查询薪资组
        List<HrmSalaryGroupDO> salaryGroups = salaryGroupMapper.selectListByIdDesc();
        if (CollUtil.isEmpty(salaryGroups)) {
            return Collections.emptyMap();
        }

        // 2. 构建员工和部门对应的薪资组 Map
        Map<Long, HrmSalaryGroupDO> employeeSalaryGroupMap = new HashMap<>();
        Map<Long, HrmSalaryGroupDO> deptSalaryGroupMap = new HashMap<>();
        for (HrmSalaryGroupDO salaryGroup : salaryGroups) {
            CollUtil.emptyIfNull(salaryGroup.getEmployeeIds())
                    .forEach(employeeId -> employeeSalaryGroupMap.put(employeeId, salaryGroup));
            CollUtil.emptyIfNull(salaryGroup.getDeptIds())
                    .forEach(deptId -> deptSalaryGroupMap.put(deptId, salaryGroup));
        }

        // 3. 员工单独配置优先，其次匹配最近的上级部门配置
        Map<Long, List<Long>> parentDeptIdCache = new HashMap<>();
        Map<Long, HrmSalaryGroupDO> result = new LinkedHashMap<>();
        for (HrmEmployeeDO employee : employees) {
            if (employee.getId() == null) {
                continue;
            }
            HrmSalaryGroupDO salaryGroup = employeeSalaryGroupMap.get(employee.getId());
            if (salaryGroup == null) {
                salaryGroup = findDeptSalaryGroup(employee.getDeptId(), deptSalaryGroupMap, parentDeptIdCache);
            }
            if (salaryGroup != null) {
                result.put(employee.getId(), salaryGroup);
            }
        }
        return result;
    }

    @Override
    public Map<Long, Long> getSalaryGroupCountMapByTaxRuleIds(Collection<Long> taxRuleIds) {
        if (CollUtil.isEmpty(taxRuleIds)) {
            return Collections.emptyMap();
        }
        return salaryGroupMapper.selectCountMapByTaxRuleIds(taxRuleIds);
    }

    private HrmSalaryGroupDO findDeptSalaryGroup(Long employeeDeptId,
                                                 Map<Long, HrmSalaryGroupDO> deptSalaryGroupMap,
                                                 Map<Long, List<Long>> parentDeptIdCache) {
        if (employeeDeptId == null) {
            return null;
        }
        HrmSalaryGroupDO salaryGroup = deptSalaryGroupMap.get(employeeDeptId);
        if (salaryGroup != null) {
            return salaryGroup;
        }
        List<Long> parentDeptIds = parentDeptIdCache.computeIfAbsent(employeeDeptId,
                deptId -> convertList(deptApi.getParentDeptList(deptId), DeptRespDTO::getId));
        for (Long parentDeptId : parentDeptIds) {
            salaryGroup = deptSalaryGroupMap.get(parentDeptId);
            if (salaryGroup != null) {
                return salaryGroup;
            }
        }
        return null;
    }

    private HrmSalaryGroupDO validateSalaryGroupForCreateOrUpdate(Long id, HrmSalaryGroupSaveReqVO reqVO) {
        // 1. 校验薪资组是否存在
        HrmSalaryGroupDO salaryGroup = id != null ? validateSalaryGroupExists(id) : null;
        // 2. 校验薪资组名称唯一
        validateSalaryGroupNameUnique(id, reqVO.getName());
        // 3. 校验计税规则
        salaryTaxRuleService.validateSalaryTaxRuleExists(reqVO.getTaxRuleId());
        // 4. 校验适用范围
        validateSalaryGroupScope(id, reqVO.getDeptIds(), reqVO.getEmployeeIds());
        return salaryGroup;
    }

    private HrmSalaryGroupDO validateSalaryGroupExists(Long id) {
        HrmSalaryGroupDO salaryGroup = salaryGroupMapper.selectById(id);
        if (salaryGroup == null) {
            throw exception(SALARY_GROUP_NOT_EXISTS);
        }
        return salaryGroup;
    }

    private void validateSalaryGroupNameUnique(Long id, String name) {
        HrmSalaryGroupDO salaryGroup = salaryGroupMapper.selectByName(name);
        if (salaryGroup != null && ObjUtil.notEqual(salaryGroup.getId(), id)) {
            throw exception(SALARY_GROUP_NAME_DUPLICATE);
        }
    }

    private void validateSalaryGroupScope(Long id, Collection<Long> deptIds, Collection<Long> employeeIds) {
        // 1. 校验适用部门及其上下级部门未归属其他薪资组
        Set<Long> selectedDeptIds = CollUtil.isEmpty(deptIds)
                ? Collections.emptySet() : new HashSet<>(deptIds);
        deptApi.validateDeptList(selectedDeptIds);
        List<HrmSalaryGroupDO> otherSalaryGroups = convertList(salaryGroupMapper.selectListByIdDesc(),
                salaryGroup -> salaryGroup, salaryGroup -> ObjUtil.notEqual(salaryGroup.getId(), id));
        if (CollUtil.isNotEmpty(selectedDeptIds)) {
            Set<Long> deptScope = getDeptScope(selectedDeptIds);
            Set<Long> otherDeptIds = convertSetByFlatMap(
                    otherSalaryGroups, HrmSalaryGroupDO::getDeptIds, Collection::stream);
            if (!Collections.disjoint(deptScope, getDeptScope(otherDeptIds))) {
                throw exception(SALARY_GROUP_DEPT_CONFLICT);
            }
        }

        // 2. 校验适用员工存在且未归属其他薪资组
        Set<Long> selectedEmployeeIds = CollUtil.isEmpty(employeeIds)
                ? Collections.emptySet() : new HashSet<>(employeeIds);
        employeeService.validateEmployeeListExists(selectedEmployeeIds);
        Set<Long> otherEmployeeIds = convertSetByFlatMap(
                otherSalaryGroups, HrmSalaryGroupDO::getEmployeeIds, Collection::stream);
        if (!Collections.disjoint(selectedEmployeeIds, otherEmployeeIds)) {
            throw exception(SALARY_GROUP_EMPLOYEE_CONFLICT);
        }
    }

    private Set<Long> getDeptScope(Collection<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptySet();
        }
        Set<Long> result = new HashSet<>(deptIds);
        result.addAll(convertSet(deptApi.getChildDeptList(deptIds), DeptRespDTO::getId));
        return result;
    }

    private void fillSalaryGroupDefaultValue(HrmSalaryGroupDO salaryGroup) {
        salaryGroup.setSalaryStandard(HrmSalaryGroupDO.DEFAULT_SALARY_STANDARD);
        salaryGroup.setChangeRule(HrmSalaryGroupDO.DEFAULT_CHANGE_RULE);
    }

}
