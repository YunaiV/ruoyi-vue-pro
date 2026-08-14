package cn.iocoder.yudao.module.hrm.service.employee.info;

import cn.iocoder.yudao.module.hrm.service.employee.config.HrmEmployeeFieldConfigService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeChangeRecordService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeQuitInfoService;
import cn.iocoder.yudao.module.hrm.service.employee.employment.HrmEmployeeSalaryCardService;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeChangeRecordCreateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeConvertToFullTimeReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeRegularReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.changerecord.HrmEmployeeTransferReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeCreateFromUserReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeNotifyRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeCancelQuitReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeConfirmEntryReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeImportExcelVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeImportRespVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeListReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeePageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.portal.employee.vo.employee.HrmPortalEmployeeUpdateReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeQuitReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeRehireReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.employee.vo.employee.HrmEmployeeSaveReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeChangeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.insurance.config.HrmInsuranceSchemeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.employment.HrmEmployeeQuitInfoDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.employment.HrmEmployeeChangeRecordMapper;
import cn.iocoder.yudao.module.hrm.dal.mysql.employee.info.HrmEmployeeMapper;
import cn.iocoder.yudao.module.hrm.enums.employee.config.HrmEmployeeArchiveFieldEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.employment.HrmEmployeeChangeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.experience.HrmEmployeeEducationEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeEntryStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeIdTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeImportDuplicateStrategyEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeStatusTabEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeSurveyTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.employee.info.HrmEmployeeTodoTypeEnum;
import cn.iocoder.yudao.module.hrm.enums.recruit.candidate.HrmRecruitCandidateStatusEnum;
import cn.iocoder.yudao.module.hrm.framework.excel.core.HrmRecruitChannelExcelColumnSelectFunction;
import cn.iocoder.yudao.module.hrm.service.insurance.employee.HrmInsuranceEmployeeInfoService;
import cn.iocoder.yudao.module.hrm.service.insurance.config.HrmInsuranceSchemeService;
import cn.iocoder.yudao.module.hrm.service.recruit.candidate.HrmRecruitCandidateService;
import cn.iocoder.yudao.module.hrm.service.recruit.config.HrmRecruitChannelService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import jakarta.annotation.Resource;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomEmail;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomMobile;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CHANGE_TYPE_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_CONFIRM_ENTRY_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_ENTRY_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_IMPORT_LIST_IS_EMPTY;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_IMPORT_DUPLICATE_STRATEGY_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_JOB_NUMBER_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_LEADER_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_MOBILE_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_QUIT_CANCEL_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_QUIT_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_REHIRE_STATUS_INVALID;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.EMPLOYEE_USER_DUPLICATE;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.RECRUIT_CHANNEL_NOT_EXISTS;
import static cn.iocoder.yudao.module.hrm.enums.LogRecordConstants.HRM_EMPLOYEE_TYPE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmEmployeeServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({HrmEmployeeServiceImpl.class, HrmEmployeeServiceImplTest.TransactionTemplateTestConfiguration.class})
public class HrmEmployeeServiceImplTest extends BaseDbUnitTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class TransactionTemplateTestConfiguration {

