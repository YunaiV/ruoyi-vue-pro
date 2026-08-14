package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.module.hrm.service.employee.config.HrmEmployeeFieldConfigService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeQuitInfoService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeSalaryCardService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.validation.ValidationUtils;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeChangeRecordCreateReqVO;
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
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeNotifyRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeQuitReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeRehireReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.employee.vo.employee.HrmPortalEmployeeUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.quitinfo.HrmEmployeeQuitInfoSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.salarycard.HrmEmployeeSalaryCardSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.insurance.vo.employeeinfo.HrmInsuranceEmployeeInfoSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeeMapper;
import cn.iocoder.yudao.module.hrm.enums.MessageTemplateConstants;
import cn.iocoder.yudao.module.hrm.enums.employee.config.HrmEmployeeArchiveFieldEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeIdTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeImportDuplicateStrategyEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeQuitTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusTabEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeSurveyTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import cn.iocoder.yudao.module.hrm.service.insurance.employee.HrmInsuranceEmployeeInfoService;
import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import cn.iocoder.yudao.module.hrm.service.recruit.candidate.HrmRecruitCandidateService;
import cn.iocoder.yudao.module.hrm.service.recruit.config.HrmRecruitChannelService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.notify.dto.NotifySendSingleToUserReqDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.afterNow;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.beforeOrEqualNow;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getDayBeginTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.getYearsBetween;
import static cn.iocoder.yudao.module.hrm.framework.excel.core.HrmRecruitChannelExcelColumnSelectFunction.parseOption;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CONFIRM_ENTRY_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CREATE_USER_LIST_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CREATE_USER_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CHANGE_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CHANGE_TYPE_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_ENTRY_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_IMPORT_LIST_IS_EMPTY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_IMPORT_DUPLICATE_STRATEGY_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_IMPORT_REFERENCE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_JOB_NUMBER_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_LEADER_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_MOBILE_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_PROFILE_FIELD_REQUIRED;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_QUIT_CANCEL_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_QUIT_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_QUIT_TIME_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_REHIRE_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_USER_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CHANNEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CREATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CREATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CREATE_FROM_USER_LIST_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CANCEL_QUIT_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CANCEL_QUIT_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CONFIRM_ENTRY_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_CONFIRM_ENTRY_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_DELETE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_DELETE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_DEMOTE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_DEMOTE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_FULL_TIME_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_FULL_TIME_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_PROFILE_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_PROFILE_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_PROMOTE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_PROMOTE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_QUIT_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_QUIT_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_REGULAR_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_REGULAR_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_REHIRE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_REHIRE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_TRANSFER_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_TRANSFER_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_UPDATE_SUB_TYPE;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_UPDATE_SUCCESS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_ARCHIVE_FILL_MESSAGE_SUB_TYPE;

