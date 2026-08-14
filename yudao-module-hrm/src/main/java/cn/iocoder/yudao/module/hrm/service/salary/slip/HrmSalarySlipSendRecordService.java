package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendEmployeeReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipSendRecordDO;
import jakarta.validation.Valid;

/**
 * HRM 工资条发放记录 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalarySlipSendRecordService {

    /**
     * 发放工资条
     *
     * @param reqVO 工资条发放信息
     * @return 工资条发放记录编号
     */
    Long sendSalarySlip(@Valid HrmSalarySlipSendReqVO reqVO);

    /**
     * 获得工资条待发员工分页
     *
     * @param reqVO 分页查询
     * @return 员工月度工资分页
     */
    PageResult<HrmSalaryMonthEmployeeRecordDO> getSalarySlipSendEmployeePage(
            HrmSalarySlipSendEmployeeReqVO reqVO);

    /**
     * 删除工资条发放记录
     *
     * @param id 工资条发放记录编号
     */
    void deleteSalarySlipSendRecord(Long id);

    /**
     * 获得工资条发放记录分页
     *
     * @param reqVO 分页查询
     * @return 工资条发放记录分页
     */
    PageResult<HrmSalarySlipSendRecordDO> getSalarySlipSendRecordPage(
            HrmSalarySlipSendRecordPageReqVO reqVO);

    /**
     * 获得工资条发放记录
     *
     * @param id 发放记录编号
     * @return 工资条发放记录
     */
    HrmSalarySlipSendRecordDO getSalarySlipSendRecord(Long id);

}
