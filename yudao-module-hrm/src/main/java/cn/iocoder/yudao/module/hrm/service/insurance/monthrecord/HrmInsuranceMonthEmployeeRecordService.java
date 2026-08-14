package cn.iocoder.yudao.module.hrm.service.insurance.monthrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordCreateListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.monthrecord.employee.HrmInsuranceMonthEmployeeRecordUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.monthrecord.HrmInsuranceMonthRecordDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * HRM 员工月度社保 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmInsuranceMonthEmployeeRecordService {

    /**
     * 创建月度员工社保记录
     *
     * @param monthRecord 月度社保表
     */
    void createMonthEmployeeRecordList(HrmInsuranceMonthRecordDO monthRecord);

    /**
     * 添加月度参保员工
     *
     * @param reqVO 添加信息
     */
    void createMonthEmployeeRecordList(HrmInsuranceMonthEmployeeRecordCreateListReqVO reqVO);

    /**
     * 更新员工月度社保记录
     *
     * @param reqVO 更新信息
     */
    void updateMonthEmployeeRecord(HrmInsuranceMonthEmployeeRecordUpdateReqVO reqVO);

    /**
     * 停止员工参保
     *
     * @param ids 员工月度社保记录编号集合
     */
    void stopMonthEmployeeRecordList(Collection<Long> ids);

    /**
     * 删除指定月度社保表的员工记录
     *
     * @param monthRecordId 月度社保表编号
     */
    void deleteMonthEmployeeRecordListByMonthRecordId(Long monthRecordId);

    /**
     * 获得员工月度社保分页
     *
     * @param reqVO 分页查询
     * @return 员工月度社保分页
     */
    PageResult<HrmInsuranceMonthEmployeeRecordDO> getMonthEmployeeRecordPage(
            HrmInsuranceMonthEmployeeRecordPageReqVO reqVO);

    /**
     * 获得员工月度社保记录
     *
     * @param id 员工月度社保记录编号
     * @return 员工月度社保记录
     */
    HrmInsuranceMonthEmployeeRecordDO getMonthEmployeeRecord(Long id);

    /**
     * 获得指定员工的月度社保记录
     *
     * @param employeeId 员工编号
     * @param year 年份
     * @return 月度社保记录列表
     */
    List<HrmInsuranceMonthEmployeeRecordDO> getMonthEmployeeRecordListByEmployeeIdAndYear(
            Long employeeId, Integer year);

    /**
     * 获得指定月度社保表的员工记录
     *
     * @param monthRecordId 月度社保表编号
     * @return 员工月度社保记录列表
     */
    List<HrmInsuranceMonthEmployeeRecordDO> getMonthEmployeeRecordListByMonthRecordId(Long monthRecordId);

    /**
     * 获得使用指定社保方案的月度记录数量
     *
     * @param schemeId 社保方案编号
     * @return 月度记录数量
     */
    long getMonthEmployeeRecordCountBySchemeId(Long schemeId);

    /**
     * 获得各社保方案的历史月记录数量
     *
     * @param schemeIds 社保方案编号集合
     * @return 社保方案编号与历史月记录数量的映射
     */
    Map<Long, Long> getMonthEmployeeRecordCountMapBySchemeIds(Collection<Long> schemeIds);

    /**
     * 将指定社保方案下的月度员工记录迁移到新方案
     *
     * @param schemeId 旧社保方案编号
     * @param newSchemeId 新社保方案编号
     */
    void updateInsuranceMonthEmployeeRecordSchemeIdBySchemeId(Long schemeId, Long newSchemeId);

    /**
     * 获得指定月度社保表的未参保员工列表
     *
     * @param monthRecordId 月度社保表编号
     * @return 未参保员工列表
     */
    List<HrmEmployeeDO> getUninsuredEmployeeList(Long monthRecordId);

    /**
     * 获得指定月份正常参保的员工记录 Map
     *
     * @param year 年份
     * @param month 月份
     * @return 员工编号与月度社保记录的映射
     */
    Map<Long, HrmInsuranceMonthEmployeeRecordDO> getNormalMonthEmployeeRecordMap(Integer year, Integer month);

}
