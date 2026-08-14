package cn.iocoder.yudao.module.hrm.service.employee.experience;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.trainingexperience.HrmEmployeeTrainingExperienceSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.experience.HrmEmployeeTrainingExperienceDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.experience.HrmEmployeeTrainingExperienceMapper;
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
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_TRAINING_EXPERIENCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.*;

/**
 * HRM 员工培训经历 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeTrainingExperienceServiceImpl implements HrmEmployeeTrainingExperienceService {

    @Resource
    private HrmEmployeeTrainingExperienceMapper trainingExperienceMapper;

    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_TRAINING_EXPERIENCE_CREATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_TRAINING_EXPERIENCE_CREATE_SUCCESS)
    public Long createTrainingExperience(HrmEmployeeTrainingExperienceSaveReqVO reqVO) {
        // 1. 校验员工
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());

        // 2. 创建培训经历
        HrmEmployeeTrainingExperienceDO trainingExperience = BeanUtils.toBean(
                        reqVO, HrmEmployeeTrainingExperienceDO.class)
                .setStartTime(getDayBeginTime(reqVO.getStartTime()))
                .setEndTime(getDayBeginTime(reqVO.getEndTime()));
        trainingExperienceMapper.insert(trainingExperience);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("trainingExperience", trainingExperience);
        return trainingExperience.getId();
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_TRAINING_EXPERIENCE_UPDATE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_TRAINING_EXPERIENCE_UPDATE_SUCCESS)
    public void updateTrainingExperience(HrmEmployeeTrainingExperienceSaveReqVO reqVO) {
        // 1. 校验员工和培训经历
        employeeService.validateEmployeeExists(reqVO.getEmployeeId());
        HrmEmployeeTrainingExperienceDO trainingExperience = validateTrainingExperienceExists(reqVO.getId());
        if (notEqual(trainingExperience.getEmployeeId(), reqVO.getEmployeeId())) {
            throw exception(EMPLOYEE_RESOURCE_BELONG_INVALID, "培训经历");
        }

        // 2. 更新培训经历
        trainingExperienceMapper.updateById(BeanUtils.toBean(reqVO, HrmEmployeeTrainingExperienceDO.class)
                .setEmployeeId(null)
                .setStartTime(getDayBeginTime(reqVO.getStartTime()))
                .setEndTime(getDayBeginTime(reqVO.getEndTime())));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(trainingExperience, HrmEmployeeTrainingExperienceSaveReqVO.class));
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_TRAINING_EXPERIENCE_DELETE_SUB_TYPE,
            bizNo = "{{#trainingExperience.employeeId}}", success = HRM_EMPLOYEE_TRAINING_EXPERIENCE_DELETE_SUCCESS)
    public void deleteTrainingExperience(Long id) {
        // 1. 校验员工培训经历存在
        HrmEmployeeTrainingExperienceDO trainingExperience = validateTrainingExperienceExists(id);

        // 2. 删除员工培训经历
        trainingExperienceMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("trainingExperience", trainingExperience);
    }

    @Override
    public HrmEmployeeTrainingExperienceDO validateTrainingExperienceExists(Long id) {
        HrmEmployeeTrainingExperienceDO trainingExperience = trainingExperienceMapper.selectById(id);
        if (trainingExperience == null) {
            throw exception(EMPLOYEE_TRAINING_EXPERIENCE_NOT_EXISTS);
        }
        return trainingExperience;
    }

    @Override
    public List<HrmEmployeeTrainingExperienceDO> getTrainingExperienceListByEmployeeId(Long employeeId) {
        return trainingExperienceMapper.selectListByEmployeeId(employeeId);
    }

}
