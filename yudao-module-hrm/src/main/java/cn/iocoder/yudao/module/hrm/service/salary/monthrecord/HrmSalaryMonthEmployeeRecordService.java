package cn.iocoder.yudao.module.hrm.service.salary.monthrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.option.HrmSalaryOptionValueVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryEmployeeMonthRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryMonthEmployeeRecordUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.monthrecord.employee.HrmSalaryPerformanceCoefficientReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryTaxRuleDO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * HRM 员工月度工资 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalaryMonthEmployeeRecordService {

    /**
     * 保存月度工资表的员工记录，并保持同一员工记录编号稳定
     *
     * @param monthRecordId 月度工资表编号
     * @param employeeRecords 员工月度工资记录集合
     */
    void saveMonthEmployeeRecordList(
            Long monthRecordId, Collection<HrmSalaryMonthEmployeeRecordDO> employeeRecords);

    /**
     * 批量计算员工月度工资
     *
     * @param employeeRecords 员工月度工资记录集合
     * @param employeeOptionValueMap 员工薪资项 Map
     * @param employeeTaxRuleMap 员工计税规则 Map
     * @return 员工月度工资记录列表
     */
    List<HrmSalaryMonthEmployeeRecordDO> calculateMonthEmployeeRecordList(
            List<HrmSalaryMonthEmployeeRecordDO> employeeRecords,
            Map<Long, List<HrmSalaryOptionValueVO>> employeeOptionValueMap,
            Map<Long, HrmSalaryTaxRuleDO> employeeTaxRuleMap);

    /**
     * 重新计算员工月度工资
     *
     * @param employeeRecord 员工月度工资记录
     * @param optionValues 薪资项值
     * @return 员工月度工资记录
     */
    HrmSalaryMonthEmployeeRecordDO calculateMonthEmployeeRecord(
            HrmSalaryMonthEmployeeRecordDO employeeRecord, List<HrmSalaryOptionValueVO> optionValues);

    /**
     * 批量修改员工月度工资
     *
     * @param reqVOs 修改信息列表
     */
    void updateMonthEmployeeRecordList(List<HrmSalaryMonthEmployeeRecordUpdateReqVO> reqVOs);

    /**
     * 删除指定月度工资表的员工记录
     *
     * @param monthRecordId 月度工资表编号
     */
    void deleteMonthEmployeeRecordListByMonthRecordId(Long monthRecordId);

    /**
     * 获得员工月度工资分页
     *
     * @param reqVO 分页查询
     * @return 员工月度工资分页
     */
    PageResult<HrmSalaryMonthEmployeeRecordDO> getMonthEmployeeRecordPage(
            HrmSalaryMonthEmployeeRecordPageReqVO reqVO);

    /**
     * 获得员工月度工资列表
     *
     * @param reqVO 列表查询
     * @return 员工月度工资列表
     */
    List<HrmSalaryMonthEmployeeRecordDO> getMonthEmployeeRecordList(
            HrmSalaryMonthEmployeeRecordListReqVO reqVO);

    /**
     * 获得指定月度工资表的员工记录
     *
     * @param monthRecordId 月度工资表编号
     * @return 员工月度工资记录列表
     */
    List<HrmSalaryMonthEmployeeRecordDO> getMonthEmployeeRecordListByMonthRecordId(Long monthRecordId);

    /**
     * 获得指定员工年月的工资记录
     *
     * @param employeeId 员工编号
     * @param year 年份
     * @param month 月份
     * @return 员工月度工资记录
     */
    HrmSalaryMonthEmployeeRecordDO getMonthEmployeeRecordByEmployeeIdAndYearMonth(
            Long employeeId, Integer year, Integer month);

    /**
     * 获得月度工资项汇总
     *
     * @param employeeRecords 员工月度工资记录列表
     * @return 工资项汇总
     */
    List<HrmSalaryMonthEmployeeRecordDO.OptionValue> getMonthOptionSummary(
            List<HrmSalaryMonthEmployeeRecordDO> employeeRecords);

    /**
     * 获得员工异动分类数量
     *
     * @param reqVO 查询条件
     * @return 异动类型与员工数量的映射
     */
    Map<Integer, Long> getMonthEmployeeChangeCount(HrmSalaryMonthEmployeeRecordPageReqVO reqVO);

    /**
     * 获得员工绩效系数 Map
     *
     * @param reqVO 查询条件
     * @return 员工编号与绩效系数的映射
     */
    Map<Long, BigDecimal> getPerformanceCoefficientMap(HrmSalaryPerformanceCoefficientReqVO reqVO);

    /**
     * 获得指定员工的月度工资分页
     *
     * @param reqVO 分页查询
     * @return 员工月度工资分页
     */
    PageResult<HrmSalaryMonthEmployeeRecordDO> getEmployeeMonthRecordPage(
            HrmSalaryEmployeeMonthRecordPageReqVO reqVO);

}
