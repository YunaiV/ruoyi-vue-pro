package cn.iocoder.yudao.module.hrm.service.salary.slip;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.HrmSalarySlipRemarkReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.employee.info.HrmEmployeeDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.monthrecord.HrmSalaryMonthEmployeeRecordDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipDO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipTemplateDO;
import cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip.HrmSalarySlipMapper;
import cn.iocoder.yudao.module.hrm.service.employee.info.HrmEmployeeService;
import cn.iocoder.yudao.module.system.api.notify.NotifyMessageSendApi;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipReadStatusEnum;
import cn.iocoder.yudao.module.hrm.enums.salary.slip.HrmSalarySlipTemplateOptionTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_SLIP_ALREADY_SENT;
import static cn.iocoder.yudao.module.hrm.enums.ErrorCodeConstants.SALARY_SLIP_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link HrmSalarySlipServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(HrmSalarySlipServiceImpl.class)
public class HrmSalarySlipServiceImplTest extends BaseDbUnitTest {

    @Resource
    private HrmSalarySlipServiceImpl salarySlipService;
    @Resource
    private HrmSalarySlipMapper salarySlipMapper;

    @MockitoBean
    private HrmEmployeeService employeeService;
    @MockitoBean
    private NotifyMessageSendApi notifyMessageSendApi;

    @Test
    public void testCreateSalarySlipList_success() {
        // mock 数据
        HrmSalaryMonthEmployeeRecordDO employeeRecord = new HrmSalaryMonthEmployeeRecordDO()
                .setId(1001L).setEmployeeId(2001L).setYear(2026).setMonth(8)
                .setRealPaySalary(new BigDecimal("7910.00"))
                .setOptionValues(Arrays.asList(
                        createMonthOptionValue(10101, "基本工资", new BigDecimal("8000.00")),
                        createMonthOptionValue(24001, "实发工资", new BigDecimal("7910.00"))));
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(employeeRecord.getEmployeeId())
                .setName("测试员工").setUserId(3001L);
        when(employeeService.getEmployeeMap(Collections.singleton(employee.getId())))
                .thenReturn(Collections.singletonMap(employee.getId(), employee));
        HrmSalarySlipTemplateDO.Option category = createTemplateOption(
                "工资明细", HrmSalarySlipTemplateOptionTypeEnum.CATEGORY.getType(), -1);
        category.setChildren(Arrays.asList(
                createTemplateOption("基本工资", HrmSalarySlipTemplateOptionTypeEnum.ITEM.getType(), 10101),
                createTemplateOption("实发工资", HrmSalarySlipTemplateOptionTypeEnum.ITEM.getType(), 24001)));
        HrmSalarySlipTemplateDO template = new HrmSalarySlipTemplateDO()
                .setHideEmpty(false).setOptions(Collections.singletonList(category));

        // 调用
        salarySlipService.createSalarySlipList(3001L, Collections.singletonList(employeeRecord), template);

        // 断言
        HrmSalarySlipPageReqVO reqVO = new HrmSalarySlipPageReqVO();
        reqVO.setSendRecordId(3001L);
        PageResult<HrmSalarySlipDO> pageResult = salarySlipService.getSalarySlipPage(reqVO);
        assertEquals(1L, pageResult.getTotal());
        HrmSalarySlipDO salarySlip = pageResult.getList().get(0);
        assertEquals(employeeRecord.getEmployeeId(), salarySlip.getEmployeeId());
        assertEquals(1, salarySlip.getOptions().size());
        assertEquals(2, salarySlip.getOptions().get(0).getChildren().size());
        verify(notifyMessageSendApi).sendSingleMessageToAdmin(argThat(sendReqVO ->
                employee.getUserId().equals(sendReqVO.getUserId())
                        && "7910.00".equals(sendReqVO.getTemplateParams().get("realSalary"))));
    }

