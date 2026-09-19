package cn.iocoder.yudao.module.oa.service.officialdoc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.*;
import cn.iocoder.yudao.module.oa.dal.mysql.officialdoc.*;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaOfficialDocReceiveServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaOfficialDocReceiveServiceImpl.class)
public class OaOfficialDocReceiveServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaOfficialDocReceiveService officialDocReceiveService;

    @Resource
    private OaOfficialDocReceiveMapper officialDocReceiveMapper;

    @MockitoBean
    private AdminUserApi adminUserApi;
    @MockitoBean
    private BpmProcessInstanceApi processInstanceApi;
    @MockitoBean
    private OaNoRedisDAO noRedisDAO;

    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 4})
    public void testDeleteOfficialDocReceive_allowedStatuses(int status) {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setStatus(status);
        officialDocReceiveMapper.insert(receive);

        // 调用，并断言：其他人不能删除
        assertServiceException(() -> officialDocReceiveService.deleteOfficialDocReceive(receive.getId(), 2L),
                OFFICIAL_DOC_ACCESS_DENIED);
        officialDocReceiveService.deleteOfficialDocReceive(receive.getId(), 1L);
        assertNull(officialDocReceiveMapper.selectById(receive.getId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testDeleteOfficialDocReceive_activeOrApproved(int status) {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setStatus(status);
        officialDocReceiveMapper.insert(receive);

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.deleteOfficialDocReceive(receive.getId(), 1L),
                OFFICIAL_DOC_STATUS_INVALID);
        assertNotNull(officialDocReceiveMapper.selectById(receive.getId()));
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend() {
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class, o -> o.setId(100L)
                .setMainDeptIds(Arrays.asList(10L, 10L, 11L)).setCopyDeptIds(Arrays.asList(10L, 12L, 12L))
                .setSecrecyLevel(0).setUrgencyLevel(0).setFileUrls(Collections.emptyList()));
        // mock 方法
        when(noRedisDAO.generate("SW")).thenReturn("SW1", "SW2", "SW3", "SW4");

        // 调用
        officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        // 断言
        List<OaOfficialDocReceiveDO> receives = officialDocReceiveMapper.selectListBySendId(100L);
        assertEquals(4, receives.size());
        assertNotNull(CollUtil.findOne(receives, o -> o.getReceiveDeptId().equals(10L) && o.getReceiveType() == 0));
        assertNotNull(CollUtil.findOne(receives, o -> o.getReceiveDeptId().equals(10L) && o.getReceiveType() == 1));
        assertNotNull(CollUtil.findOne(receives, o -> o.getReceiveDeptId().equals(11L) && o.getReceiveType() == 0));
        assertNotNull(CollUtil.findOne(receives, o -> o.getReceiveDeptId().equals(12L) && o.getReceiveType() == 1));
        assertTrue(receives.stream().allMatch(o -> o.getHandleStatus() == 0 && o.getStatus() == -1));
        assertTrue(receives.stream().allMatch(o -> o.getHandlerUserId() == null));
        assertTrue(receives.stream().allMatch(o -> "".equals(o.getCreator())));
        verify(noRedisDAO, times(4)).generate("SW");
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend_repeatedCall() {
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class, o -> o.setId(100L)
                .setMainDeptIds(Collections.singletonList(10L)).setCopyDeptIds(Collections.singletonList(10L))
                .setSecrecyLevel(0).setUrgencyLevel(0).setFileUrls(Collections.emptyList()));
        // mock 方法
        when(noRedisDAO.generate("SW")).thenReturn("SW1", "SW2");

        // 调用
        officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        // 断言
        List<OaOfficialDocReceiveDO> receives = officialDocReceiveMapper.selectListBySendId(100L);
        assertEquals(2, receives.size());
        assertNotNull(CollUtil.findOne(receives, o -> o.getReceiveType() == 0));
        assertNotNull(CollUtil.findOne(receives, o -> o.getReceiveType() == 1));
        verify(noRedisDAO, times(2)).generate("SW");
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend_existingMain() {
        // mock 数据
        OaOfficialDocReceiveDO mainReceive = randomOfficialDocReceiveDO().setSendId(100L).setReceiveType(0);
        officialDocReceiveMapper.insert(mainReceive);
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class, o -> o.setId(100L)
                .setMainDeptIds(Collections.singletonList(10L)).setCopyDeptIds(Collections.singletonList(10L))
                .setSecrecyLevel(0).setUrgencyLevel(0).setFileUrls(Collections.emptyList()));
        // mock 方法
        when(noRedisDAO.generate("SW")).thenReturn("SW-COPY");

        // 调用
        officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        // 断言
        List<OaOfficialDocReceiveDO> receives = officialDocReceiveMapper.selectListBySendId(100L);
        assertEquals(2, receives.size());
        assertEquals(mainReceive.getId(), CollUtil.findOne(receives, o -> o.getReceiveType() == 0).getId());
        assertEquals(1, officialDocReceiveMapper.selectByNo("SW-COPY").getReceiveType());
        verify(noRedisDAO).generate("SW");
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend_existingCopy() {
        // mock 数据
        OaOfficialDocReceiveDO copyReceive = randomOfficialDocReceiveDO().setSendId(100L).setReceiveType(1);
        officialDocReceiveMapper.insert(copyReceive);
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class, o -> o.setId(100L)
                .setMainDeptIds(Collections.singletonList(10L)).setCopyDeptIds(Collections.singletonList(10L))
                .setSecrecyLevel(0).setUrgencyLevel(0).setFileUrls(Collections.emptyList()));
        // mock 方法
        when(noRedisDAO.generate("SW")).thenReturn("SW-MAIN");

        // 调用
        officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        // 断言
        List<OaOfficialDocReceiveDO> receives = officialDocReceiveMapper.selectListBySendId(100L);
        assertEquals(2, receives.size());
        assertEquals(copyReceive.getId(), CollUtil.findOne(receives, o -> o.getReceiveType() == 1).getId());
        assertEquals(0, officialDocReceiveMapper.selectByNo("SW-MAIN").getReceiveType());
        verify(noRedisDAO).generate("SW");
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend_onlyMain() {
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class, o -> o.setId(100L)
                .setMainDeptIds(Collections.singletonList(10L)).setCopyDeptIds(null)
                .setSecrecyLevel(0).setUrgencyLevel(0).setFileUrls(Collections.emptyList()));
        // mock 方法
        when(noRedisDAO.generate("SW")).thenReturn("SW-MAIN");

        // 调用
        officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        // 断言
        List<OaOfficialDocReceiveDO> receives = officialDocReceiveMapper.selectListBySendId(100L);
        assertEquals(1, receives.size());
        assertEquals(0, CollUtil.getFirst(receives).getReceiveType());
        assertEquals(10L, CollUtil.getFirst(receives).getReceiveDeptId());
        verify(noRedisDAO).generate("SW");
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend_onlyCopy() {
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class, o -> o.setId(100L)
                .setMainDeptIds(Collections.emptyList()).setCopyDeptIds(Collections.singletonList(10L))
                .setSecrecyLevel(0).setUrgencyLevel(0).setFileUrls(Collections.emptyList()));
        // mock 方法
        when(noRedisDAO.generate("SW")).thenReturn("SW-COPY");

        // 调用
        officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        // 断言
        List<OaOfficialDocReceiveDO> receives = officialDocReceiveMapper.selectListBySendId(100L);
        assertEquals(1, receives.size());
        assertEquals(1, CollUtil.getFirst(receives).getReceiveType());
        assertEquals(10L, CollUtil.getFirst(receives).getReceiveDeptId());
        verify(noRedisDAO).generate("SW");
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend_emptyDeptIds() {
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class,
                o -> o.setId(100L).setMainDeptIds(null).setCopyDeptIds(Collections.emptyList()));

        // 调用
        officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        // 断言
        assertTrue(CollUtil.isEmpty(officialDocReceiveMapper.selectListBySendId(100L)));
        verifyNoInteractions(noRedisDAO);
    }

    @Test
    public void testCreateOfficialDocReceive() {
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(randomOfficialDocReceiveDO(), OaOfficialDocReceiveSaveReqVO.class)
                .setId(999L).setReceiveType(1).setHandlerUserId(null);
        // mock 方法
        when(noRedisDAO.generate("SW")).thenReturn("SW-MANUAL-1");

        // 调用
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(1L), new MockHttpServletRequest());
        Long id;
        try {
            id = officialDocReceiveService.createOfficialDocReceive(reqVO);
        } finally {
            SecurityContextHolder.clearContext();
        }
        // 断言
        OaOfficialDocReceiveDO receive = officialDocReceiveMapper.selectById(id);
        assertNotEquals(999L, id);
        assertNull(receive.getSendId());
        assertNull(receive.getHandlerUserId());
        assertNull(receive.getProcessInstanceId());
        assertEquals("1", receive.getCreator());
        assertEquals("SW-MANUAL-1", receive.getNo());
        assertEquals(reqVO.getTitle(), receive.getTitle());
        assertEquals(reqVO.getReceiveTime(), receive.getReceiveTime());
        assertEquals(0, receive.getReceiveType());
        assertEquals(-1, receive.getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateOfficialDocReceive_manual() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setSendId(null).setHandlerUserId(null);
        officialDocReceiveMapper.insert(receive);
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(receive, OaOfficialDocReceiveSaveReqVO.class)
                .setTitle("手工收文修改").setDocumentNo("来文〔2026〕1号").setHandlerUserId(2L);

        // 调用
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(reqVO.getTitle(), result.getTitle());
        assertEquals(reqVO.getDocumentNo(), result.getDocumentNo());
        assertEquals(2L, result.getHandlerUserId());
        assertNull(result.getSendId());
        assertServiceException(() -> officialDocReceiveService.updateOfficialDocReceive(reqVO, 2L), OFFICIAL_DOC_ACCESS_DENIED);
    }

    @Test
    public void testSubmitOfficialDocReceive_manual() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setSendId(null).setHandlerUserId(null);
        officialDocReceiveMapper.insert(receive);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenReturn("manual-process");

        // 调用
        String processInstanceId = officialDocReceiveService.submitOfficialDocReceive(receive.getId(), 1L);
        // 断言
        assertEquals("manual-process", processInstanceId);
        assertEquals(1, officialDocReceiveMapper.selectById(receive.getId()).getStatus());
    }

    @Test
    public void testDeleteOfficialDocReceive_manual() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setSendId(null).setHandlerUserId(null);
        officialDocReceiveMapper.insert(receive);

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.deleteOfficialDocReceive(receive.getId(), 2L),
                OFFICIAL_DOC_ACCESS_DENIED);
        officialDocReceiveService.deleteOfficialDocReceive(receive.getId(), 1L);
        assertNull(officialDocReceiveMapper.selectById(receive.getId()));
    }

    @Test
    public void testClaimOfficialDocReceive() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setHandleStatus(0).setHandlerUserId(null)
                .setReceiveTime(null);
        receive.setCreator("");
        officialDocReceiveMapper.insert(receive);
        // mock 方法
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setDeptId(10L));

        // 调用
        officialDocReceiveService.claimOfficialDocReceive(receive.getId(), 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertNull(result.getHandlerUserId());
        assertEquals("1", result.getCreator());
        assertEquals(1, result.getHandleStatus());
        assertNull(result.getReceiveTime());
        assertServiceException(() -> officialDocReceiveService.claimOfficialDocReceive(receive.getId(), 1L),
                OFFICIAL_DOC_STATUS_INVALID);
    }

    @Test
    public void testClaimOfficialDocReceive_preserveBusinessFields() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setHandleStatus(0).setHandlerUserId(2L)
                .setReceiveTime(LocalDateTime.of(2026, 9, 1, 10, 0));
        receive.setCreator("");
        officialDocReceiveMapper.insert(receive);
        // mock 方法
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setDeptId(10L));

        // 调用
        officialDocReceiveService.claimOfficialDocReceive(receive.getId(), 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals("1", result.getCreator());
        assertEquals(1, result.getHandleStatus());
        assertEquals(receive.getHandlerUserId(), result.getHandlerUserId());
        assertEquals(receive.getReceiveTime(), result.getReceiveTime());
    }

    @Test
    public void testClaimOfficialDocReceive_otherDept() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setHandleStatus(0).setHandlerUserId(null);
        receive.setCreator("");
        officialDocReceiveMapper.insert(receive);
        // mock 方法
        when(adminUserApi.getUser(2L)).thenReturn(new AdminUserRespDTO().setDeptId(20L));

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.claimOfficialDocReceive(receive.getId(), 2L),
                OFFICIAL_DOC_ACCESS_DENIED);
        assertNull(officialDocReceiveMapper.selectById(receive.getId()).getHandlerUserId());
    }

    @Test
    public void testUpdateOfficialDocReceive_formalFileUrl() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setSendId(null);
        officialDocReceiveMapper.insert(receive);
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(receive, OaOfficialDocReceiveSaveReqVO.class);
        reqVO.setFormalFileUrl("https://example.com/document.pdf")
                .setFileUrls(Collections.singletonList("https://example.com/attachment.pdf"));

        // 调用
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
        // 断言
        assertEquals(reqVO.getFormalFileUrl(), officialDocReceiveMapper.selectById(receive.getId()).getFormalFileUrl());
        assertEquals(reqVO.getFileUrls(), officialDocReceiveMapper.selectById(receive.getId()).getFileUrls());
    }

    @Test
    public void testUpdateOfficialDocReceive_generatedFilesReadOnly() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setSendId(10L)
                .setFileUrls(Collections.singletonList("https://example.com/original.txt"))
                .setFormalFileUrl("https://example.com/original.pdf");
        officialDocReceiveMapper.insert(receive);
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(receive, OaOfficialDocReceiveSaveReqVO.class)
                .setTitle("编辑自动收文").setFormalFileUrl("https://example.com/replacement.pdf")
                .setFileUrls(Collections.singletonList("https://example.com/replacement.txt"));

        // 调用
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(reqVO.getTitle(), result.getTitle());
        assertEquals(receive.getFileUrls(), result.getFileUrls());
        assertEquals(receive.getFormalFileUrl(), result.getFormalFileUrl());

        // 调用，并断言清空请求也不能删除来源文件
        reqVO.setFileUrls(Collections.emptyList()).setFormalFileUrl("");
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
        result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(receive.getFileUrls(), result.getFileUrls());
        assertEquals(receive.getFormalFileUrl(), result.getFormalFileUrl());
    }

    @Test
    public void testUpdateOfficialDocReceive_generatedWithoutFiles() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setSendId(10L).setFileUrls(null).setFormalFileUrl(null);
        officialDocReceiveMapper.insert(receive);
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(receive, OaOfficialDocReceiveSaveReqVO.class)
                .setFileUrls(Collections.singletonList("https://example.com/attachment.pdf"))
                .setFormalFileUrl("https://example.com/document.pdf");

        // 调用
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertNull(result.getFileUrls());
        assertNull(result.getFormalFileUrl());
    }

    @Test
    public void testUpdateOfficialDocReceive_ignoreNullFields() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setFormalFileUrl("https://example.com/document.pdf");
        officialDocReceiveMapper.insert(receive);
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(receive, OaOfficialDocReceiveSaveReqVO.class);
        reqVO.setFormalFileUrl(null).setInstruction(null).setResult(null).setDeadlineTime(null)
                .setSummary(null).setRemark(null).setFileUrls(null);

        // 调用
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(receive.getFormalFileUrl(), result.getFormalFileUrl());
        assertEquals(receive.getInstruction(), result.getInstruction());
        assertEquals(receive.getResult(), result.getResult());
        assertEquals(receive.getDeadlineTime(), result.getDeadlineTime());
        assertEquals(receive.getSummary(), result.getSummary());
        assertEquals(receive.getRemark(), result.getRemark());
        assertEquals(receive.getFileUrls(), result.getFileUrls());
    }

    @Test
    public void testUpdateOfficialDocReceive_notCreator() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO();
        officialDocReceiveMapper.insert(receive);

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.updateOfficialDocReceive(toBean(receive, OaOfficialDocReceiveSaveReqVO.class), 2L),
                OFFICIAL_DOC_ACCESS_DENIED);
    }

    @Test
    public void testSubmitOfficialDocReceive() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO();
        officialDocReceiveMapper.insert(receive);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenReturn("receive-process");

        // 调用
        officialDocReceiveService.submitOfficialDocReceive(receive.getId(), 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(1, result.getStatus());
        assertEquals(2, result.getHandleStatus());
        // 重复提交抛出业务异常，不创建新的流程
        assertServiceException(() -> officialDocReceiveService.submitOfficialDocReceive(receive.getId(), 1L),
                OFFICIAL_DOC_STATUS_INVALID);
        verify(processInstanceApi, times(1)).createProcessInstance(eq(1L), any());
    }

    @Test
    public void testSubmitOfficialDocReceive_immediateApprove() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO();
        officialDocReceiveMapper.insert(receive);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenAnswer(invocation -> {
            officialDocReceiveService.updateOfficialDocReceiveStatus(receive.getId(), 2);
            return "auto-process";
        });

        // 调用
        officialDocReceiveService.submitOfficialDocReceive(receive.getId(), 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(2, result.getStatus());
        assertEquals(3, result.getHandleStatus());
        assertEquals("auto-process", result.getProcessInstanceId());
    }

    @Test
    public void testSubmitOfficialDocReceive_processFailureRollback() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO();
        officialDocReceiveMapper.insert(receive);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenThrow(new IllegalStateException("未配置流程"));

        // 调用，并断言
        assertThrows(IllegalStateException.class, () -> officialDocReceiveService.submitOfficialDocReceive(receive.getId(), 1L));
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(-1, result.getStatus());
        assertEquals(1, result.getHandleStatus());
        assertNull(result.getProcessInstanceId());
    }

    @Test
    public void testUpdateOfficialDocReceiveStatus_duplicate() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setStatus(1).setHandleStatus(2)
                .setProcessInstanceId("receive-process");
        officialDocReceiveMapper.insert(receive);

        // 调用
        officialDocReceiveService.updateOfficialDocReceiveStatus(receive.getId(), 2);
        officialDocReceiveService.updateOfficialDocReceiveStatus(receive.getId(), 2);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(2, result.getStatus());
        assertEquals(3, result.getHandleStatus());
    }

    @Test
    public void testGetOfficialDocReceive() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO();
        officialDocReceiveMapper.insert(receive);

        // 调用
        OaOfficialDocReceiveDO result = officialDocReceiveService.getOfficialDocReceive(receive.getId());
        // 断言
        assertEquals(receive.getId(), result.getId());
        assertEquals(receive.getTitle(), result.getTitle());
        verifyNoInteractions(adminUserApi, processInstanceApi);
    }

    @Test
    public void testGetOfficialDocReceive_notExists() {

        // 调用，并断言
        assertNull(officialDocReceiveService.getOfficialDocReceive(-1L));
    }

    @Test
    public void testUpdateOfficialDocReceive_notExists() {
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(randomOfficialDocReceiveDO(), OaOfficialDocReceiveSaveReqVO.class)
                .setId(-1L);

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L), OFFICIAL_DOC_NOT_EXISTS);
    }

    @Test
    public void testUpdateOfficialDocReceiveStatus_reject() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setStatus(1).setProcessInstanceId("receive-process");
        officialDocReceiveMapper.insert(receive);

        // 调用
        officialDocReceiveService.updateOfficialDocReceiveStatus(receive.getId(), 3);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(3, result.getStatus());
        assertEquals(1, result.getHandleStatus());
        assertEquals("receive-process", result.getProcessInstanceId());
    }

    @Test
    public void testUpdateOfficialDocReceiveStatus_notExists() {

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.updateOfficialDocReceiveStatus(-1L, 2), OFFICIAL_DOC_NOT_EXISTS);
    }

    @Test
    public void testGetOfficialDocReceivePage_noDept() {
        // mock 方法
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO());

        // 调用
        PageResult<OaOfficialDocReceiveDO> result = officialDocReceiveService.getOfficialDocReceivePage(new OaOfficialDocReceivePageReqVO(), 1L);
        // 断言
        assertEquals(0L, result.getTotal());
    }

    @Test
    public void testGetOfficialDocReceivePage_deptScope() {
        // mock 数据
        officialDocReceiveMapper.insert(randomOfficialDocReceiveDO().setTitle("本部门"));
        officialDocReceiveMapper.insert(randomOfficialDocReceiveDO().setReceiveDeptId(20L));
        // mock 方法
        when(adminUserApi.getUser(1L)).thenReturn(new AdminUserRespDTO().setDeptId(10L));

        // 调用
        PageResult<OaOfficialDocReceiveDO> result = officialDocReceiveService.getOfficialDocReceivePage(new OaOfficialDocReceivePageReqVO(), 1L);
        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals("本部门", CollUtil.getFirst(result.getList()).getTitle());
    }


    @Test
    public void testSubmitOfficialDocReceive_copyOnly() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setReceiveType(1);
        officialDocReceiveMapper.insert(receive);

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.submitOfficialDocReceive(receive.getId(), 1L),
                OFFICIAL_DOC_STATUS_INVALID);
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testUpdateOfficialDocReceive_generatedFields() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO();
        officialDocReceiveMapper.insert(receive);
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(receive, OaOfficialDocReceiveSaveReqVO.class)
                .setTitle("修改后的收文标题").setDocumentNo("收文〔2026〕2号")
                .setSecrecyLevel(1).setUrgencyLevel(2).setReceiveDeptId(20L).setHandlerUserId(2L);

        // 调用
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
        // 断言
        OaOfficialDocReceiveDO result = officialDocReceiveMapper.selectById(receive.getId());
        assertEquals(reqVO.getTitle(), result.getTitle());
        assertEquals(reqVO.getDocumentNo(), result.getDocumentNo());
        assertEquals(reqVO.getSecrecyLevel(), result.getSecrecyLevel());
        assertEquals(reqVO.getUrgencyLevel(), result.getUrgencyLevel());
        assertEquals(20L, result.getReceiveDeptId());
        assertEquals(2L, result.getHandlerUserId());
        assertEquals("1", result.getCreator());
        assertEquals(receive.getSendId(), result.getSendId());
        assertEquals(receive.getStatus(), result.getStatus());
        assertServiceException(() -> officialDocReceiveService.updateOfficialDocReceive(reqVO, 2L), OFFICIAL_DOC_ACCESS_DENIED);
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
    }

    @Test
    public void testUpdateOfficialDocReceive_preserveSource() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setSendId(10L);
        officialDocReceiveMapper.insert(receive);
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(receive, OaOfficialDocReceiveSaveReqVO.class).setTitle("编辑自动收文");

        // 调用
        officialDocReceiveService.updateOfficialDocReceive(reqVO, 1L);
        // 断言
        assertEquals(reqVO.getTitle(), officialDocReceiveMapper.selectById(receive.getId()).getTitle());
        assertEquals(10L, officialDocReceiveMapper.selectById(receive.getId()).getSendId());
    }

    @Test
    public void testSubmitOfficialDocReceive_afterHandlerChanged() {
        // mock 数据
        OaOfficialDocReceiveDO receive = randomOfficialDocReceiveDO().setHandlerUserId(2L);
        officialDocReceiveMapper.insert(receive);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenReturn("creator-process");

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.submitOfficialDocReceive(receive.getId(), 2L),
                OFFICIAL_DOC_ACCESS_DENIED);
        assertEquals("creator-process", officialDocReceiveService.submitOfficialDocReceive(receive.getId(), 1L));
    }

    @Test
    public void testCreateOfficialDocReceive_noDuplicate() {
        // mock 数据
        officialDocReceiveMapper.insert(randomOfficialDocReceiveDO().setNo("SW-DUPLICATE"));
        // 准备参数
        OaOfficialDocReceiveSaveReqVO reqVO = toBean(randomOfficialDocReceiveDO(), OaOfficialDocReceiveSaveReqVO.class);
        // mock 方法
        when(noRedisDAO.generate(OaNoRedisDAO.OFFICIAL_DOC_RECEIVE_NO_PREFIX)).thenReturn("SW-DUPLICATE");

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.createOfficialDocReceive(reqVO),
                OFFICIAL_DOC_RECEIVE_NO_DUPLICATE);
        assertEquals(1L, officialDocReceiveMapper.selectCount());
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend_noDuplicate() {
        // mock 数据
        officialDocReceiveMapper.insert(randomOfficialDocReceiveDO().setNo("SW-DUPLICATE").setSendId(99L));
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class,
                o -> o.setId(100L).setMainDeptIds(Arrays.asList(10L)).setCopyDeptIds(Collections.emptyList()));
        // mock 方法
        when(noRedisDAO.generate(OaNoRedisDAO.OFFICIAL_DOC_RECEIVE_NO_PREFIX)).thenReturn("SW-DUPLICATE");

        // 调用，并断言异常
        assertServiceException(() -> officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send),
                OFFICIAL_DOC_RECEIVE_NO_DUPLICATE);
        assertTrue(CollUtil.isEmpty(officialDocReceiveMapper.selectListBySendId(100L)));
    }

    @Test
    public void testCreateOfficialDocReceiveListByOfficialDocSend_notClaimedByOperator() {
        // 准备参数
        OaOfficialDocSendDO send = randomPojo(OaOfficialDocSendDO.class,
                o -> o.setId(100L).setMainDeptIds(Arrays.asList(10L)).setCopyDeptIds(Collections.emptyList())
                        .setSecrecyLevel(0).setUrgencyLevel(0).setFileUrls(Collections.emptyList()));
        // mock 方法
        when(noRedisDAO.generate(OaNoRedisDAO.OFFICIAL_DOC_RECEIVE_NO_PREFIX)).thenReturn("SW-UNCLAIMED");
        SecurityFrameworkUtils.setLoginUser(new LoginUser().setId(1L), new MockHttpServletRequest());

        // 调用
        try {
            officialDocReceiveService.createOfficialDocReceiveListByOfficialDocSend(send);
        } finally {
            SecurityContextHolder.clearContext();
        }
        // 断言
        assertEquals("", officialDocReceiveMapper.selectByNo("SW-UNCLAIMED").getCreator());
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaOfficialDocReceiveDO randomOfficialDocReceiveDO() {
        return randomPojo(OaOfficialDocReceiveDO.class, o -> o.setId(null)
                .setReceiveDeptId(10L).setHandlerUserId(1L).setReceiveType(0).setHandleStatus(1)
                .setSecrecyLevel(0).setUrgencyLevel(0).setStatus(-1).setProcessInstanceId(null)
                .setFileUrls(Collections.emptyList()).setCreator("1"));
    }

}
