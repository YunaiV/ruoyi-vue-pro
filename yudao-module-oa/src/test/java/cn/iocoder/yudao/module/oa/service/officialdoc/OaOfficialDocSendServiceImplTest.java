package cn.iocoder.yudao.module.oa.service.officialdoc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send.*;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.*;
import cn.iocoder.yudao.module.oa.dal.mysql.officialdoc.*;
import cn.iocoder.yudao.module.oa.dal.redis.no.OaNoRedisDAO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.object.BeanUtils.toBean;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaOfficialDocSendServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaOfficialDocSendServiceImpl.class)
public class OaOfficialDocSendServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaOfficialDocSendService officialDocSendService;

    @Resource
    private OaOfficialDocSendMapper officialDocSendMapper;

    @MockBean
    private BpmProcessInstanceApi processInstanceApi;
    @MockBean
    private OaNoRedisDAO noRedisDAO;
    @MockBean
    private DeptApi deptApi;
    @MockBean
    private OaOfficialDocTemplateService officialDocTemplateService;
    @MockBean
    private OaOfficialDocReceiveService officialDocReceiveService;


    @ParameterizedTest
    @ValueSource(ints = {-1, 3, 4})
    public void testDeleteOfficialDocSend_allowedStatuses(int status) {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setStatus(status);
        officialDocSendMapper.insert(send);

        // 调用，并断言：其他人不能删除
        assertServiceException(() -> officialDocSendService.deleteOfficialDocSend(send.getId(), 2L), OFFICIAL_DOC_ACCESS_DENIED);
        officialDocSendService.deleteOfficialDocSend(send.getId(), 1L);
        assertNull(officialDocSendMapper.selectById(send.getId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    public void testDeleteOfficialDocSend_activeOrApproved(int status) {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setStatus(status);
        officialDocSendMapper.insert(send);

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.deleteOfficialDocSend(send.getId(), 1L), OFFICIAL_DOC_STATUS_INVALID);
        assertNotNull(officialDocSendMapper.selectById(send.getId()));
    }

    @Test
    public void testCreateOfficialDocSend_noDuplicate() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setNo("FW20260916000001");
        officialDocSendMapper.insert(send);
        // 准备参数
        OaOfficialDocSendSaveReqVO reqVO = toBean(send, OaOfficialDocSendSaveReqVO.class).setId(null);
        // mock 方法
        when(noRedisDAO.generate(OaNoRedisDAO.OFFICIAL_DOC_SEND_NO_PREFIX)).thenReturn(send.getNo());

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.createOfficialDocSend(reqVO, 1L),
                OFFICIAL_DOC_SEND_NO_DUPLICATE);
        assertEquals(1L, officialDocSendMapper.selectCount());
    }

    @Test
    public void testGetOfficialDocSendMap() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO();
        officialDocSendMapper.insert(send);

        // 调用
        Map<Long, OaOfficialDocSendDO> result = officialDocSendService.getOfficialDocSendMap(Collections.singleton(send.getId()));
        // 断言
        assertEquals(1, result.size());
        assertEquals(send.getTitle(), result.get(send.getId()).getTitle());
    }

    @Test
    public void testGetOfficialDocSendMap_empty() {
        // mock 方法
        OaOfficialDocSendService service = mock(OaOfficialDocSendService.class, CALLS_REAL_METHODS);

        // 调用
        Map<Long, OaOfficialDocSendDO> result = service.getOfficialDocSendMap(Collections.emptyList());
        // 断言
        assertTrue(result.isEmpty());
        verify(service, never()).getOfficialDocSendList(any());
    }

    @Test
    public void testGetOfficialDocSendList() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO();
        officialDocSendMapper.insert(send);
        OaOfficialDocSendDO otherSend = randomOfficialDocSendDO();
        officialDocSendMapper.insert(otherSend);

        // 调用
        List<OaOfficialDocSendDO> result = officialDocSendService.getOfficialDocSendList(Collections.singleton(send.getId()));
        // 断言
        assertEquals(1, result.size());
        assertEquals(send.getId(), CollUtil.getFirst(result).getId());
        assertEquals(send.getTitle(), CollUtil.getFirst(result).getTitle());
    }

    @Test
    public void testGetOfficialDocSendList_empty() {
        // mock 数据
        officialDocSendMapper.insert(randomOfficialDocSendDO());

        // 调用
        List<OaOfficialDocSendDO> result = officialDocSendService.getOfficialDocSendList(Collections.emptyList());
        // 断言
        assertTrue(CollUtil.isEmpty(result));
    }

    @Test
    public void testCreateOfficialDocSend() {
        // 准备参数
        OaOfficialDocSendSaveReqVO reqVO = toBean(randomOfficialDocSendDO(), OaOfficialDocSendSaveReqVO.class).setId(null);
        // mock 方法
        when(noRedisDAO.generate("FW")).thenReturn("FW20260913000001");

        // 调用
        Long id = officialDocSendService.createOfficialDocSend(reqVO, 1L);
        // 断言
        OaOfficialDocSendDO send = officialDocSendMapper.selectById(id);
        assertEquals(-1, send.getStatus());
        assertEquals("FW20260913000001", send.getNo());
        assertEquals("测试〔2026〕1号", send.getDocumentNo());
        assertNull(send.getProcessInstanceId());
        assertEquals(Arrays.asList(10L, 11L), send.getMainDeptIds());
        verify(officialDocTemplateService).validateOfficialDocTemplate(reqVO.getTemplateId());
    }

    @Test
    public void testCreateOfficialDocSend_disabledTemplate() {
        // 准备参数
        OaOfficialDocSendSaveReqVO reqVO = toBean(randomOfficialDocSendDO(), OaOfficialDocSendSaveReqVO.class);
        // mock 方法
        when(officialDocTemplateService.validateOfficialDocTemplate(reqVO.getTemplateId()))
                .thenThrow(exception(OFFICIAL_DOC_TEMPLATE_DISABLED));

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.createOfficialDocSend(reqVO, 1L), OFFICIAL_DOC_TEMPLATE_DISABLED);
        verifyNoInteractions(noRedisDAO);
    }

    @Test
    public void testUpdateOfficialDocSend_notOwner() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO();
        officialDocSendMapper.insert(send);

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.updateOfficialDocSend(toBean(send, OaOfficialDocSendSaveReqVO.class), 2L),
                OFFICIAL_DOC_ACCESS_DENIED);
    }

    @Test
    public void testDeleteOfficialDocSend_submitted() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setStatus(1);
        officialDocSendMapper.insert(send);

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.deleteOfficialDocSend(send.getId(), 1L), OFFICIAL_DOC_STATUS_INVALID);
        assertNotNull(officialDocSendMapper.selectById(send.getId()));
    }

    @Test
    public void testSubmitOfficialDocSend() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO();
        officialDocSendMapper.insert(send);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenReturn("process-1");

        // 调用
        String processId = officialDocSendService.submitOfficialDocSend(send.getId(), 1L);
        // 断言
        assertEquals("process-1", processId);
        assertServiceException(() -> officialDocSendService.submitOfficialDocSend(send.getId(), 1L), OFFICIAL_DOC_STATUS_INVALID);
        assertEquals(1, officialDocSendMapper.selectById(send.getId()).getStatus());
        verify(processInstanceApi, times(1)).createProcessInstance(eq(1L), any());
    }

    @Test
    public void testSubmitOfficialDocSend_immediateApprove() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO();
        officialDocSendMapper.insert(send);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenAnswer(invocation -> {
            officialDocSendService.updateOfficialDocSendStatus(send.getId(), "auto-process", 2);
            return "auto-process";
        });

        // 调用
        officialDocSendService.submitOfficialDocSend(send.getId(), 1L);
        // 断言
        assertEquals(2, officialDocSendMapper.selectById(send.getId()).getStatus());
        assertEquals("auto-process", officialDocSendMapper.selectById(send.getId()).getProcessInstanceId());
        verify(officialDocReceiveService).createOfficialDocReceiveListByOfficialDocSend(any());
    }

    @Test
    public void testSubmitOfficialDocSend_processFailureRollback() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO();
        officialDocSendMapper.insert(send);
        // mock 方法
        when(processInstanceApi.createProcessInstance(eq(1L), any())).thenThrow(new IllegalStateException("未配置流程"));

        // 调用，并断言
        assertThrows(IllegalStateException.class, () -> officialDocSendService.submitOfficialDocSend(send.getId(), 1L));
        assertEquals(-1, officialDocSendMapper.selectById(send.getId()).getStatus());
    }

    @Test
    public void testUpdateOfficialDocSendStatus_duplicate() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setStatus(1).setProcessInstanceId("process-1");
        officialDocSendMapper.insert(send);

        // 调用
        officialDocSendService.updateOfficialDocSendStatus(send.getId(), "process-1", 2);
        officialDocSendService.updateOfficialDocSendStatus(send.getId(), "process-1", 2);
        officialDocSendService.updateOfficialDocSendStatus(send.getId(), "process-1", 3);
        // 断言
        assertEquals(2, officialDocSendMapper.selectById(send.getId()).getStatus());
        verify(officialDocReceiveService, times(1)).createOfficialDocReceiveListByOfficialDocSend(any());
    }

    @Test
    public void testUpdateOfficialDocSendStatus_wrongProcess() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setStatus(1).setProcessInstanceId("process-1");
        officialDocSendMapper.insert(send);

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.updateOfficialDocSendStatus(send.getId(), "process-other", 2),
                OFFICIAL_DOC_PROCESS_MISMATCH);
        verifyNoInteractions(officialDocReceiveService);
    }

    @Test
    public void testGetOfficialDocSend() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO();
        officialDocSendMapper.insert(send);

        // 调用
        OaOfficialDocSendDO result = officialDocSendService.getOfficialDocSend(send.getId());
        // 断言
        assertEquals(send.getId(), result.getId());
        assertEquals(send.getTitle(), result.getTitle());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testGetOfficialDocSend_notExists() {

        // 调用，并断言
        assertNull(officialDocSendService.getOfficialDocSend(999L));
    }

    @Test
    public void testGetOfficialDocSendPage() {
        // mock 数据
        officialDocSendMapper.insert(randomOfficialDocSendDO().setTitle("当前用户"));
        officialDocSendMapper.insert((OaOfficialDocSendDO) randomOfficialDocSendDO().setTitle("其他用户").setCreator("2"));

        // 调用
        PageResult<OaOfficialDocSendDO> result = officialDocSendService.getOfficialDocSendPage(new OaOfficialDocSendPageReqVO(), 1L);
        // 断言
        assertEquals(1L, result.getTotal());
        assertEquals("当前用户", CollUtil.getFirst(result.getList()).getTitle());
    }


    @Test
    public void testUpdateOfficialDocSend_ignoreNullFields() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO();
        officialDocSendMapper.insert(send);
        // 准备参数
        OaOfficialDocSendSaveReqVO reqVO = toBean(send, OaOfficialDocSendSaveReqVO.class)
                .setTitle("修改后的标题").setContent(null).setRemark(null).setCopyDeptIds(null);

        // 调用
        officialDocSendService.updateOfficialDocSend(reqVO, 1L);
        // 断言
        OaOfficialDocSendDO result = officialDocSendMapper.selectById(send.getId());
        assertEquals("修改后的标题", result.getTitle());
        assertEquals(send.getContent(), result.getContent());
        assertEquals(send.getRemark(), result.getRemark());
        assertEquals(send.getCopyDeptIds(), result.getCopyDeptIds());
        assertEquals(-1, result.getStatus());
        assertEquals(send.getCreator(), result.getCreator());
        assertEquals(send.getProcessInstanceId(), result.getProcessInstanceId());
    }

    @Test
    public void testCreateOfficialDocSend_documentNoDuplicate() {
        // mock 数据
        OaOfficialDocSendDO existing = randomOfficialDocSendDO().setDocumentNo("测试〔2026〕1号");
        officialDocSendMapper.insert(existing);
        // 准备参数：新增请求携带已有编号也不能排除已有发文
        OaOfficialDocSendSaveReqVO reqVO = toBean(existing, OaOfficialDocSendSaveReqVO.class);

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.createOfficialDocSend(reqVO, 1L),
                OFFICIAL_DOC_SEND_DOCUMENT_NO_DUPLICATE);
        assertEquals(1L, officialDocSendMapper.selectCount());
        verifyNoInteractions(noRedisDAO, processInstanceApi);
    }

    @Test
    public void testUpdateOfficialDocSend_documentNoDuplicate() {
        // mock 数据
        officialDocSendMapper.insert(randomOfficialDocSendDO().setDocumentNo("测试〔2026〕1号"));
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setSequence(2).setDocumentNo("测试〔2026〕2号");
        officialDocSendMapper.insert(send);
        // 准备参数
        OaOfficialDocSendSaveReqVO reqVO = toBean(send, OaOfficialDocSendSaveReqVO.class).setSequence(1);

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.updateOfficialDocSend(reqVO, 1L),
                OFFICIAL_DOC_SEND_DOCUMENT_NO_DUPLICATE);
        assertEquals("测试〔2026〕2号", officialDocSendMapper.selectById(send.getId()).getDocumentNo());
    }

    @Test
    public void testUpdateOfficialDocSend_keepDocumentNo() {
        // mock 数据
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setDocumentNo("测试〔2026〕1号");
        officialDocSendMapper.insert(send);
        // 准备参数
        OaOfficialDocSendSaveReqVO reqVO = toBean(send, OaOfficialDocSendSaveReqVO.class).setTitle("修改标题");

        // 调用
        officialDocSendService.updateOfficialDocSend(reqVO, 1L);
        // 断言
        assertEquals("修改标题", officialDocSendMapper.selectById(send.getId()).getTitle());
        assertEquals(send.getDocumentNo(), officialDocSendMapper.selectById(send.getId()).getDocumentNo());
    }

    @Test
    public void testSubmitOfficialDocSend_documentNoDuplicate() {
        // mock 数据：历史重复草稿不能继续提交
        officialDocSendMapper.insert(randomOfficialDocSendDO().setDocumentNo("测试〔2026〕1号"));
        OaOfficialDocSendDO send = randomOfficialDocSendDO().setDocumentNo("测试〔2026〕1号");
        officialDocSendMapper.insert(send);

        // 调用，并断言异常
        assertServiceException(() -> officialDocSendService.submitOfficialDocSend(send.getId(), 1L),
                OFFICIAL_DOC_SEND_DOCUMENT_NO_DUPLICATE);
        assertEquals(-1, officialDocSendMapper.selectById(send.getId()).getStatus());
        verifyNoInteractions(processInstanceApi);
    }

    @Test
    public void testCreateOfficialDocSend_withoutSequence() {
        // mock 数据
        officialDocSendMapper.insert(randomOfficialDocSendDO().setSequence(null).setDocumentNo("测试〔2026〕"));
        // 准备参数
        OaOfficialDocSendSaveReqVO reqVO = toBean(randomOfficialDocSendDO(), OaOfficialDocSendSaveReqVO.class).setSequence(null);
        // mock 方法
        when(noRedisDAO.generate("FW")).thenReturn("FW20260918000001");

        // 调用
        Long id = officialDocSendService.createOfficialDocSend(reqVO, 1L);
        // 断言
        assertEquals("测试〔2026〕", officialDocSendMapper.selectById(id).getDocumentNo());
        assertEquals(2L, officialDocSendMapper.selectCount());
    }

    @Test
    public void testCreateOfficialDocSend_deletedDocumentNo() {
        // mock 数据
        OaOfficialDocSendDO deleted = randomOfficialDocSendDO().setDocumentNo("测试〔2026〕1号");
        officialDocSendMapper.insert(deleted);
        officialDocSendMapper.deleteById(deleted.getId());
        // 准备参数
        OaOfficialDocSendSaveReqVO reqVO = toBean(randomOfficialDocSendDO(), OaOfficialDocSendSaveReqVO.class);
        // mock 方法
        when(noRedisDAO.generate("FW")).thenReturn("FW20260918000001");

        // 调用
        Long id = officialDocSendService.createOfficialDocSend(reqVO, 1L);
        // 断言
        assertEquals("测试〔2026〕1号", officialDocSendMapper.selectById(id).getDocumentNo());
        assertEquals(1L, officialDocSendMapper.selectCount());
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaOfficialDocSendDO randomOfficialDocSendDO() {
        return randomPojo(OaOfficialDocSendDO.class, o -> o.setId(null)
                .setNoPrefix("测试").setYear(2026).setSequence(1).setSecrecyLevel(0).setUrgencyLevel(0)
                .setDisclosureType(0).setStatus(-1).setProcessInstanceId(null).setSignerUserId(null)
                .setMainDeptIds(Arrays.asList(10L, 11L)).setCopyDeptIds(Collections.singletonList(12L))
                .setFileUrls(Collections.singletonList("https://example.com/attachment.pdf")).setCreator("1"));
    }

}
