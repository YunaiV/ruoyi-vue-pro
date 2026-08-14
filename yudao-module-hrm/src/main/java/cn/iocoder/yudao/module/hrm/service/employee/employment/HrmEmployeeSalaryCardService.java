package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.salarycard.HrmEmployeeSalaryCardSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeSalaryCardDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 员工工资卡 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeSalaryCardService {

    /**
     * 保存员工工资卡
     *
     * @param reqVO 工资卡信息
     * @return 工资卡编号
     */
    Long saveSalaryCard(@Valid HrmEmployeeSalaryCardSaveReqVO reqVO);

    /**
     * 删除员工工资卡
     *
     * @param employeeId 员工编号
     */
    void deleteSalaryCardByEmployeeId(Long employeeId);

    /**
     * 校验员工工资卡是否存在
     *
     * @param id 工资卡编号
     * @return 工资卡
     */
    HrmEmployeeSalaryCardDO validateSalaryCardExists(Long id);

    /**
     * 获得员工工资卡
     *
     * @param employeeId 员工编号
     * @return 工资卡
     */
    HrmEmployeeSalaryCardDO getSalaryCardByEmployeeId(Long employeeId);

    /**
     * 获得员工工资卡列表
     *
     * @param employeeIds 员工编号集合
     * @return 员工工资卡列表
     */
    List<HrmEmployeeSalaryCardDO> getSalaryCardList(Collection<Long> employeeIds);

    /**
     * 获得员工工资卡 Map
     *
     * @param employeeIds 员工编号集合
     * @return 员工编号与工资卡的映射
     */
    default Map<Long, HrmEmployeeSalaryCardDO> getSalaryCardMap(Collection<Long> employeeIds) {
        return convertMap(getSalaryCardList(employeeIds), HrmEmployeeSalaryCardDO::getEmployeeId);
    }

}