/**
 * HRM 员工档案 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class HrmEmployeeServiceImpl implements HrmEmployeeService {

    @Resource
    private HrmEmployeeMapper employeeMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private ILogRecordService logRecordService;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmRecruitChannelService recruitChannelService;
    @Resource
    private HrmEmployeeFieldConfigService employeeFieldConfigService;
    @Resource
    private HrmEmployeeChangeRecordService employeeChangeRecordService;
    @Resource
    private HrmEmployeeQuitInfoService employeeQuitInfoService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmEmployeeSalaryCardService employeeSalaryCardService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmInsuranceEmployeeInfoService insuranceEmployeeInfoService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmInsuranceSchemeService insuranceSchemeService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private HrmRecruitCandidateService recruitCandidateService;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CONFIRM_ENTRY_SUB_TYPE,
            bizNo = "{{#reqVO.id}}", success = HRM_EMPLOYEE_CONFIRM_ENTRY_SUCCESS)
    public void confirmEmployeeEntry(HrmEmployeeConfirmEntryReqVO reqVO) {
        // 1. 校验员工状态和完整固定字段
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getId());
        if (ObjUtil.notEqual(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus(), employee.getEntryStatus())) {
            throw exception(EMPLOYEE_CONFIRM_ENTRY_STATUS_INVALID);
        }
        reqVO.setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        ValidationUtils.validate(reqVO);
        employeeFieldConfigService.validateEmployeeCreateFields(
                reqVO, HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        validateEmployeeForCreateOrUpdate(employee.getId(), reqVO);

        // 2. 保存完整员工固定字段并确认入职
        HrmEmployeeDO updateObj = buildEmployee(reqVO).setId(employee.getId())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()).setLeaveTime(null);
        employeeMapper.updateForEntryById(updateObj);

        // 3. 同步招聘候选人状态
        if (employee.getCandidateId() != null) {
            recruitCandidateService.confirmRecruitCandidateEntry(
                    employee.getCandidateId(), updateObj.getEntryTime());
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_REHIRE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_REHIRE_SUCCESS)
    public void rehireEmployee(HrmEmployeeRehireReqVO reqVO) {
        // 1. 校验员工和再入职信息
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getEmployeeId());
        if (ObjUtil.notEqual(HrmEmployeeEntryStatusEnum.LEFT.getStatus(), employee.getEntryStatus())) {
            throw exception(EMPLOYEE_REHIRE_STATUS_INVALID);
        }
        reqVO.setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        ValidationUtils.validate(reqVO);
        employeeFieldConfigService.validateEmployeeCreateFields(
                reqVO, HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
        validateEmployeeForCreateOrUpdate(employee.getId(), reqVO);

        // 2.1 计算试用期和转正日期
        int probation = defaultIfNull(reqVO.getProbation(), 0);
        Integer status = reqVO.getStatus();
        LocalDateTime regularTime = null;
        if (HrmEmployeeTypeEnum.FORMAL.getType().equals(reqVO.getType())) {
            regularTime = reqVO.getEntryTime().plusMonths(probation);
            status = probation > 0 && regularTime.isAfter(LocalDateTime.now())
                    ? HrmEmployeeStatusEnum.PROBATION.getStatus() : HrmEmployeeStatusEnum.REGULAR.getStatus();
        } else {
            probation = 0;
        }
        LocalDateTime companyAgeStartTime = defaultIfNull(
                reqVO.getCompanyAgeStartTime(), reqVO.getEntryTime());
        // 2.2 保存再入职异动记录
        createEmployeeChangeRecord(BeanUtils.toBean(
                reqVO, HrmEmployeeChangeRecordCreateReqVO.class)
                .setNewDeptId(reqVO.getDeptId()).setNewPostName(reqVO.getPostName())
                .setNewPostLevel(reqVO.getPostLevel()).setNewWorkAddress(reqVO.getWorkAddress())
                .setNewLeaderEmployeeId(reqVO.getLeaderEmployeeId())
                .setProbation(probation).setEffectTime(reqVO.getEntryTime()),
                employee, HrmEmployeeChangeTypeEnum.REHIRE.getType());
        // 2.3 清理原离职信息
        employeeQuitInfoService.deleteEmployeeQuitInfo(employee.getId());

        // 3. 更新员工档案
        HrmEmployeeDO updateObj = BeanUtils.toBean(reqVO, HrmEmployeeDO.class);
        normalizeEmployeePersonalInfo(updateObj);
        updateObj.setId(employee.getId())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()).setEntryTime(reqVO.getEntryTime())
                .setCompanyAgeStartTime(companyAgeStartTime)
                .setCompanyAge(getYearsBetween(companyAgeStartTime.toLocalDate(), LocalDate.now()))
                .setType(reqVO.getType()).setStatus(status).setProbation(probation).setRegularTime(regularTime)
                .setDeptId(reqVO.getDeptId()).setLeaderEmployeeId(reqVO.getLeaderEmployeeId())
                .setPostName(reqVO.getPostName()).setPostLevel(reqVO.getPostLevel())
                .setWorkCity(reqVO.getWorkCity()).setWorkAddress(reqVO.getWorkAddress())
                .setWorkDetailAddress(reqVO.getWorkDetailAddress()).setLeaveTime(null);
        employeeMapper.updateForEntryById(updateObj);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_REGULAR_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_REGULAR_SUCCESS)
    public void regularEmployee(HrmEmployeeRegularReqVO reqVO) {
        // 1. 校验员工
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getEmployeeId());
        validateEmployeeInService(employee, HrmEmployeeChangeTypeEnum.REGULAR);
        if (ObjUtil.notEqual(HrmEmployeeStatusEnum.PROBATION.getStatus(), employee.getStatus())) {
            throw exception(EMPLOYEE_CHANGE_STATUS_INVALID, HrmEmployeeChangeTypeEnum.REGULAR.getName());
        }
        HrmEmployeeChangeRecordCreateReqVO changeRecord = fillUnchangedPositionFields(
                BeanUtils.toBean(reqVO, HrmEmployeeChangeRecordCreateReqVO.class), employee);
        validatePositionChange(employee, HrmEmployeeChangeTypeEnum.REGULAR,
                changeRecord.getNewDeptId(), changeRecord.getNewLeaderEmployeeId());

        // 2. 创建转正记录
        HrmEmployeeChangeRecordDO savedChangeRecord = createEmployeeChangeRecord(
                changeRecord, employee, HrmEmployeeChangeTypeEnum.REGULAR.getType());

        // 3. 同步预约转正时间，并立即应用已到期的转正
        employeeMapper.updateById(new HrmEmployeeDO().setId(employee.getId())
                .setRegularTime(savedChangeRecord.getEffectTime()));
        if (beforeOrEqualNow(savedChangeRecord.getEffectTime())) {
            applyEmployeeChange(savedChangeRecord);
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_TRANSFER_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_TRANSFER_SUCCESS)
    public void transferEmployee(HrmEmployeeTransferReqVO reqVO) {
        // 1. 校验员工
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getEmployeeId());
        validatePositionChange(employee, HrmEmployeeChangeTypeEnum.TRANSFER,
                reqVO.getNewDeptId(), reqVO.getNewLeaderEmployeeId());

        // 2. 创建调岗记录
        HrmEmployeeChangeRecordDO changeRecord = createEmployeeChangeRecord(
                BeanUtils.toBean(reqVO, HrmEmployeeChangeRecordCreateReqVO.class),
                employee, HrmEmployeeChangeTypeEnum.TRANSFER.getType());

        // 3. 立即应用已生效的调岗
        if (beforeOrEqualNow(changeRecord.getEffectTime())) {
            employeeMapper.updatePositionById(changeRecord.getEmployeeId(), changeRecord.getNewDeptId(),
                    changeRecord.getNewPostName(), changeRecord.getNewPostLevel(),
                    changeRecord.getNewWorkAddress(), changeRecord.getNewLeaderEmployeeId());
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_PROMOTE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_PROMOTE_SUCCESS)
    public void promoteEmployee(HrmEmployeePromoteReqVO reqVO) {
        // 1. 校验员工
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getEmployeeId());
        validatePositionChange(employee, HrmEmployeeChangeTypeEnum.PROMOTION,
                reqVO.getNewDeptId(), reqVO.getNewLeaderEmployeeId());

        // 2. 创建晋升记录
        HrmEmployeeChangeRecordDO changeRecord = createEmployeeChangeRecord(
                BeanUtils.toBean(reqVO, HrmEmployeeChangeRecordCreateReqVO.class),
                employee, HrmEmployeeChangeTypeEnum.PROMOTION.getType());

        // 3. 立即应用已生效的晋升
        if (beforeOrEqualNow(changeRecord.getEffectTime())) {
            employeeMapper.updatePositionById(changeRecord.getEmployeeId(), changeRecord.getNewDeptId(),
                    changeRecord.getNewPostName(), changeRecord.getNewPostLevel(),
                    changeRecord.getNewWorkAddress(), changeRecord.getNewLeaderEmployeeId());
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_DEMOTE_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_DEMOTE_SUCCESS)
    public void demoteEmployee(HrmEmployeeDemoteReqVO reqVO) {
        // 1. 校验员工
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getEmployeeId());
        validatePositionChange(employee, HrmEmployeeChangeTypeEnum.DEMOTION,
                reqVO.getNewDeptId(), reqVO.getNewLeaderEmployeeId());

        // 2. 创建降级记录
        HrmEmployeeChangeRecordDO changeRecord = createEmployeeChangeRecord(
                BeanUtils.toBean(reqVO, HrmEmployeeChangeRecordCreateReqVO.class),
                employee, HrmEmployeeChangeTypeEnum.DEMOTION.getType());

        // 3. 立即应用已生效的降级
        if (beforeOrEqualNow(changeRecord.getEffectTime())) {
            employeeMapper.updatePositionById(changeRecord.getEmployeeId(), changeRecord.getNewDeptId(),
                    changeRecord.getNewPostName(), changeRecord.getNewPostLevel(),
                    changeRecord.getNewWorkAddress(), changeRecord.getNewLeaderEmployeeId());
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_FULL_TIME_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_FULL_TIME_SUCCESS)
    public void convertEmployeeToFullTime(HrmEmployeeConvertToFullTimeReqVO reqVO) {
        // 1. 校验员工
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getEmployeeId());
        validateEmployeeInService(employee, HrmEmployeeChangeTypeEnum.FULL_TIME);
        if (!HrmEmployeeStatusEnum.CONVERTIBLE_TO_FULL_TIME_STATUSES.contains(employee.getStatus())) {
            throw exception(EMPLOYEE_CHANGE_STATUS_INVALID, HrmEmployeeChangeTypeEnum.FULL_TIME.getName());
        }
        HrmEmployeeChangeRecordCreateReqVO changeRecord = fillUnchangedPositionFields(
                BeanUtils.toBean(reqVO, HrmEmployeeChangeRecordCreateReqVO.class), employee);
        validatePositionChange(employee, HrmEmployeeChangeTypeEnum.FULL_TIME,
                changeRecord.getNewDeptId(), changeRecord.getNewLeaderEmployeeId());

        // 2. 创建转为全职记录
        HrmEmployeeChangeRecordDO savedChangeRecord = createEmployeeChangeRecord(
                changeRecord, employee, HrmEmployeeChangeTypeEnum.FULL_TIME.getType());

        // 3. 立即应用已生效的转为全职
        if (beforeOrEqualNow(savedChangeRecord.getEffectTime())) {
            applyEmployeeChange(savedChangeRecord);
        }

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyEmployeeChange(HrmEmployeeChangeRecordDO changeRecord) {
        // 1. 校验异动类型
        if (ObjectUtils.notEqualsAny(changeRecord.getType(), HrmEmployeeChangeTypeEnum.REGULAR.getType(),
                HrmEmployeeChangeTypeEnum.TRANSFER.getType(), HrmEmployeeChangeTypeEnum.PROMOTION.getType(),
                HrmEmployeeChangeTypeEnum.DEMOTION.getType(), HrmEmployeeChangeTypeEnum.FULL_TIME.getType())) {
            throw exception(EMPLOYEE_CHANGE_TYPE_INVALID, changeRecord.getType());
        }

        // 2. 已生效记录、未到期记录和非在职员工不再重复处理
        if (changeRecord.getAppliedTime() != null || afterNow(changeRecord.getEffectTime())) {
            return false;
        }
        HrmEmployeeDO employee = employeeMapper.selectById(changeRecord.getEmployeeId());
        if (employee == null || !HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(employee.getEntryStatus())) {
            employeeChangeRecordService.updateEmployeeChangeRecordAppliedTime(changeRecord.getId(), LocalDateTime.now());
            return false;
        }

        // 3. 按异动类型更新员工主档
        if (HrmEmployeeChangeTypeEnum.REGULAR.getType().equals(changeRecord.getType())) {
            if (ObjUtil.notEqual(employee.getStatus(), HrmEmployeeStatusEnum.PROBATION.getStatus())) {
                employeeChangeRecordService.updateEmployeeChangeRecordAppliedTime(
                        changeRecord.getId(), LocalDateTime.now());
                return false;
            }
            fillUnchangedPositionFields(changeRecord, employee);
            employeeMapper.updateById(new HrmEmployeeDO().setId(changeRecord.getEmployeeId())
                    .setRegularTime(changeRecord.getEffectTime())
                    .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus()));
            employeeMapper.updatePositionById(changeRecord.getEmployeeId(), changeRecord.getNewDeptId(),
                    changeRecord.getNewPostName(), changeRecord.getNewPostLevel(),
                    changeRecord.getNewWorkAddress(), changeRecord.getNewLeaderEmployeeId());
        } else if (ObjectUtils.equalsAny(changeRecord.getType(), HrmEmployeeChangeTypeEnum.TRANSFER.getType(),
                HrmEmployeeChangeTypeEnum.PROMOTION.getType(), HrmEmployeeChangeTypeEnum.DEMOTION.getType())) {
            employeeMapper.updatePositionById(changeRecord.getEmployeeId(), changeRecord.getNewDeptId(),
                    changeRecord.getNewPostName(), changeRecord.getNewPostLevel(),
                    changeRecord.getNewWorkAddress(), changeRecord.getNewLeaderEmployeeId());
        } else if (HrmEmployeeChangeTypeEnum.FULL_TIME.getType().equals(changeRecord.getType())) {
            if (!HrmEmployeeStatusEnum.CONVERTIBLE_TO_FULL_TIME_STATUSES.contains(employee.getStatus())) {
                employeeChangeRecordService.updateEmployeeChangeRecordAppliedTime(
                        changeRecord.getId(), LocalDateTime.now());
                return false;
            }
            fillUnchangedPositionFields(changeRecord, employee);
            int probation = defaultIfNull(changeRecord.getProbation(), 0);
            LocalDateTime regularTime = changeRecord.getEffectTime().plusMonths(probation);
            employeeMapper.updateById(new HrmEmployeeDO().setId(changeRecord.getEmployeeId())
                    .setType(HrmEmployeeTypeEnum.FORMAL.getType())
                    .setStatus(probation == 0 ? HrmEmployeeStatusEnum.REGULAR.getStatus()
                            : HrmEmployeeStatusEnum.PROBATION.getStatus())
                    .setProbation(probation).setRegularTime(regularTime));
            employeeMapper.updatePositionById(changeRecord.getEmployeeId(), changeRecord.getNewDeptId(),
                    changeRecord.getNewPostName(), changeRecord.getNewPostLevel(),
                    changeRecord.getNewWorkAddress(), changeRecord.getNewLeaderEmployeeId());
        }

        // 4. 标记异动记录已实际生效
        employeeChangeRecordService.updateEmployeeChangeRecordAppliedTime(changeRecord.getId(), LocalDateTime.now());
        return true;
    }

    @Override
    public List<HrmEmployeeDO> getDueRegularEmployeeList(LocalDateTime deadlineTime) {
        return employeeMapper.selectListByRegularTimeBeforeOrEqual(deadlineTime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyEmployeeRegular(Long employeeId) {
        // 1. 只处理仍在职、处于试用期且转正时间已到的员工
        HrmEmployeeDO employee = employeeMapper.selectById(employeeId);
        if (employee == null || !HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(employee.getEntryStatus())
                || ObjUtil.notEqual(employee.getStatus(), HrmEmployeeStatusEnum.PROBATION.getStatus())
                || employee.getRegularTime() == null || afterNow(employee.getRegularTime())) {
            return false;
        }
        // 2. 将员工状态更新为正式
        employeeMapper.updateById(new HrmEmployeeDO().setId(employeeId)
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus()));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyEmployeeQuit(HrmEmployeeQuitInfoDO quitInfo) {
        // 1. 校验员工
        HrmEmployeeDO employee = employeeMapper.selectById(quitInfo.getEmployeeId());
        if (employee == null
                || ObjUtil.notEqual(employee.getEntryStatus(), HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                || quitInfo.getPlanQuitTime() == null || afterNow(quitInfo.getPlanQuitTime())) {
            return false;
        }

        // 2. 更新员工离职状态
        LocalDate companyAgeStartDate = employee.getCompanyAgeStartTime() == null
                ? null : employee.getCompanyAgeStartTime().toLocalDate();
        employeeMapper.updateById(new HrmEmployeeDO().setId(employee.getId())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setLeaveTime(quitInfo.getPlanQuitTime())
                .setCompanyAge(getYearsBetween(companyAgeStartDate, quitInfo.getPlanQuitTime().toLocalDate())));
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_QUIT_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_QUIT_SUCCESS)
    public void quitEmployee(HrmEmployeeQuitReqVO reqVO) {
        // 1. 校验员工状态和离职信息
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getEmployeeId());
        if (ObjectUtils.notEqualsAny(employee.getEntryStatus(), HrmEmployeeEntryStatusEnum.ACTIVE.getStatus(),
                HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus(),
                HrmEmployeeEntryStatusEnum.LEFT.getStatus())) {
            throw exception(EMPLOYEE_QUIT_STATUS_INVALID);
        }
        boolean alreadyLeft = HrmEmployeeEntryStatusEnum.LEFT.getStatus().equals(employee.getEntryStatus());
        if (alreadyLeft && afterNow(reqVO.getPlanQuitTime())) {
            throw exception(EMPLOYEE_QUIT_TIME_INVALID);
        }

        // 2. 保存离职信息
        if (HrmEmployeeQuitTypeEnum.RETIREMENT.getType().equals(reqVO.getType())) {
            reqVO.setReason(null);
        }
        HrmEmployeeQuitInfoSaveReqVO quitInfo = BeanUtils.toBean(reqVO, HrmEmployeeQuitInfoSaveReqVO.class)
                .setApplyQuitTime(getDayBeginTime(reqVO.getApplyQuitTime()))
                .setSalarySettlementTime(getDayBeginTime(reqVO.getSalarySettlementTime()))
                .setOldEmployeeStatus(employee.getStatus());
        employeeQuitInfoService.saveEmployeeQuitInfo(quitInfo);

        // 3. 更新员工离职状态
        boolean effectiveImmediately = beforeOrEqualNow(reqVO.getPlanQuitTime());
        Integer entryStatus = alreadyLeft ? HrmEmployeeEntryStatusEnum.LEFT.getStatus()
                : HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus();
        Integer companyAge = null;
        if (alreadyLeft || effectiveImmediately) {
            entryStatus = HrmEmployeeEntryStatusEnum.LEFT.getStatus();
            companyAge = getYearsBetween(employee.getCompanyAgeStartTime() != null
                    ? employee.getCompanyAgeStartTime().toLocalDate() : null, reqVO.getPlanQuitTime().toLocalDate());
        }
        employeeMapper.updateById(new HrmEmployeeDO().setId(reqVO.getEmployeeId())
                .setEntryStatus(entryStatus).setLeaveTime(reqVO.getPlanQuitTime()).setCompanyAge(companyAge));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CANCEL_QUIT_SUB_TYPE,
            bizNo = "{{#reqVO.employeeId}}", success = HRM_EMPLOYEE_CANCEL_QUIT_SUCCESS)
    public void cancelEmployeeQuit(HrmEmployeeCancelQuitReqVO reqVO) {
        // 1. 校验员工和离职信息
        HrmEmployeeDO employee = validateEmployeeExists(reqVO.getEmployeeId());
        if (ObjUtil.notEqual(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus(), employee.getEntryStatus())) {
            throw exception(EMPLOYEE_QUIT_CANCEL_STATUS_INVALID);
        }
        employeeQuitInfoService.validateQuitInfoByEmployeeId(reqVO.getEmployeeId());

        // 2. 删除离职信息
        employeeQuitInfoService.deleteEmployeeQuitInfo(reqVO.getEmployeeId());

        // 3. 恢复员工在职状态
        employeeMapper.updateById(new HrmEmployeeDO().setId(reqVO.getEmployeeId())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));
        employeeMapper.updateLeaveTimeById(reqVO.getEmployeeId(), null);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_CREATE_SUB_TYPE,
            bizNo = "{{#employee.id}}", success = HRM_EMPLOYEE_CREATE_SUCCESS)
    public Long createEmployee(HrmEmployeeSaveReqVO createReqVO) {
        // 1. 校验员工档案及其关联数据
        employeeFieldConfigService.validateEmployeeCreateFields(createReqVO, createReqVO.getEntryStatus());
        validateEmployeeForCreateOrUpdate(null, createReqVO);

        // 2. 插入员工档案
        HrmEmployeeDO employee = buildEmployee(createReqVO);
        employeeMapper.insert(employee);

        // 3. 事务提交后发送员工端开通通知
        sendEmployeeOpenedMessage(Collections.singletonList(employee));

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
        return employee.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createEmployeeList(List<HrmEmployeeCreateFromUserReqVO> createReqVOList) {
        // 1.1 校验后台用户，并一次性加载用户资料
        Set<Long> userIds = new HashSet<>();
        for (HrmEmployeeCreateFromUserReqVO employeeReqVO : createReqVOList) {
            if (!userIds.add(employeeReqVO.getUserId())) {
                throw exception(EMPLOYEE_CREATE_USER_LIST_DUPLICATE);
            }
        }
        adminUserApi.validateUserList(userIds);
        Map<Long, AdminUserRespDTO> userMap = convertMap(
                adminUserApi.getUserList(userIds), AdminUserRespDTO::getId);
        // 1.2 构建并校验全部员工档案，避免部分数据校验失败时已经执行写库
        Set<String> jobNumbers = new HashSet<>();
        Set<String> mobiles = new HashSet<>();
        List<HrmEmployeeSaveReqVO> saveReqVOList = new ArrayList<>();
        for (HrmEmployeeCreateFromUserReqVO employeeReqVO : createReqVOList) {
            AdminUserRespDTO user = userMap.get(employeeReqVO.getUserId());
            if (user == null) {
                throw exception(EMPLOYEE_CREATE_USER_NOT_EXISTS);
            }
            HrmEmployeeSaveReqVO saveReqVO = BeanUtils.toBean(employeeReqVO, HrmEmployeeSaveReqVO.class);
            employeeFieldConfigService.validateEmployeeCreateFields(
                    saveReqVO, HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
            saveReqVO.setName(user.getNickname()).setEmail(user.getEmail()).setSex(user.getSex())
                    .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                    .setCompanyAgeStartTime(employeeReqVO.getEntryTime());
            if (!jobNumbers.add(saveReqVO.getJobNumber())) {
                throw exception(EMPLOYEE_JOB_NUMBER_DUPLICATE);
            }
            if (!mobiles.add(saveReqVO.getMobile())) {
                throw exception(EMPLOYEE_MOBILE_DUPLICATE);
            }
            ValidationUtils.validate(saveReqVO);
            validateEmployeeForCreateOrUpdate(null, saveReqVO);
            saveReqVOList.add(saveReqVO);
        }

        // 2. 批量插入员工档案
        List<HrmEmployeeDO> employees = convertList(saveReqVOList, this::buildEmployee);
        employeeMapper.insertBatch(employees);

        // 3. 事务提交后发送员工端开通通知，并按员工档案分别记录操作日志
        sendEmployeeOpenedMessage(employees);
        recordEmployeeCreatedLogs(employees);
        return convertList(employees, HrmEmployeeDO::getId);
    }

    @Override
    public List<Long> getBoundUserIdList() {
        return convertList(employeeMapper.selectListWithUserId(), HrmEmployeeDO::getUserId);
    }

    @Override
    public HrmEmployeeNotifyRespVO sendEmployeeProfileFillMessage(List<Long> employeeIds) {
        // 1. 校验并加载员工
        Set<Long> distinctEmployeeIds = new HashSet<>(employeeIds);
        List<HrmEmployeeDO> employees = employeeMapper.selectByIds(distinctEmployeeIds);
        if (employees.size() != distinctEmployeeIds.size()) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }

        // 2. 向已经绑定后台账号的员工发送填写档案通知
        int successCount = 0;
        int skippedCount = 0;
        int failureCount = 0;
        List<HrmEmployeeDO> notifiedEmployees = new ArrayList<>();
        for (HrmEmployeeDO employee : employees) {
            if (employee.getUserId() == null) {
                skippedCount++;
                continue;
            }
            if (sendEmployeeMessage(employee, MessageTemplateConstants.EMPLOYEE_ARCHIVE_FILL)) {
                successCount++;
                notifiedEmployees.add(employee);
            } else {
                failureCount++;
            }
        }
        HrmEmployeeNotifyRespVO notifyResult = new HrmEmployeeNotifyRespVO()
                .setSuccessCount(successCount).setSkippedCount(skippedCount).setFailureCount(failureCount);

        // 3. 按员工档案分别记录操作日志，确保可在员工详情中查询
        recordEmployeeProfileFillMessageLogs(notifiedEmployees);

        // 4. 返回通知结果
        return notifyResult;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_UPDATE_SUB_TYPE,
            bizNo = "{{#updateReqVO.id}}", success = HRM_EMPLOYEE_UPDATE_SUCCESS)
    public void updateEmployee(HrmEmployeeSaveReqVO updateReqVO) {
        // 1. 校验员工档案及其关联数据
        HrmEmployeeDO employee = validateEmployeeForCreateOrUpdate(updateReqVO.getId(), updateReqVO);

        // 2. 更新员工档案
        HrmEmployeeDO updateObj = buildEmployeeForUpdate(updateReqVO, employee);
        employeeMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(employee, HrmEmployeeSaveReqVO.class));
    }

    @Override
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_DELETE_SUB_TYPE,
            bizNo = "{{#employee.id}}", success = HRM_EMPLOYEE_DELETE_SUCCESS)
    public void deleteEmployee(Long id) {
        // 1. 校验员工档案是否存在
        HrmEmployeeDO employee = validateEmployeeExists(id);

        // 2. 删除员工档案
        employeeMapper.deleteById(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployeeList(List<Long> ids) {
        // 1. 查询员工
        List<HrmEmployeeDO> employees = employeeMapper.selectByIds(ids);
        if (CollUtil.isEmpty(employees)) {
            return;
        }

        // 2. 删除员工
        for (HrmEmployeeDO employee : employees) {
            getSelf().deleteEmployee(employee.getId());
        }
    }

    @Override
    public void updateEmployeeChannelByChannelId(Long channelId, Long newChannelId) {
        employeeMapper.updateChannelIdByChannelId(channelId, newChannelId);
    }

    @Override
    public HrmEmployeeDO getEmployee(Long id) {
        if (id == null) {
            return null;
        }
        return employeeMapper.selectById(id);
    }

    @Override
    public HrmEmployeeDO getEmployeeByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return employeeMapper.selectByUserId(userId);
    }

    @Override
    public HrmEmployeeDO getEmployeeByJobNumber(String jobNumber) {
        return employeeMapper.selectByJobNumber(jobNumber);
    }

    @Override
    public HrmEmployeeDO validateEmployeeExists(Long id) {
        HrmEmployeeDO employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

    @Override
    public HrmEmployeeDO validateEmployeeExistsForUpdate(Long id) {
        HrmEmployeeDO employee = employeeMapper.selectByIdForUpdate(id);
        if (employee == null) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

    @Override
    public void validateEmployeeListExists(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        Set<Long> employeeIds = new HashSet<>(ids);
        if (getEmployeeListByIds(employeeIds).size() != employeeIds.size()) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
    }

    @Override
    public void validateEmployeeListByEntryStatus(Collection<Long> ids, Integer entryStatus) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        Set<Long> employeeIds = new HashSet<>(ids);
        List<HrmEmployeeDO> employees = getEmployeeListByIds(employeeIds);
        if (employees.size() != employeeIds.size()) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
        HrmEmployeeDO invalidEmployee = CollUtil.findOne(employees,
                employee -> ObjUtil.notEqual(employee.getEntryStatus(), entryStatus));
        if (invalidEmployee != null) {
            throw exception(EMPLOYEE_ENTRY_STATUS_INVALID, invalidEmployee.getName());
        }
    }

    @Override
    public HrmEmployeeDO getEmployeeByCandidateId(Long candidateId) {
        return employeeMapper.selectByCandidateId(candidateId);
    }

    @Override
    public List<HrmEmployeeDO> getEmployeeListByCandidateIds(Collection<Long> candidateIds) {
        if (CollUtil.isEmpty(candidateIds)) {
            return Collections.emptyList();
        }
        return employeeMapper.selectListByCandidateIds(candidateIds);
    }

    @Override
    public List<HrmEmployeeDO> getEmployeeListByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return employeeMapper.selectByIds(ids);
    }

    @Override
    public List<HrmEmployeeDO> getEmployeeListByEntryStatus(Collection<Integer> entryStatuses) {
        if (CollUtil.isEmpty(entryStatuses)) {
            return Collections.emptyList();
        }
        return employeeMapper.selectListByEntryStatus(entryStatuses);
    }

    @Override
    public List<HrmEmployeeDO> getEmployeeListByDeptIds(Collection<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyList();
        }
        return employeeMapper.selectListByDeptIds(deptIds);
    }

    @Override
    public List<HrmEmployeeDO> getEmployeeListByLeaderEmployeeId(Long leaderEmployeeId) {
        return employeeMapper.selectListByLeaderEmployeeId(leaderEmployeeId);
    }

    @Override
    public PageResult<HrmEmployeeDO> getEmployeePage(HrmEmployeePageReqVO pageReqVO) {
        return employeeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<HrmEmployeeDO> getEmployeeList(HrmEmployeeListReqVO listReqVO) {
        return employeeMapper.selectList(BeanUtils.toBean(listReqVO, HrmEmployeePageReqVO.class));
    }

    @Override
    public HrmEmployeeImportRespVO importEmployeeList(List<HrmEmployeeImportExcelVO> importEmployees,
                                                      Integer duplicateStrategy) {
        // 1. 参数校验
        if (CollUtil.isEmpty(importEmployees)) {
            throw exception(EMPLOYEE_IMPORT_LIST_IS_EMPTY);
        }
        HrmEmployeeImportDuplicateStrategyEnum strategy =
                HrmEmployeeImportDuplicateStrategyEnum.valueOf(duplicateStrategy);
        if (strategy == null) {
            throw exception(EMPLOYEE_IMPORT_DUPLICATE_STRATEGY_INVALID);
        }

        // 2. 遍历，逐个创建 or 更新
        HrmEmployeeImportRespVO respVO = HrmEmployeeImportRespVO.builder()
                .createJobNumbers(new ArrayList<>()).updateJobNumbers(new ArrayList<>())
                .skipJobNumbers(new ArrayList<>())
                .failureJobNumbers(new LinkedHashMap<>()).build();
        // 下拉选项的格式化与反向解析统一收敛到 HrmRecruitChannelExcelColumnSelectFunction。
        List<HrmRecruitChannelDO> recruitChannels = recruitChannelService.getRecruitChannelSimpleList();
        AtomicInteger index = new AtomicInteger(1);
        importEmployees.forEach(importEmployee -> {
            int currentIndex = index.getAndIncrement();
            String employeeLabel = getImportEmployeeLabel(importEmployee, currentIndex);
            String failureKey = getImportFailureKey(employeeLabel, currentIndex);
            HrmEmployeeSaveReqVO saveReqVO = BeanUtils.toBean(importEmployee, HrmEmployeeSaveReqVO.class);
            Long schemeId;
            // 2.1 将 Excel 业务键解析为内部关联编号，并校验员工、工资卡和社保字段
            try {
                schemeId = resolveImportedEmployeeRelations(importEmployee, saveReqVO);
                if (StrUtil.isNotBlank(importEmployee.getChannelName())) {
                    HrmRecruitChannelDO recruitChannel = parseOption(
                            recruitChannels, importEmployee.getChannelName());
                    if (recruitChannel == null) {
                        throw exception(RECRUIT_CHANNEL_NOT_EXISTS);
                    }
                    saveReqVO.setChannelId(recruitChannel.getId());
                }
                ValidationUtils.validate(saveReqVO);
                validateImportedEmployeeResources(importEmployee, schemeId);
            } catch (ConstraintViolationException | ServiceException ex) {
                respVO.getFailureJobNumbers().put(failureKey, ex.getMessage());
                return;
            }

            HrmEmployeeDO existEmployee = getImportExistEmployee(saveReqVO);
            // 2.2 根据重复策略处理已经存在的员工
            if (existEmployee != null) {
                if (strategy == HrmEmployeeImportDuplicateStrategyEnum.SKIP) {
                    respVO.getSkipJobNumbers().add(employeeLabel);
                    return;
                }
                if (strategy == HrmEmployeeImportDuplicateStrategyEnum.FAIL) {
                    respVO.getFailureJobNumbers().put(failureKey, "员工档案已存在");
                    return;
                }
            }

            // 2.3 在独立事务内保存员工及其工资卡、社保信息
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    validateEmployeeForCreateOrUpdate(
                            existEmployee == null ? null : existEmployee.getId(), saveReqVO);
                    HrmEmployeeDO importedEmployee = existEmployee == null
                            ? buildImportedEmployee(saveReqVO)
                            : buildEmployeeForUpdate(saveReqVO, existEmployee);
                    if (existEmployee == null) {
                        employeeMapper.insert(importedEmployee);
                    } else {
                        employeeMapper.updateById(importedEmployee);
                    }
                    saveImportedEmployeeResources(importedEmployee.getId(), importEmployee, schemeId);
                });
                if (existEmployee == null) {
                    respVO.getCreateJobNumbers().add(employeeLabel);
                } else {
                    respVO.getUpdateJobNumbers().add(employeeLabel);
                }
            } catch (Exception ex) {
                if (ex instanceof ConstraintViolationException || ex instanceof ServiceException) {
                    respVO.getFailureJobNumbers().put(failureKey, ex.getMessage());
                } else {
                    log.error("[importEmployeeList][员工({}) 导入异常]", employeeLabel, ex);
                    respVO.getFailureJobNumbers().put(failureKey, "导入异常");
                }
            }
        });
        return respVO;
    }

    @Override
    public Map<Integer, Long> getEmployeeStatusCount(HrmEmployeePageReqVO pageReqVO) {
        Map<Integer, Long> countMap = new LinkedHashMap<>();
        for (HrmEmployeeStatusTabEnum statusTab : HrmEmployeeStatusTabEnum.values()) {
            countMap.put(statusTab.getStatus(), 0L);
        }
        for (Map<String, Object> record : employeeMapper.selectCountListByStatus(pageReqVO)) {
            Integer status = MapUtil.getInt(record, "status");
            Integer entryStatus = MapUtil.getInt(record, "entryStatus");
            Long count = MapUtil.getLong(record, "count");
            if (count == null) {
                continue;
            }
            // 在职人数包含“在职”和“待离职”，并继续按员工状态统计
            if (HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(entryStatus)) {
                addStatusCount(countMap, HrmEmployeeStatusTabEnum.ACTIVE.getStatus(), count);
                if (HrmEmployeeStatusEnum.valueOf(status) != null) {
                    addStatusCount(countMap, status, count);
                }
                if (HrmEmployeeStatusEnum.FULL_TIME_STATUSES.contains(status)) {
                    addStatusCount(countMap, HrmEmployeeStatusTabEnum.FULL_TIME.getStatus(), count);
                }
            }
            if (HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus().equals(entryStatus)) {
                addStatusCount(countMap, HrmEmployeeStatusTabEnum.PENDING_ENTRY.getStatus(), count);
            } else if (HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus().equals(entryStatus)) {
                addStatusCount(countMap, HrmEmployeeStatusTabEnum.PENDING_LEAVE.getStatus(), count);
            } else if (HrmEmployeeEntryStatusEnum.LEFT.getStatus().equals(entryStatus)) {
                addStatusCount(countMap, HrmEmployeeStatusTabEnum.LEFT.getStatus(), count);
            }
        }
        return countMap;
    }

    @Override
    public Map<Long, Map<Integer, Long>> getEmployeeCountMapByDeptAndType() {
        // 1. 查询各部门在职员工的聘用形式数量
        List<Map<String, Object>> countList = employeeMapper.selectCountListByDeptIdAndType(
                HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES);

        // 2. 转换为部门编号、聘用形式与数量的映射
        Map<Long, Map<Integer, Long>> countMap = new LinkedHashMap<>();
        for (Map<String, Object> record : countList) {
            Long deptId = MapUtil.getLong(record, "deptId");
            Integer type = MapUtil.getInt(record, "type");
            Long count = MapUtil.getLong(record, "count");
            if (deptId == null || count == null) {
                continue;
            }
            countMap.computeIfAbsent(deptId, key -> new LinkedHashMap<>()).put(type, count);
        }
        return countMap;
    }

    @Override
    public Map<Integer, Long> getEmployeeSurveyCountMap() {
        LocalDate now = LocalDate.now();
        LocalDateTime[] monthTimes = LocalDateTimeUtils.getMonthDateTimeRange(
                now.getYear(), now.getMonthValue());
        Map<Integer, Long> surveyCountMap = new HashMap<>();

        // 1. 统计本月入职和待入职人数
        for (Map<String, Object> record : employeeMapper.selectCountListByEntryStatusAndEntryTimeBetween(
                monthTimes)) {
            Integer entryStatus = MapUtil.getInt(record, "entryStatus");
            Long count = MapUtil.getLong(record, "count");
            if (HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus().equals(entryStatus)) {
                surveyCountMap.put(HrmEmployeeSurveyTypeEnum.PENDING_ENTRY.getType(), count);
            } else {
                surveyCountMap.merge(HrmEmployeeSurveyTypeEnum.ENTRY.getType(), count, Long::sum);
            }
        }

        // 2. 统计本月离职和待离职人数
        for (Map<String, Object> record : employeeMapper.selectCountListByEntryStatusAndLeaveTimeBetween(
                monthTimes)) {
            Integer entryStatus = MapUtil.getInt(record, "entryStatus");
            Integer surveyType = HrmEmployeeEntryStatusEnum.LEFT.getStatus().equals(entryStatus)
                    ? HrmEmployeeSurveyTypeEnum.LEAVE.getType()
                    : HrmEmployeeSurveyTypeEnum.PENDING_LEAVE.getType();
            surveyCountMap.put(surveyType, MapUtil.getLong(record, "count"));
        }

        // 3. 统计本月转正和调岗人数
        for (Map<String, Object> record : employeeMapper.selectCountListByChangeTypeAndEffectTimeBetween(
                monthTimes)) {
            Integer changeType = MapUtil.getInt(record, "changeType");
            Integer surveyType = HrmEmployeeChangeTypeEnum.REGULAR.getType().equals(changeType)
                    ? HrmEmployeeSurveyTypeEnum.REGULAR.getType()
                    : HrmEmployeeSurveyTypeEnum.TRANSFER.getType();
            surveyCountMap.put(surveyType, MapUtil.getLong(record, "count"));
        }
        return surveyCountMap;
    }

    // ==================== 员工个人操作 ====================

    @Override
    public HrmEmployeeDO validateEmployeeBySelf(Long userId) {
        HrmEmployeeDO employee = getEmployeeByUserId(userId);
        if (employee == null) {
            throw exception(EMPLOYEE_NOT_EXISTS);
        }
        return employee;
    }

    @Override
    @SuppressWarnings("ExtractMethodRecommender")
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = HRM_EMPLOYEE_TYPE, subType = HRM_EMPLOYEE_PROFILE_UPDATE_SUB_TYPE,
            bizNo = "{{#employee.id}}", success = HRM_EMPLOYEE_PROFILE_UPDATE_SUCCESS)
    public void updateEmployeeBySelf(Long userId, HrmPortalEmployeeUpdateReqVO updateReqVO) {
        // 1. 校验员工档案和可编辑字段
        HrmEmployeeDO employee = validateEmployeeBySelf(userId);
        Set<String> editableFields = employeeFieldConfigService.getEditableArchiveFieldNames();
        if (CollUtil.isEmpty(editableFields)) {
            return;
        }
        if (editableFields.contains(HrmEmployeeArchiveFieldEnum.NAME.getName())
                && StrUtil.isBlank(updateReqVO.getName())) {
            throw exception(EMPLOYEE_PROFILE_FIELD_REQUIRED,
                    HrmEmployeeArchiveFieldEnum.NAME.getTitle());
        }
        if (editableFields.contains(HrmEmployeeArchiveFieldEnum.MOBILE.getName())
                && StrUtil.isNotBlank(updateReqVO.getMobile())) {
            validateMobileUnique(employee.getId(), updateReqVO.getMobile());
        }

        // 2.1 按字段白名单构造员工档案
        HrmEmployeeDO updateObj = new HrmEmployeeDO().setId(employee.getId())
                .setName(editableFields.contains(HrmEmployeeArchiveFieldEnum.NAME.getName())
                        ? updateReqVO.getName() : employee.getName())
                .setMobile(editableFields.contains(HrmEmployeeArchiveFieldEnum.MOBILE.getName())
                        ? updateReqVO.getMobile() : employee.getMobile())
                .setEmail(editableFields.contains(HrmEmployeeArchiveFieldEnum.EMAIL.getName())
                        ? updateReqVO.getEmail() : employee.getEmail())
                .setCountry(editableFields.contains(HrmEmployeeArchiveFieldEnum.COUNTRY.getName())
                        ? updateReqVO.getCountry() : employee.getCountry())
                .setNation(editableFields.contains(HrmEmployeeArchiveFieldEnum.NATION.getName())
                        ? updateReqVO.getNation() : employee.getNation())
                .setIdType(editableFields.contains(HrmEmployeeArchiveFieldEnum.ID_TYPE.getName())
                        ? updateReqVO.getIdType() : employee.getIdType())
                .setIdNumber(editableFields.contains(HrmEmployeeArchiveFieldEnum.ID_NUMBER.getName())
                        ? updateReqVO.getIdNumber() : employee.getIdNumber())
                .setSex(editableFields.contains(HrmEmployeeArchiveFieldEnum.SEX.getName())
                        ? updateReqVO.getSex() : employee.getSex())
                .setNativePlace(editableFields.contains(HrmEmployeeArchiveFieldEnum.NATIVE_PLACE.getName())
                        ? updateReqVO.getNativePlace() : employee.getNativePlace())
                .setBirthday(editableFields.contains(HrmEmployeeArchiveFieldEnum.BIRTHDAY.getName())
                        ? updateReqVO.getBirthday() : employee.getBirthday())
                .setAddress(editableFields.contains(HrmEmployeeArchiveFieldEnum.ADDRESS.getName())
                        ? updateReqVO.getAddress() : employee.getAddress())
                .setHighestEducation(editableFields.contains(
                        HrmEmployeeArchiveFieldEnum.HIGHEST_EDUCATION.getName())
                        ? updateReqVO.getHighestEducation() : employee.getHighestEducation());
        normalizeEmployeePersonalInfo(updateObj);
        // 2.2 更新员工档案。出生日期和年龄随身份证信息联动更新
        Set<String> updateFields = new HashSet<>(editableFields);
        if (updateFields.contains(HrmEmployeeArchiveFieldEnum.ID_TYPE.getName())
                || updateFields.contains(HrmEmployeeArchiveFieldEnum.ID_NUMBER.getName())) {
            updateFields.add(HrmEmployeeArchiveFieldEnum.BIRTHDAY.getName());
        }
        if (updateFields.contains(HrmEmployeeArchiveFieldEnum.BIRTHDAY.getName())) {
            updateFields.add(HrmEmployeeArchiveFieldEnum.AGE.getName());
        }
        employeeMapper.updateProfile(updateObj, updateFields);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("employee", employee);
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT,
                BeanUtils.toBean(employee, HrmPortalEmployeeUpdateReqVO.class));
    }

    private HrmEmployeeDO validateEmployeeForCreateOrUpdate(Long id, HrmEmployeeSaveReqVO reqVO) {
        // 1. 校验员工档案是否存在
        HrmEmployeeDO employee = id != null ? validateEmployeeExists(id) : null;
        // 2. 校验工号、后台用户和手机号的唯一性
        validateEmployeeUnique(id, reqVO);
        // 3. 校验关联的后台用户和部门
        if (reqVO.getUserId() != null) {
            adminUserApi.validateUser(reqVO.getUserId());
        }
        // 4. 校验部门和直属上级
        validateDeptAndLeader(id, reqVO.getDeptId(), reqVO.getLeaderEmployeeId());
        // 5. 校验招聘渠道和候选人
        if (reqVO.getChannelId() != null) {
            recruitChannelService.validateRecruitChannelExists(reqVO.getChannelId());
        }
        if (reqVO.getCandidateId() != null) {
            recruitCandidateService.validateRecruitCandidateExists(reqVO.getCandidateId());
        }
        return employee;
    }

    private void validatePositionChange(HrmEmployeeDO employee, HrmEmployeeChangeTypeEnum changeType,
                                        Long newDeptId, Long newLeaderEmployeeId) {
        // 1. 校验员工处于在职状态
        validateEmployeeInService(employee, changeType);
        // 2. 校验新部门和新直属上级
        validateDeptAndLeader(employee.getId(), newDeptId, newLeaderEmployeeId);
    }

    private void validateEmployeeInService(HrmEmployeeDO employee, HrmEmployeeChangeTypeEnum changeType) {
        if (!HrmEmployeeEntryStatusEnum.ACTIVE_STATUSES.contains(employee.getEntryStatus())) {
            throw exception(EMPLOYEE_CHANGE_STATUS_INVALID, changeType.getName());
        }
    }

    private void validateDeptAndLeader(Long employeeId, Long deptId, Long leaderEmployeeId) {
        // 1. 校验部门是否存在
        if (deptId != null) {
            deptApi.validateDeptList(Collections.singleton(deptId));
        }
        // 2. 校验直属上级链路不存在自引用或环路
        if (leaderEmployeeId == null) {
            return;
        }
        Set<Long> visitedEmployeeIds = new HashSet<>();
        Long currentLeaderEmployeeId = leaderEmployeeId;
        while (currentLeaderEmployeeId != null) {
            if (currentLeaderEmployeeId.equals(employeeId)
                    || !visitedEmployeeIds.add(currentLeaderEmployeeId)) {
                throw exception(EMPLOYEE_LEADER_INVALID);
            }
            currentLeaderEmployeeId = validateEmployeeExists(currentLeaderEmployeeId).getLeaderEmployeeId();
        }
    }

    private void validateEmployeeUnique(Long id, HrmEmployeeSaveReqVO reqVO) {
        // 1. 校验工号唯一
        validateJobNumberUnique(id, reqVO.getJobNumber());
        // 2. 校验后台用户唯一
        validateUserUnique(id, reqVO.getUserId());
        // 3. 校验手机号唯一
        validateMobileUnique(id, reqVO.getMobile());
    }

    private void validateJobNumberUnique(Long id, String jobNumber) {
        if (StrUtil.isBlank(jobNumber)) {
            return;
        }
        HrmEmployeeDO employee = employeeMapper.selectByJobNumber(jobNumber);
        if (employee == null) {
            return;
        }
        if (id == null || ObjUtil.notEqual(id, employee.getId())) {
            throw exception(EMPLOYEE_JOB_NUMBER_DUPLICATE);
        }
    }

    private void validateUserUnique(Long id, Long userId) {
        if (userId == null) {
            return;
        }
        HrmEmployeeDO employee = employeeMapper.selectByUserId(userId);
        if (employee == null) {
            return;
        }
        if (id == null || ObjUtil.notEqual(id, employee.getId())) {
            throw exception(EMPLOYEE_USER_DUPLICATE);
        }
    }

    private void validateMobileUnique(Long id, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return;
        }
        HrmEmployeeDO employee = employeeMapper.selectByMobile(mobile);
        if (employee == null) {
            return;
        }
        if (id == null || ObjUtil.notEqual(id, employee.getId())) {
            throw exception(EMPLOYEE_MOBILE_DUPLICATE);
        }
    }

    /**
     * 构建员工档案，并补充由业务规则计算的状态、日期和司龄
     *
     * @param reqVO 员工档案保存请求对象
     * @return 构建后的员工档案对象
     */
    private HrmEmployeeDO buildEmployee(HrmEmployeeSaveReqVO reqVO) {
        HrmEmployeeDO employee = BeanUtils.toBean(reqVO, HrmEmployeeDO.class);
        normalizeEmployeePersonalInfo(employee);
        LocalDate today = LocalDate.now();
        // 正式员工根据入职日期和试用期计算员工状态、转正日期
        if (HrmEmployeeTypeEnum.FORMAL.getType().equals(employee.getType())) {
            int probation = employee.getProbation() != null ? employee.getProbation() : 0;
            LocalDateTime regularTime = employee.getEntryTime().plusMonths(probation);
            employee.setProbation(probation).setRegularTime(regularTime)
                    .setStatus(probation > 0 && regularTime.isAfter(LocalDateTime.now())
                            ? HrmEmployeeStatusEnum.PROBATION.getStatus()
                            : HrmEmployeeStatusEnum.REGULAR.getStatus());
        } else {
            employee.setProbation(0).setRegularTime(null);
        }
        // 根据司龄开始日期计算司龄
        if (employee.getCompanyAgeStartTime() == null) {
            employee.setCompanyAgeStartTime(employee.getEntryTime());
        }
        employee.setCompanyAge(getYearsBetween(employee.getCompanyAgeStartTime().toLocalDate(), today));
        return employee;
    }

    /**
     * 构建员工档案更新对象，并保留只能通过专用流程修改的生命周期字段
     *
     * @param reqVO 员工档案保存请求对象
     * @param employee 原员工档案
     * @return 构建后的员工档案更新对象
     */
    private HrmEmployeeDO buildEmployeeForUpdate(HrmEmployeeSaveReqVO reqVO, HrmEmployeeDO employee) {
        HrmEmployeeSaveReqVO normalizedReqVO = BeanUtils.toBean(reqVO, HrmEmployeeSaveReqVO.class)
                .setEntryStatus(employee.getEntryStatus()).setType(employee.getType())
                .setStatus(employee.getStatus()).setProbation(employee.getProbation());
        return buildEmployee(normalizedReqVO).setId(employee.getId())
                .setEntryStatus(employee.getEntryStatus()).setType(employee.getType())
                .setStatus(employee.getStatus()).setProbation(employee.getProbation())
                .setRegularTime(employee.getRegularTime()).setLeaveTime(employee.getLeaveTime());
    }

    /**
     * 构建导入员工档案。导入允许覆盖完整固定字段，正式员工填写转正时间时以导入值为准。
     *
     * @param reqVO 员工导入请求
     * @return 员工档案
     */
    private HrmEmployeeDO buildImportedEmployee(HrmEmployeeSaveReqVO reqVO) {
        HrmEmployeeDO employee = buildEmployee(reqVO);
        if (HrmEmployeeTypeEnum.FORMAL.getType().equals(employee.getType()) && reqVO.getRegularTime() != null) {
            employee.setRegularTime(reqVO.getRegularTime())
                    .setStatus(employee.getProbation() > 0 && reqVO.getRegularTime().isAfter(LocalDateTime.now())
                            ? HrmEmployeeStatusEnum.PROBATION.getStatus()
                            : HrmEmployeeStatusEnum.REGULAR.getStatus());
        }
        return employee;
    }

    /**
     * 校验导入的工资卡和社保字段组
     *
     * @param importEmployee 导入员工
     * @param schemeId 社保方案编号
     */
    private void validateImportedEmployeeResources(HrmEmployeeImportExcelVO importEmployee, Long schemeId) {
        HrmEmployeeSalaryCardSaveReqVO salaryCard = buildImportedSalaryCard(0L, importEmployee);
        if (salaryCard != null) {
            ValidationUtils.validate(salaryCard);
        }
        HrmInsuranceEmployeeInfoSaveReqVO insuranceEmployeeInfo =
                buildImportedInsuranceEmployeeInfo(0L, importEmployee, schemeId);
        if (insuranceEmployeeInfo != null) {
            ValidationUtils.validate(insuranceEmployeeInfo);
        }
    }

    /**
     * 保存导入员工的工资卡和社保字段组
     *
     * @param employeeId 员工编号
     * @param importEmployee 导入员工
     * @param schemeId 社保方案编号
     */
    private void saveImportedEmployeeResources(
            Long employeeId, HrmEmployeeImportExcelVO importEmployee, Long schemeId) {
        HrmEmployeeSalaryCardSaveReqVO salaryCard = buildImportedSalaryCard(employeeId, importEmployee);
        if (salaryCard != null) {
            employeeSalaryCardService.saveSalaryCard(salaryCard);
        }
        HrmInsuranceEmployeeInfoSaveReqVO insuranceEmployeeInfo =
                buildImportedInsuranceEmployeeInfo(employeeId, importEmployee, schemeId);
        if (insuranceEmployeeInfo != null) {
            insuranceEmployeeInfoService.saveInsuranceEmployeeInfo(insuranceEmployeeInfo);
        }
    }

    /**
     * 在员工档案事务提交后发送员工端开通通知，避免回滚的数据产生误通知
     *
     * @param employees 已创建员工列表
     */
    private void sendEmployeeOpenedMessage(List<HrmEmployeeDO> employees) {
        executeAfterCommit(() -> employees.forEach(employee -> {
            if (employee.getUserId() != null) {
                sendEmployeeMessage(employee, MessageTemplateConstants.EMPLOYEE_OPENED);
            }
        }));
    }

    /**
     * 在批量建档事务提交后，按员工档案分别记录操作日志
     *
     * @param employees 已创建员工列表
     */
    private void recordEmployeeCreatedLogs(List<HrmEmployeeDO> employees) {
        executeAfterCommit(() -> employees.forEach(employee -> logRecordService.record(
                com.mzt.logapi.beans.LogRecord.builder()
                        .type(HRM_EMPLOYEE_TYPE).subType(HRM_EMPLOYEE_CREATE_FROM_USER_LIST_SUB_TYPE)
                        .bizNo(String.valueOf(employee.getId()))
                        .action("从后台用户创建了员工档案【" + employee.getName() + "】")
                        .build())));
    }

    /**
     * 按员工档案分别记录发送填写档案通知的操作日志
     *
     * @param employees 通知发送成功的员工列表
     */
    private void recordEmployeeProfileFillMessageLogs(List<HrmEmployeeDO> employees) {
        employees.forEach(employee -> logRecordService.record(
                com.mzt.logapi.beans.LogRecord.builder()
                        .type(HRM_EMPLOYEE_TYPE).subType(HRM_EMPLOYEE_ARCHIVE_FILL_MESSAGE_SUB_TYPE)
                        .bizNo(String.valueOf(employee.getId()))
                        .action("向员工【" + employee.getName() + "】发送了填写档案通知")
                        .build()));
    }

    /**
     * 在当前事务提交后执行任务；无事务时立即执行
     *
     * @param task 待执行任务
     */
    private void executeAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                task.run();
            }

        });
    }

    /**
     * 向员工绑定的后台账号发送站内信。消息失败不回滚员工业务。
     *
     * @param employee 员工
     * @param templateCode 消息模板编码
     * @return 是否发送成功
     */
    private boolean sendEmployeeMessage(HrmEmployeeDO employee, String templateCode) {
        try {
            notifyMessageSendApi.sendSingleMessageToAdmin(new NotifySendSingleToUserReqDTO()
                    .setUserId(employee.getUserId()).setTemplateCode(templateCode)
                    .setTemplateParams(Collections.singletonMap("employeeName", employee.getName())));
            return true;
        } catch (RuntimeException ex) {
            log.warn("[sendEmployeeMessage][employeeId({}) userId({}) templateCode({}) 发送失败]",
                    employee.getId(), employee.getUserId(), templateCode, ex);
            return false;
        }
    }

    private HrmEmployeeSalaryCardSaveReqVO buildImportedSalaryCard(
            Long employeeId, HrmEmployeeImportExcelVO importEmployee) {
        if (StrUtil.isAllBlank(importEmployee.getBankCardNumber(), importEmployee.getBankName(),
                importEmployee.getBankBranchName()) && importEmployee.getBankAreaId() == null) {
            return null;
        }
        return new HrmEmployeeSalaryCardSaveReqVO().setEmployeeId(employeeId)
                .setBankCardNumber(importEmployee.getBankCardNumber()).setBankAreaId(importEmployee.getBankAreaId())
                .setBankName(importEmployee.getBankName()).setBankBranchName(importEmployee.getBankBranchName());
    }

    private HrmInsuranceEmployeeInfoSaveReqVO buildImportedInsuranceEmployeeInfo(
            Long employeeId, HrmEmployeeImportExcelVO importEmployee, Long schemeId) {
        if (importEmployee.getFirstSocialSecurity() == null && importEmployee.getFirstAccumulationFund() == null
                && StrUtil.isAllBlank(importEmployee.getSocialSecurityNumber(),
                importEmployee.getAccumulationFundNumber())
                && importEmployee.getSocialSecurityStartMonth() == null && schemeId == null) {
            return null;
        }
        return new HrmInsuranceEmployeeInfoSaveReqVO().setEmployeeId(employeeId)
                .setFirstSocialSecurity(importEmployee.getFirstSocialSecurity())
                .setFirstAccumulationFund(importEmployee.getFirstAccumulationFund())
                .setSocialSecurityNumber(importEmployee.getSocialSecurityNumber())
                .setAccumulationFundNumber(importEmployee.getAccumulationFundNumber())
                .setSocialSecurityStartMonth(importEmployee.getSocialSecurityStartMonth())
                .setSchemeId(schemeId);
    }

    /**
     * 规范化员工个人信息，并根据身份证信息补充出生日期、计算年龄
     */
    private void normalizeEmployeePersonalInfo(HrmEmployeeDO employee) {
        LocalDate today = LocalDate.now();
        // 根据身份证补充出生日期
        if (HrmEmployeeIdTypeEnum.ID_CARD.getType().equals(employee.getIdType())
                && StrUtil.isNotBlank(employee.getIdNumber())) {
            LocalDate birthdayDate = LocalDate.parse(IdcardUtil.getBirthByIdCard(employee.getIdNumber()),
                    DateTimeFormatter.BASIC_ISO_DATE);
            employee.setBirthday(birthdayDate.atStartOfDay());
        }
        // 根据出生日期计算年龄
        if (employee.getBirthday() == null) {
            employee.setAge(null);
            return;
        }
        employee.setAge(getYearsBetween(employee.getBirthday().toLocalDate(), today));
    }

    /**
     * 创建员工异动记录
     *
     * @param record 异动记录
     * @param employee 员工
     * @param type 异动类型
     * @return 已保存的异动记录
     */
    private HrmEmployeeChangeRecordDO createEmployeeChangeRecord(
            HrmEmployeeChangeRecordCreateReqVO record, HrmEmployeeDO employee, Integer type) {
        record.setEmployeeId(employee.getId()).setType(type)
                .setOldDeptId(employee.getDeptId()).setOldPostName(employee.getPostName())
                .setOldPostLevel(employee.getPostLevel()).setOldWorkAddress(employee.getWorkAddress())
                .setOldLeaderEmployeeId(employee.getLeaderEmployeeId());
        return employeeChangeRecordService.createEmployeeChangeRecord(record);
    }

    /**
     * 将未填写的任职字段补充为员工当前值，避免转正、转全职误清空主档。
     *
     * @param record 异动请求
     * @param employee 员工
     * @return 补充后的异动请求
     */
    private HrmEmployeeChangeRecordCreateReqVO fillUnchangedPositionFields(
            HrmEmployeeChangeRecordCreateReqVO record, HrmEmployeeDO employee) {
        record.setNewDeptId(defaultIfNull(record.getNewDeptId(), employee.getDeptId()))
                .setNewPostName(StrUtil.blankToDefault(record.getNewPostName(), employee.getPostName()))
                .setNewPostLevel(StrUtil.blankToDefault(record.getNewPostLevel(), employee.getPostLevel()))
                .setNewWorkAddress(StrUtil.blankToDefault(record.getNewWorkAddress(), employee.getWorkAddress()))
                .setNewLeaderEmployeeId(defaultIfNull(
                        record.getNewLeaderEmployeeId(), employee.getLeaderEmployeeId()));
        return record;
    }

    /**
     * 将历史异动记录中缺省的任职字段补充为员工当前值。
     *
     * @param record 异动记录
     * @param employee 员工
     */
    private void fillUnchangedPositionFields(HrmEmployeeChangeRecordDO record, HrmEmployeeDO employee) {
        record.setNewDeptId(defaultIfNull(record.getNewDeptId(), employee.getDeptId()))
                .setNewPostName(StrUtil.blankToDefault(record.getNewPostName(), employee.getPostName()))
                .setNewPostLevel(StrUtil.blankToDefault(record.getNewPostLevel(), employee.getPostLevel()))
                .setNewWorkAddress(StrUtil.blankToDefault(record.getNewWorkAddress(), employee.getWorkAddress()))
                .setNewLeaderEmployeeId(defaultIfNull(
                        record.getNewLeaderEmployeeId(), employee.getLeaderEmployeeId()));
    }

    private void addStatusCount(Map<Integer, Long> countMap, Integer status, Long count) {
        countMap.put(status, countMap.getOrDefault(status, 0L) + count);
    }

    /**
     * 将导入模板中的业务键解析为员工档案关联编号
     *
     * @param importEmployee 导入员工
     * @param saveReqVO 员工保存参数
     * @return 社保方案编号
     */
    private Long resolveImportedEmployeeRelations(HrmEmployeeImportExcelVO importEmployee,
                                                  HrmEmployeeSaveReqVO saveReqVO) {
        // 1. 根据直属上级工号解析员工编号
        if (StrUtil.isNotBlank(importEmployee.getLeaderJobNumber())) {
            String leaderJobNumber = StrUtil.trim(importEmployee.getLeaderJobNumber());
            HrmEmployeeDO leaderEmployee = employeeMapper.selectByJobNumber(leaderJobNumber);
            if (leaderEmployee == null) {
                throw exception(EMPLOYEE_IMPORT_REFERENCE_NOT_EXISTS, "直属上级工号", leaderJobNumber);
            }
            saveReqVO.setLeaderEmployeeId(leaderEmployee.getId());
        }

        // 2. 根据后台用户手机号解析绑定用户编号
        if (StrUtil.isNotBlank(importEmployee.getUserMobile())) {
            String userMobile = StrUtil.trim(importEmployee.getUserMobile());
            AdminUserRespDTO user = adminUserApi.getUserByMobile(userMobile);
            if (user == null) {
                throw exception(EMPLOYEE_IMPORT_REFERENCE_NOT_EXISTS, "绑定用户手机号", userMobile);
            }
            saveReqVO.setUserId(user.getId());
        }

        // 3. 根据社保方案名称解析方案编号
        if (StrUtil.isBlank(importEmployee.getSchemeName())) {
            return null;
        }
        String schemeName = StrUtil.trim(importEmployee.getSchemeName());
        HrmInsuranceSchemeDO scheme = insuranceSchemeService.getSchemeByName(schemeName);
        if (scheme == null) {
            throw exception(EMPLOYEE_IMPORT_REFERENCE_NOT_EXISTS, "参保方案名称", schemeName);
        }
        return scheme.getId();
    }

    private String getImportEmployeeLabel(HrmEmployeeImportExcelVO importEmployee, int index) {
        if (StrUtil.isNotBlank(importEmployee.getJobNumber())) {
            return importEmployee.getJobNumber();
        }
        if (StrUtil.isNotBlank(importEmployee.getUserMobile())) {
            return "用户 " + importEmployee.getUserMobile();
        }
        return "第 " + (index + 1) + " 行";
    }

    private String getImportFailureKey(String employeeLabel, int index) {
        String rowLabel = "第 " + (index + 1) + " 行";
        return rowLabel.equals(employeeLabel) ? rowLabel : rowLabel + "（" + employeeLabel + "）";
    }

    private HrmEmployeeDO getImportExistEmployee(HrmEmployeeSaveReqVO saveReqVO) {
        if (StrUtil.isNotBlank(saveReqVO.getJobNumber())) {
            return employeeMapper.selectByJobNumber(saveReqVO.getJobNumber());
        }
        if (saveReqVO.getUserId() != null) {
            return employeeMapper.selectByUserId(saveReqVO.getUserId());
        }
        return null;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private HrmEmployeeServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
