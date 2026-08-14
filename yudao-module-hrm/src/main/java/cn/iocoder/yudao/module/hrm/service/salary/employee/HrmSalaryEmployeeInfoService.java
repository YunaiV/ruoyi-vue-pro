package cn.iocoder.yudao.module.hrm.service.salary.employee;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateListRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.employeeinfo.HrmSalaryEmployeeInfoImportRespVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.employee.HrmSalaryEmployeeInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryOptionDO;
import javax.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 员工薪资信息 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmSalaryEmployeeInfoService {

    /**
     * 获得员工薪资信息分页
     *
     * @param reqVO 分页查询
     * @return 员工分页
     */
    PageResult<HrmEmployeeDO> getSalaryEmployeeInfoPage(HrmSalaryEmployeeInfoPageReqVO reqVO);

    /**
     * 获得员工薪资信息状态数量
     *
     * @param reqVO 查询条件
     * @return 状态与员工数量的映射
     */
    Map<Integer, Long> getSalaryEmployeeInfoStatusCount(HrmSalaryEmployeeInfoPageReqVO reqVO);

    /**
     * 修改员工薪资信息
     *
     * <p>首次定薪时创建当前薪资信息；立即调薪时更新当前薪资信息；延迟调薪时创建待生效记录。</p>
     *
     * @param reqVO 员工定薪或调薪信息
     * @return 定薪或调薪记录编号
     */
    Long updateSalaryEmployeeInfo(@Valid HrmSalaryEmployeeInfoUpdateReqVO reqVO);

    /**
     * 批量更新员工薪资信息
     *
     * @param reqVO 批量调薪信息
     * @return 批量调薪结果
     */
    HrmSalaryEmployeeInfoUpdateListRespVO updateSalaryEmployeeInfoList(
            @Valid HrmSalaryEmployeeInfoUpdateListReqVO reqVO);

    /**
     * 获得员工薪资信息
     *
     * @param employeeId 员工编号
     * @return 员工薪资信息
     */
    HrmSalaryEmployeeInfoDO getSalaryEmployeeInfoByEmployeeId(Long employeeId);

    /**
     * 获得员工薪资信息列表
     *
     * @param employeeIds 员工编号集合
     * @return 员工薪资信息列表
     */
    List<HrmSalaryEmployeeInfoDO> getSalaryEmployeeInfoList(Collection<Long> employeeIds);

    /**
     * 获得员工薪资信息 Map
     *
     * @param employeeIds 员工编号集合
     * @return 员工编号与薪资信息的映射
     */
    default Map<Long, HrmSalaryEmployeeInfoDO> getSalaryEmployeeInfoMap(Collection<Long> employeeIds) {
        return convertMap(getSalaryEmployeeInfoList(employeeIds), HrmSalaryEmployeeInfoDO::getEmployeeId);
    }

    // ==================== 内部调用 ====================

    /**
     * 应用指定日期前已到期的调薪记录
     *
     * @param targetDate 目标日期
     * @return 受影响的员工编号列表
     */
    List<Long> applyDueSalaryChanges(LocalDate targetDate);

    /**
     * 获得员工在工资周期内的生效工资项
     *
     * <p>试用期转正或调薪在周期内生效时，按生效前后的自然日比例混合计算。</p>
     *
     * @param employee 员工
     * @param salaryTimes 工资周期双闭区间
     * @return 生效工资项
     */
    List<HrmSalaryEmployeeInfoDO.SalaryOption> getEffectiveSalaryOptionList(
            HrmEmployeeDO employee, LocalDateTime[] salaryTimes);

    // ==================== 导入 ====================

    /**
     * 导入员工定薪信息
     *
     * @param rows Excel 数据行
     * @return 导入结果
     */
    HrmSalaryEmployeeInfoImportRespVO importFixSalaryList(List<Map<Integer, String>> rows);

    /**
     * 导入员工调薪信息
     *
     * @param rows Excel 数据行
     * @return 导入结果
     */
    HrmSalaryEmployeeInfoImportRespVO importChangeSalaryList(List<Map<Integer, String>> rows);

    /**
     * 获得员工薪资导入使用的薪资项列表
     *
     * @return 薪资项列表
     */
    List<HrmSalaryOptionDO> getSalaryImportOptionList();

}
