package cn.iocoder.yudao.module.im.service.sensitiveword;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.dal.mysql.sensitiveword.ImSensitiveWordMapper;
import com.google.common.cache.LoadingCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.MESSAGE_SENSITIVE_WORD_BLOCKED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.SENSITIVE_WORD_DUPLICATED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.SENSITIVE_WORD_NOT_EXISTS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link ImSensitiveWordServiceImpl} 的单元测试
 *
 * @author 芋道源码
 */
public class ImSensitiveWordServiceImplTest extends BaseMockitoUnitTest {

    private static final Long TENANT_ID = 1L;

    @InjectMocks
    private ImSensitiveWordServiceImpl sensitiveWordService;

    @Mock
    private ImSensitiveWordMapper imSensitiveWordMapper;

    @BeforeEach
    public void setUp() {
        // 设置租户上下文
        TenantContextHolder.setTenantId(TENANT_ID);
        // mock 启用的敏感词，部分用例不触发缓存加载
        lenient().when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of(
                        ImSensitiveWordDO.builder().id(1L).word("badword")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                        ImSensitiveWordDO.builder().id(2L).word("违禁词")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build()
                ));
    }

    @AfterEach
    public void tearDown() {
        // 清理租户上下文
        TenantContextHolder.clear();
    }

    @Test
    public void testValidateText_null() {
        // 调用，并断言
        assertDoesNotThrow(() -> sensitiveWordService.validateText(null));
    }

    @Test
    public void testValidateText_empty() {
        // 调用，并断言
        assertDoesNotThrow(() -> sensitiveWordService.validateText(""));
    }

    @Test
    public void testValidateText_clean() {
        // 调用，并断言
        assertDoesNotThrow(() -> sensitiveWordService.validateText("hello world"));
    }

    @Test
    public void testValidateText_tenantDisabled() {
        // 准备上下文
        ReflectionTestUtils.setField(sensitiveWordService, "tenantEnable", false);
        TenantContextHolder.clear();

        // 调用，并断言
        assertDoesNotThrow(() -> sensitiveWordService.validateText("hello world"));
        assertServiceException(() -> sensitiveWordService.validateText("this contains badword here"), MESSAGE_SENSITIVE_WORD_BLOCKED);
        assertNull(TenantContextHolder.getTenantId());
    }

    @Test
    public void testValidateText_tenantEnabledWithoutTenantId() {
        // 准备上下文
        TenantContextHolder.clear();

        // 调用，并断言异常
        assertThrows(NullPointerException.class, () -> sensitiveWordService.validateText("hello world"));
        // 断言
        verifyNoInteractions(imSensitiveWordMapper);
    }

    @Test
    public void testValidateText_tenantIsolation() {
        // mock 不同租户的词库
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenAnswer(invocation -> List.of(ImSensitiveWordDO.builder()
                        .word(TenantContextHolder.getRequiredTenantId().equals(TENANT_ID) ? "firstbad" : "secondbad")
                        .build()));

        // 调用，并断言当前租户的词库
        assertServiceException(() -> sensitiveWordService.validateText("firstbad"), MESSAGE_SENSITIVE_WORD_BLOCKED);
        assertDoesNotThrow(() -> sensitiveWordService.validateText("secondbad"));
        // 调用，并断言切换租户后的词库
        TenantContextHolder.setTenantId(2L);
        assertDoesNotThrow(() -> sensitiveWordService.validateText("firstbad"));
        assertServiceException(() -> sensitiveWordService.validateText("secondbad"), MESSAGE_SENSITIVE_WORD_BLOCKED);
        // 调用，并断言切回租户后复用缓存
        TenantContextHolder.setTenantId(TENANT_ID);
        assertServiceException(() -> sensitiveWordService.validateText("firstbad"), MESSAGE_SENSITIVE_WORD_BLOCKED);
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testDeleteSensitiveWordList_tenantDisabled() {
        // 准备上下文
        ReflectionTestUtils.setField(sensitiveWordService, "tenantEnable", false);
        TenantContextHolder.clear();
        // 准备缓存
        sensitiveWordService.validateText("hello world");

        // 调用，并断言携带租户编号时仍复用缓存
        TenantContextHolder.setTenantId(2L);
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        // mock 删除后的词库
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus())).thenReturn(List.of());

        // 调用
        sensitiveWordService.deleteSensitiveWordList(ListUtil.of(1L, 2L));

        // 断言缓存已刷新，上下文不变
        assertDoesNotThrow(() -> sensitiveWordService.validateText("badword"));
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
        assertEquals(2L, TenantContextHolder.getTenantId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateText_tenantDisabledRefresh() {
        // 准备上下文
        ReflectionTestUtils.setField(sensitiveWordService, "tenantEnable", false);
        TenantContextHolder.clear();
        // mock 初始更新时间
        when(imSensitiveWordMapper.selectMaxUpdateTime(0L)).thenReturn(LocalDateTime.of(2026, 9, 1, 0, 0));
        // 准备缓存
        sensitiveWordService.validateText("hello world");
        // mock 词库更新，并断言刷新线程无租户编号
        when(imSensitiveWordMapper.selectMaxUpdateTime(0L)).thenAnswer(invocation -> {
            assertNull(TenantContextHolder.getTenantId());
            return LocalDateTime.of(2026, 9, 2, 0, 0);
        });
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(List.of(ImSensitiveWordDO.builder().word("newbad").build()));

        // 调用
        LoadingCache<Long, ?> cache = (LoadingCache<Long, ?>) ReflectionTestUtils.getField(
                sensitiveWordService, "sensitiveWordBsCaches");
        cache.refresh(0L);

        // 断言缓存异步刷新
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertServiceException(() -> sensitiveWordService.validateText("newbad"), MESSAGE_SENSITIVE_WORD_BLOCKED));
        assertDoesNotThrow(() -> sensitiveWordService.validateText("badword"));
    }

    @Test
    public void testValidateText_hitEnglish() {
        // 调用，并断言异常
        assertServiceException(() -> sensitiveWordService.validateText("this contains badword here"), MESSAGE_SENSITIVE_WORD_BLOCKED);
    }

    @Test
    public void testValidateText_hitChinese() {
        // 调用，并断言异常
        assertServiceException(() -> sensitiveWordService.validateText("这条消息里有违禁词哦"), MESSAGE_SENSITIVE_WORD_BLOCKED);
    }

    @Test
    public void testValidateText_lazyLoadsCacheOnFirstCall() {
        // 调用
        sensitiveWordService.validateText("hello world");

        // 断言
        verify(imSensitiveWordMapper, times(1)).selectMaxUpdateTime(TENANT_ID);
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testValidateText_reusesCachedBsAcrossCalls() {
        // 调用
        sensitiveWordService.validateText("hello world");
        sensitiveWordService.validateText("another text");

        // 断言复用缓存
        verify(imSensitiveWordMapper, times(1)).selectMaxUpdateTime(TENANT_ID);
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testCreateSensitiveWord_invalidatesCacheAndReloadsOnNextValidate() {
        // 准备缓存
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // mock 新增后的词库
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of(
                        ImSensitiveWordDO.builder().id(1L).word("badword")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                        ImSensitiveWordDO.builder().id(2L).word("违禁词")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                        ImSensitiveWordDO.builder().id(3L).word("newbad")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build()
                ));
        when(imSensitiveWordMapper.selectByWord("newbad")).thenReturn(null);

        // 准备参数
        ImSensitiveWordSaveReqVO reqVO = new ImSensitiveWordSaveReqVO();
        reqVO.setWord("newbad");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        sensitiveWordService.createSensitiveWord(reqVO);

        // 断言缓存已刷新
        assertServiceException(() -> sensitiveWordService.validateText("contains newbad here"), MESSAGE_SENSITIVE_WORD_BLOCKED);
        // 断言原有敏感词仍然生效
        assertServiceException(() -> sensitiveWordService.validateText("contains badword here"), MESSAGE_SENSITIVE_WORD_BLOCKED);

        // 断言词库重新加载
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testCreateSensitiveWord_duplicateWord_throws() {
        // mock 同名敏感词
        when(imSensitiveWordMapper.selectByWord("dup")).thenReturn(
                ImSensitiveWordDO.builder().id(99L).word("dup")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());

        // 准备参数
        ImSensitiveWordSaveReqVO reqVO = new ImSensitiveWordSaveReqVO();
        reqVO.setWord("dup");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用，并断言异常
        assertServiceException(() -> sensitiveWordService.createSensitiveWord(reqVO), SENSITIVE_WORD_DUPLICATED, reqVO.getWord());

        // 断言
        verify(imSensitiveWordMapper, never()).insert(any(ImSensitiveWordDO.class));
    }

    @Test
    public void testUpdateSensitiveWord_invalidatesCache() {
        // 准备缓存
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // mock 原敏感词
        when(imSensitiveWordMapper.selectById(1L)).thenReturn(
                ImSensitiveWordDO.builder().id(1L).word("badword")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
        when(imSensitiveWordMapper.selectByWord("updatedbad")).thenReturn(null);
        // mock 修改后的词库
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of(
                        ImSensitiveWordDO.builder().id(1L).word("updatedbad")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build()
                ));

        // 准备参数
        ImSensitiveWordSaveReqVO reqVO = new ImSensitiveWordSaveReqVO();
        reqVO.setId(1L);
        reqVO.setWord("updatedbad");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        sensitiveWordService.updateSensitiveWord(reqVO);

        // 断言缓存已刷新
        assertServiceException(() -> sensitiveWordService.validateText("contains updatedbad here"), MESSAGE_SENSITIVE_WORD_BLOCKED);

        // 断言词库重新加载
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testDeleteSensitiveWord_invalidatesCache() {
        // 准备缓存
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // mock 原敏感词
        when(imSensitiveWordMapper.selectById(1L)).thenReturn(
                ImSensitiveWordDO.builder().id(1L).word("badword")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
        // mock 删除后的词库
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of(
                        ImSensitiveWordDO.builder().id(2L).word("违禁词")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build()
                ));

        // 调用
        sensitiveWordService.deleteSensitiveWord(1L);

        // 断言缓存已刷新
        assertDoesNotThrow(() -> sensitiveWordService.validateText("contains badword here"));
        // 断言未删除的敏感词仍然生效
        assertServiceException(() -> sensitiveWordService.validateText("这条消息里有违禁词哦"), MESSAGE_SENSITIVE_WORD_BLOCKED);

        // 断言词库重新加载
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testDeleteSensitiveWord_notExists_throws() {
        // mock 敏感词不存在
        when(imSensitiveWordMapper.selectById(999L)).thenReturn(null);

        // 调用，并断言异常
        assertServiceException(() -> sensitiveWordService.deleteSensitiveWord(999L), SENSITIVE_WORD_NOT_EXISTS);

        // 断言
        verify(imSensitiveWordMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteSensitiveWordList_invalidatesCache() {
        // 准备缓存
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // mock 删除后的词库
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of());

        // 调用
        sensitiveWordService.deleteSensitiveWordList(ListUtil.of(1L, 2L));

        // 断言缓存已刷新
        assertDoesNotThrow(() -> sensitiveWordService.validateText("contains badword here"));
        assertDoesNotThrow(() -> sensitiveWordService.validateText("这条消息里有违禁词哦"));

        // 断言
        verify(imSensitiveWordMapper, times(1)).deleteByIds(ListUtil.of(1L, 2L));
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testDeleteSensitiveWordList_emptyIds_skip() {
        // 调用
        sensitiveWordService.deleteSensitiveWordList(ListUtil.of());

        // 断言
        verify(imSensitiveWordMapper, never()).deleteByIds(anyList());
    }

}
