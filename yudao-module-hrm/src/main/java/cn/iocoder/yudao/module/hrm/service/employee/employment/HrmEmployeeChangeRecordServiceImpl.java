package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeChangeRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment.HrmEmployeeChangeRecordMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayBeginTime;

/**
 * HRM 员工异动记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeChangeRecordServiceImpl implements HrmEmployeeChangeRecordService {

    @Resource
    private HrmEmployeeChangeRecordMapper changeRecordMapper;

    @Override
    public HrmEmployeeChangeRecordDO createEmployeeChangeRecord(HrmEmployeeChangeRecordCreateReqVO createReqVO) {
        boolean pendingEffect = HrmEmployeeChangeTypeEnum.PENDING_EFFECT_TYPES.contains(createReqVO.getType());
        // 转正、调岗、晋升、降级和转全职按自然日生效；再入职保留实际入职时间
        if (pendingEffect) {
            createReqVO.setEffectTime(getDayBeginTime(createReqVO.getEffectTime()));
        }
        HrmEmployeeChangeRecordDO changeRecord = BeanUtils.toBean(createReqVO, HrmEmployeeChangeRecordDO.class);
        // 非预约生效的再入职记录创建时即视为已生效
        if (!pendingEffect) {
            changeRecord.setAppliedTime(LocalDateTime.now());
        }
        changeRecordMapper.insert(changeRecord);
        return changeRecord;
    }

    @Override
    public List<HrmEmployeeChangeRecordDO> getEmployeeChangeRecordListByEmployeeId(Long employeeId) {
        return changeRecordMapper.selectListByEmployeeId(employeeId);
    }

    @Override
    public List<HrmEmployeeChangeRecordDO> getPendingEmployeeChangeRecordList(LocalDateTime deadlineTime) {
        return changeRecordMapper.selectListByAppliedTimeNullAndEffectTimeBeforeOrEqual(
                deadlineTime, HrmEmployeeChangeTypeEnum.PENDING_EFFECT_TYPES);
    }

    @Override
    public void updateEmployeeChangeRecordAppliedTime(Long id, LocalDateTime appliedTime) {
        changeRecordMapper.updateById(new HrmEmployeeChangeRecordDO().setId(id).setAppliedTime(appliedTime));
    }

    @Override
    public List<HrmEmployeeChangeRecordDO> getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
            Collection<Long> employeeIds, LocalDateTime[] effectTimes) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        return changeRecordMapper.selectListByEmployeeIdsAndEffectTimeBetween(
                employeeIds, effectTimes);
    }

}
