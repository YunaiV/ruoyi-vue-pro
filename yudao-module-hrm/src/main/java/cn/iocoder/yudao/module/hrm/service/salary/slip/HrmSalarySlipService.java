package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipRemarkReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import jakarta.validation.Valid;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * HRM 工资条 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalarySlipService {

    /**
     * 创建员工工资条
     *
     * @param sendRecordId 发放记录编号
     * @param employeeRecords 员工月度工资记录列表
     * @param template 工资条模板快照
     */
    void createSalarySlipList(Long sendRecordId, List<HrmSalaryMonthEmployeeRecordDO> employeeRecords,
                              HrmSalarySlipTemplateDO template);

    /**
     * 删除发放记录对应的工资条
     *
     * @param sendRecordId 发放记录编号
     */
    void deleteSalarySlipListBySendRecordId(Long sendRecordId);

    /**
     * 获得工资条发放记录的已读人数 Map
     *
     * @param sendRecordIds 发放记录编号集合
     * @return 发放记录编号与已读人数的映射
     */
    Map<Long, Long> getSalarySlipReadCountMap(Collection<Long> sendRecordIds);

    /**
     * 获得工资条分页
     *
     * @param reqVO 分页查询
     * @return 工资条分页
     */
    PageResult<HrmSalarySlipDO> getSalarySlipPage(HrmSalarySlipPageReqVO reqVO);

    /**
     * 获得工资条详情
     *
     * @param id 工资条编号
     * @return 工资条详情
     */
    HrmSalarySlipDO getSalarySlip(Long id);

    /**
     * 获得指定员工的工资条列表
     *
     * @param employeeId 员工编号
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @param orderType 排序字段类型
     * @param order 排序方式
     * @return 工资条列表
     */
    List<HrmSalarySlipDO> getSalarySlipListByEmployeeId(
            Long employeeId, YearMonth startMonth, YearMonth endMonth,
            Integer orderType, Integer order);

    /**
     * 获得指定员工的工资条详情
     *
     * @param id 工资条编号
     * @param employeeId 员工编号
     * @return 工资条详情
     */
    HrmSalarySlipDO getSalarySlipByIdAndEmployeeId(Long id, Long employeeId);

    /**
     * 获得已发放工资条的员工月度工资记录编号集合
     *
     * @param monthEmployeeRecordIds 员工月度工资记录编号集合
     * @return 已发放的员工月度工资记录编号集合
     */
    Set<Long> getSentMonthEmployeeRecordIdSet(Collection<Long> monthEmployeeRecordIds);

    /**
     * 标记指定员工的工资条为已读
     *
     * @param employeeId 员工编号
     * @param ids 工资条编号列表
     */
    void markSalarySlipListRead(Long employeeId, List<Long> ids);

    /**
     * 修改工资条备注
     *
     * @param reqVO 工资条备注信息
     */
    void updateSalarySlipRemark(@Valid HrmSalarySlipRemarkReqVO reqVO);

}
