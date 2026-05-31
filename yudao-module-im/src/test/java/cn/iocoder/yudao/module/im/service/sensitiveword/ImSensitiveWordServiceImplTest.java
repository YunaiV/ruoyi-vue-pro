package cn.iocoder.yudao.module.im.service.sensitiveword;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.dal.mysql.sensitiveword.ImSensitiveWordMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.MESSAGE_SENSITIVE_WORD_BLOCKED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.SENSITIVE_WORD_DUPLICATED;
import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.SENSITIVE_WORD_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * IM 敏感词 Service 单元测试
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
        // 设置租户上下文；validateText 与 loadFresh 都依赖 TenantContextHolder
        TenantContextHolder.setTenantId(TENANT_ID);
        // mock 启用敏感词列表；LoadingCache 在首次 validateText 时懒加载，无需主动 init
        // 用 lenient 避免 null / empty 等不触发 cache load 的用例报 UnnecessaryStubbing
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
        // 清理租户上下文，避免污染其它测试
        TenantContextHolder.clear();
    }

    @Test
    public void testValidateText_null() {
        // null 直接返回，不抛异常
        assertDoesNotThrow(() -> sensitiveWordService.validateText(null));
    }

    @Test
    public void testValidateText_empty() {
        assertDoesNotThrow(() -> sensitiveWordService.validateText(""));
    }

    @Test
    public void testValidateText_clean() {
        // 正常文本不应命中
        assertDoesNotThrow(() -> sensitiveWordService.validateText("hello world"));
    }

    @Test
    public void testValidateText_hitEnglish() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> sensitiveWordService.validateText("this contains badword here"));
        assertEquals(MESSAGE_SENSITIVE_WORD_BLOCKED.getCode(), exception.getCode());
    }

    @Test
    public void testValidateText_hitChinese() {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> sensitiveWordService.validateText("这条消息里有违禁词哦"));
        assertEquals(MESSAGE_SENSITIVE_WORD_BLOCKED.getCode(), exception.getCode());
    }

    @Test
    public void testValidateText_lazyLoadsCacheOnFirstCall() {
        // 调用：首次 validateText 应触发 cache load
        sensitiveWordService.validateText("hello world");

        // 断言：mapper 各调用 1 次（loadFresh 里先取 maxUpdateTime 再读词库）
        verify(imSensitiveWordMapper, times(1)).selectMaxUpdateTime(TENANT_ID);
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testValidateText_reusesCachedBsAcrossCalls() {
        // 调用：连续两次 validateText
        sensitiveWordService.validateText("hello world");
        sensitiveWordService.validateText("another text");

        // 断言：第二次复用 cache，mapper 仍只被调用 1 次
        verify(imSensitiveWordMapper, times(1)).selectMaxUpdateTime(TENANT_ID);
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testCreateSensitiveWord_invalidatesCacheAndReloadsOnNextValidate() {
        // 准备：首次 validateText 触发 cache load（旧词库）
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // 准备：mapper 返回新词库（额外多一个 newbad），并让 createSensitiveWord 走通
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

        // 调用：新增敏感词，触发 invalidate
        ImSensitiveWordSaveReqVO reqVO = new ImSensitiveWordSaveReqVO();
        reqVO.setWord("newbad");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        sensitiveWordService.createSensitiveWord(reqVO);

        // 调用：再次 validateText，应触发重新 load
        ServiceException exception = assertThrows(ServiceException.class,
                () -> sensitiveWordService.validateText("contains newbad here"));
        assertEquals(MESSAGE_SENSITIVE_WORD_BLOCKED.getCode(), exception.getCode());
        // 旧词依然命中
        assertThrows(ServiceException.class,
                () -> sensitiveWordService.validateText("contains badword here"));

        // 断言：selectListByStatus 共被调用 2 次（首次 load + invalidate 后 reload）
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testCreateSensitiveWord_duplicateWord_throws() {
        // 准备：mock 已存在同名敏感词
        when(imSensitiveWordMapper.selectByWord("dup")).thenReturn(
                ImSensitiveWordDO.builder().id(99L).word("dup")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());

        // 调用 + 断言：重复敏感词抛 SENSITIVE_WORD_DUPLICATED
        ImSensitiveWordSaveReqVO reqVO = new ImSensitiveWordSaveReqVO();
        reqVO.setWord("dup");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        ServiceException exception = assertThrows(ServiceException.class,
                () -> sensitiveWordService.createSensitiveWord(reqVO));
        assertEquals(SENSITIVE_WORD_DUPLICATED.getCode(), exception.getCode());

        // 断言：未走到 insert
        verify(imSensitiveWordMapper, never()).insert(any(ImSensitiveWordDO.class));
    }

    @Test
    public void testUpdateSensitiveWord_invalidatesCache() {
        // 准备：首次 validateText 触发 cache load
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // 准备：让 update 校验通过
        when(imSensitiveWordMapper.selectById(1L)).thenReturn(
                ImSensitiveWordDO.builder().id(1L).word("badword")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
        when(imSensitiveWordMapper.selectByWord("updatedbad")).thenReturn(null);
        // 准备：reload 时返回新词库
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of(
                        ImSensitiveWordDO.builder().id(1L).word("updatedbad")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build()
                ));

        // 调用：更新敏感词，触发 invalidate
        ImSensitiveWordSaveReqVO reqVO = new ImSensitiveWordSaveReqVO();
        reqVO.setId(1L);
        reqVO.setWord("updatedbad");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        sensitiveWordService.updateSensitiveWord(reqVO);

        // 调用 + 断言：再次 validateText 应使用新词库
        ServiceException exception = assertThrows(ServiceException.class,
                () -> sensitiveWordService.validateText("contains updatedbad here"));
        assertEquals(MESSAGE_SENSITIVE_WORD_BLOCKED.getCode(), exception.getCode());

        // 断言：selectListByStatus 共被调用 2 次
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testDeleteSensitiveWord_invalidatesCache() {
        // 准备：首次 validateText 触发 cache load
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // 准备：让 delete 校验通过
        when(imSensitiveWordMapper.selectById(1L)).thenReturn(
                ImSensitiveWordDO.builder().id(1L).word("badword")
                        .status(CommonStatusEnum.ENABLE.getStatus()).build());
        // 准备：reload 时返回剩余词库（只剩中文那条）
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of(
                        ImSensitiveWordDO.builder().id(2L).word("违禁词")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build()
                ));

        // 调用：删除 badword，触发 invalidate
        sensitiveWordService.deleteSensitiveWord(1L);

        // 调用 + 断言：badword 已被删，不再命中
        assertDoesNotThrow(() -> sensitiveWordService.validateText("contains badword here"));
        // 中文词依然在
        assertThrows(ServiceException.class,
                () -> sensitiveWordService.validateText("这条消息里有违禁词哦"));

        // 断言：selectListByStatus 共被调用 2 次
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testDeleteSensitiveWord_notExists_throws() {
        // 准备：selectById 返回 null
        when(imSensitiveWordMapper.selectById(999L)).thenReturn(null);

        // 调用 + 断言：抛 SENSITIVE_WORD_NOT_EXISTS
        ServiceException exception = assertThrows(ServiceException.class,
                () -> sensitiveWordService.deleteSensitiveWord(999L));
        assertEquals(SENSITIVE_WORD_NOT_EXISTS.getCode(), exception.getCode());

        // 断言：未走到 deleteById
        verify(imSensitiveWordMapper, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteSensitiveWordList_invalidatesCache() {
        // 准备：首次 validateText 触发 cache load
        sensitiveWordService.validateText("hello world");
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // 准备：reload 时返回空词库
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(ListUtil.of());

        // 调用：批量删除，触发 invalidate
        sensitiveWordService.deleteSensitiveWordList(ListUtil.of(1L, 2L));

        // 调用 + 断言：所有词都不再命中
        assertDoesNotThrow(() -> sensitiveWordService.validateText("contains badword here"));
        assertDoesNotThrow(() -> sensitiveWordService.validateText("这条消息里有违禁词哦"));

        // 断言：deleteByIds 被调用 1 次；selectListByStatus 共 2 次
        verify(imSensitiveWordMapper, times(1)).deleteByIds(ListUtil.of(1L, 2L));
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    @Test
    public void testDeleteSensitiveWordList_emptyIds_skip() {
        // 调用：空列表直接返回
        sensitiveWordService.deleteSensitiveWordList(ListUtil.of());

        // 断言：mapper 不被调用
        verify(imSensitiveWordMapper, never()).deleteByIds(anyList());
    }

}
