package cn.iocoder.yudao.module.im.service.sensitiveword;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;
import cn.iocoder.yudao.module.im.dal.mysql.sensitiveword.ImSensitiveWordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static cn.iocoder.yudao.module.im.enums.ErrorCodeConstants.MESSAGE_SENSITIVE_WORD_BLOCKED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * IM 敏感词 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImSensitiveWordServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImSensitiveWordServiceImpl sensitiveWordService;

    @Mock
    private ImSensitiveWordMapper imSensitiveWordMapper;

    @BeforeEach
    public void setUp() {
        // 模拟 @PostConstruct 初始化：加载 2 个启用的敏感词
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(List.of(
                        ImSensitiveWordDO.builder().id(1L).word("badword")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build(),
                        ImSensitiveWordDO.builder().id(2L).word("违禁词")
                                .status(CommonStatusEnum.ENABLE.getStatus()).build()
                ));
        sensitiveWordService.init();
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
    public void testRefresh_reloadsFromMapper() {
        // 刷新前：第一次调用 mapper
        verify(imSensitiveWordMapper, times(1)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());

        // 准备：新列表不含原词
        when(imSensitiveWordMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus()))
                .thenReturn(List.of(
                        ImSensitiveWordDO.builder().id(3L).word("new").status(CommonStatusEnum.ENABLE.getStatus()).build()
                ));

        // 调用：刷新
        sensitiveWordService.refresh();

        // 断言：原敏感词不再命中，新词命中
        assertDoesNotThrow(() -> sensitiveWordService.validateText("contains badword"));
        assertThrows(ServiceException.class,
                () -> sensitiveWordService.validateText("contains new"));
        verify(imSensitiveWordMapper, times(2)).selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
