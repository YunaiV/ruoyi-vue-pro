package cn.iocoder.yudao.module.hrm.service.salary.employee;

import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryChangeRecordDO;

import java.time.LocalDate;
import java.util.List;

/**
 * HRM 定薪/调薪记录 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalaryChangeRecordService {

    /**
     * 创建定薪/调薪记录
     *
     * @param changeRecord 定薪/调薪记录
     * @return 记录编号
     */
    @SuppressWarnings("UnusedReturnValue")
    Long createSalaryChangeRecord(HrmSalaryChangeRecordDO changeRecord);

    /**
     * 更新定薪/调薪记录
     *
     * @param changeRecord 定薪/调薪记录
     */
    void updateSalaryChangeRecord(HrmSalaryChangeRecordDO changeRecord);

    /**
     * 按原状态更新定薪/调薪记录状态
     *
     * @param id 记录编号
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @return 是否更新成功
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean updateSalaryChangeRecordStatus(Long id, Integer oldStatus, Integer newStatus);

    /**
     * 校验定薪/调薪记录是否存在
     *
     * @param id 记录编号
     * @return 定薪/调薪记录
     */
    HrmSalaryChangeRecordDO validateSalaryChangeRecordExists(Long id);

    /**
     * 判断员工是否存在待生效的定薪/调薪记录
     *
     * @param employeeId 员工编号
     * @param excludeId 排除的记录编号
     * @return 是否存在
     */
    boolean hasPendingSalaryChangeRecord(Long employeeId, Long excludeId);

    /**
     * 判断员工是否存在未取消的调薪记录
     *
     * @param employeeId 员工编号
     * @return 是否存在
     */
    boolean hasUncancelledSalaryAdjustmentRecord(Long employeeId);

    /**
     * 获得员工定薪记录
     *
     * @param employeeId 员工编号
     * @return 定薪记录
     */
    HrmSalaryChangeRecordDO getSalarySetRecordByEmployeeId(Long employeeId);

    /**
     * 获得定薪/调薪记录
     *
     * @param id 记录编号
     * @return 定薪/调薪记录
     */
    HrmSalaryChangeRecordDO getSalaryChangeRecord(Long id);

    /**
     * 获得员工定薪、调薪记录列表
     *
     * @param employeeId 员工编号
     * @return 定薪、调薪记录列表
     */
    List<HrmSalaryChangeRecordDO> getSalaryChangeRecordList(Long employeeId);

    /**
     * 取消待生效的定薪/调薪记录
     *
     * @param id 记录编号
     */
    void cancelSalaryChangeRecord(Long id);

    /**
     * 删除未生效的定薪/调薪记录
     *
     * @param id 记录编号
     */
    void deleteSalaryChangeRecord(Long id);

    /**
     * 获得指定日期前已到期的调薪记录数量
     *
     * @param targetDate 目标日期
     * @return 到期记录数量
     */
    long getDueSalaryChangeCount(LocalDate targetDate);

    /**
     * 获得指定日期前已到期的调薪记录列表
     *
     * @param targetDate 目标日期
     * @return 到期记录列表
     */
    List<HrmSalaryChangeRecordDO> getDueSalaryChangeRecordList(LocalDate targetDate);

}
