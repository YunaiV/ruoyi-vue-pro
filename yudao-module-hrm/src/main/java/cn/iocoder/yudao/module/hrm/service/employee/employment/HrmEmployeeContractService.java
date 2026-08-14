package cn.iocoder.yudao.module.hrm.service.employee.employment;

import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.contract.HrmEmployeeContractSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeContractDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HRM 员工合同 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeContractService {

    /**
     * 创建员工合同
     *
     * @param reqVO 合同信息
     * @return 合同编号
     */
    Long createContract(@Valid HrmEmployeeContractSaveReqVO reqVO);

    /**
     * 更新员工合同
     *
     * @param reqVO 合同信息
     */
    void updateContract(@Valid HrmEmployeeContractSaveReqVO reqVO);

    /**
     * 删除员工合同
     *
     * @param id 合同编号
     */
    void deleteContract(Long id);

    /**
     * 校验员工合同是否存在
     *
     * @param id 合同编号
     * @return 员工合同
     */
    HrmEmployeeContractDO validateContractExists(Long id);

    /**
     * 获得员工合同列表
     *
     * @param employeeId 员工编号
     * @return 合同列表
     */
    List<HrmEmployeeContractDO> getContractListByEmployeeId(Long employeeId);

    /**
     * 获得指定月份到期合同的员工数量
     *
     * @param endTimes 合同结束时间范围
     * @return 员工数量
     */
    Long getExpireEmployeeCountInMonth(LocalDateTime[] endTimes);

}
