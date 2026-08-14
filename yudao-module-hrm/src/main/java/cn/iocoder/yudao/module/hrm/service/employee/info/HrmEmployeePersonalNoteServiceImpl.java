package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.personalnote.HrmEmployeePersonalNoteCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeePersonalNoteDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeePersonalNoteMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_PERSONAL_NOTE_NOT_EXISTS;

/**
 * HRM 员工个人备忘 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeePersonalNoteServiceImpl implements HrmEmployeePersonalNoteService {

    @Resource
    private HrmEmployeePersonalNoteMapper personalNoteMapper;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    public Long createPersonalNote(Long employeeId, HrmEmployeePersonalNoteCreateReqVO reqVO) {
        // 1. 校验员工
        employeeService.validateEmployeeExists(employeeId);

        // 2. 创建个人备忘
        HrmEmployeePersonalNoteDO personalNote = BeanUtils.toBean(reqVO, HrmEmployeePersonalNoteDO.class)
                .setEmployeeId(employeeId);
        personalNoteMapper.insert(personalNote);
        return personalNote.getId();
    }

    @Override
    public void deletePersonalNote(Long employeeId, Long id) {
        // 1. 校验个人备忘属于当前员工
        HrmEmployeePersonalNoteDO personalNote = personalNoteMapper.selectById(id);
        if (personalNote == null || ObjUtil.notEqual(personalNote.getEmployeeId(), employeeId)) {
            throw exception(EMPLOYEE_PERSONAL_NOTE_NOT_EXISTS);
        }

        // 2. 删除个人备忘
        personalNoteMapper.deleteById(id);
    }

    @Override
    public List<HrmEmployeePersonalNoteDO> getPersonalNoteList(
            Long employeeId, LocalDateTime[] reminderTimes) {
        return personalNoteMapper.selectListByEmployeeIdAndReminderTimeBetween(employeeId, reminderTimes);
    }

}
