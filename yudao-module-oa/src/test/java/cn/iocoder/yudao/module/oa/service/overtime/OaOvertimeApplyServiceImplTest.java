package cn.iocoder.yudao.module.oa.service.overtime;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.overtime.vo.OaOvertimeApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.overtime.OaOvertimeApplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.overtime.OaOvertimeApplyMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaOvertimeApplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaOvertimeApplyServiceImpl.class)
public class OaOvertimeApplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaOvertimeApplyService overtimeApplyService;

    @Resource
    private OaOvertimeApplyMapper overtimeApplyMapper;

    @MockBean
    private BpmProcessInstanceApi processInstanceApi;

    @BeforeEach
    public void before() {
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(10L).setUserType(2), new MockHttpServletRequest());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testDraft_updateAndSubmitPermissions() {
        // mock 数据
        Long id = overtimeApplyService.createOvertimeApply(randomOvertimeApplySaveReqVO());
        OaOvertimeApplySaveReqVO reqVO = randomOvertimeApplySaveReqVO().setId(id).setTitle("修改后的草稿");

        // 调用，并断言：他人不能修改、提交
        assertServiceException(() -> overtimeApplyService.updateOvertimeApply(reqVO, 99L), APPLY_ACCESS_DENIED);
        assertServiceException(() -> overtimeApplyService.submitOvertimeApply(new OaOvertimeApplySubmitReqVO()
                .setId(id), 99L), APPLY_ACCESS_DENIED);
        overtimeApplyService.updateOvertimeApply(reqVO, 10L);
        assertEquals("修改后的草稿", overtimeApplyMapper.selectById(id).getTitle());
        assertEquals(-1, overtimeApplyMapper.selectById(id).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmit_duplicateAndUpdateRunning() {
        // mock 数据
        Long id = overtimeApplyService.createOvertimeApply(randomOvertimeApplySaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        overtimeApplyService.submitOvertimeApply(new OaOvertimeApplySubmitReqVO().setId(id), 10L);
        // 断言
        assertServiceException(() -> overtimeApplyService.submitOvertimeApply(new OaOvertimeApplySubmitReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        assertServiceException(() -> overtimeApplyService.updateOvertimeApply(randomOvertimeApplySaveReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
    }

    @Test
    public void testCreateOvertimeApply_success() {
        // 准备参数
        OaOvertimeApplySaveReqVO reqVO = randomOvertimeApplySaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        Long id = overtimeApplyService.createOvertimeApply(reqVO);
        assertEquals(-1, overtimeApplyMapper.selectById(id).getStatus());
        assertNull(overtimeApplyMapper.selectById(id).getProcessInstanceId());
        verifyNoInteractions(processInstanceApi);
        overtimeApplyService.submitOvertimeApply(new OaOvertimeApplySubmitReqVO().setId(id), 10L);

        // 断言
        OaOvertimeApplyDO application = overtimeApplyMapper.selectById(id);
        assertEquals("10", application.getCreator());
        assertEquals(1, application.getStatus());
        assertEquals("process-1", application.getProcessInstanceId());
        assertEquals(new BigDecimal("0.5"), application.getDays());
        verify(processInstanceApi).createProcessInstance(eq(10L), argThat(dto ->
                "oa_overtime_apply".equals(dto.getProcessDefinitionKey()) && id.toString().equals(dto.getBusinessKey())
                        && !dto.getVariables().containsKey("title")));
    }

    @Test
    public void testCreateOvertimeApply_processFailureRollback() {
        // 准备参数
        OaOvertimeApplySaveReqVO reqVO = randomOvertimeApplySaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(anyLong(), any())).thenThrow(new IllegalStateException("流程尚未配置"));

        // 调用
        Long id = overtimeApplyService.createOvertimeApply(reqVO);
        // 断言
        assertThrows(IllegalStateException.class, () -> overtimeApplyService.submitOvertimeApply(
                new OaOvertimeApplySubmitReqVO().setId(id), 10L));
        assertEquals(1L, overtimeApplyMapper.selectCount());
        assertEquals(-1, overtimeApplyMapper.selectById(id).getStatus());
        assertNull(overtimeApplyMapper.selectById(id).getProcessInstanceId());
    }

    @Test
    public void testGetOvertimeApply_success() {
        // mock 数据
        OaOvertimeApplyDO application = randomOvertimeApplyDO();
        overtimeApplyMapper.insert(application);

        // 调用
        OaOvertimeApplyDO result = overtimeApplyService.getOvertimeApply(application.getId());

        // 断言
        assertEquals(application.getId(), result.getId());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetOvertimeApply_notExists() {

        // 调用
        OaOvertimeApplyDO result = overtimeApplyService.getOvertimeApply(1024L);

        // 断言
        assertNull(result);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitOvertimeApply_synchronousApproval() {
        // mock 数据
        Long id = overtimeApplyService.createOvertimeApply(randomOvertimeApplySaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            BpmProcessInstanceCreateReqDTO reqDTO = invocation.getArgument(1);
            overtimeApplyService.updateOvertimeApplyStatus(Long.valueOf(reqDTO.getBusinessKey()), 2);
            return "process-1";
        });

        // 调用
        overtimeApplyService.submitOvertimeApply(new OaOvertimeApplySubmitReqVO().setId(id), 10L);

        // 断言
        OaOvertimeApplyDO overtimeApply = overtimeApplyMapper.selectById(id);
        assertEquals(2, overtimeApply.getStatus());
        assertEquals("process-1", overtimeApply.getProcessInstanceId());
    }

    @Test
    public void testGetOvertimeApplyPage_scopeAndFilter() {
        // mock 数据
        OaOvertimeApplyDO application = randomOvertimeApplyDO().setTitle("匹配申请");
        overtimeApplyMapper.insert(application);
        OaOvertimeApplyDO otherApplication = randomOvertimeApplyDO().setTitle("匹配申请");
        otherApplication.setCreator("99");
        overtimeApplyMapper.insert(otherApplication);
        overtimeApplyMapper.insert(randomOvertimeApplyDO().setId(null).setStatus(2).setTitle("匹配申请"));
        // 准备参数
        OaOvertimeApplyPageReqVO reqVO = new OaOvertimeApplyPageReqVO().setTitle("匹配").setStatus(1);

        // 调用
        PageResult<OaOvertimeApplyDO> page = overtimeApplyService.getOvertimeApplyPage(10L, reqVO);

        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(application.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testUpdateOvertimeApplyStatus_successAndRepeated() {
        // mock 数据
        OaOvertimeApplyDO application = randomOvertimeApplyDO();
        overtimeApplyMapper.insert(application);

        // 调用
        overtimeApplyService.updateOvertimeApplyStatus(application.getId(), 2);
        overtimeApplyService.updateOvertimeApplyStatus(application.getId(), 2);

        // 断言
        assertEquals(2, overtimeApplyMapper.selectById(application.getId()).getStatus());
    }

    @Test
    public void testUpdateOvertimeApplyStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> overtimeApplyService.updateOvertimeApplyStatus(1024L, 2), APPLY_NOT_EXISTS);
    }

    @Test
    public void testCreateOvertimeApply_roundHalfUp() {
        // 准备参数
        OaOvertimeApplySaveReqVO reqVO = randomOvertimeApplySaveReqVO()
                .setStartTime(LocalDateTime.of(2026, 9, 13, 23, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 14, 2, 36));

        // 调用
        Long id = overtimeApplyService.createOvertimeApply(reqVO);

        // 断言：3.6 小时为 0.15 天，保留一位小数四舍五入为 0.2 天
        assertEquals(new BigDecimal("0.2"), overtimeApplyMapper.selectById(id).getDays());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateOvertimeApply_recalculateDays() {
        // mock 数据
        Long id = overtimeApplyService.createOvertimeApply(randomOvertimeApplySaveReqVO());
        // 准备参数
        OaOvertimeApplySaveReqVO reqVO = randomOvertimeApplySaveReqVO().setId(id)
                .setStartTime(LocalDateTime.of(2026, 9, 13, 8, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 14, 20, 0));

        // 调用
        overtimeApplyService.updateOvertimeApply(reqVO, 10L);

        // 断言
        OaOvertimeApplyDO overtimeApply = overtimeApplyMapper.selectById(id);
        assertEquals(new BigDecimal("1.5"), overtimeApply.getDays());
        assertEquals(-1, overtimeApply.getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    // ========== 随机对象 ==========

    /**
     * 构造申请参数，固定业务字段的合法取值。
     *
     * @return 请求参数
     */
    private static OaOvertimeApplySaveReqVO randomOvertimeApplySaveReqVO() {
        return randomPojo(OaOvertimeApplySaveReqVO.class).setTitle("业务申请").setUrgency(1).setId(null).setType(1)
                .setStartTime(LocalDateTime.of(2026, 9, 13, 8, 0)).setEndTime(LocalDateTime.of(2026, 9, 13, 20, 0));
    }

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaOvertimeApplyDO randomOvertimeApplyDO() {
        return randomPojo(OaOvertimeApplyDO.class, application -> {
            application.setId(null).setTitle("申请").setUrgency(1).setStatus(1).setProcessInstanceId("process-1");
            application.setCreator("10").setDeleted(false);
        });
    }

}