        @Bean
        TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
            return new TransactionTemplate(transactionManager);
        }

    }

    @Resource
    private HrmEmployeeServiceImpl employeeService;

    @Resource
    private HrmEmployeeMapper employeeMapper;
    @Resource
    private HrmEmployeeChangeRecordMapper employeeChangeRecordMapper;

    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private NotifyMessageSendApi notifyMessageSendApi;
    @MockitoBean
    private ILogRecordService logRecordService;
    @MockitoBean
    private DeptApi deptApi;
    @MockitoBean
    private HrmRecruitChannelService recruitChannelService;
    @MockitoBean
    private HrmEmployeeFieldConfigService employeeFieldConfigService;
    @MockitoBean
    private HrmEmployeeChangeRecordService changeRecordService;
    @MockitoBean
    private HrmEmployeeQuitInfoService quitInfoService;
    @MockitoBean
    private HrmEmployeeSalaryCardService employeeSalaryCardService;
    @MockitoBean
    private HrmInsuranceEmployeeInfoService insuranceEmployeeInfoService;
    @MockitoBean
    private HrmInsuranceSchemeService insuranceSchemeService;
    @MockitoBean
    private HrmRecruitCandidateService recruitCandidateService;

    @BeforeEach
    public void setUpChangeRecordService() {
        lenient().when(changeRecordService.createEmployeeChangeRecord(any())).thenAnswer(invocation -> {
            HrmEmployeeChangeRecordCreateReqVO reqVO = invocation.getArgument(0);
            return BeanUtils.toBean(reqVO, HrmEmployeeChangeRecordDO.class).setId(randomLongId());
        });
    }

    @Test
    public void testConfirmEmployeeEntry_success() {
        // mock 数据
        Long candidateId = randomLongId();
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())
                .setEntryTime(null).setCandidateId(candidateId)
                .setDeptId(10L).setPostName("开发工程师").setPostLevel("P5")
                .setWorkAddress("杭州").setLeaderEmployeeId(11L)
                .setRegularTime(LocalDateTime.now().plusMonths(1))
                .setLeaveTime(LocalDateTime.now().plusYears(1)));
        employeeMapper.insert(employee);
        HrmEmployeeConfirmEntryReqVO confirmReqVO = BeanUtils.toBean(
                randomEmployeeSaveReqVO(o -> o.setId(employee.getId())
                        .setEntryTime(LocalDateTime.now().minusMinutes(1)).setCandidateId(candidateId)
                        .setDeptId(null).setPostName(null).setPostLevel(null)
                        .setWorkAddress(null).setLeaderEmployeeId(null)
                        .setType(HrmEmployeeTypeEnum.INFORMAL.getType())
                        .setStatus(HrmEmployeeStatusEnum.INTERN.getStatus())),
                HrmEmployeeConfirmEntryReqVO.class);

        // 调用
        employeeService.confirmEmployeeEntry(confirmReqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus(), dbEmployee.getEntryStatus());
        assertEquals(LocalDate.now(), dbEmployee.getEntryTime().toLocalDate());
        assertNull(dbEmployee.getDeptId());
        assertNull(dbEmployee.getPostName());
        assertNull(dbEmployee.getPostLevel());
        assertNull(dbEmployee.getWorkAddress());
        assertNull(dbEmployee.getLeaderEmployeeId());
        assertNull(dbEmployee.getRegularTime());
        assertNull(dbEmployee.getLeaveTime());
        verify(recruitCandidateService).confirmRecruitCandidateEntry(
                candidateId, dbEmployee.getEntryTime());
    }

    @Test
    public void testConfirmEmployeeEntry_statusInvalid() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO();
        employeeMapper.insert(employee);
        HrmEmployeeConfirmEntryReqVO reqVO = BeanUtils.toBean(
                randomEmployeeSaveReqVO(o -> o.setId(employee.getId())), HrmEmployeeConfirmEntryReqVO.class);

        // 调用，并断言异常
        assertServiceException(() -> employeeService.confirmEmployeeEntry(reqVO),
                EMPLOYEE_CONFIRM_ENTRY_STATUS_INVALID);
    }

    @Test
    public void testRehireEmployee_success() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setLeaveTime(LocalDate.now().minusDays(1).atTime(18, 30))
                .setDeptId(10L).setPostName("开发工程师").setPostLevel("P5")
                .setWorkAddress("杭州").setLeaderEmployeeId(11L));
        employeeMapper.insert(employee);
        // 准备参数
        LocalDateTime entryTime = LocalDateTime.now().minusMinutes(1).withNano(0);
        HrmEmployeeRehireReqVO reqVO = BeanUtils.toBean(
                randomEmployeeSaveReqVO(), HrmEmployeeRehireReqVO.class);
        reqVO.setEmployeeId(employee.getId()).setEntryTime(entryTime)
                .setCompanyAgeStartTime(entryTime)
                .setDeptId(20L).setPostName("技术经理").setPostLevel("P7")
                .setLeaderEmployeeId(null).setWorkCity("上海").setWorkAddress("浦东新区")
                .setWorkDetailAddress("张江路 1 号")
                .setType(HrmEmployeeTypeEnum.FORMAL.getType()).setStatus(null).setProbation(3);

        // 调用
        employeeService.rehireEmployee(reqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus(), dbEmployee.getEntryStatus());
        assertEquals(entryTime, dbEmployee.getEntryTime());
        assertEquals(entryTime, dbEmployee.getCompanyAgeStartTime());
        assertNull(dbEmployee.getLeaveTime());
        assertEquals(HrmEmployeeStatusEnum.PROBATION.getStatus(), dbEmployee.getStatus());
        assertEquals(20L, dbEmployee.getDeptId());
        assertEquals("技术经理", dbEmployee.getPostName());
        assertNull(dbEmployee.getLeaderEmployeeId());
        verify(quitInfoService).deleteEmployeeQuitInfo(employee.getId());
        verify(changeRecordService).createEmployeeChangeRecord(argThat((HrmEmployeeChangeRecordCreateReqVO record) ->
                HrmEmployeeChangeTypeEnum.REHIRE.getType().equals(record.getType())
                        && employee.getDeptId().equals(record.getOldDeptId())
                        && reqVO.getDeptId().equals(record.getNewDeptId())));
    }

    @Test
    public void testRehireEmployee_statusInvalid() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO();
        employeeMapper.insert(employee);
        // 准备参数
        HrmEmployeeRehireReqVO reqVO = randomPojo(HrmEmployeeRehireReqVO.class,
                o -> o.setEmployeeId(employee.getId()).setEntryTime(LocalDate.now().atStartOfDay())
                        .setType(HrmEmployeeTypeEnum.FORMAL.getType()));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.rehireEmployee(reqVO), EMPLOYEE_REHIRE_STATUS_INVALID);
    }

    @Test
    public void testChangeEmployee_effectiveTransfer() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o.setDeptId(10L)
                .setPostName("开发工程师").setPostLevel("P5")
                .setWorkAddress("杭州").setLeaderEmployeeId(11L));
        employeeMapper.insert(employee);
        doAnswer(invocation -> {
            HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
            assertEquals(10L, dbEmployee.getDeptId());
            assertEquals("开发工程师", dbEmployee.getPostName());
            HrmEmployeeChangeRecordCreateReqVO record = invocation.getArgument(0);
            return BeanUtils.toBean(record, HrmEmployeeChangeRecordDO.class).setId(randomLongId());
        }).when(changeRecordService).createEmployeeChangeRecord(argThat(record ->
                employee.getId().equals(record.getEmployeeId())));
        // 准备参数
        HrmEmployeeTransferReqVO reqVO = new HrmEmployeeTransferReqVO();
        reqVO.setEmployeeId(employee.getId());
        reqVO.setReason(1);
        reqVO.setNewDeptId(20L);
        reqVO.setNewPostName("技术经理");
        reqVO.setNewPostLevel("P7");
        reqVO.setNewWorkAddress("上海");
        reqVO.setNewLeaderEmployeeId(null);
        reqVO.setEffectTime(LocalDate.now().atStartOfDay());

        // 调用
        employeeService.transferEmployee(reqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(20L, dbEmployee.getDeptId());
        assertEquals("技术经理", dbEmployee.getPostName());
        verify(changeRecordService).createEmployeeChangeRecord(argThat(record ->
                employee.getDeptId().equals(record.getOldDeptId())
                        && employee.getPostName().equals(record.getOldPostName())
                        && reqVO.getEffectTime().equals(record.getEffectTime())));
    }

    @Test
    public void testRegularEmployee_updatePosition() {
        // mock 数据
        HrmEmployeeDO leader = randomEmployeeDO();
        employeeMapper.insert(leader);
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setStatus(HrmEmployeeStatusEnum.PROBATION.getStatus()).setProbation(3)
                .setDeptId(10L).setPostName("开发工程师").setPostLevel("P5")
                .setWorkAddress("杭州").setLeaderEmployeeId(null));
        employeeMapper.insert(employee);
        // 准备参数
        LocalDateTime effectTime = LocalDate.now().atStartOfDay();
        HrmEmployeeRegularReqVO reqVO = new HrmEmployeeRegularReqVO();
        reqVO.setEmployeeId(employee.getId()).setReason(1).setEffectTime(effectTime)
                .setNewDeptId(20L).setNewPostName("技术经理").setNewPostLevel("P7")
                .setNewWorkAddress("上海").setNewLeaderEmployeeId(leader.getId());

        // 调用
        employeeService.regularEmployee(reqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeStatusEnum.REGULAR.getStatus(), dbEmployee.getStatus());
        assertEquals(3, dbEmployee.getProbation());
        assertEquals(effectTime, dbEmployee.getRegularTime());
        assertEquals(20L, dbEmployee.getDeptId());
        assertEquals("技术经理", dbEmployee.getPostName());
        assertEquals("P7", dbEmployee.getPostLevel());
        assertEquals("上海", dbEmployee.getWorkAddress());
        assertEquals(leader.getId(), dbEmployee.getLeaderEmployeeId());
        verify(changeRecordService).createEmployeeChangeRecord(argThat(record ->
                employee.getDeptId().equals(record.getOldDeptId())
                        && reqVO.getNewDeptId().equals(record.getNewDeptId())
                        && reqVO.getNewPostName().equals(record.getNewPostName())));
    }

    @Test
    public void testConvertEmployeeToFullTime_withProbationAndPosition() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setType(HrmEmployeeTypeEnum.INFORMAL.getType())
                .setStatus(HrmEmployeeStatusEnum.INTERN.getStatus()).setProbation(0)
                .setDeptId(10L).setPostName("实习开发").setPostLevel("实习")
                .setWorkAddress("杭州").setLeaderEmployeeId(null));
        employeeMapper.insert(employee);
        // 准备参数
        LocalDateTime effectTime = LocalDate.now().atStartOfDay();
        HrmEmployeeConvertToFullTimeReqVO reqVO = new HrmEmployeeConvertToFullTimeReqVO();
        reqVO.setEmployeeId(employee.getId()).setReason(1).setProbation(3).setEffectTime(effectTime)
                .setNewDeptId(20L).setNewPostName("开发工程师").setNewPostLevel("P5")
                .setNewWorkAddress("上海");

        // 调用
        employeeService.convertEmployeeToFullTime(reqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeTypeEnum.FORMAL.getType(), dbEmployee.getType());
        assertEquals(HrmEmployeeStatusEnum.PROBATION.getStatus(), dbEmployee.getStatus());
        assertEquals(3, dbEmployee.getProbation());
        assertEquals(effectTime.plusMonths(3), dbEmployee.getRegularTime());
        assertEquals(20L, dbEmployee.getDeptId());
        assertEquals("开发工程师", dbEmployee.getPostName());
        verify(changeRecordService).createEmployeeChangeRecord(argThat(record ->
                Integer.valueOf(3).equals(record.getProbation())
                        && reqVO.getNewDeptId().equals(record.getNewDeptId())));
    }

    @Test
    public void testChangeEmployee_future() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o.setDeptId(10L));
        employeeMapper.insert(employee);
        // 准备参数
        HrmEmployeeTransferReqVO reqVO = new HrmEmployeeTransferReqVO();
        reqVO.setEmployeeId(employee.getId());
        reqVO.setReason(1);
        reqVO.setNewDeptId(20L);
        reqVO.setEffectTime(LocalDate.now().plusDays(1).atStartOfDay());

        // 调用
        employeeService.transferEmployee(reqVO);

        // 断言
        assertEquals(10L, employeeMapper.selectById(employee.getId()).getDeptId());
        verify(changeRecordService).createEmployeeChangeRecord(argThat(record ->
                employee.getDeptId().equals(record.getOldDeptId())
                        && reqVO.getEffectTime().equals(record.getEffectTime())));
    }

    @Test
    public void testApplyEmployeeChange_success() {
        // mock 数据
        LocalDateTime effectTime = LocalDate.now().atStartOfDay();
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setDeptId(10L).setPostName("开发工程师").setPostLevel("P5")
                .setWorkAddress("杭州").setLeaderEmployeeId(11L));
        employeeMapper.insert(employee);
        HrmEmployeeChangeRecordDO changeRecord = HrmEmployeeChangeRecordDO.builder()
                .employeeId(employee.getId()).type(HrmEmployeeChangeTypeEnum.TRANSFER.getType())
                .newDeptId(20L).newPostName("技术经理").newPostLevel("P7")
                .newWorkAddress("上海").newLeaderEmployeeId(21L).effectTime(effectTime).build();

        // 调用
        employeeService.applyEmployeeChange(changeRecord);

        // 断言
        HrmEmployeeDO changedEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(20L, changedEmployee.getDeptId());
        assertEquals("技术经理", changedEmployee.getPostName());
    }

    @Test
    public void testApplyEmployeeChange_typeInvalid() {
        // 准备参数
        HrmEmployeeChangeRecordDO changeRecord = HrmEmployeeChangeRecordDO.builder()
                .employeeId(randomLongId()).type(99).build();

        // 调用，并断言异常
        assertServiceException(() -> employeeService.applyEmployeeChange(changeRecord),
                EMPLOYEE_CHANGE_TYPE_INVALID, changeRecord.getType());
    }

    @Test
    public void testApplyEmployeeRegular_success() {
        // mock 数据
        LocalDateTime regularTime = LocalDateTime.now().minusMinutes(1);
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setStatus(HrmEmployeeStatusEnum.PROBATION.getStatus())
                .setRegularTime(regularTime));
        employeeMapper.insert(employee);

        // 调用
        List<HrmEmployeeDO> regularEmployees =
                employeeService.getDueRegularEmployeeList(LocalDateTime.now());
        assertTrue(employeeService.applyEmployeeRegular(employee.getId()));

        // 断言
        assertEquals(1, regularEmployees.size());
        assertEquals(employee.getId(), regularEmployees.get(0).getId());
        assertEquals(HrmEmployeeStatusEnum.REGULAR.getStatus(),
                employeeMapper.selectById(employee.getId()).getStatus());
    }

    @Test
    public void testApplyEmployeeQuit_success() {
        // mock 数据
        LocalDate effectTime = LocalDate.now();
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                .setCompanyAgeStartTime(effectTime.minusYears(3).atStartOfDay()));
        employeeMapper.insert(employee);
        LocalDateTime planQuitTime = LocalDateTime.now().minusMinutes(1);
        HrmEmployeeQuitInfoDO quitInfo = HrmEmployeeQuitInfoDO.builder()
                .employeeId(employee.getId()).planQuitTime(planQuitTime).build();

        // 调用
        boolean result = employeeService.applyEmployeeQuit(quitInfo);

        // 断言
        assertTrue(result);
        HrmEmployeeDO leftEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeEntryStatusEnum.LEFT.getStatus(), leftEmployee.getEntryStatus());
        assertEquals(planQuitTime, leftEmployee.getLeaveTime());
        assertEquals(3, leftEmployee.getCompanyAge());
    }

    @Test
    public void testApplyEmployeeRegular_leftEmployee_doesNotRevive() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setStatus(HrmEmployeeStatusEnum.PROBATION.getStatus())
                .setRegularTime(LocalDateTime.now().minusDays(1)));
        employeeMapper.insert(employee);

        // 调用
        boolean result = employeeService.applyEmployeeRegular(employee.getId());

        // 断言
        assertFalse(result);
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeEntryStatusEnum.LEFT.getStatus(), dbEmployee.getEntryStatus());
        assertEquals(HrmEmployeeStatusEnum.PROBATION.getStatus(), dbEmployee.getStatus());
    }

    @Test
    public void testQuitEmployee_pendingLeave() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus()));
        employeeMapper.insert(employee);
        // 准备参数
        LocalDateTime planQuitTime = LocalDateTime.now().plusDays(10).withNano(0);
        HrmEmployeeQuitReqVO reqVO = randomPojo(HrmEmployeeQuitReqVO.class,
                o -> o.setEmployeeId(employee.getId()).setPlanQuitTime(planQuitTime)
                        .setApplyQuitTime(LocalDateTime.now().minusDays(1).withNano(0))
                        .setSalarySettlementTime(planQuitTime).setType(1).setReason(1));

        // 调用
        employeeService.quitEmployee(reqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus(), dbEmployee.getEntryStatus());
        assertEquals(planQuitTime, dbEmployee.getLeaveTime());
        verify(quitInfoService).saveEmployeeQuitInfo(argThat(quitInfo ->
                employee.getId().equals(quitInfo.getEmployeeId())
                        && employee.getStatus().equals(quitInfo.getOldEmployeeStatus())
                        && reqVO.getApplyQuitTime().toLocalDate().atStartOfDay()
                                .equals(quitInfo.getApplyQuitTime())
                        && reqVO.getSalarySettlementTime().toLocalDate().atStartOfDay()
                                .equals(quitInfo.getSalarySettlementTime())));
    }

    @Test
    public void testQuitEmployee_sameDayNaturalDates() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus()));
        employeeMapper.insert(employee);
        // 准备参数：申请、计划离职和薪资结算在同一自然日，时刻先后不影响日期校验
        LocalDate quitDate = LocalDate.now().plusDays(10);
        HrmEmployeeQuitReqVO reqVO = randomPojo(HrmEmployeeQuitReqVO.class,
                o -> o.setEmployeeId(employee.getId()).setPlanQuitTime(quitDate.atTime(9, 0))
                        .setApplyQuitTime(quitDate.atTime(18, 0))
                        .setSalarySettlementTime(quitDate.atStartOfDay()).setType(1).setReason(1));

        // 调用
        employeeService.quitEmployee(reqVO);

        // 断言
        verify(quitInfoService).saveEmployeeQuitInfo(argThat(quitInfo ->
                quitDate.atStartOfDay().equals(quitInfo.getApplyQuitTime())
                        && quitDate.atStartOfDay().equals(quitInfo.getSalarySettlementTime())));
    }

    @Test
    public void testQuitEmployee_effective() {
        // mock 数据
        LocalDate companyAgeStartDate = LocalDate.now().minusYears(3);
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setCompanyAgeStartTime(companyAgeStartDate.atStartOfDay()));
        employeeMapper.insert(employee);
        // 准备参数
        LocalDateTime planQuitTime = LocalDateTime.now().minusMinutes(1).withNano(0);
        HrmEmployeeQuitReqVO reqVO = randomPojo(HrmEmployeeQuitReqVO.class,
                o -> o.setEmployeeId(employee.getId()).setPlanQuitTime(planQuitTime)
                        .setApplyQuitTime(planQuitTime.minusDays(7))
                        .setSalarySettlementTime(planQuitTime).setType(1).setReason(1));

        // 调用
        employeeService.quitEmployee(reqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeEntryStatusEnum.LEFT.getStatus(), dbEmployee.getEntryStatus());
        assertEquals(3, dbEmployee.getCompanyAge());
    }

    @Test
    public void testQuitEmployee_statusInvalid() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus()));
        employeeMapper.insert(employee);
        // 准备参数
        HrmEmployeeQuitReqVO reqVO = randomPojo(HrmEmployeeQuitReqVO.class,
                o -> o.setEmployeeId(employee.getId()).setPlanQuitTime(LocalDateTime.now()).setType(1).setReason(1));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.quitEmployee(reqVO), EMPLOYEE_QUIT_STATUS_INVALID);
    }

    @Test
    public void testQuitEmployee_leftUpdate() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setCompanyAgeStartTime(LocalDate.now().minusYears(2).atStartOfDay()));
        employeeMapper.insert(employee);
        // 准备参数
        LocalDateTime planQuitTime = LocalDateTime.now().minusDays(1).withNano(0);
        HrmEmployeeQuitReqVO reqVO = randomPojo(HrmEmployeeQuitReqVO.class,
                o -> o.setEmployeeId(employee.getId()).setPlanQuitTime(planQuitTime)
                        .setApplyQuitTime(planQuitTime.minusDays(7))
                        .setSalarySettlementTime(planQuitTime).setType(1).setReason(1));

        // 调用
        employeeService.quitEmployee(reqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeEntryStatusEnum.LEFT.getStatus(), dbEmployee.getEntryStatus());
        assertEquals(planQuitTime, dbEmployee.getLeaveTime());
        assertEquals(1, dbEmployee.getCompanyAge());
        verify(quitInfoService).saveEmployeeQuitInfo(argThat(quitInfo ->
                employee.getId().equals(quitInfo.getEmployeeId())));
    }

    @Test
    public void testCancelEmployeeQuit_success() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                .setLeaveTime(LocalDateTime.now().plusDays(10)));
        employeeMapper.insert(employee);
        HrmEmployeeCancelQuitReqVO reqVO = new HrmEmployeeCancelQuitReqVO()
                .setEmployeeId(employee.getId()).setReason("员工撤回离职");

        // 调用
        employeeService.cancelEmployeeQuit(reqVO);

        // 断言
        HrmEmployeeDO dbEmployee = employeeMapper.selectById(employee.getId());
        assertEquals(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus(), dbEmployee.getEntryStatus());
        assertNull(dbEmployee.getLeaveTime());
        verify(quitInfoService).validateQuitInfoByEmployeeId(employee.getId());
        verify(quitInfoService).deleteEmployeeQuitInfo(employee.getId());
    }

    @Test
    public void testCancelEmployeeQuit_statusInvalid() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));
        employeeMapper.insert(employee);
        HrmEmployeeCancelQuitReqVO reqVO = new HrmEmployeeCancelQuitReqVO()
                .setEmployeeId(employee.getId()).setReason("员工撤回离职");

        // 调用，并断言异常
        assertServiceException(() -> employeeService.cancelEmployeeQuit(reqVO),
                EMPLOYEE_QUIT_CANCEL_STATUS_INVALID);
    }

    @Test
    public void testCreateEmployee_success() {
        // 准备参数
        LocalDateTime entryTime = LocalDate.now().minusMonths(1).atTime(9, 0);
        LocalDateTime birthday = LocalDate.now().minusYears(30).atTime(8, 30);
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o.setIdType(HrmEmployeeIdTypeEnum.OTHER.getType())
                .setBirthday(birthday).setEntryTime(entryTime)
                .setCompanyAgeStartTime(null).setProbation(3));

        // 调用
        Long employeeId = employeeService.createEmployee(reqVO);

        // 断言
        assertNotNull(employeeId);
        HrmEmployeeDO employee = employeeMapper.selectById(employeeId);
        assertPojoEquals(reqVO, employee, "id", "birthday", "age", "status", "regularTime",
                "companyAgeStartTime", "companyAge");
        assertEquals(30, employee.getAge());
        assertEquals(birthday, employee.getBirthday());
        assertEquals(HrmEmployeeStatusEnum.PROBATION.getStatus(), employee.getStatus());
        assertEquals(entryTime.plusMonths(3), employee.getRegularTime());
        assertEquals(entryTime, employee.getCompanyAgeStartTime());
        assertEquals(0, employee.getCompanyAge());
    }

    @Test
    public void testCreateEmployee_idCard15Birthday() {
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o
                .setIdType(HrmEmployeeIdTypeEnum.ID_CARD.getType())
                .setIdNumber("130503670401001")
                .setBirthday(LocalDateTime.of(2000, 1, 1, 12, 30)));

        // 调用
        Long employeeId = employeeService.createEmployee(reqVO);

        // 断言
        HrmEmployeeDO employee = employeeMapper.selectById(employeeId);
        assertEquals(LocalDate.of(1967, 4, 1).atStartOfDay(), employee.getBirthday());
        assertEquals(Period.between(LocalDate.of(1967, 4, 1), LocalDate.now()).getYears(), employee.getAge());
    }

    @Test
    public void testCreateEmployee_zeroProbationFutureEntry() {
        // 准备参数
        LocalDateTime entryTime = LocalDate.now().plusMonths(1).atTime(9, 0);
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o
                .setEntryTime(entryTime).setProbation(0));

        // 调用
        Long employeeId = employeeService.createEmployee(reqVO);

        // 断言
        HrmEmployeeDO employee = employeeMapper.selectById(employeeId);
        assertEquals(HrmEmployeeStatusEnum.REGULAR.getStatus(), employee.getStatus());
        assertEquals(entryTime, employee.getRegularTime());
    }

    @Test
    public void testCreateEmployee_validateRelations() {
        // mock 数据
        HrmEmployeeDO leaderEmployee = randomEmployeeDO();
        employeeMapper.insert(leaderEmployee);
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o.setUserId(randomLongId())
                .setDeptId(randomLongId()).setLeaderEmployeeId(leaderEmployee.getId())
                .setChannelId(randomLongId()).setCandidateId(randomLongId()));

        // 调用
        employeeService.createEmployee(reqVO);

        // 断言
        verify(adminUserApi).validateUser(reqVO.getUserId());
        verify(deptApi).validateDeptList(singleton(reqVO.getDeptId()));
        verify(recruitChannelService).validateRecruitChannelExists(reqVO.getChannelId());
        verify(recruitCandidateService).validateRecruitCandidateExists(reqVO.getCandidateId());
    }

    @Test
    public void testCreateEmployeeList_success() {
        // mock 数据
        AdminUserRespDTO firstUser = randomPojo(AdminUserRespDTO.class, o -> o.setId(randomLongId())
                .setNickname("张三").setMobile("15601691301").setEmail("zhangsan@example.com").setSex(1)
                .setDeptId(10L));
        AdminUserRespDTO secondUser = randomPojo(AdminUserRespDTO.class, o -> o.setId(randomLongId())
                .setNickname("李四").setMobile("15601691302").setEmail("lisi@example.com").setSex(2)
                .setDeptId(20L));
        // 准备参数
        HrmEmployeeCreateFromUserReqVO firstReqVO = new HrmEmployeeCreateFromUserReqVO()
                .setUserId(firstUser.getId()).setJobNumber("HRM-BATCH-001").setMobile(firstUser.getMobile())
                .setDeptId(firstUser.getDeptId()).setType(HrmEmployeeTypeEnum.FORMAL.getType())
                .setProbation(0).setEntryTime(LocalDate.now().atTime(9, 0));
        HrmEmployeeCreateFromUserReqVO secondReqVO = new HrmEmployeeCreateFromUserReqVO()
                .setUserId(secondUser.getId()).setJobNumber("HRM-BATCH-002").setMobile(secondUser.getMobile())
                .setDeptId(secondUser.getDeptId()).setType(HrmEmployeeTypeEnum.INFORMAL.getType())
                .setStatus(HrmEmployeeStatusEnum.INTERN.getStatus()).setEntryTime(LocalDate.now().atTime(9, 0));
        List<HrmEmployeeCreateFromUserReqVO> createReqVOList = asList(firstReqVO, secondReqVO);
        // mock 方法
        when(adminUserApi.getUserList(argThat(ids -> ids.containsAll(asList(firstUser.getId(), secondUser.getId())))))
                .thenReturn(asList(firstUser, secondUser));
        doAnswer(invocation -> {
            HrmEmployeeSaveReqVO saveReqVO = invocation.getArgument(0);
            assertNull(saveReqVO.getName());
            assertNull(saveReqVO.getEmail());
            assertNull(saveReqVO.getSex());
            assertNull(saveReqVO.getCompanyAgeStartTime());
            return null;
        }).when(employeeFieldConfigService).validateEmployeeCreateFields(any(), any());

        // 调用
        List<Long> result = employeeService.createEmployeeList(createReqVOList);

        // 断言
        assertEquals(2, result.size());
        assertEquals(2L, employeeMapper.selectCount());
        assertEquals("张三", employeeMapper.selectByUserId(firstUser.getId()).getName());
        assertEquals("李四", employeeMapper.selectByUserId(secondUser.getId()).getName());
        assertEquals(firstReqVO.getEntryTime(),
                employeeMapper.selectByUserId(firstUser.getId()).getCompanyAgeStartTime());
        verify(adminUserApi).validateUserList(argThat(ids ->
                ids.containsAll(asList(firstUser.getId(), secondUser.getId()))));
        ArgumentCaptor<LogRecord> logRecordCaptor = ArgumentCaptor.forClass(LogRecord.class);
        verify(logRecordService, times(2)).record(logRecordCaptor.capture());
        Map<String, LogRecord> logRecordMap = convertMap(logRecordCaptor.getAllValues(), LogRecord::getBizNo);
        HrmEmployeeDO firstEmployee = employeeMapper.selectByUserId(firstUser.getId());
        HrmEmployeeDO secondEmployee = employeeMapper.selectByUserId(secondUser.getId());
        assertEquals("从后台用户创建了员工档案【张三】",
                logRecordMap.get(String.valueOf(firstEmployee.getId())).getAction());
        assertEquals("从后台用户创建了员工档案【李四】",
                logRecordMap.get(String.valueOf(secondEmployee.getId())).getAction());
    }

    @Test
    public void testSendEmployeeProfileFillMessage_withSkippedAndFailure() {
        // mock 数据
        HrmEmployeeDO successEmployee = randomEmployeeDO(o -> o.setUserId(randomLongId()));
        employeeMapper.insert(successEmployee);
        HrmEmployeeDO skippedEmployee = randomEmployeeDO(o -> o.setUserId(null));
        employeeMapper.insert(skippedEmployee);
        HrmEmployeeDO failureEmployee = randomEmployeeDO(o -> o.setUserId(randomLongId()));
        employeeMapper.insert(failureEmployee);
        // 准备参数
        List<Long> employeeIds = asList(successEmployee.getId(), skippedEmployee.getId(), failureEmployee.getId());
        // mock 方法
        doThrow(new IllegalStateException("template unavailable")).when(notifyMessageSendApi)
                .sendSingleMessageToAdmin(argThat(message -> failureEmployee.getUserId().equals(message.getUserId())));

        // 调用
        HrmEmployeeNotifyRespVO result = employeeService.sendEmployeeProfileFillMessage(employeeIds);

        // 断言
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals(1, result.getFailureCount());
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(argThat(message ->
                successEmployee.getUserId().equals(message.getUserId())));
        ArgumentCaptor<LogRecord> logRecordCaptor = ArgumentCaptor.forClass(LogRecord.class);
        verify(logRecordService).record(logRecordCaptor.capture());
        LogRecord logRecord = logRecordCaptor.getValue();
        assertEquals(HRM_EMPLOYEE_TYPE, logRecord.getType());
        assertEquals(String.valueOf(successEmployee.getId()), logRecord.getBizNo());
        assertEquals("向员工【" + successEmployee.getName() + "】发送了填写档案通知",
                logRecord.getAction());
    }

    @Test
    public void testUpdateEmployee_success() {
        // mock 数据
        HrmEmployeeDO dbEmployee = randomEmployeeDO();
        employeeMapper.insert(dbEmployee);
        // 准备参数
        LocalDateTime entryTime = LocalDate.now().minusYears(2).atTime(9, 15);
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o.setId(dbEmployee.getId())
                .setType(HrmEmployeeTypeEnum.INFORMAL.getType())
                .setStatus(HrmEmployeeStatusEnum.CONSULTANT.getStatus()).setProbation(3)
                .setEntryTime(entryTime).setCompanyAgeStartTime(entryTime));

        // 调用
        employeeService.updateEmployee(reqVO);

        // 断言
        HrmEmployeeDO employee = employeeMapper.selectById(dbEmployee.getId());
        assertPojoEquals(reqVO, employee, "age", "entryStatus", "type", "status",
                "probation", "regularTime", "leaveTime", "companyAge");
        assertEquals(dbEmployee.getType(), employee.getType());
        assertEquals(dbEmployee.getStatus(), employee.getStatus());
        assertEquals(dbEmployee.getProbation(), employee.getProbation());
        assertEquals(dbEmployee.getRegularTime(), employee.getRegularTime());
        assertEquals(2, employee.getCompanyAge());
    }

    @Test
    public void testUpdateEmployee_notExists() {
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o.setId(randomLongId()));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.updateEmployee(reqVO), EMPLOYEE_NOT_EXISTS);
    }

    @Test
    public void testUpdateEmployee_leaderInvalid() {
        // mock 数据
        HrmEmployeeDO dbEmployee = randomEmployeeDO();
        employeeMapper.insert(dbEmployee);
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o
                .setId(dbEmployee.getId()).setLeaderEmployeeId(dbEmployee.getId()));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.updateEmployee(reqVO), EMPLOYEE_LEADER_INVALID);
    }

    @Test
    public void testUpdateEmployee_leaderDescendantInvalid() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o.setLeaderEmployeeId(null));
        employeeMapper.insert(employee);
        HrmEmployeeDO childEmployee = randomEmployeeDO(o -> o.setLeaderEmployeeId(employee.getId()));
        employeeMapper.insert(childEmployee);
        HrmEmployeeDO grandchildEmployee = randomEmployeeDO(o -> o.setLeaderEmployeeId(childEmployee.getId()));
        employeeMapper.insert(grandchildEmployee);
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o
                .setId(employee.getId()).setLeaderEmployeeId(grandchildEmployee.getId()));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.updateEmployee(reqVO), EMPLOYEE_LEADER_INVALID);
    }

    @Test
    public void testDeleteEmployee_success() {
        // mock 数据
        HrmEmployeeDO dbEmployee = randomEmployeeDO();
        employeeMapper.insert(dbEmployee);

        // 调用
        employeeService.deleteEmployee(dbEmployee.getId());

        // 断言
        assertNull(employeeMapper.selectById(dbEmployee.getId()));
    }

    @Test
    public void testDeleteEmployee_notExists() {
        // 准备参数
        Long id = randomLongId();

        // 调用，并断言异常
        assertServiceException(() -> employeeService.deleteEmployee(id), EMPLOYEE_NOT_EXISTS);
    }

    @Test
    public void testDeleteEmployeeList_successAndDeduplicate() {
        // mock 数据
        HrmEmployeeDO firstEmployee = randomEmployeeDO();
        employeeMapper.insert(firstEmployee);
        HrmEmployeeDO secondEmployee = randomEmployeeDO();
        employeeMapper.insert(secondEmployee);
        HrmEmployeeDO retainedEmployee = randomEmployeeDO();
        employeeMapper.insert(retainedEmployee);
        // 准备参数
        List<Long> ids = asList(firstEmployee.getId(), secondEmployee.getId(), firstEmployee.getId());

        // 调用
        employeeService.deleteEmployeeList(ids);

        // 断言
        assertNull(employeeMapper.selectById(firstEmployee.getId()));
        assertNull(employeeMapper.selectById(secondEmployee.getId()));
        assertNotNull(employeeMapper.selectById(retainedEmployee.getId()));
    }

    @Test
    public void testDeleteEmployeeList_ignoreNotExists() {
        // mock 数据
        HrmEmployeeDO firstEmployee = randomEmployeeDO();
        employeeMapper.insert(firstEmployee);
        HrmEmployeeDO secondEmployee = randomEmployeeDO();
        employeeMapper.insert(secondEmployee);
        // 准备参数
        List<Long> ids = asList(firstEmployee.getId(), randomLongId(), secondEmployee.getId());

        // 调用
        employeeService.deleteEmployeeList(ids);

        // 断言
        assertNull(employeeMapper.selectById(firstEmployee.getId()));
        assertNull(employeeMapper.selectById(secondEmployee.getId()));
    }

    @Test
    public void testCreateEmployee_jobNumberDuplicate() {
        // mock 数据
        HrmEmployeeDO dbEmployee = randomEmployeeDO();
        employeeMapper.insert(dbEmployee);
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o.setJobNumber(dbEmployee.getJobNumber()));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.createEmployee(reqVO), EMPLOYEE_JOB_NUMBER_DUPLICATE);
    }

    @Test
    public void testCreateEmployee_mobileDuplicate() {
        // mock 数据
        HrmEmployeeDO dbEmployee = randomEmployeeDO();
        employeeMapper.insert(dbEmployee);
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o.setMobile(dbEmployee.getMobile()));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.createEmployee(reqVO), EMPLOYEE_MOBILE_DUPLICATE);
    }

    @Test
    public void testCreateEmployee_userDuplicate() {
        // mock 数据
        HrmEmployeeDO dbEmployee = randomEmployeeDO(o -> o.setUserId(randomLongId()));
        employeeMapper.insert(dbEmployee);
        // 准备参数
        HrmEmployeeSaveReqVO reqVO = randomEmployeeSaveReqVO(o -> o.setUserId(dbEmployee.getUserId()));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.createEmployee(reqVO), EMPLOYEE_USER_DUPLICATE);
    }

    @Test
    public void testGetEmployeeByBusinessKey_duplicateReturnsLatest() {
        // mock 数据
        Long userId = randomLongId();
        Long candidateId = randomLongId();
        String jobNumber = "HRM-DUPLICATE";
        HrmEmployeeDO firstEmployee = randomEmployeeDO(o -> o.setUserId(userId)
                .setCandidateId(candidateId).setJobNumber(jobNumber));
        employeeMapper.insert(firstEmployee);
        HrmEmployeeDO latestEmployee = randomEmployeeDO(o -> o.setUserId(userId)
                .setCandidateId(candidateId).setJobNumber(jobNumber));
        employeeMapper.insert(latestEmployee);

        // 调用，并断言
        assertEquals(latestEmployee.getId(), employeeService.getEmployeeByUserId(userId).getId());
        assertEquals(latestEmployee.getId(), employeeService.getEmployeeByJobNumber(jobNumber).getId());
        assertEquals(latestEmployee.getId(), employeeService.getEmployeeByCandidateId(candidateId).getId());
    }

    @Test
    public void testValidateEmployeeListByEntryStatus_success() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()));
        employeeMapper.insert(employee);

        // 调用
        employeeService.validateEmployeeListByEntryStatus(
                singleton(employee.getId()), HrmEmployeeEntryStatusEnum.ACTIVE.getStatus());
    }

    @Test
    public void testValidateEmployeeListByEntryStatus_invalid() {
        // mock 数据
        HrmEmployeeDO employee = randomEmployeeDO(o -> o.setName("已离职员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus()));
        employeeMapper.insert(employee);

        // 调用，并断言异常
        assertServiceException(() -> employeeService.validateEmployeeListByEntryStatus(
                        singleton(employee.getId()), HrmEmployeeEntryStatusEnum.ACTIVE.getStatus()),
                EMPLOYEE_ENTRY_STATUS_INVALID, employee.getName());
    }

    @Test
    public void testUpdateEmployeeChannelByChannelId_success() {
        // mock 数据
        HrmEmployeeDO firstEmployee = randomEmployeeDO(o -> o.setChannelId(10L));
        employeeMapper.insert(firstEmployee);
        HrmEmployeeDO secondEmployee = randomEmployeeDO(o -> o.setChannelId(10L));
        employeeMapper.insert(secondEmployee);
        HrmEmployeeDO retainedEmployee = randomEmployeeDO(o -> o.setChannelId(30L));
        employeeMapper.insert(retainedEmployee);

        // 调用
        employeeService.updateEmployeeChannelByChannelId(10L, 20L);

        // 断言
        assertEquals(20L, employeeMapper.selectById(firstEmployee.getId()).getChannelId());
        assertEquals(20L, employeeMapper.selectById(secondEmployee.getId()).getChannelId());
        assertEquals(30L, employeeMapper.selectById(retainedEmployee.getId()).getChannelId());
    }

    @Test
    public void testGetEmployeePage() {
        // mock 数据
        LocalDateTime entryTime = LocalDateTime.of(2026, 1, 15, 9, 0);
        LocalDateTime regularTime = LocalDateTime.of(2026, 4, 15, 9, 0);
        HrmEmployeeDO dbEmployee = randomEmployeeDO(o -> o.setName("张三").setJobNumber("HRM001")
                .setMobile("15601691300").setSex(1).setEntryTime(entryTime).setDeptId(100L)
                .setPostName("Java 开发工程师").setRegularTime(regularTime).setWorkAddress("西湖区")
                .setChannelId(10L)
                .setType(HrmEmployeeTypeEnum.FORMAL.getType())
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.PROBATION.getStatus()));
        employeeMapper.insert(dbEmployee);
        // 测试各查询条件不匹配
        employeeMapper.insert(cloneIgnoreId(dbEmployee, o -> o.setName("李四")));
        employeeMapper.insert(cloneIgnoreId(dbEmployee, o -> o.setJobNumber("RD001")));
        employeeMapper.insert(cloneIgnoreId(dbEmployee, o -> o.setMobile("13900000000")));
        employeeMapper.insert(cloneIgnoreId(dbEmployee, o -> o.setSex(2)));
        employeeMapper.insert(cloneIgnoreId(dbEmployee,
                o -> o.setEntryTime(LocalDate.of(2025, 12, 31).atStartOfDay())));
        employeeMapper.insert(cloneIgnoreId(dbEmployee, o -> o.setDeptId(101L)));
        employeeMapper.insert(cloneIgnoreId(dbEmployee, o -> o.setPostName("产品经理")));
        employeeMapper.insert(cloneIgnoreId(dbEmployee,
                o -> o.setRegularTime(LocalDate.of(2026, 5, 2).atStartOfDay())));
        employeeMapper.insert(cloneIgnoreId(dbEmployee, o -> o.setWorkAddress("滨江区")));
        employeeMapper.insert(cloneIgnoreId(dbEmployee, o -> o.setChannelId(20L)));
        employeeMapper.insert(cloneIgnoreId(dbEmployee,
                o -> o.setType(HrmEmployeeTypeEnum.INFORMAL.getType())));
        employeeMapper.insert(cloneIgnoreId(dbEmployee,
                o -> o.setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));
        employeeMapper.insert(cloneIgnoreId(dbEmployee,
                o -> o.setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())));
        // 准备参数
        HrmEmployeePageReqVO reqVO = new HrmEmployeePageReqVO();
        reqVO.setName("张");
        reqVO.setJobNumber("HRM");
        reqVO.setMobile("156");
        reqVO.setSex(1);
        reqVO.setEntryTime(new LocalDateTime[]{
                LocalDate.of(2026, 1, 1).atStartOfDay(), LocalDate.of(2026, 2, 1).atStartOfDay()});
        reqVO.setDeptId(100L);
        reqVO.setPostName("Java");
        reqVO.setRegularTime(new LocalDateTime[]{
                LocalDate.of(2026, 4, 1).atStartOfDay(), LocalDate.of(2026, 5, 1).atStartOfDay()});
        reqVO.setWorkAddress("西湖");
        reqVO.setChannelId(10L);
        reqVO.setType(HrmEmployeeTypeEnum.FORMAL.getType());
        reqVO.setStatusCategory(HrmEmployeeStatusEnum.PROBATION.getStatus());

        // 调用
        PageResult<HrmEmployeeDO> pageResult = employeeService.getEmployeePage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbEmployee, pageResult.getList().get(0));
    }

    @Test
    public void testGetEmployeePage_birthdayTodo() {
        // mock 数据
        LocalDate today = LocalDate.now();
        HrmEmployeeDO birthdayEmployee = randomEmployeeDO(o -> o
                .setBirthday(today.minusYears(20).atStartOfDay()));
        employeeMapper.insert(birthdayEmployee);
        employeeMapper.insert(randomEmployeeDO(o -> o
                .setBirthday(today.plusMonths(1).minusYears(20).atStartOfDay())));
        // 准备参数
        HrmEmployeePageReqVO reqVO = new HrmEmployeePageReqVO();
        reqVO.setTodoType(HrmEmployeeTodoTypeEnum.BIRTHDAY.getType());

        // 调用
        PageResult<HrmEmployeeDO> pageResult = employeeService.getEmployeePage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(birthdayEmployee.getId(), pageResult.getList().get(0).getId());
    }

    @Test
    public void testGetEmployeeList_exportFiltersAndIds() {
        // mock 数据
        HrmEmployeeDO selectedEmployee = randomEmployeeDO(o -> o.setName("张三").setDeptId(100L));
        employeeMapper.insert(selectedEmployee);
        HrmEmployeeDO unselectedEmployee = randomEmployeeDO(o -> o.setName("张四").setDeptId(100L));
        employeeMapper.insert(unselectedEmployee);
        HrmEmployeeDO otherDeptEmployee = randomEmployeeDO(o -> o.setName("张五").setDeptId(200L));
        employeeMapper.insert(otherDeptEmployee);
        // 准备参数
        HrmEmployeeListReqVO reqVO = new HrmEmployeeListReqVO();
        reqVO.setName("张");
        reqVO.setDeptId(100L);
        reqVO.setIds(asList(selectedEmployee.getId(), otherDeptEmployee.getId()));

        // 调用
        List<HrmEmployeeDO> employees = employeeService.getEmployeeList(reqVO);

        // 断言
        assertEquals(1, employees.size());
        assertPojoEquals(selectedEmployee, employees.get(0));
    }

    @Test
    public void testGetEmployeeList_notLimitedByPageSize() {
        // mock 数据
        for (int i = 1; i <= 105; i++) {
            int index = i;
            employeeMapper.insert(randomEmployeeDO(o -> o.setName(String.format("员工%03d", index))
                    .setJobNumber(String.format("LIST%03d", index)).setDeptId(100L)));
        }
        // 准备参数
        HrmEmployeeListReqVO reqVO = new HrmEmployeeListReqVO();
        reqVO.setDeptId(100L);

        // 调用
        List<HrmEmployeeDO> employees = employeeService.getEmployeeList(reqVO);

        // 断言
        assertEquals(105, employees.size());
        assertEquals("LIST105", employees.get(0).getJobNumber());
        assertEquals("LIST001", employees.get(104).getJobNumber());
    }

    @Test
    public void testImportEmployeeList_createUpdateAndFailure() {
        // mock 数据
        HrmEmployeeDO updateEmployee = randomEmployeeDO(o -> o.setName("导入前")
                .setJobNumber("IMPORT001").setMobile("15601691501")
                .setRegularTime(LocalDate.now().minusMonths(1).atStartOfDay()));
        employeeMapper.insert(updateEmployee);
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("手机号已占用")
                .setJobNumber("IMPORT_DUP").setMobile("15601691502")));
        // 准备参数
        List<HrmEmployeeImportExcelVO> importEmployees = asList(
                randomEmployeeImportExcelVO(o -> o.setName("新增员工")
                        .setJobNumber("IMPORT002").setMobile("15601691503")
                        .setBankCardNumber("622202600001").setBankAreaId(330100)
                        .setBankName("招商银行").setBankBranchName("杭州高新支行")
                        .setFirstSocialSecurity(false).setFirstAccumulationFund(false)
                        .setSocialSecurityNumber("SB20260001")
                        .setAccumulationFundNumber("GJJ20260001")
                        .setSocialSecurityStartMonth(LocalDateTime.of(2026, 7, 1, 0, 0))),
                randomEmployeeImportExcelVO(o -> o.setName("导入后")
                        .setJobNumber("IMPORT001").setMobile("15601691501")
                        .setType(HrmEmployeeTypeEnum.INFORMAL.getType())
                        .setStatus(HrmEmployeeStatusEnum.INTERN.getStatus()).setProbation(3)),
                randomEmployeeImportExcelVO(o -> o.setName("失败员工")
                        .setJobNumber("IMPORT003").setMobile("15601691502")));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(importEmployees,
                HrmEmployeeImportDuplicateStrategyEnum.UPDATE.getStrategy());

        // 断言
        assertEquals(singletonList("IMPORT002"), respVO.getCreateJobNumbers());
        assertEquals(singletonList("IMPORT001"), respVO.getUpdateJobNumbers());
        assertEquals(1, respVO.getFailureJobNumbers().size());
        assertEquals("手机号已绑定员工档案",
                respVO.getFailureJobNumbers().get("第 4 行（IMPORT003）"));
        HrmEmployeeDO createdEmployee = employeeMapper.selectByJobNumber("IMPORT002");
        assertNotNull(createdEmployee);
        assertEquals("新增员工", createdEmployee.getName());
        verify(employeeSalaryCardService).saveSalaryCard(argThat(reqVO ->
                createdEmployee.getId().equals(reqVO.getEmployeeId())
                        && "622202600001".equals(reqVO.getBankCardNumber())
                        && Integer.valueOf(330100).equals(reqVO.getBankAreaId())));
        verify(insuranceEmployeeInfoService).saveInsuranceEmployeeInfo(argThat(reqVO ->
                createdEmployee.getId().equals(reqVO.getEmployeeId())
                        && Boolean.FALSE.equals(reqVO.getFirstSocialSecurity())
                        && "SB20260001".equals(reqVO.getSocialSecurityNumber())));
        HrmEmployeeDO updatedEmployee = employeeMapper.selectByJobNumber("IMPORT001");
        assertEquals(updateEmployee.getId(), updatedEmployee.getId());
        assertEquals("导入后", updatedEmployee.getName());
        assertEquals(updateEmployee.getType(), updatedEmployee.getType());
        assertEquals(updateEmployee.getStatus(), updatedEmployee.getStatus());
        assertEquals(0, updatedEmployee.getProbation());
        assertEquals(updateEmployee.getRegularTime(), updatedEmployee.getRegularTime());
    }

    @Test
    public void testImportEmployeeList_existsWithoutUpdate() {
        // mock 数据
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("已存在员工")
                .setJobNumber("IMPORT004").setMobile("15601691504")));
        // 准备参数
        List<HrmEmployeeImportExcelVO> importEmployees = singletonList(randomEmployeeImportExcelVO(o -> o
                .setName("不允许更新").setJobNumber("IMPORT004").setMobile("15601691504")));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(importEmployees,
                HrmEmployeeImportDuplicateStrategyEnum.FAIL.getStrategy());

        // 断言
        assertTrue(respVO.getCreateJobNumbers().isEmpty());
        assertTrue(respVO.getUpdateJobNumbers().isEmpty());
        assertEquals("员工档案已存在",
                respVO.getFailureJobNumbers().get("第 2 行（IMPORT004）"));
    }

    @Test
    public void testImportEmployeeList_skipExistingEmployee() {
        // mock 数据
        HrmEmployeeDO existingEmployee = randomEmployeeDO(o -> o.setName("已存在员工")
                .setJobNumber("IMPORT_SKIP").setMobile("15601691510"));
        employeeMapper.insert(existingEmployee);
        // 准备参数
        HrmEmployeeImportExcelVO importEmployee = randomEmployeeImportExcelVO(o -> o
                .setName("应跳过员工").setJobNumber("IMPORT_SKIP").setMobile("15601691510"));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(singletonList(importEmployee),
                HrmEmployeeImportDuplicateStrategyEnum.SKIP.getStrategy());

        // 断言
        assertEquals(singletonList("IMPORT_SKIP"), respVO.getSkipJobNumbers());
        assertTrue(respVO.getCreateJobNumbers().isEmpty());
        assertTrue(respVO.getUpdateJobNumbers().isEmpty());
        assertTrue(respVO.getFailureJobNumbers().isEmpty());
        assertEquals("已存在员工", employeeMapper.selectById(existingEmployee.getId()).getName());
    }

    @Test
    public void testImportEmployeeList_duplicateStrategyInvalid() {
        // 准备参数
        HrmEmployeeImportExcelVO importEmployee = randomEmployeeImportExcelVO(o -> o
                .setName("导入员工").setJobNumber("IMPORT_INVALID").setMobile("15601691511"));

        // 调用，并断言异常
        assertServiceException(() -> employeeService.importEmployeeList(singletonList(importEmployee), 99),
                EMPLOYEE_IMPORT_DUPLICATE_STRATEGY_INVALID);
    }

    @Test
    public void testImportEmployeeList_resourceFailureRollback() {
        // 准备参数
        HrmEmployeeImportExcelVO importEmployee = randomEmployeeImportExcelVO(o -> o
                .setName("事务回滚").setJobNumber("IMPORT005").setMobile("15601691505")
                .setBankCardNumber("622202600005")
                .setFirstSocialSecurity(false).setFirstAccumulationFund(false));
        // mock 方法
        when(insuranceEmployeeInfoService.saveInsuranceEmployeeInfo(any()))
                .thenThrow(new ServiceException(500, "社保保存失败"));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(
                singletonList(importEmployee), HrmEmployeeImportDuplicateStrategyEnum.UPDATE.getStrategy());

        // 断言
        assertNull(employeeMapper.selectByJobNumber("IMPORT005"));
        assertEquals("社保保存失败",
                respVO.getFailureJobNumbers().get("第 2 行（IMPORT005）"));
    }

    @Test
    public void testImportEmployeeList_resolveRecruitChannelOption() {
        // mock 数据
        HrmRecruitChannelDO recruitChannel = randomPojo(HrmRecruitChannelDO.class,
                o -> o.setId(10L).setName("BOSS直聘"));
        when(recruitChannelService.getRecruitChannelSimpleList()).thenReturn(singletonList(recruitChannel));
        // 准备参数
        HrmEmployeeImportExcelVO importEmployee = randomEmployeeImportExcelVO(o -> o
                .setName("渠道员工").setJobNumber("IMPORT006").setMobile("15601691506")
                .setChannelName(HrmRecruitChannelExcelColumnSelectFunction.formatOption(recruitChannel)));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(
                singletonList(importEmployee), HrmEmployeeImportDuplicateStrategyEnum.UPDATE.getStrategy());

        // 断言
        assertEquals(singletonList("IMPORT006"), respVO.getCreateJobNumbers());
        assertTrue(respVO.getFailureJobNumbers().isEmpty());
        assertEquals(recruitChannel.getId(), employeeMapper.selectByJobNumber("IMPORT006").getChannelId());
        verify(recruitChannelService).validateRecruitChannelExists(recruitChannel.getId());
    }

    @Test
    public void testImportEmployeeList_resolvesBusinessReferences() {
        // mock 数据
        HrmEmployeeDO leader = randomEmployeeDO(o -> o.setJobNumber("LEADER001"));
        employeeMapper.insert(leader);
        AdminUserRespDTO user = new AdminUserRespDTO();
        user.setId(20L);
        user.setMobile("15601691600");
        when(adminUserApi.getUserByMobile(user.getMobile())).thenReturn(user);
        HrmInsuranceSchemeDO scheme = randomPojo(HrmInsuranceSchemeDO.class,
                o -> o.setId(30L).setName("杭州标准方案"));
        when(insuranceSchemeService.getSchemeByName(scheme.getName())).thenReturn(scheme);
        HrmEmployeeImportExcelVO importEmployee = randomEmployeeImportExcelVO(o -> o
                .setName("业务键员工").setJobNumber("IMPORT_BUSINESS_KEY").setMobile("15601691601")
                .setDeptId(10L).setLeaderJobNumber(leader.getJobNumber())
                .setUserMobile(user.getMobile()).setSchemeName(scheme.getName())
                .setFirstSocialSecurity(false).setFirstAccumulationFund(false));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(
                singletonList(importEmployee), HrmEmployeeImportDuplicateStrategyEnum.UPDATE.getStrategy());

        // 断言
        assertTrue(respVO.getFailureJobNumbers().isEmpty());
        HrmEmployeeDO employee = employeeMapper.selectByJobNumber(importEmployee.getJobNumber());
        assertEquals(importEmployee.getDeptId(), employee.getDeptId());
        assertEquals(leader.getId(), employee.getLeaderEmployeeId());
        assertEquals(user.getId(), employee.getUserId());
        verify(insuranceEmployeeInfoService).saveInsuranceEmployeeInfo(argThat(reqVO ->
                employee.getId().equals(reqVO.getEmployeeId()) && scheme.getId().equals(reqVO.getSchemeId())));
    }

    @Test
    public void testImportEmployeeList_resolveRecruitChannelOptionWithDuplicateNames() {
        // mock 数据
        HrmRecruitChannelDO firstChannel = randomPojo(HrmRecruitChannelDO.class,
                o -> o.setId(10L).setName("内部推荐"));
        HrmRecruitChannelDO secondChannel = randomPojo(HrmRecruitChannelDO.class,
                o -> o.setId(20L).setName("内部推荐"));
        when(recruitChannelService.getRecruitChannelSimpleList()).thenReturn(asList(firstChannel, secondChannel));
        // 准备参数
        HrmEmployeeImportExcelVO importEmployee = randomEmployeeImportExcelVO(o -> o
                .setName("渠道员工").setJobNumber("IMPORT007").setMobile("15601691507")
                .setChannelName("内部推荐（20）"));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(
                singletonList(importEmployee), HrmEmployeeImportDuplicateStrategyEnum.UPDATE.getStrategy());

        // 断言
        assertEquals(singletonList("IMPORT007"), respVO.getCreateJobNumbers());
        assertTrue(respVO.getFailureJobNumbers().isEmpty());
        assertEquals(secondChannel.getId(), employeeMapper.selectByJobNumber("IMPORT007").getChannelId());
        verify(recruitChannelService).validateRecruitChannelExists(secondChannel.getId());
    }

    @Test
    public void testImportEmployeeList_recruitChannelNameNotSupported() {
        // mock 数据
        HrmRecruitChannelDO firstChannel = randomPojo(HrmRecruitChannelDO.class,
                o -> o.setId(10L).setName("内部推荐"));
        HrmRecruitChannelDO secondChannel = randomPojo(HrmRecruitChannelDO.class,
                o -> o.setId(20L).setName("内部推荐"));
        when(recruitChannelService.getRecruitChannelSimpleList()).thenReturn(asList(firstChannel, secondChannel));
        // 准备参数
        HrmEmployeeImportExcelVO importEmployee = randomEmployeeImportExcelVO(o -> o
                .setName("重复渠道员工").setJobNumber("IMPORT008").setMobile("15601691508")
                .setChannelName("内部推荐"));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(
                singletonList(importEmployee), HrmEmployeeImportDuplicateStrategyEnum.UPDATE.getStrategy());

        // 断言
        assertNull(employeeMapper.selectByJobNumber("IMPORT008"));
        assertEquals(RECRUIT_CHANNEL_NOT_EXISTS.getMsg(),
                respVO.getFailureJobNumbers().get("第 2 行（IMPORT008）"));
    }

    @Test
    public void testImportEmployeeList_recruitChannelNameNotExists() {
        // 准备参数
        HrmEmployeeImportExcelVO importEmployee = randomEmployeeImportExcelVO(o -> o
                .setName("未知渠道员工").setJobNumber("IMPORT009").setMobile("15601691509")
                .setChannelName("不存在的渠道"));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(
                singletonList(importEmployee), HrmEmployeeImportDuplicateStrategyEnum.UPDATE.getStrategy());

        // 断言
        assertNull(employeeMapper.selectByJobNumber("IMPORT009"));
        assertEquals("招聘渠道不存在",
                respVO.getFailureJobNumbers().get("第 2 行（IMPORT009）"));
    }

    @Test
    public void testImportEmployeeList_empty() {
        // 调用，并断言异常
        assertServiceException(() -> employeeService.importEmployeeList(emptyList(),
                        HrmEmployeeImportDuplicateStrategyEnum.UPDATE.getStrategy()),
                EMPLOYEE_IMPORT_LIST_IS_EMPTY);
    }

    @Test
    public void testImportEmployeeList_duplicateFailuresKeepEveryRow() {
        // mock 数据
        employeeMapper.insert(randomEmployeeDO(o -> o.setJobNumber("IMPORT_DUP_ROW")
                .setMobile("15601691560")));
        // 准备参数
        List<HrmEmployeeImportExcelVO> importEmployees = asList(
                randomEmployeeImportExcelVO(o -> o.setJobNumber("IMPORT_DUP_ROW")
                        .setMobile("15601691560")),
                randomEmployeeImportExcelVO(o -> o.setJobNumber("IMPORT_DUP_ROW")
                        .setMobile("15601691560")));

        // 调用
        HrmEmployeeImportRespVO respVO = employeeService.importEmployeeList(importEmployees,
                HrmEmployeeImportDuplicateStrategyEnum.FAIL.getStrategy());

        // 断言
        assertEquals(2, respVO.getFailureJobNumbers().size());
        assertEquals("员工档案已存在",
                respVO.getFailureJobNumbers().get("第 2 行（IMPORT_DUP_ROW）"));
        assertEquals("员工档案已存在",
                respVO.getFailureJobNumbers().get("第 3 行（IMPORT_DUP_ROW）"));
    }

    @Test
    public void testGetEmployeeStatusCount() {
        // mock 数据
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("正式员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("试用员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.PROBATION.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("实习员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.INTERN.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("待离职员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("待入职员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("离职员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));

        // 调用
        Map<Integer, Long> countMap = employeeService.getEmployeeStatusCount(new HrmEmployeePageReqVO());

        // 断言
        assertEquals(2L, countMap.get(HrmEmployeeStatusEnum.REGULAR.getStatus()));
        assertEquals(1L, countMap.get(HrmEmployeeStatusEnum.PROBATION.getStatus()));
        assertEquals(1L, countMap.get(HrmEmployeeStatusEnum.INTERN.getStatus()));
        assertEquals(4L, countMap.get(HrmEmployeeStatusTabEnum.ACTIVE.getStatus()));
        assertEquals(3L, countMap.get(HrmEmployeeStatusTabEnum.FULL_TIME.getStatus()));
        assertEquals(1L, countMap.get(HrmEmployeeStatusTabEnum.PENDING_ENTRY.getStatus()));
        assertEquals(1L, countMap.get(HrmEmployeeStatusTabEnum.PENDING_LEAVE.getStatus()));
        assertEquals(1L, countMap.get(HrmEmployeeStatusTabEnum.LEFT.getStatus()));
    }

    @Test
    public void testGetEmployeeStatusCount_ignoreStatusCategory() {
        // mock 数据
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("正式员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("离职员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));
        // 准备参数
        HrmEmployeePageReqVO countReqVO = new HrmEmployeePageReqVO();
        countReqVO.setName("正式员工");
        countReqVO.setStatusCategory(HrmEmployeeStatusTabEnum.LEFT.getStatus());

        // 调用
        Map<Integer, Long> filteredCountMap = employeeService.getEmployeeStatusCount(countReqVO);

        // 断言
        assertEquals(1L, filteredCountMap.get(HrmEmployeeStatusEnum.REGULAR.getStatus()));
        assertEquals(1L, filteredCountMap.get(HrmEmployeeStatusTabEnum.ACTIVE.getStatus()));
        assertEquals(1L, filteredCountMap.get(HrmEmployeeStatusTabEnum.FULL_TIME.getStatus()));
        assertEquals(0L, filteredCountMap.get(HrmEmployeeStatusTabEnum.LEFT.getStatus()));
    }

    @Test
    public void testGetEmployeePage_statusCategory() {
        // mock 数据
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("正式员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("试用员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.PROBATION.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("实习员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.INTERN.getStatus())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setName("待离职员工")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())));
        // 准备参数
        HrmEmployeePageReqVO reqVO = new HrmEmployeePageReqVO();
        reqVO.setStatusCategory(HrmEmployeeStatusTabEnum.FULL_TIME.getStatus());

        // 调用并断言
        assertEquals(3L, employeeService.getEmployeePage(reqVO).getTotal());
        reqVO.setStatusCategory(HrmEmployeeStatusEnum.INTERN.getStatus());
        PageResult<HrmEmployeeDO> internPage = employeeService.getEmployeePage(reqVO);
        assertEquals(1L, internPage.getTotal());
        assertEquals("实习员工", internPage.getList().get(0).getName());
    }

    @Test
    public void testGetEmployeeCountMapByDeptAndType() {
        // mock 数据
        Long firstDeptId = randomLongId();
        Long secondDeptId = randomLongId();
        employeeMapper.insert(randomEmployeeDO(o -> o.setDeptId(firstDeptId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setType(HrmEmployeeTypeEnum.FORMAL.getType())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setDeptId(firstDeptId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                .setType(HrmEmployeeTypeEnum.INFORMAL.getType())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setDeptId(firstDeptId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setType(null)));
        employeeMapper.insert(randomEmployeeDO(o -> o.setDeptId(firstDeptId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setType(HrmEmployeeTypeEnum.FORMAL.getType())));
        employeeMapper.insert(randomEmployeeDO(o -> o.setDeptId(secondDeptId)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setType(HrmEmployeeTypeEnum.FORMAL.getType())));

        // 调用
        Map<Long, Map<Integer, Long>> countMap = employeeService.getEmployeeCountMapByDeptAndType();

        // 断言
        assertEquals(3L, countMap.get(firstDeptId).values().stream().mapToLong(Long::longValue).sum());
        assertEquals(1L, countMap.get(firstDeptId).get(HrmEmployeeTypeEnum.FORMAL.getType()));
        assertEquals(1L, countMap.get(firstDeptId).get(HrmEmployeeTypeEnum.INFORMAL.getType()));
        assertEquals(1L, countMap.get(firstDeptId).get(null));
        assertEquals(1L, countMap.get(secondDeptId).get(HrmEmployeeTypeEnum.FORMAL.getType()));
    }

    @Test
    public void testGetEmployeeSurveyCountMap() {
        // mock 数据
        LocalDateTime monthBeginTime = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        HrmEmployeeDO entryEmployee = randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setEntryTime(monthBeginTime.plusDays(1)));
        employeeMapper.insert(entryEmployee);
        employeeMapper.insert(randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_ENTRY.getStatus())
                .setEntryTime(monthBeginTime.plusDays(2))));
        employeeMapper.insert(randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.LEFT.getStatus())
                .setLeaveTime(monthBeginTime.plusDays(3))));
        employeeMapper.insert(randomEmployeeDO(o -> o
                .setEntryStatus(HrmEmployeeEntryStatusEnum.PENDING_LEAVE.getStatus())
                .setLeaveTime(monthBeginTime.plusDays(4))));
        employeeChangeRecordMapper.insert(HrmEmployeeChangeRecordDO.builder()
                .employeeId(entryEmployee.getId()).type(HrmEmployeeChangeTypeEnum.REGULAR.getType())
                .effectTime(monthBeginTime.plusDays(5)).build());
        employeeChangeRecordMapper.insert(HrmEmployeeChangeRecordDO.builder()
                .employeeId(entryEmployee.getId()).type(HrmEmployeeChangeTypeEnum.REGULAR.getType())
                .effectTime(monthBeginTime.plusDays(6)).build());
        employeeChangeRecordMapper.insert(HrmEmployeeChangeRecordDO.builder()
                .employeeId(entryEmployee.getId()).type(HrmEmployeeChangeTypeEnum.TRANSFER.getType())
                .effectTime(monthBeginTime.plusDays(7)).build());

        // 调用
        Map<Integer, Long> countMap = employeeService.getEmployeeSurveyCountMap();

        // 断言
        assertEquals(1L, countMap.get(HrmEmployeeSurveyTypeEnum.ENTRY.getType()));
        assertEquals(1L, countMap.get(HrmEmployeeSurveyTypeEnum.PENDING_ENTRY.getType()));
        assertEquals(1L, countMap.get(HrmEmployeeSurveyTypeEnum.LEAVE.getType()));
        assertEquals(1L, countMap.get(HrmEmployeeSurveyTypeEnum.PENDING_LEAVE.getType()));
        assertEquals(1L, countMap.get(HrmEmployeeSurveyTypeEnum.REGULAR.getType()));
        assertEquals(1L, countMap.get(HrmEmployeeSurveyTypeEnum.TRANSFER.getType()));
    }

    @Test
    public void testUpdateEmployeeBySelf_success() {
        // mock 数据
        Long userId = randomLongId();
        HrmEmployeeDO dbEmployee = randomEmployeeDO(o -> o.setUserId(userId).setName("修改前")
                .setMobile("15601691550").setEmail("before@example.com")
                .setBirthday(LocalDate.of(1990, 1, 1).atStartOfDay()).setAge(36));
        employeeMapper.insert(dbEmployee);
        // 准备参数
        LocalDateTime birthday = LocalDate.now().minusYears(28).atTime(10, 20);
        HrmPortalEmployeeUpdateReqVO reqVO = randomPojo(HrmPortalEmployeeUpdateReqVO.class, o -> o
                .setName("修改后").setMobile("15601691551").setEmail("after@example.com")
                .setBirthday(birthday));
        // mock 方法
        when(employeeFieldConfigService.getEditableArchiveFieldNames()).thenReturn(new HashSet<>(asList(
                HrmEmployeeArchiveFieldEnum.NAME.getName(),
                HrmEmployeeArchiveFieldEnum.EMAIL.getName(),
                HrmEmployeeArchiveFieldEnum.BIRTHDAY.getName())));

        // 调用
        employeeService.updateEmployeeBySelf(userId, reqVO);

        // 断言
        HrmEmployeeDO employee = employeeMapper.selectById(dbEmployee.getId());
        assertEquals("修改后", employee.getName());
        assertEquals("15601691550", employee.getMobile());
        assertEquals("after@example.com", employee.getEmail());
        assertEquals(birthday, employee.getBirthday());
        assertEquals(28, employee.getAge());
    }

    // ========== 随机对象 ==========

    @SafeVarargs
    private static HrmEmployeeSaveReqVO randomEmployeeSaveReqVO(Consumer<HrmEmployeeSaveReqVO>... consumers) {
        Consumer<HrmEmployeeSaveReqVO> consumer = o -> o.setId(null).setName(randomString())
                .setJobNumber("HRM" + randomLongId()).setUserId(null).setMobile(randomMobile())
                .setCountry("中国").setNation("汉族").setIdType(HrmEmployeeIdTypeEnum.OTHER.getType())
                .setIdNumber(randomString()).setSex(1).setEmail(randomEmail()).setNativePlace("浙江杭州")
                .setBirthday(LocalDate.now().minusYears(30).atStartOfDay()).setAge(null)
                .setAddress("杭州市西湖区").setHighestEducation(HrmEmployeeEducationEnum.BACHELOR.getEducation())
                .setDeptId(null).setLeaderEmployeeId(null)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .setType(HrmEmployeeTypeEnum.FORMAL.getType())
                .setEntryTime(LocalDate.now().minusYears(1).atStartOfDay())
                .setProbation(0).setRegularTime(null).setLeaveTime(null)
                .setPostName(randomString()).setPostLevel("P6").setWorkCity("杭州").setWorkAddress("西湖区")
                .setWorkDetailAddress("文三路").setChannelId(null).setCompanyAgeStartTime(null)
                .setCompanyAge(null).setCandidateId(null).setRemark(randomString());
        return randomPojo(HrmEmployeeSaveReqVO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmEmployeeDO randomEmployeeDO(Consumer<HrmEmployeeDO>... consumers) {
        Consumer<HrmEmployeeDO> consumer = o -> o.setName(randomString())
                .setJobNumber("HRM" + randomLongId()).setUserId(null).setMobile(randomMobile())
                .setCountry("中国").setNation("汉族").setIdType(HrmEmployeeIdTypeEnum.OTHER.getType())
                .setIdNumber(randomString()).setSex(1).setEmail(randomEmail()).setNativePlace("浙江杭州")
                .setBirthday(LocalDate.now().minusYears(30).atStartOfDay()).setAge(30)
                .setAddress("杭州市西湖区").setHighestEducation(HrmEmployeeEducationEnum.BACHELOR.getEducation())
                .setDeptId(null).setLeaderEmployeeId(null)
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .setType(HrmEmployeeTypeEnum.FORMAL.getType())
                .setEntryTime(LocalDate.now().minusYears(1).atStartOfDay()).setProbation(0)
                .setRegularTime(LocalDate.now().minusYears(1).atStartOfDay()).setLeaveTime(null)
                .setPostName(randomString()).setPostLevel("P6").setWorkCity("杭州").setWorkAddress("西湖区")
                .setWorkDetailAddress("文三路").setChannelId(null)
                .setCompanyAgeStartTime(LocalDate.now().minusYears(1).atStartOfDay()).setCompanyAge(1)
                .setCandidateId(null).setRemark(randomString()).setDeleted(false);
        return randomPojo(HrmEmployeeDO.class, ArrayUtils.append(consumer, consumers));
    }

    @SafeVarargs
    private static HrmEmployeeImportExcelVO randomEmployeeImportExcelVO(
            Consumer<HrmEmployeeImportExcelVO>... consumers) {
        Consumer<HrmEmployeeImportExcelVO> consumer = o -> o.setName(randomString())
                .setJobNumber("IMPORT" + randomLongId()).setMobile(randomMobile())
                .setCountry("中国").setNation("汉族").setIdType(HrmEmployeeIdTypeEnum.OTHER.getType())
                .setIdNumber(randomString()).setSex(1).setEmail(randomEmail()).setNativePlace("浙江杭州")
                .setBirthday(LocalDate.now().minusYears(30).atStartOfDay())
                .setAddress("杭州市西湖区")
                .setHighestEducation(HrmEmployeeEducationEnum.BACHELOR.getEducation())
                .setDeptId(null).setLeaderJobNumber(null).setPostName(randomString()).setPostLevel("P6")
                .setEntryStatus(HrmEmployeeEntryStatusEnum.ACTIVE.getStatus())
                .setStatus(HrmEmployeeStatusEnum.REGULAR.getStatus())
                .setType(HrmEmployeeTypeEnum.FORMAL.getType())
                .setEntryTime(LocalDate.now().minusMonths(1).atStartOfDay()).setProbation(0)
                .setRegularTime(null).setLeaveTime(null).setWorkCity("杭州").setWorkAddress("西湖区")
                .setWorkDetailAddress("文三路").setChannelName(null).setCompanyAgeStartTime(null)
                .setUserMobile(null).setBankCardNumber(null).setBankAreaId(null).setBankName(null)
                .setBankBranchName(null).setFirstSocialSecurity(null).setFirstAccumulationFund(null)
                .setSocialSecurityNumber(null).setAccumulationFundNumber(null)
                .setSocialSecurityStartMonth(null).setSchemeName(null).setRemark(randomString());
        return randomPojo(HrmEmployeeImportExcelVO.class, ArrayUtils.append(consumer, consumers));
    }

}
