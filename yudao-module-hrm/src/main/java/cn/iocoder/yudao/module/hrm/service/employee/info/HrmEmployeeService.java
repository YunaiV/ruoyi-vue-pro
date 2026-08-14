package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeConvertToFullTimeReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeDemoteReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeePromoteReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeRegularReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeTransferReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeImportExcelVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeImportRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeCreateFromUserReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeCancelQuitReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeConfirmEntryReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeQuitReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeRehireReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeNotifyRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.employee.vo.employee.HrmPortalEmployeeUpdateReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * HRM 员工档案 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmEmployeeService {

    /**
     * 确认员工入职
     *
     * @param reqVO 员工固定字段
     */
    void confirmEmployeeEntry(@Valid HrmEmployeeConfirmEntryReqVO reqVO);

    /**
     * 办理离职员工再入职
     *
     * @param reqVO 再入职信息
     */
    void rehireEmployee(@Valid HrmEmployeeRehireReqVO reqVO);

    /**
     * 办理员工转正
     *
     * @param reqVO 异动信息
     */
    void regularEmployee(@Valid HrmEmployeeRegularReqVO reqVO);

    /**
     * 办理员工调岗
     *
     * @param reqVO 异动信息
     */
    void transferEmployee(@Valid HrmEmployeeTransferReqVO reqVO);

    /**
     * 办理员工晋升
     *
     * @param reqVO 异动信息
     */
    void promoteEmployee(@Valid HrmEmployeePromoteReqVO reqVO);

    /**
     * 办理员工降级
     *
     * @param reqVO 异动信息
     */
    void demoteEmployee(@Valid HrmEmployeeDemoteReqVO reqVO);

    /**
     * 办理员工转为全职
     *
     * @param reqVO 异动信息
     */
    void convertEmployeeToFullTime(@Valid HrmEmployeeConvertToFullTimeReqVO reqVO);

    /**
     * 应用一条员工异动记录
     *
     * @param changeRecord 异动记录
     */
    boolean applyEmployeeChange(HrmEmployeeChangeRecordDO changeRecord);

    /**
     * 获得到期且仍处于试用期的员工列表
     *
     * @param deadlineTime 截止时间
     * @return 员工列表
     */
    List<HrmEmployeeDO> getDueRegularEmployeeList(LocalDateTime deadlineTime);

    /**
     * 应用一名员工的到期转正
     *
     * @param employeeId 员工编号
     */
    boolean applyEmployeeRegular(Long employeeId);

    /**
     * 应用一名员工的到期离职
     *
     * @param quitInfo 离职信息
     * @return 是否应用成功
     */
    boolean applyEmployeeQuit(HrmEmployeeQuitInfoDO quitInfo);

    /**
     * 办理员工离职
     *
     * @param reqVO 离职信息
     */
    void quitEmployee(@Valid HrmEmployeeQuitReqVO reqVO);

    /**
     * 取消员工离职
     *
     * @param reqVO 取消离职信息
     */
    void cancelEmployeeQuit(@Valid HrmEmployeeCancelQuitReqVO reqVO);

    /**
     * 创建员工档案
     *
     * @param createReqVO 员工档案信息
     * @return 员工档案编号
     */
    Long createEmployee(@Valid HrmEmployeeSaveReqVO createReqVO);

    /**
     * 从未建档的后台用户批量创建员工档案
     *
     * @param createReqVOList 批量创建信息
     * @return 员工档案编号列表
     */
    List<Long> createEmployeeList(@Valid List<HrmEmployeeCreateFromUserReqVO> createReqVOList);

    /**
     * 获得已经建立员工档案的后台用户编号列表
     *
     * @return 后台用户编号列表
     */
    List<Long> getBoundUserIdList();

    /**
     * 发送填写员工档案通知
     *
     * @param employeeIds 员工编号列表
     * @return 发送结果
     */
    HrmEmployeeNotifyRespVO sendEmployeeProfileFillMessage(List<Long> employeeIds);

    /**
     * 更新员工档案
     *
     * @param updateReqVO 员工档案信息
     */
    void updateEmployee(@Valid HrmEmployeeSaveReqVO updateReqVO);

    /**
     * 删除员工档案
     *
     * @param id 员工档案编号
     */
    void deleteEmployee(Long id);

    /**
     * 批量删除员工档案
     *
     * @param ids 员工档案编号集合
     */
    void deleteEmployeeList(List<Long> ids);

    /**
     * 将指定招聘渠道的员工迁移到新渠道
     *
     * @param channelId 原招聘渠道编号
     * @param newChannelId 新招聘渠道编号
     */
    void updateEmployeeChannelByChannelId(Long channelId, Long newChannelId);

    /**
     * 获得员工档案
     *
     * @param id 员工档案编号
     * @return 员工档案
     */
    HrmEmployeeDO getEmployee(Long id);

    /**
     * 获得后台用户对应的员工档案
     *
     * @param userId 后台用户编号
     * @return 员工档案
     */
    HrmEmployeeDO getEmployeeByUserId(Long userId);

    /**
     * 获得指定工号的员工档案
     *
     * @param jobNumber 工号
     * @return 员工档案
     */
    HrmEmployeeDO getEmployeeByJobNumber(String jobNumber);

    /**
     * 校验员工档案是否存在
     *
     * @param id 员工档案编号
     * @return 员工档案
     */
    HrmEmployeeDO validateEmployeeExists(Long id);

    /**
     * 校验员工档案是否存在，并锁定员工记录
     *
     * @param id 员工档案编号
     * @return 员工档案
     */
    HrmEmployeeDO validateEmployeeExistsForUpdate(Long id);

    /**
     * 校验员工档案是否都存在
     *
     * @param ids 员工档案编号集合
     */
    void validateEmployeeListExists(Collection<Long> ids);

    /**
     * 校验员工档案是否都处于指定入职状态
     *
     * @param ids 员工档案编号集合
     * @param entryStatus 入职状态
     */
    void validateEmployeeListByEntryStatus(Collection<Long> ids, Integer entryStatus);

    /**
     * 获得招聘候选人对应的员工
     *
     * @param candidateId 招聘候选人编号
     * @return 员工
     */
    HrmEmployeeDO getEmployeeByCandidateId(Long candidateId);

    /**
     * 获得招聘候选人对应的员工列表
     *
     * @param candidateIds 招聘候选人编号集合
     * @return 员工列表
     */
    List<HrmEmployeeDO> getEmployeeListByCandidateIds(Collection<Long> candidateIds);

    /**
     * 获得招聘候选人对应的员工 Map
     *
     * @param candidateIds 招聘候选人编号集合
     * @return 招聘候选人编号与员工的映射
     */
    default Map<Long, HrmEmployeeDO> getEmployeeMapByCandidateIds(Collection<Long> candidateIds) {
        return convertMap(getEmployeeListByCandidateIds(candidateIds), HrmEmployeeDO::getCandidateId);
    }

    /**
     * 获得员工档案列表
     *
     * @param ids 员工档案编号集合
     * @return 员工档案列表
     */
    List<HrmEmployeeDO> getEmployeeListByIds(Collection<Long> ids);

    /**
     * 获得指定入职状态的员工列表
     *
     * @param entryStatuses 入职状态集合
     * @return 员工列表
     */
    List<HrmEmployeeDO> getEmployeeListByEntryStatus(Collection<Integer> entryStatuses);

    /**
     * 获得指定部门的员工列表
     *
     * @param deptIds 部门编号集合
     * @return 员工列表
     */
    List<HrmEmployeeDO> getEmployeeListByDeptIds(Collection<Long> deptIds);

    /**
     * 获得指定直属上级的员工列表
     *
     * @param leaderEmployeeId 直属上级员工编号
     * @return 员工列表
     */
    List<HrmEmployeeDO> getEmployeeListByLeaderEmployeeId(Long leaderEmployeeId);

    /**
     * 获得员工档案 Map
     *
     * @param ids 员工档案编号集合
     * @return 员工档案 Map
     */
    default Map<Long, HrmEmployeeDO> getEmployeeMap(Collection<Long> ids) {
        return convertMap(getEmployeeListByIds(ids), HrmEmployeeDO::getId);
    }

    /**
     * 获得员工档案分页
     *
     * @param pageReqVO 分页查询
     * @return 员工档案分页
     */
    PageResult<HrmEmployeeDO> getEmployeePage(HrmEmployeePageReqVO pageReqVO);

    /**
     * 获得员工档案列表
     *
     * @param listReqVO 列表查询
     * @return 员工档案列表
     */
    List<HrmEmployeeDO> getEmployeeList(HrmEmployeeListReqVO listReqVO);

    /**
     * 导入员工档案
     *
     * @param importEmployees 导入员工列表
     * @param duplicateStrategy 重复员工处理策略
     * @return 导入结果
     */
    HrmEmployeeImportRespVO importEmployeeList(List<HrmEmployeeImportExcelVO> importEmployees,
                                               Integer duplicateStrategy);

    /**
     * 获得员工状态数量
     *
     * @param pageReqVO 分页查询
     * @return 状态页签与数量的映射
     */
    Map<Integer, Long> getEmployeeStatusCount(HrmEmployeePageReqVO pageReqVO);

    /**
     * 获得各部门在职员工的聘用形式数量
     *
     * @return 部门编号、聘用形式与数量的映射
     */
    Map<Long, Map<Integer, Long>> getEmployeeCountMapByDeptAndType();

    /**
     * 获得首页员工概况数量
     *
     * @return 首页概况类型与员工数量的映射
     */
    Map<Integer, Long> getEmployeeSurveyCountMap();

    // ==================== 员工个人操作 ====================

    /**
     * 校验当前用户对应的员工档案是否存在
     *
     * @param userId 系统用户编号
     * @return 员工档案
     */
    HrmEmployeeDO validateEmployeeBySelf(Long userId);

    /**
     * 更新当前用户自己的员工档案：仅更新员工档案设置允许员工本人维护的字段
     *
     * @param userId 系统用户编号
     * @param updateReqVO 员工档案信息
     */
    void updateEmployeeBySelf(Long userId, @Valid HrmPortalEmployeeUpdateReqVO updateReqVO);

}