    @Test
    public void testCreateSalarySlipList_insertAfterDeletedSlip() {
        // mock 数据
        HrmSalarySlipDO deletedSlip = createSalarySlip(1001L, 2001L, "历史工资条");
        salarySlipMapper.deleteById(deletedSlip.getId());
        HrmSalaryMonthEmployeeRecordDO employeeRecord = new HrmSalaryMonthEmployeeRecordDO()
                .setId(deletedSlip.getMonthEmployeeRecordId()).setEmployeeId(deletedSlip.getEmployeeId())
                .setYear(2026).setMonth(8).setRealPaySalary(new BigDecimal("8000.00"))
                .setOptionValues(Collections.emptyList());
        HrmEmployeeDO employee = new HrmEmployeeDO().setId(employeeRecord.getEmployeeId())
                .setName("测试员工").setUserId(3001L);
        when(employeeService.getEmployeeMap(Collections.singleton(employee.getId())))
                .thenReturn(Collections.singletonMap(employee.getId(), employee));
        HrmSalarySlipTemplateDO template = new HrmSalarySlipTemplateDO()
                .setHideEmpty(false).setOptions(Collections.emptyList());

        // 调用
        salarySlipService.createSalarySlipList(
                4001L, Collections.singletonList(employeeRecord), template);

        // 断言
        assertNull(salarySlipMapper.selectById(deletedSlip.getId()));
        HrmSalarySlipPageReqVO reqVO = new HrmSalarySlipPageReqVO();
        reqVO.setSendRecordId(4001L);
        HrmSalarySlipDO newSlip = salarySlipService.getSalarySlipPage(reqVO).getList().get(0);
        assertNotEquals(deletedSlip.getId(), newSlip.getId());
        assertEquals(Long.valueOf(4001L), newSlip.getSendRecordId());
        assertEquals(0, new BigDecimal("8000.00").compareTo(newSlip.getRealPaySalary()));
    }

    @Test
    public void testCreateSalarySlipList_employeeYearMonthAlreadySent() {
        // mock 数据
        HrmSalarySlipDO sentSlip = createSalarySlip(1001L, 2001L, "已发放");
        HrmSalaryMonthEmployeeRecordDO employeeRecord = new HrmSalaryMonthEmployeeRecordDO()
                .setId(1002L).setEmployeeId(sentSlip.getEmployeeId())
                .setYear(sentSlip.getYear()).setMonth(sentSlip.getMonth())
                .setRealPaySalary(new BigDecimal("8000.00")).setOptionValues(Collections.emptyList());
        when(employeeService.getEmployeeMap(Collections.singleton(employeeRecord.getEmployeeId())))
                .thenReturn(Collections.singletonMap(employeeRecord.getEmployeeId(),
                        new HrmEmployeeDO().setId(employeeRecord.getEmployeeId()).setUserId(3001L)));
        HrmSalarySlipTemplateDO template = new HrmSalarySlipTemplateDO()
                .setHideEmpty(false).setOptions(Collections.emptyList());

        // 调用，并断言异常
        assertServiceException(() -> salarySlipService.createSalarySlipList(
                4001L, Collections.singletonList(employeeRecord), template),
                SALARY_SLIP_ALREADY_SENT);
    }

    @Test
    public void testGetSalarySlipPage() {
        // mock 数据
        HrmSalarySlipDO matchedSlip = createSalarySlip(1001L, 2001L, "已确认无误");
        // 测试查询条件不匹配
        createSalarySlip(1002L, 2002L, "待确认");

        // 准备参数
        HrmSalarySlipPageReqVO reqVO = new HrmSalarySlipPageReqVO();
        reqVO.setSendRecordId(matchedSlip.getSendRecordId());
        reqVO.setEmployeeIds(Collections.singletonList(matchedSlip.getEmployeeId()));
        reqVO.setRemark("无误");

        // 调用
        PageResult<HrmSalarySlipDO> pageResult = salarySlipService.getSalarySlipPage(reqVO);

        // 断言
        assertEquals(1L, pageResult.getTotal());
        assertEquals(matchedSlip.getId(), pageResult.getList().get(0).getId());
    }

