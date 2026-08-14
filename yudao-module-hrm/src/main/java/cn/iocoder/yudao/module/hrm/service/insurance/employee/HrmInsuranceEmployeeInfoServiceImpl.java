package cn.iocoder.yudao.module.hrm.service.insurance.employee;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.employeeinfo.HrmInsuranceEmployeeInfoSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.employee.HrmInsuranceEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.insurance.employee.HrmInsuranceEmployeeInfoMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_RESOURCE_BELONG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_EMPLOYEE_INFO_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.INSURANCE_EMP_STATUS_ILLEGAL;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_EMPLOYEE_INFO_UPDATE_SCHEME_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_EMPLOYEE_INFO_UPDATE_SCHEME_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_EMPLOYEE_INFO_SAVE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_EMPLOYEE_INFO_SAVE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_INSURANCE_EMPLOYEE_INFO_TYPE;

/**
 * HRM 员工参保信息 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmInsuranceEmployeeInfoServiceImpl implements HrmInsuranceEmployeeInfoService {

    @Resource
    private HrmInsuranceEmployeeInfoMapper insuranceEmployeeInfoMapper;
    @Resource
    private HrmEmployeeService employeeService;
    @Resource
    private HrmInsuranceSchemeService insuranceSchemeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_EMPLOYEE_INFO_TYPE, subType = HRM_INSURANCE_EMPLOYEE_INFO_SAVE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_INSURANCE_EMPLOYEE_INFO_SAVE_SUCCESS)
    public Long saveInsuranceEmployeeInfo(HrmInsuranceEmployeeInfoSaveReqVO reqVO) {
        // 1. 校验员工、参保信息和参保方案
        HrmEmployeeDO employee = employeeService.validateEmployeeExistsForUpdate(reqVO.getEmployeeId());
        HrmInsuranceEmployeeInfoDO oldEmployeeInfo = insuranceEmployeeInfoMapper
                .selectByEmployeeId(reqVO.getEmployeeId());
        if (reqVO.getId() != null) {
            oldEmployeeInfo = validateInsuranceEmployeeInfoExists(reqVO.getId());
            if (notEqual(oldEmployeeInfo.getEmployeeId(), reqVO.getEmployeeId())) {
                throw exception(EMPLOYEE_RESOURCE_BELONG_INVALID, "参保信息");
            }
        }
        if (reqVO.getSchemeId() != null
                && (oldEmployeeInfo == null || notEqual(oldEmployeeInfo.getSchemeId(), reqVO.getSchemeId()))) {
            validateEmployeeEligible(employee);
            insuranceSchemeService.validateSchemeExists(reqVO.getSchemeId());
        }

        // 2. 保存员工参保信息
        HrmInsuranceEmployeeInfoDO employeeInfo = BeanUtils.toBean(
                reqVO, HrmInsuranceEmployeeInfoDO.class);
        if (employeeInfo.getSocialSecurityStartMonth() != null) {
            employeeInfo.setSocialSecurityStartMonth(
                    LocalDateTimeUtils.beginOfMonth(employeeInfo.getSocialSecurityStartMonth()));
        }
        if (employeeInfo.getId() == null && oldEmployeeInfo != null) {
            employeeInfo.setId(oldEmployeeInfo.getId());
        }
        insuranceEmployeeInfoMapper.insertOrUpdate(employeeInfo);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, oldEmployeeInfo == null
                ? new HrmInsuranceEmployeeInfoSaveReqVO()
                : BeanUtils.toBean(oldEmployeeInfo, HrmInsuranceEmployeeInfoSaveReqVO.class));
        return employeeInfo.getId();
    }

    @Override
    public HrmInsuranceEmployeeInfoDO validateInsuranceEmployeeInfoExists(Long id) {
        HrmInsuranceEmployeeInfoDO employeeInfo = insuranceEmployeeInfoMapper.selectById(id);
        if (employeeInfo == null) {
            throw exception(INSURANCE_EMPLOYEE_INFO_NOT_EXISTS);
        }
        return employeeInfo;
    }

    @Override
    public HrmInsuranceEmployeeInfoDO getInsuranceEmployeeInfoByEmployeeId(Long employeeId) {
        return insuranceEmployeeInfoMapper.selectByEmployeeId(employeeId);
    }

    @Override
    public List<HrmInsuranceEmployeeInfoDO> getInsuranceEmployeeInfoList() {
        return getLatestEmployeeInfoList(insuranceEmployeeInfoMapper.selectListByIdDesc());
    }

    @Override
    public List<HrmInsuranceEmployeeInfoDO> getInsuranceEmployeeInfoList(Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        return getLatestEmployeeInfoList(insuranceEmployeeInfoMapper.selectListByEmployeeIds(employeeIds));
    }

    @Override
    public long getInsuranceEmployeeInfoCountBySchemeId(Long schemeId) {
        return getInsuranceEmployeeInfoCountMapBySchemeIds(Collections.singleton(schemeId))
                .getOrDefault(schemeId, 0L);
    }

    @Override
    public Map<Long, Long> getInsuranceEmployeeInfoCountMapBySchemeIds(Collection<Long> schemeIds) {
        if (CollUtil.isEmpty(schemeIds)) {
            return Collections.emptyMap();
        }
        List<HrmInsuranceEmployeeInfoDO> employeeInfos = getInsuranceEmployeeInfoList();
        employeeInfos.removeIf(employeeInfo -> !schemeIds.contains(employeeInfo.getSchemeId()));
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSet(employeeInfos, HrmInsuranceEmployeeInfoDO::getEmployeeId));
        Map<Long, Long> countMap = new HashMap<>();
        for (HrmInsuranceEmployeeInfoDO employeeInfo : employeeInfos) {
            if (employeeMap.containsKey(employeeInfo.getEmployeeId())) {
                countMap.merge(employeeInfo.getSchemeId(), 1L, Long::sum);
            }
        }
        return countMap;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_INSURANCE_EMPLOYEE_INFO_TYPE,
            subType = HRM_INSURANCE_EMPLOYEE_INFO_UPDATE_SCHEME_SUB_TYPE,
            bizNo = "{{#employeeId}}", success = HRM_INSURANCE_EMPLOYEE_INFO_UPDATE_SCHEME_SUCCESS)
    public void updateEmployeeScheme(Long employeeId, Long schemeId) {
        // 1. 校验员工和社保方案
        HrmEmployeeDO employee = employeeService.validateEmployeeExistsForUpdate(employeeId);
        validateEmployeeEligible(employee);
        HrmInsuranceSchemeDO newScheme = insuranceSchemeService.validateSchemeExists(schemeId);

        // 2. 创建或更新员工参保方案
        HrmInsuranceEmployeeInfoDO employeeInfo = insuranceEmployeeInfoMapper.selectByEmployeeId(employeeId);
        HrmInsuranceSchemeDO oldScheme = employeeInfo == null || employeeInfo.getSchemeId() == null ? null
                : insuranceSchemeService.getScheme(employeeInfo.getSchemeId());
        if (employeeInfo == null) {
            employeeInfo = HrmInsuranceEmployeeInfoDO.builder().employeeId(employeeId).schemeId(schemeId)
                    .firstSocialSecurity(false).firstAccumulationFund(false).build();
            insuranceEmployeeInfoMapper.insert(employeeInfo);
        } else {
            insuranceEmployeeInfoMapper.updateById(new HrmInsuranceEmployeeInfoDO()
                    .setId(employeeInfo.getId()).setSchemeId(schemeId));
        }

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
        LogRecordContext.putVariable("oldSchemeName", oldScheme == null ? "未设置" : oldScheme.getName());
        LogRecordContext.putVariable("newSchemeName", newScheme.getName());
    }

    @Override
    public void updateInsuranceEmployeeInfoSchemeIdBySchemeId(Long schemeId, Long newSchemeId) {
        insuranceEmployeeInfoMapper.updateBySchemeId(schemeId,
                new HrmInsuranceEmployeeInfoDO().setSchemeId(newSchemeId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSocialSecurityStartMonthIfAbsent(Long employeeId, LocalDateTime startMonth) {
        // 1. 锁定员工，并检查起缴月份
        employeeService.validateEmployeeExistsForUpdate(employeeId);
        HrmInsuranceEmployeeInfoDO employeeInfo = insuranceEmployeeInfoMapper.selectByEmployeeId(employeeId);
        if (employeeInfo != null && employeeInfo.getSocialSecurityStartMonth() != null) {
            return;
        }

        // 2. 创建员工参保信息或回填起缴月份
        LocalDateTime socialSecurityStartMonth = LocalDateTimeUtils.beginOfMonth(startMonth);
        if (employeeInfo == null) {
            employeeInfo = HrmInsuranceEmployeeInfoDO.builder().employeeId(employeeId)
                    .firstSocialSecurity(false).firstAccumulationFund(false)
                    .socialSecurityStartMonth(socialSecurityStartMonth).build();
            insuranceEmployeeInfoMapper.insert(employeeInfo);
            return;
        }
        insuranceEmployeeInfoMapper.updateById(new HrmInsuranceEmployeeInfoDO().setId(employeeInfo.getId())
                .setSocialSecurityStartMonth(socialSecurityStartMonth));
    }

    private List<HrmInsuranceEmployeeInfoDO> getLatestEmployeeInfoList(
            List<HrmInsuranceEmployeeInfoDO> employeeInfos) {
        return new ArrayList<>(convertMap(employeeInfos,
                HrmInsuranceEmployeeInfoDO::getEmployeeId).values());
    }

    /**
     * 校验员工是否可以设置参保方案
     *
     * @param employee 员工
     */
    private void validateEmployeeEligible(HrmEmployeeDO employee) {
        if (!HrmEmployeeStatusEnum.FULL_TIME_STATUSES.contains(employee.getStatus())
                || !HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(employee.getEntryStatus())) {
            throw exception(INSURANCE_EMP_STATUS_ILLEGAL);
        }
    }

}
