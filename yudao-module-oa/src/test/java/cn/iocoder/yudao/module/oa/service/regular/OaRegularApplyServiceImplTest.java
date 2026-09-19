package cn.iocoder.yudao.module.oa.service.regular;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplyPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplySaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.regular.vo.OaRegularApplySubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.regular.OaRegularApplyDO;
import cn.iocoder.yudao.module.oa.dal.mysql.regular.OaRegularApplyMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaRegularApplyServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaRegularApplyServiceImpl.class)
public class OaRegularApplyServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaRegularApplyService regularApplyService;

    @Resource
    private OaRegularApplyMapper regularApplyMapper;

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
        Long id = regularApplyService.createRegularApply(randomRegularApplySaveReqVO());
        OaRegularApplySaveReqVO reqVO = randomRegularApplySaveReqVO().setId(id).setTitle("修改后的草稿");

        // 调用，并断言：他人不能修改、提交
        assertServiceException(() -> regularApplyService.updateRegularApply(reqVO, 99L), APPLY_ACCESS_DENIED);
        assertServiceException(() -> regularApplyService.submitRegularApply(new OaRegularApplySubmitReqVO()
                .setId(id), 99L), APPLY_ACCESS_DENIED);
        regularApplyService.updateRegularApply(reqVO, 10L);
        assertEquals("修改后的草稿", regularApplyMapper.selectById(id).getTitle());
        assertEquals(-1, regularApplyMapper.selectById(id).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmit_duplicateAndUpdateRunning() {
        // mock 数据
        Long id = regularApplyService.createRegularApply(randomRegularApplySaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        regularApplyService.submitRegularApply(new OaRegularApplySubmitReqVO().setId(id), 10L);
        // 断言
        assertServiceException(() -> regularApplyService.submitRegularApply(new OaRegularApplySubmitReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        assertServiceException(() -> regularApplyService.updateRegularApply(randomRegularApplySaveReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
    }

    @Test
    public void testCreateRegularApply_success() {
        // 准备参数
        OaRegularApplySaveReqVO reqVO = randomRegularApplySaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        Long id = regularApplyService.createRegularApply(reqVO);
        assertEquals(-1, regularApplyMapper.selectById(id).getStatus());
        assertNull(regularApplyMapper.selectById(id).getProcessInstanceId());
        verifyNoInteractions(processInstanceApi);
        regularApplyService.submitRegularApply(new OaRegularApplySubmitReqVO().setId(id), 10L);

        // 断言
        OaRegularApplyDO application = regularApplyMapper.selectById(id);
        assertEquals("10", application.getCreator());
        assertEquals(1, application.getStatus());
        assertEquals("process-1", application.getProcessInstanceId());
        assertEquals(1, application.getDays());
        verify(processInstanceApi).createProcessInstance(eq(10L), argThat(dto ->
                "oa_regular_apply".equals(dto.getProcessDefinitionKey()) && id.toString().equals(dto.getBusinessKey())
                        && !dto.getVariables().containsKey("title")));
    }

    @Test
    public void testCreateRegularApply_processFailureRollback() {
        // 准备参数
        OaRegularApplySaveReqVO reqVO = randomRegularApplySaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(anyLong(), any())).thenThrow(new IllegalStateException("流程尚未配置"));

        // 调用
        Long id = regularApplyService.createRegularApply(reqVO);
        // 断言
        assertThrows(IllegalStateException.class, () -> regularApplyService.submitRegularApply(
                new OaRegularApplySubmitReqVO().setId(id), 10L));
        assertEquals(1L, regularApplyMapper.selectCount());
        assertEquals(-1, regularApplyMapper.selectById(id).getStatus());
        assertNull(regularApplyMapper.selectById(id).getProcessInstanceId());
    }

    @Test
    public void testGetRegularApply_success() {
        // mock 数据
        OaRegularApplyDO application = randomRegularApplyDO();
        regularApplyMapper.insert(application);

        // 调用
        OaRegularApplyDO result = regularApplyService.getRegularApply(application.getId());

        // 断言
        assertEquals(application.getId(), result.getId());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetRegularApply_notExists() {

        // 调用
        OaRegularApplyDO result = regularApplyService.getRegularApply(1024L);

        // 断言
        assertNull(result);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitRegularApply_synchronousApproval() {
        // mock 数据
        Long id = regularApplyService.createRegularApply(randomRegularApplySaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            BpmProcessInstanceCreateReqDTO reqDTO = invocation.getArgument(1);
            regularApplyService.updateRegularApplyStatus(Long.valueOf(reqDTO.getBusinessKey()), 2);
            return "process-1";
        });

        // 调用
        regularApplyService.submitRegularApply(new OaRegularApplySubmitReqVO().setId(id), 10L);

        // 断言
        OaRegularApplyDO regularApply = regularApplyMapper.selectById(id);
        assertEquals(2, regularApply.getStatus());
        assertEquals("process-1", regularApply.getProcessInstanceId());
    }

    @Test
    public void testGetRegularApplyPage_scopeAndFilter() {
        // mock 数据
        OaRegularApplyDO application = randomRegularApplyDO().setTitle("匹配申请");
        regularApplyMapper.insert(application);
        OaRegularApplyDO otherApplication = randomRegularApplyDO().setTitle("匹配申请");
        otherApplication.setCreator("99");
        regularApplyMapper.insert(otherApplication);
        regularApplyMapper.insert(randomRegularApplyDO().setId(null).setStatus(2).setTitle("匹配申请"));
        // 准备参数
        OaRegularApplyPageReqVO reqVO = new OaRegularApplyPageReqVO().setTitle("匹配").setStatus(1);

        // 调用
        PageResult<OaRegularApplyDO> page = regularApplyService.getRegularApplyPage(10L, reqVO);

        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(application.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testUpdateRegularApplyStatus_successAndRepeated() {
        // mock 数据
        OaRegularApplyDO application = randomRegularApplyDO();
        regularApplyMapper.insert(application);

        // 调用
        regularApplyService.updateRegularApplyStatus(application.getId(), 2);
        regularApplyService.updateRegularApplyStatus(application.getId(), 2);

        // 断言
        assertEquals(2, regularApplyMapper.selectById(application.getId()).getStatus());
    }

    @Test
    public void testUpdateRegularApplyStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> regularApplyService.updateRegularApplyStatus(1024L, 2), APPLY_NOT_EXISTS);
    }

    @Test
    public void testCreateRegularApply_crossMidnight() {
        // 准备参数
        OaRegularApplySaveReqVO reqVO = randomRegularApplySaveReqVO()
                .setStartTime(LocalDateTime.of(2026, 9, 13, 23, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 14, 1, 0));

        // 调用
        Long id = regularApplyService.createRegularApply(reqVO);

        // 断言：跨午夜的 2 小时按时长向上取整为 1 天
        assertEquals(1, regularApplyMapper.selectById(id).getDays());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateRegularApply_recalculateDays() {
        // mock 数据
        Long id = regularApplyService.createRegularApply(randomRegularApplySaveReqVO());
        // 准备参数
        OaRegularApplySaveReqVO reqVO = randomRegularApplySaveReqVO().setId(id)
                .setStartTime(LocalDateTime.of(2026, 9, 13, 8, 0))
                .setEndTime(LocalDateTime.of(2026, 9, 14, 20, 0));

        // 调用
        regularApplyService.updateRegularApply(reqVO, 10L);

        // 断言
        OaRegularApplyDO regularApply = regularApplyMapper.selectById(id);
        assertEquals(2, regularApply.getDays());
        assertEquals(-1, regularApply.getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    // ========== 随机对象 ==========

    /**
     * 构造申请参数，固定业务字段的合法取值。
     *
     * @return 请求参数
     */
    private static OaRegularApplySaveReqVO randomRegularApplySaveReqVO() {
        return randomPojo(OaRegularApplySaveReqVO.class).setTitle("业务申请").setUrgency(1).setId(null)
                .setStartTime(LocalDateTime.of(2026, 9, 13, 8, 0)).setEndTime(LocalDateTime.of(2026, 9, 13, 20, 0));
    }

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaRegularApplyDO randomRegularApplyDO() {
        return randomPojo(OaRegularApplyDO.class, application -> {
            application.setId(null).setTitle("申请").setUrgency(1).setStatus(1).setProcessInstanceId("process-1");
            application.setCreator("10").setDeleted(false);
        });
    }

}