    @Test
    public void testGetSalarySlipListByEmployeeId_orderBySendSequence() {
        // mock 数据：先发 2026-08，之后补发 2025-12
        Long employeeId = 2001L;
        HrmSalarySlipDO firstSentSlip = createSalarySlip(
                1001L, employeeId, 2026, 8, new BigDecimal("8000.00"));
        HrmSalarySlipDO laterSentOldMonthSlip = createSalarySlip(
                1002L, employeeId, 2025, 12, new BigDecimal("7000.00"));

        // 调用：排序类型 1、方向 1 按发放时序倒序
        List<HrmSalarySlipDO> salarySlips = salarySlipService.getSalarySlipListByEmployeeId(
                employeeId, YearMonth.of(2025, 1), YearMonth.of(2026, 12), 1, 1);

        // 断言：后补发的旧月份仍排在前面，不能按工资年月重排
        assertEquals(Arrays.asList(laterSentOldMonthSlip.getId(), firstSentSlip.getId()),
                Arrays.asList(salarySlips.get(0).getId(), salarySlips.get(1).getId()));
    }

    @Test
    public void testGetSalarySlipListByEmployeeId_orderByRealPaySalaryStable() {
        // mock 数据
        Long employeeId = 2001L;
        HrmSalarySlipDO firstSlip = createSalarySlip(
                1001L, employeeId, 2026, 7, new BigDecimal("8000.00"));
        HrmSalarySlipDO secondSlip = createSalarySlip(
                1002L, employeeId, 2026, 8, new BigDecimal("8000.00"));

        // 调用：相同实发工资按工资条编号稳定倒序
        List<HrmSalarySlipDO> salarySlips = salarySlipService.getSalarySlipListByEmployeeId(
                employeeId, null, null, 2, 1);

        // 断言
        assertEquals(Arrays.asList(secondSlip.getId(), firstSlip.getId()),
                Arrays.asList(salarySlips.get(0).getId(), salarySlips.get(1).getId()));
    }

    @Test
    public void testUpdateSalarySlipRemark_success() {
        // mock 数据
        HrmSalarySlipDO salarySlip = createSalarySlip(1001L, 2001L, "待确认");
        // 准备参数
        HrmSalarySlipRemarkReqVO remarkReqVO = new HrmSalarySlipRemarkReqVO();
        remarkReqVO.setId(salarySlip.getId());
        remarkReqVO.setRemark("复核通过");

        // 调用
        salarySlipService.updateSalarySlipRemark(remarkReqVO);

        // 断言
        assertEquals("复核通过",
                salarySlipMapper.selectById(salarySlip.getId()).getRemark());
    }

    @Test
    public void testMarkSalarySlipListRead_success() {
        // mock 数据
        HrmSalarySlipDO salarySlip = createSalarySlip(1001L, 2001L, "待确认");

        // 调用
        salarySlipService.markSalarySlipListRead(
                salarySlip.getEmployeeId(), Collections.singletonList(salarySlip.getId()));

        // 断言
        assertEquals(HrmSalarySlipReadStatusEnum.READ.getStatus(),
                salarySlipMapper.selectById(salarySlip.getId()).getReadStatus());
    }

    @Test
    public void testGetSalarySlipReadCountMap() {
        // mock 数据
        HrmSalarySlipDO salarySlip = createSalarySlip(1001L, 2001L, "待确认");
        salarySlipMapper.updateById(new HrmSalarySlipDO().setId(salarySlip.getId())
                .setReadStatus(HrmSalarySlipReadStatusEnum.READ.getStatus()));
        // 准备参数
        List<Long> sendRecordIds = Collections.singletonList(salarySlip.getSendRecordId());

        // 调用
        Map<Long, Long> readCountMap = salarySlipService.getSalarySlipReadCountMap(
                sendRecordIds);

        // 断言
        assertEquals(1L, readCountMap.get(salarySlip.getSendRecordId()));
    }

