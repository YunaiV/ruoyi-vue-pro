package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeChangeRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import javax.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * HRM 员工异动记录 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeChangeRecordService {

    /**
     * 创建员工异动记录
     *
     * @param createReqVO 异动记录
     */
    HrmEmployeeChangeRecordDO createEmployeeChangeRecord(@Valid HrmEmployeeChangeRecordCreateReqVO createReqVO);

    /**
     * 获得员工的异动记录列表
     *
     * @param employeeId 员工编号
     * @return 异动记录列表
     */
    List<HrmEmployeeChangeRecordDO> getEmployeeChangeRecordListByEmployeeId(Long employeeId);

    /**
     * 获得到期且尚未生效的异动记录列表
     *
     * @param deadlineTime 截止时间
     * @return 异动记录列表
     */
    List<HrmEmployeeChangeRecordDO> getPendingEmployeeChangeRecordList(LocalDateTime deadlineTime);

    /**
     * 更新员工异动记录的实际生效时间
     *
     * @param id 异动记录编号
     * @param appliedTime 实际生效时间
     */
    void updateEmployeeChangeRecordAppliedTime(Long id, LocalDateTime appliedTime);

    /**
     * 获得指定员工和生效日期范围的异动记录列表
     *
     * @param employeeIds 员工编号集合
     * @param effectTimes 生效时间双闭区间
     * @return 异动记录列表
     */
    List<HrmEmployeeChangeRecordDO> getEmployeeChangeRecordListByEmployeeIdsAndEffectTimeBetween(
            Collection<Long> employeeIds, LocalDateTime[] effectTimes);

}
