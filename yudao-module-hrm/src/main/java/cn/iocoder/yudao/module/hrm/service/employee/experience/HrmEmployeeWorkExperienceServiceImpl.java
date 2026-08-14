package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.workexperience.HrmEmployeeWorkExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeWorkExperienceDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience.HrmEmployeeWorkExperienceMapper;
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
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_RESOURCE_BELONG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_WORK_EXPERIENCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * HRM 员工工作经历 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeWorkExperienceServiceImpl implements HrmEmployeeWorkExperienceService {

    @Resource
    private HrmEmployeeWorkExperienceMapper workExperienceMapper;

    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_WORK_EXPERIENCE_CREATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_WORK_EXPERIENCE_CREATE_SUCCESS)
    public Long createWorkExperience(HrmEmployeeWorkExperienceSaveReqVO reqVO) {
        // 1. 校验员工
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());

        // 2. 创建工作经历
        HrmEmployeeWorkExperienceDO workExperience = BeanUtils.toBean(reqVO, HrmEmployeeWorkExperienceDO.class)
                .setStartTime(getDayBeginTime(reqVO.getStartTime()))
                .setEndTime(getDayBeginTime(reqVO.getEndTime()));
        workExperienceMapper.insert(workExperience);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("workExperience", workExperience);
        return workExperience.getId();
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_WORK_EXPERIENCE_UPDATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_WORK_EXPERIENCE_UPDATE_SUCCESS)
    public void updateWorkExperience(HrmEmployeeWorkExperienceSaveReqVO reqVO) {
        // 1. 校验员工和工作经历
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());
        HrmEmployeeWorkExperienceDO workExperience = validateWorkExperienceExists(reqVO.getId());
        if (notEqual(workExperience.getEmployeeId(), reqVO.getEmployeeId())) {
            throw exception(EMPLOYEE_RESOURCE_BELONG_INVALID, "工作经历");
        }

        // 2. 更新工作经历
        workExperienceMapper.updateById(BeanUtils.toBean(reqVO, HrmEmployeeWorkExperienceDO.class)
                .setEmployeeId(null)
                .setStartTime(getDayBeginTime(reqVO.getStartTime()))
                .setEndTime(getDayBeginTime(reqVO.getEndTime())));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(workExperience, HrmEmployeeWorkExperienceSaveReqVO.class));
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_WORK_EXPERIENCE_DELETE_SUB_TYPE,
            bizNo = "{{#workExperience.employeeId}}", success = HRM_EMPLOYEE_WORK_EXPERIENCE_DELETE_SUCCESS)
    public void deleteWorkExperience(Long id) {
        // 1. 校验员工工作经历存在
        HrmEmployeeWorkExperienceDO workExperience = validateWorkExperienceExists(id);

        // 2. 删除员工工作经历
        workExperienceMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("workExperience", workExperience);
    }

    @Override
    public HrmEmployeeWorkExperienceDO validateWorkExperienceExists(Long id) {
        HrmEmployeeWorkExperienceDO workExperience = workExperienceMapper.selectById(id);
        if (workExperience == null) {
            throw exception(EMPLOYEE_WORK_EXPERIENCE_NOT_EXISTS);
        }
        return workExperience;
    }

    @Override
    public List<HrmEmployeeWorkExperienceDO> getWorkExperienceListByEmployeeId(Long employeeId) {
        return workExperienceMapper.selectListByEmployeeId(employeeId);
    }

}
