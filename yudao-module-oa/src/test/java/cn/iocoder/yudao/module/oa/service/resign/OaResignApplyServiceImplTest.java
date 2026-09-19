package cn.iocoder.yudao.module.oa.service.resign;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.resign.vo.OaResignApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.resign.vo.OaResignApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.resign.vo.OaResignApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.resign.OaResignApplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.resign.OaResignApplyMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaResignApplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaResignApplyServiceImpl.class)
public class OaResignApplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaResignApplyService resignApplyService;

    @Resource
    private OaResignApplyMapper resignApplyMapper;

    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
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
        Long id = resignApplyService.createResignApply(randomResignApplySaveReqVO());
        OaResignApplySaveReqVO reqVO = randomResignApplySaveReqVO().setId(id).setTitle("修改后的草稿");

        // 调用，并断言：他人不能修改、提交
        assertServiceException(() -> resignApplyService.updateResignApply(reqVO, 99L), APPLY_ACCESS_DENIED);
        assertServiceException(() -> resignApplyService.submitResignApply(new OaResignApplySubmitReqVO()
                .setId(id), 99L), APPLY_ACCESS_DENIED);
        resignApplyService.updateResignApply(reqVO, 10L);
        assertEquals("修改后的草稿", resignApplyMapper.selectById(id).getTitle());
        assertEquals(-1, resignApplyMapper.selectById(id).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmit_duplicateAndUpdateRunning() {
        // mock 数据
        Long id = resignApplyService.createResignApply(randomResignApplySaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        resignApplyService.submitResignApply(new OaResignApplySubmitReqVO().setId(id), 10L);
        // 断言
        assertServiceException(() -> resignApplyService.submitResignApply(new OaResignApplySubmitReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        assertServiceException(() -> resignApplyService.updateResignApply(randomResignApplySaveReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
    }

    @Test
    public void testCreateResignApply_success() {
        // 准备参数
        OaResignApplySaveReqVO reqVO = randomResignApplySaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        Long id = resignApplyService.createResignApply(reqVO);
        assertEquals(-1, resignApplyMapper.selectById(id).getStatus());
        assertNull(resignApplyMapper.selectById(id).getProcessInstanceId());
        verifyNoInteractions(processInstanceApi);
        resignApplyService.submitResignApply(new OaResignApplySubmitReqVO().setId(id), 10L);

        // 断言
        OaResignApplyDO application = resignApplyMapper.selectById(id);
        assertEquals("10", application.getCreator());
        assertEquals(1, application.getStatus());
        assertEquals("process-1", application.getProcessInstanceId());
        verify(processInstanceApi).createProcessInstance(eq(10L), argThat(dto ->
                "oa_resign_apply".equals(dto.getProcessDefinitionKey()) && id.toString().equals(dto.getBusinessKey())));
    }

    @Test
    public void testCreateResignApply_processFailureRollback() {
        // 准备参数
        OaResignApplySaveReqVO reqVO = randomResignApplySaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(anyLong(), any())).thenThrow(new IllegalStateException("流程尚未配置"));

        // 调用
        Long id = resignApplyService.createResignApply(reqVO);
        // 断言
        assertThrows(IllegalStateException.class, () -> resignApplyService.submitResignApply(
                new OaResignApplySubmitReqVO().setId(id), 10L));
        assertEquals(1L, resignApplyMapper.selectCount());
        assertEquals(-1, resignApplyMapper.selectById(id).getStatus());
        assertNull(resignApplyMapper.selectById(id).getProcessInstanceId());
    }

    @Test
    public void testGetResignApply_owner() {
        // mock 数据
        OaResignApplyDO application = randomResignApplyDO();
        resignApplyMapper.insert(application);

        // 调用
        OaResignApplyDO result = resignApplyService.getResignApply(application.getId());

        // 断言
        assertEquals(application.getId(), result.getId());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetResignApply_notExists() {

        // 调用
        OaResignApplyDO result = resignApplyService.getResignApply(1024L);

        // 断言
        assertNull(result);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetResignApplyPage_scopeAndFilter() {
        // mock 数据
        OaResignApplyDO application = randomResignApplyDO().setTitle("匹配申请");
        resignApplyMapper.insert(application);
        OaResignApplyDO otherApplication = randomResignApplyDO().setTitle("匹配申请");
        otherApplication.setCreator("99");
        resignApplyMapper.insert(otherApplication);
        resignApplyMapper.insert(randomResignApplyDO().setId(null).setStatus(2).setTitle("匹配申请"));
        // 准备参数
        OaResignApplyPageReqVO reqVO = new OaResignApplyPageReqVO().setTitle("匹配").setStatus(1);

        // 调用
        PageResult<OaResignApplyDO> page = resignApplyService.getResignApplyPage(10L, reqVO);

        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(application.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4})
    public void testUpdateResignApplyStatus_successAndRepeated(Integer status) {
        // mock 数据
        OaResignApplyDO application = randomResignApplyDO();
        resignApplyMapper.insert(application);

        // 调用
        resignApplyService.updateResignApplyStatus(application.getId(), status);
        resignApplyService.updateResignApplyStatus(application.getId(), status);

        // 断言
        assertEquals(status, resignApplyMapper.selectById(application.getId()).getStatus());
    }
    @Test
    public void testUpdateResignApplyStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> resignApplyService.updateResignApplyStatus(1024L, 2), APPLY_NOT_EXISTS);
    }

    @Test
    public void testSubmitResignApply_synchronousApproval() {
        // mock 数据
        Long id = resignApplyService.createResignApply(randomResignApplySaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            BpmProcessInstanceCreateReqDTO reqDTO = invocation.getArgument(1);
            resignApplyService.updateResignApplyStatus(Long.valueOf(reqDTO.getBusinessKey()), 2);
            return "process-1";
        });

        // 调用
        resignApplyService.submitResignApply(new OaResignApplySubmitReqVO().setId(id), 10L);

        // 断言
        OaResignApplyDO resignApply = resignApplyMapper.selectById(id);
        assertEquals(2, resignApply.getStatus());
        assertEquals("process-1", resignApply.getProcessInstanceId());
    }

    // ========== 随机对象 ==========

    /**
     * 构造申请参数，固定业务字段的合法取值。
     *
     * @return 请求参数
     */
    private static OaResignApplySaveReqVO randomResignApplySaveReqVO() {
        return randomPojo(OaResignApplySaveReqVO.class).setTitle("业务申请").setUrgency(1).setId(null).setHandoverUserId(20L)
                .setHasPendingReimbursement(false);
    }

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaResignApplyDO randomResignApplyDO() {
        return randomPojo(OaResignApplyDO.class, application -> {
            application.setId(null).setTitle("申请").setUrgency(1).setStatus(1).setProcessInstanceId("process-1");
            application.setCreator("10").setDeleted(false);
        });
    }

}
