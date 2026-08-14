package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.educationexperience.HrmEmployeeEducationExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeEducationExperienceDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience.HrmEmployeeEducationExperienceMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayBeginTime;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_EDUCATION_EXPERIENCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_RESOURCE_BELONG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * HRM 员工教育经历 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeEducationExperienceServiceImpl implements HrmEmployeeEducationExperienceService {

    @Resource
    private HrmEmployeeEducationExperienceMapper educationExperienceMapper;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_EDUCATION_EXPERIENCE_CREATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_EDUCATION_EXPERIENCE_CREATE_SUCCESS)
    public Long createEducationExperience(HrmEmployeeEducationExperienceSaveReqVO reqVO) {
        // 1. 校验员工
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());

        // 2. 创建教育经历
        HrmEmployeeEducationExperienceDO educationExperience = BeanUtils.toBean(
                        reqVO, HrmEmployeeEducationExperienceDO.class)
                .setAdmissionTime(getDayBeginTime(reqVO.getAdmissionTime()))
                .setGraduationTime(getDayBeginTime(reqVO.getGraduationTime()));
        educationExperienceMapper.insert(educationExperience);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("educationExperience", educationExperience);
        return educationExperience.getId();
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_EDUCATION_EXPERIENCE_UPDATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_EDUCATION_EXPERIENCE_UPDATE_SUCCESS)
    public void updateEducationExperience(HrmEmployeeEducationExperienceSaveReqVO reqVO) {
        // 1. 校验员工和教育经历
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());
        HrmEmployeeEducationExperienceDO educationExperience = validateEducationExperienceExists(reqVO.getId());
        if (notEqual(educationExperience.getEmployeeId(), reqVO.getEmployeeId())) {
            throw exception(EMPLOYEE_RESOURCE_BELONG_INVALID, "教育经历");
        }

        // 2. 更新教育经历
        educationExperienceMapper.updateById(BeanUtils.toBean(reqVO, HrmEmployeeEducationExperienceDO.class)
                .setEmployeeId(null)
                .setAdmissionTime(getDayBeginTime(reqVO.getAdmissionTime()))
                .setGraduationTime(getDayBeginTime(reqVO.getGraduationTime())));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(educationExperience, HrmEmployeeEducationExperienceSaveReqVO.class));
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_EDUCATION_EXPERIENCE_DELETE_SUB_TYPE,
            bizNo = "{{#educationExperience.employeeId}}", success = HRM_EMPLOYEE_EDUCATION_EXPERIENCE_DELETE_SUCCESS)
    public void deleteEducationExperience(Long id) {
        // 1. 校验员工教育经历存在
        HrmEmployeeEducationExperienceDO educationExperience = validateEducationExperienceExists(id);

        // 2. 删除员工教育经历
        educationExperienceMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("educationExperience", educationExperience);
    }

    @Override
    public HrmEmployeeEducationExperienceDO validateEducationExperienceExists(Long id) {
        HrmEmployeeEducationExperienceDO educationExperience = educationExperienceMapper.selectById(id);
        if (educationExperience == null) {
            throw exception(EMPLOYEE_EDUCATION_EXPERIENCE_NOT_EXISTS);
        }
        return educationExperience;
    }

    @Override
    public List<HrmEmployeeEducationExperienceDO> getEducationExperienceListByEmployeeId(Long employeeId) {
        return educationExperienceMapper.selectListByEmployeeId(employeeId);
    }

}
