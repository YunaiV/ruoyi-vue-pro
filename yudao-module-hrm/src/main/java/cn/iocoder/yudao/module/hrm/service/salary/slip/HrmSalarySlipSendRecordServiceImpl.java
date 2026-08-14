package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthEmployeeRecordService;
import cn.iocoder.yudao.module.hrm.service.salary.monthrecord.HrmSalaryMonthRecordService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendEmployeeReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipSendRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip.HrmSalarySlipSendRecordMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.hrm.enums.salary.monthrecord.HrmSalaryMonthRecordStatusEnum;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_EMP_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_MONTH_RECORD_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_SLIP_RECORD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_SLIP_EMPLOYEE_ACCOUNT_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_SLIP_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_SLIP_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_SLIP_SEND_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_SLIP_SEND_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_SALARY_SLIP_TYPE;

/**
 * HRM 工资条发放记录 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmSalarySlipSendRecordServiceImpl implements HrmSalarySlipSendRecordService {

    @Resource
    private HrmSalarySlipSendRecordMapper salarySlipSendRecordMapper;

    @Resource
    private HrmSalaryMonthRecordService monthRecordService;
    @Resource
    private HrmSalaryMonthEmployeeRecordService monthEmployeeRecordService;
    @Resource
    private HrmSalarySlipTemplateService salarySlipTemplateService;
    @Resource
    private HrmSalarySlipService salarySlipService;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_SLIP_TYPE, subType = HRM_SALARY_SLIP_SEND_SUB_TYPE,
            bizNo = "{{#salarySlipSendRecord.id}}", success = HRM_SALARY_SLIP_SEND_SUCCESS)
    public Long sendSalarySlip(HrmSalarySlipSendReqVO reqVO) {
        // 1.1 校验月度工资表，并构建本次发放的工资条模板快照
        HrmSalaryMonthRecordDO monthRecord = monthRecordService.validateMonthRecordExistsForUpdate(
                reqVO.getMonthRecordId());
        if (Objects.equals(monthRecord.getStatus(), HrmSalaryMonthRecordStatusEnum.UNCOMPUTED.getStatus())) {
            throw exception(SALARY_MONTH_RECORD_STATUS_INVALID);
        }
        HrmSalarySlipTemplateDO template = salarySlipTemplateService.buildSalarySlipTemplateSnapshot(
                reqVO.getHideEmpty(), reqVO.getOptions());
        // 1.2  筛选本次发放的员工月度工资记录
        List<HrmSalaryMonthEmployeeRecordDO> employeeRecords;
        if (Boolean.TRUE.equals(reqVO.getAll())) {
            employeeRecords = getSalarySlipSendEmployeeRecordList(reqVO);
        } else {
            Set<Long> employeeIds = convertSet(reqVO.getEmployeeIds());
            employeeRecords = monthEmployeeRecordService.getMonthEmployeeRecordList(new HrmSalaryMonthEmployeeRecordListReqVO()
                            .setMonthRecordId(reqVO.getMonthRecordId()).setEmployeeIds(employeeIds));
            Set<Long> monthEmployeeIds = convertSet(
                    employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId);
            if (!monthEmployeeIds.containsAll(employeeIds) || monthEmployeeIds.size() != employeeIds.size()) {
                throw exception(SALARY_MONTH_EMP_RECORD_NOT_EXISTS);
            }
        }
        if (CollUtil.isEmpty(employeeRecords)) {
            throw exception(SALARY_MONTH_EMP_RECORD_NOT_EXISTS);
        }
        // 1.3 仅向已绑定后台账号的员工发放工资条
        Map<Long, HrmEmployeeDO> employeeMap = employeeService.getEmployeeMap(
                convertSet(employeeRecords, HrmSalaryMonthEmployeeRecordDO::getEmployeeId));
        List<HrmSalaryMonthEmployeeRecordDO> sendableEmployeeRecords = filterList(
                employeeRecords, employeeRecord -> {
                    HrmEmployeeDO employee = employeeMap.get(employeeRecord.getEmployeeId());
                    return employee != null && employee.getUserId() != null;
                });
        if (Boolean.FALSE.equals(reqVO.getAll()) && sendableEmployeeRecords.size() != employeeRecords.size()) {
            throw exception(SALARY_SLIP_EMPLOYEE_ACCOUNT_NOT_EXISTS);
        }
        if (CollUtil.isEmpty(sendableEmployeeRecords)) {
            throw exception(SALARY_SLIP_EMPLOYEE_ACCOUNT_NOT_EXISTS);
        }

        // 2.1 创建工资条发放记录
        HrmSalarySlipSendRecordDO salarySlipSendRecord = HrmSalarySlipSendRecordDO.builder()
                .monthRecordId(reqVO.getMonthRecordId()).employeeCount(monthRecord.getEmployeeCount())
                .sendEmployeeCount(sendableEmployeeRecords.size()).year(monthRecord.getYear())
                .month(monthRecord.getMonth()).build();
        salarySlipSendRecordMapper.insert(salarySlipSendRecord);
        // 2.2 创建员工工资条
        salarySlipService.createSalarySlipList(
                salarySlipSendRecord.getId(), sendableEmployeeRecords, template);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("salarySlipSendRecord", salarySlipSendRecord);
        return salarySlipSendRecord.getId();
    }

    @Override
    public PageResult<HrmSalaryMonthEmployeeRecordDO> getSalarySlipSendEmployeePage(
            HrmSalarySlipSendEmployeeReqVO reqVO) {
        // 1. 获得月度工资表
        if (monthRecordService.getMonthRecord(reqVO.getMonthRecordId()) == null) {
            return PageResult.empty();
        }

        // 2. 根据员工条件筛选员工编号
        List<Long> employeeIds = getSalarySlipSendEmployeeIdList(reqVO.getSearch(), reqVO.getDeptId());
        if (employeeIds != null && CollUtil.isEmpty(employeeIds)) {
            return PageResult.empty();
        }

        // 3. 查询员工月度工资记录分页
        HrmSalaryMonthEmployeeRecordPageReqVO pageReqVO = BeanUtils.toBean(reqVO, HrmSalaryMonthEmployeeRecordPageReqVO.class)
                .setEmployeeIds(employeeIds).setSalarySlipSent(reqVO.getSent());
        return monthEmployeeRecordService.getMonthEmployeeRecordPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_SALARY_SLIP_TYPE, subType = HRM_SALARY_SLIP_DELETE_SUB_TYPE,
            bizNo = "{{#salarySlipSendRecord.id}}", success = HRM_SALARY_SLIP_DELETE_SUCCESS)
    public void deleteSalarySlipSendRecord(Long id) {
        // 1. 校验工资条发放记录存在
        HrmSalarySlipSendRecordDO salarySlipSendRecord = validateSalarySlipSendRecordExists(id);

        // 2. 删除工资条和发放记录
        salarySlipService.deleteSalarySlipListBySendRecordId(id);
        salarySlipSendRecordMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("salarySlipSendRecord", salarySlipSendRecord);
    }

    @Override
    public PageResult<HrmSalarySlipSendRecordDO> getSalarySlipSendRecordPage(HrmSalarySlipSendRecordPageReqVO reqVO) {
        return salarySlipSendRecordMapper.selectPage(reqVO);
    }

    @Override
    public HrmSalarySlipSendRecordDO getSalarySlipSendRecord(Long id) {
        return salarySlipSendRecordMapper.selectById(id);
    }

    private List<HrmSalaryMonthEmployeeRecordDO> getSalarySlipSendEmployeeRecordList(
            HrmSalarySlipSendReqVO reqVO) {
        // 1. 根据员工条件筛选员工编号
        List<Long> employeeIds = getSalarySlipSendEmployeeIdList(reqVO.getSearch(), reqVO.getDeptId());
        if (employeeIds != null && CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }

        // 2. 查询符合发放条件的员工月度工资记录
        HrmSalaryMonthEmployeeRecordListReqVO listReqVO = new HrmSalaryMonthEmployeeRecordListReqVO()
                .setMonthRecordId(reqVO.getMonthRecordId()).setEmployeeIds(employeeIds).setSalarySlipSent(reqVO.getSent());
        return monthEmployeeRecordService.getMonthEmployeeRecordList(listReqVO);
    }

    private List<Long> getSalarySlipSendEmployeeIdList(String search, Long deptId) {
        if (StrUtil.isBlank(search) && deptId == null) {
            return null;
        }
        HrmEmployeeListReqVO employeeReqVO = new HrmEmployeeListReqVO().setSearch(search).setDeptId(deptId);
        List<HrmEmployeeDO> employees = employeeService.getEmployeeList(employeeReqVO);
        return convertList(employees, HrmEmployeeDO::getId);
    }

    private HrmSalarySlipSendRecordDO validateSalarySlipSendRecordExists(Long id) {
        HrmSalarySlipSendRecordDO salarySlipSendRecord = salarySlipSendRecordMapper.selectById(id);
        if (salarySlipSendRecord == null) {
            throw exception(SALARY_SLIP_RECORD_NOT_EXISTS);
        }
        return salarySlipSendRecord;
    }

}
