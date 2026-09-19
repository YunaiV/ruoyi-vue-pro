package cn.iocoder.yudao.module.oa.service.reimbursement;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.jackson.config.YudaoJacksonAutoConfiguration;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementSaveReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.reimbursement.vo.OaReimbursementSubmitReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.reimbursement.OaReimbursementDO;
import cn.iocoder.yudao.module.oa.dal.mysql.reimbursement.OaReimbursementMapper;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaReimbursementServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import({OaReimbursementServiceImpl.class, JacksonAutoConfiguration.class, YudaoJacksonAutoConfiguration.class})
public class OaReimbursementServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaReimbursementService reimbursementService;

    @Resource
    private OaReimbursementMapper reimbursementMapper;
    @Resource
    private ObjectMapper objectMapper;

    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private BpmProcessInstanceApi processInstanceApi;

    @BeforeEach
    public void before() {
        // JSON 处理器为静态共享配置，每个用例恢复当前上下文的生产日期序列化配置
        JacksonTypeHandler.setObjectMapper(objectMapper);
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(10L).setUserType(2), new MockHttpServletRequest());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testDraft_updateAndSubmitPermissions() {
        // mock 数据
        Long id = reimbursementService.createReimbursement(randomReimbursementSaveReqVO());
        OaReimbursementSaveReqVO reqVO = randomReimbursementSaveReqVO().setId(id).setTitle("修改后的草稿");

        // 调用，并断言：他人不能修改、提交
        assertServiceException(() -> reimbursementService.updateReimbursement(reqVO, 99L), APPLY_ACCESS_DENIED);
        assertServiceException(() -> reimbursementService.submitReimbursement(new OaReimbursementSubmitReqVO()
                .setId(id), 99L), APPLY_ACCESS_DENIED);
        reimbursementService.updateReimbursement(reqVO, 10L);
        assertEquals("修改后的草稿", reimbursementMapper.selectById(id).getTitle());
        assertEquals(-1, reimbursementMapper.selectById(id).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmit_duplicateAndUpdateRunning() {
        // mock 数据
        Long id = reimbursementService.createReimbursement(randomReimbursementSaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        reimbursementService.submitReimbursement(new OaReimbursementSubmitReqVO().setId(id), 10L);
        // 断言
        assertServiceException(() -> reimbursementService.submitReimbursement(new OaReimbursementSubmitReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        assertServiceException(() -> reimbursementService.updateReimbursement(randomReimbursementSaveReqVO()
                .setId(id), 10L), APPLY_STATUS_INVALID);
        verify(processInstanceApi, times(1)).createProcessInstance(eq(10L), any());
    }

    @Test
    public void testCreateReimbursement_success() {
        // 准备参数
        OaReimbursementSaveReqVO reqVO = randomReimbursementSaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenReturn("process-1");

        // 调用
        Long id = reimbursementService.createReimbursement(reqVO);
        assertEquals(-1, reimbursementMapper.selectById(id).getStatus());
        assertNull(reimbursementMapper.selectById(id).getProcessInstanceId());
        verifyNoInteractions(processInstanceApi);
        reimbursementService.submitReimbursement(new OaReimbursementSubmitReqVO().setId(id), 10L);

        // 断言
        OaReimbursementDO application = reimbursementMapper.selectById(id);
        assertEquals("10", application.getCreator());
        assertEquals(1, application.getStatus());
        assertEquals("process-1", application.getProcessInstanceId());
        assertEquals(new BigDecimal("12.34"), application.getTotalPrice());
        assertEquals(2, application.getInvoiceCount());
        assertEquals(1, CollUtil.getFirst(application.getItems()).getExpenseType());
        verify(processInstanceApi).createProcessInstance(eq(10L), argThat(dto ->
                "oa_reimbursement".equals(dto.getProcessDefinitionKey()) && id.toString().equals(dto.getBusinessKey())
                        && !dto.getVariables().containsKey("title")));
    }

    @Test
    public void testCreateReimbursement_processFailureRollback() {
        // 准备参数
        OaReimbursementSaveReqVO reqVO = randomReimbursementSaveReqVO();
        // mock 方法
        when(processInstanceApi.createProcessInstance(anyLong(), any())).thenThrow(new IllegalStateException("流程尚未配置"));

        // 调用
        Long id = reimbursementService.createReimbursement(reqVO);
        // 断言
        assertThrows(IllegalStateException.class, () -> reimbursementService.submitReimbursement(
                new OaReimbursementSubmitReqVO().setId(id), 10L));
        assertEquals(1L, reimbursementMapper.selectCount());
        assertEquals(-1, reimbursementMapper.selectById(id).getStatus());
        assertNull(reimbursementMapper.selectById(id).getProcessInstanceId());
    }

    @Test
    public void testGetReimbursement_success() {
        // mock 数据
        OaReimbursementDO application = randomReimbursementDO();
        reimbursementMapper.insert(application);

        // 调用
        OaReimbursementDO result = reimbursementService.getReimbursement(application.getId());

        // 断言
        assertEquals(application.getId(), result.getId());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetReimbursement_notExists() {

        // 调用
        OaReimbursementDO result = reimbursementService.getReimbursement(1024L);

        // 断言
        assertNull(result);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testSubmitReimbursement_synchronousApproval() {
        // mock 数据
        Long id = reimbursementService.createReimbursement(randomReimbursementSaveReqVO());
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(10L), any())).thenAnswer(invocation -> {
            BpmProcessInstanceCreateReqDTO reqDTO = invocation.getArgument(1);
            reimbursementService.updateReimbursementStatus(Long.valueOf(reqDTO.getBusinessKey()), 2);
            return "process-1";
        });

        // 调用
        reimbursementService.submitReimbursement(new OaReimbursementSubmitReqVO().setId(id), 10L);

        // 断言
        OaReimbursementDO reimbursement = reimbursementMapper.selectById(id);
        assertEquals(2, reimbursement.getStatus());
        assertEquals("process-1", reimbursement.getProcessInstanceId());
    }

    @Test
    public void testGetReimbursementPage_scopeAndFilter() {
        // mock 数据
        OaReimbursementDO application = randomReimbursementDO().setTitle("匹配申请");
        reimbursementMapper.insert(application);
        OaReimbursementDO otherApplication = randomReimbursementDO().setTitle("匹配申请");
        otherApplication.setCreator("99");
        reimbursementMapper.insert(otherApplication);
        reimbursementMapper.insert(randomReimbursementDO().setId(null).setStatus(2).setTitle("匹配申请"));
        // 准备参数
        OaReimbursementPageReqVO reqVO = new OaReimbursementPageReqVO().setTitle("匹配").setStatus(1);

        // 调用
        PageResult<OaReimbursementDO> page = reimbursementService.getReimbursementPage(10L, reqVO);

        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(application.getId(), CollUtil.getFirst(page.getList()).getId());
    }

    @Test
    public void testUpdateReimbursementStatus_successAndRepeated() {
        // mock 数据
        OaReimbursementDO application = randomReimbursementDO();
        reimbursementMapper.insert(application);

        // 调用
        reimbursementService.updateReimbursementStatus(application.getId(), 2);
        reimbursementService.updateReimbursementStatus(application.getId(), 2);

        // 断言
        assertEquals(2, reimbursementMapper.selectById(application.getId()).getStatus());
    }

    @Test
    public void testUpdateReimbursementStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> reimbursementService.updateReimbursementStatus(1024L, 2), APPLY_NOT_EXISTS);
    }

    @Test
    public void testUpdateReimbursement_recalculateTotals() {
        // mock 数据
        Long id = reimbursementService.createReimbursement(randomReimbursementSaveReqVO());
        // 准备参数
        OaReimbursementSaveReqVO reqVO = randomReimbursementSaveReqVO().setId(id);
        CollUtil.getFirst(reqVO.getItems()).setPrice(new BigDecimal("25.50")).setInvoiceCount(3);

        // 调用
        reimbursementService.updateReimbursement(reqVO, 10L);

        // 断言
        OaReimbursementDO reimbursement = reimbursementMapper.selectById(id);
        assertEquals(new BigDecimal("25.50"), reimbursement.getTotalPrice());
        assertEquals(3, reimbursement.getInvoiceCount());
        assertEquals(new BigDecimal("25.50"), CollUtil.getFirst(reimbursement.getItems()).getPrice());
        assertEquals(-1, reimbursement.getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testCreateReimbursement_multipleItems() {
        // 准备参数
        OaReimbursementSaveReqVO reqVO = randomReimbursementSaveReqVO();
        OaReimbursementSaveReqVO.Item firstItem = CollUtil.getFirst(reqVO.getItems());
        OaReimbursementSaveReqVO.Item secondItem = new OaReimbursementSaveReqVO.Item()
                .setExpenseTime(firstItem.getExpenseTime()).setExpenseType(2)
                .setPrice(new BigDecimal("7.66")).setInvoiceCount(3);
        reqVO.setItems(Arrays.asList(firstItem, secondItem));

        // 调用
        Long id = reimbursementService.createReimbursement(reqVO);

        // 断言
        OaReimbursementDO reimbursement = reimbursementMapper.selectById(id);
        assertEquals(new BigDecimal("20.00"), reimbursement.getTotalPrice());
        assertEquals(5, reimbursement.getInvoiceCount());
        assertEquals(2, reimbursement.getItems().size());
        verify(adminUserApi).validateUser(reqVO.getWitnessUserId());
        verifyNoInteractions(processInstanceApi);
    }

    // ========== 随机对象 ==========

    /**
     * 构造申请参数，固定业务字段的合法取值。
     *
     * @return 请求参数
     */
    private static OaReimbursementSaveReqVO randomReimbursementSaveReqVO() {
        return randomPojo(OaReimbursementSaveReqVO.class).setTitle("业务申请").setUrgency(1).setId(null)
                .setWitnessUserId(20L).setPaymentMethod(1)
                .setItems(Collections.singletonList(new OaReimbursementSaveReqVO.Item()
                        .setExpenseTime(LocalDateTime.of(2026, 9, 13, 8, 0)).setExpenseType(1)
                        .setPrice(new BigDecimal("12.34")).setInvoiceCount(2)));
    }

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaReimbursementDO randomReimbursementDO() {
        return randomPojo(OaReimbursementDO.class, application -> {
            application.setId(null).setTitle("申请").setUrgency(1).setStatus(1).setProcessInstanceId("process-1");
            application.setCreator("10").setDeleted(false);
            application.setTotalPrice(new BigDecimal("12.34")).setItems(Collections.emptyList()).setInvoiceCount(2);
        });
    }

}
