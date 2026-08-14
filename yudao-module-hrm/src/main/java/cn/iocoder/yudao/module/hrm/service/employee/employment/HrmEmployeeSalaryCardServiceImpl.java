package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.salarycard.HrmEmployeeSalaryCardSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeSalaryCardDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment.HrmEmployeeSalaryCardMapper;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.ObjectUtil.notEqual;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_RESOURCE_BELONG_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_SALARY_CARD_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_SALARY_CARD_SAVE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_SALARY_CARD_SAVE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_SALARY_CARD_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_SALARY_CARD_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_TYPE;

/**
 * HRM 员工工资卡 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class HrmEmployeeSalaryCardServiceImpl implements HrmEmployeeSalaryCardService {

    @Resource
    private HrmEmployeeSalaryCardMapper salaryCardMapper;
    @Resource
    private HrmEmployeeService employeeService;

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_SALARY_CARD_SAVE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_SALARY_CARD_SAVE_SUCCESS)
    public Long saveSalaryCard(HrmEmployeeSalaryCardSaveReqVO reqVO) {
        // 1. 校验员工和工资卡
        HrmEmployeeDO employee = employeeService.validateEmployeeExists(reqVO.getEmployeeId());
        HrmEmployeeSalaryCardDO oldSalaryCard = reqVO.getId() == null
                ? salaryCardMapper.selectByEmployeeId(reqVO.getEmployeeId())
                : validateSalaryCardExists(reqVO.getId());
        if (oldSalaryCard != null && notEqual(oldSalaryCard.getEmployeeId(), reqVO.getEmployeeId())) {
            throw exception(EMPLOYEE_RESOURCE_BELONG_INVALID, "工资卡");
        }

        // 2. 保存员工工资卡
        HrmEmployeeSalaryCardDO salaryCard = BeanUtils.toBean(reqVO, HrmEmployeeSalaryCardDO.class);
        if (salaryCard.getId() == null && oldSalaryCard != null) {
            salaryCard.setId(oldSalaryCard.getId());
        }
        salaryCardMapper.insertOrUpdate(salaryCard);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, oldSalaryCard == null
                ? new HrmEmployeeSalaryCardSaveReqVO()
                : BeanUtils.toBean(oldSalaryCard, HrmEmployeeSalaryCardSaveReqVO.class));
        return salaryCard.getId();
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_SALARY_CARD_DELETE_SUB_TYPE,
            bizNo = "{{#employee.id}}", success = HRM_EMPLOYEE_SALARY_CARD_DELETE_SUCCESS)
    public void deleteSalaryCardByEmployeeId(Long employeeId) {
        // 1. 校验员工和工资卡存在
        HrmEmployeeDO employee = employeeService.validateEmployeeExists(employeeId);
        HrmEmployeeSalaryCardDO salaryCard = salaryCardMapper.selectByEmployeeId(employeeId);
        if (salaryCard == null) {
            throw exception(EMPLOYEE_SALARY_CARD_NOT_EXISTS);
        }

        // 2. 删除工资卡
        salaryCardMapper.deleteById(salaryCard.getId());

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    public HrmEmployeeSalaryCardDO validateSalaryCardExists(Long id) {
        HrmEmployeeSalaryCardDO salaryCard = salaryCardMapper.selectById(id);
        if (salaryCard == null) {
            throw exception(EMPLOYEE_SALARY_CARD_NOT_EXISTS);
        }
        return salaryCard;
    }

    @Override
    public HrmEmployeeSalaryCardDO getSalaryCardByEmployeeId(Long employeeId) {
        return salaryCardMapper.selectByEmployeeId(employeeId);
    }

    @Override
    public List<HrmEmployeeSalaryCardDO> getSalaryCardList(Collection<Long> employeeIds) {
        if (CollUtil.isEmpty(employeeIds)) {
            return Collections.emptyList();
        }
        // 历史重复数据按工资卡编号倒序只保留最新一张，维持员工单卡查询契约
        Map<Long, HrmEmployeeSalaryCardDO> salaryCardMap = new LinkedHashMap<>();
        for (HrmEmployeeSalaryCardDO salaryCard : salaryCardMapper.selectListByEmployeeIds(employeeIds)) {
            salaryCardMap.putIfAbsent(salaryCard.getEmployeeId(), salaryCard);
        }
        return CollUtil.newArrayList(salaryCardMap.values());
    }

}