    @Test
    public void testGetSentMonthEmployeeRecordIdSet() {
        // mock 数据
        HrmSalarySlipDO firstSlip = createSalarySlip(1001L, 2001L, "已确认");
        HrmSalarySlipDO secondSlip = createSalarySlip(1002L, 2002L, "待确认");
        // 准备参数
        List<Long> monthEmployeeRecordIds = Arrays.asList(
                firstSlip.getMonthEmployeeRecordId(), secondSlip.getMonthEmployeeRecordId());

        // 调用
        Set<Long> sentRecordIds = salarySlipService.getSentMonthEmployeeRecordIdSet(
                monthEmployeeRecordIds);

        // 断言
        assertEquals(2, sentRecordIds.size());
    }

    @Test
    public void testGetSalarySlipByIdAndEmployeeId_success() {
        // mock 数据
        HrmSalarySlipDO salarySlip = createSalarySlip(1001L, 2001L, "本人工资条");

        // 调用
        HrmSalarySlipDO result = salarySlipService.getSalarySlipByIdAndEmployeeId(
                salarySlip.getId(), salarySlip.getEmployeeId());

        // 断言
        assertEquals(salarySlip.getId(), result.getId());
        assertEquals(salarySlip.getEmployeeId(), result.getEmployeeId());
        assertEquals("本人工资条", result.getRemark());
    }

    @Test
    public void testGetSalarySlipByIdAndEmployeeId_notOwner() {
        // mock 数据
        HrmSalarySlipDO salarySlip = createSalarySlip(1001L, 2001L, "");

        // 调用，并断言异常（非本人不可查看）
        assertServiceException(() -> salarySlipService.getSalarySlipByIdAndEmployeeId(
                salarySlip.getId(), 9999L), SALARY_SLIP_NOT_EXISTS);
    }

    @Test
    public void testGetSalarySlipByIdAndEmployeeId_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> salarySlipService.getSalarySlipByIdAndEmployeeId(
                9999L, 2001L), SALARY_SLIP_NOT_EXISTS);
    }

    @Test
    public void testMarkSalarySlipListRead_notOwner() {
        // mock 数据
        HrmSalarySlipDO salarySlip = createSalarySlip(1001L, 2001L, "");

        // 调用，并断言异常
        assertServiceException(() -> salarySlipService.markSalarySlipListRead(
                9999L, Collections.singletonList(salarySlip.getId())), SALARY_SLIP_NOT_EXISTS);
        assertEquals(HrmSalarySlipReadStatusEnum.UNREAD.getStatus(), salarySlipMapper.selectById(salarySlip.getId()).getReadStatus());
    }

    // ========== 随机对象 ==========

    private HrmSalarySlipDO createSalarySlip(Long monthEmployeeRecordId, Long employeeId, String remark) {
        HrmSalarySlipDO salarySlip = createSalarySlip(
                monthEmployeeRecordId, employeeId, 2026, 8, new BigDecimal("7910.00"));
        salarySlipMapper.updateById(new HrmSalarySlipDO().setId(salarySlip.getId()).setRemark(remark));
        salarySlip.setRemark(remark);
        return salarySlip;
    }

    private HrmSalarySlipDO createSalarySlip(Long monthEmployeeRecordId, Long employeeId,
                                             Integer year, Integer month, BigDecimal realPaySalary) {
        HrmSalarySlipDO salarySlip = HrmSalarySlipDO.builder()
                .sendRecordId(3001L).monthEmployeeRecordId(monthEmployeeRecordId).employeeId(employeeId)
                .year(year).month(month).readStatus(HrmSalarySlipReadStatusEnum.UNREAD.getStatus())
                .realPaySalary(realPaySalary).remark("")
                .options(Collections.emptyList()).build();
        salarySlipMapper.insert(salarySlip);
        return salarySlip;
    }

    private HrmSalaryMonthEmployeeRecordDO.OptionValue createMonthOptionValue(
            Integer code, String name, BigDecimal value) {
        return HrmSalaryMonthEmployeeRecordDO.OptionValue.builder()
                .code(code).name(name).value(value).build();
    }

    private HrmSalarySlipTemplateDO.Option createTemplateOption(
            String name, Integer type, Integer code) {
        return HrmSalarySlipTemplateDO.Option.builder()
                .name(name).type(type).code(code).hidden(false).sort(code).children(Collections.emptyList()).build();
    }

}
