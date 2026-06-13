package cn.iocoder.yudao.module.im.service.websocket;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImGroupMessageDTO;
import cn.iocoder.yudao.module.im.service.websocket.dto.ImPrivateMessageDTO;
import cn.iocoder.yudao.module.infra.api.websocket.WebSocketSenderApi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * IM WebSocket 推送 Service 单元测试
 *
 * @author 芋道源码
 */
public class ImWebSocketServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private ImWebSocketServiceImpl imWebSocketService;

    @Mock
    private WebSocketSenderApi webSocketSenderApi;

    @AfterEach
    public void tearDown() {
        // 清理事务同步上下文，避免串扰其它用例
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // ========== sendPrivateMessageAsync ==========

    @Test
    public void testSendPrivateMessageAsync_noTransactionSendsImmediately() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            // 准备
            ImPrivateMessageDTO dto = new ImPrivateMessageDTO().setSenderId(1L).setReceiverId(2L);

            // 调用：无事务，应立即发送
            imWebSocketService.sendPrivateMessageAsync(2L, dto);

            // 断言
            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(2L), eq(ImPrivateMessageDTO.TYPE), eq(dto));
        }
    }

    @Test
    public void testSendPrivateMessageAsync_inTransactionDeferredUntilCommit() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            // 准备：开启事务同步
            TransactionSynchronizationManager.initSynchronization();
            try {
                ImPrivateMessageDTO dto = new ImPrivateMessageDTO().setSenderId(1L).setReceiverId(2L);

                // 调用
                imWebSocketService.sendPrivateMessageAsync(2L, dto);

                // 断言：事务未提交，未推送
                verify(webSocketSenderApi, never()).sendObject(anyInt(), anyLong(), anyString(), any());

                // 模拟事务提交
                List<TransactionSynchronization> syncs =
                        TransactionSynchronizationManager.getSynchronizations();
                assertEquals(1, syncs.size());
                syncs.forEach(TransactionSynchronization::afterCommit);

                // 断言：提交后推送
                verify(webSocketSenderApi).sendObject(
                        eq(UserTypeEnum.ADMIN.getValue()), eq(2L), eq(ImPrivateMessageDTO.TYPE), eq(dto));
            } finally {
                TransactionSynchronizationManager.clear();
            }
        }
    }

    // ========== sendGroupMessageAsync ==========

    @Test
    public void testSendGroupMessageAsync_fanOutToAllUsers() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            ImGroupMessageDTO dto = new ImGroupMessageDTO();
            dto.setGroupId(10L);
            dto.setSenderId(1L);

            imWebSocketService.sendGroupMessageAsync(ListUtil.of(1L, 2L, 3L), dto);

            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(1L), eq(ImGroupMessageDTO.TYPE), eq(dto));
            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(2L), eq(ImGroupMessageDTO.TYPE), eq(dto));
            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(3L), eq(ImGroupMessageDTO.TYPE), eq(dto));
        }
    }

    @Test
    public void testSendGroupMessageAsync_senderExceptionDoesNotBreakOthers() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            ImGroupMessageDTO dto = new ImGroupMessageDTO();
            dto.setGroupId(10L);
            // 给 1 号用户推送时抛异常，不能影响 2/3 号
            doThrow(new RuntimeException("user offline"))
                    .when(webSocketSenderApi).sendObject(anyInt(), eq(1L), anyString(), any());

            imWebSocketService.sendGroupMessageAsync(ListUtil.of(1L, 2L, 3L), dto);

            // 2L 和 3L 也都被推送
            verify(webSocketSenderApi).sendObject(anyInt(), eq(2L), anyString(), any());
            verify(webSocketSenderApi).sendObject(anyInt(), eq(3L), anyString(), any());
        }
    }

    @Test
    public void testDoSendGroupMessage_emptyUserIds_noSend() {
        ImGroupMessageDTO dto = new ImGroupMessageDTO().setGroupId(10L);

        imWebSocketService.doSendGroupMessage(Collections.emptyList(), dto);
        imWebSocketService.doSendGroupMessage(null, dto);

        verifyNoInteractions(webSocketSenderApi);
    }

    @Test
    public void testDoSendGroupMessage_distinctUserIds() {
        ImGroupMessageDTO dto = new ImGroupMessageDTO().setGroupId(10L);

        imWebSocketService.doSendGroupMessage(Arrays.asList(1L, 2L, 1L, null), dto);

        verify(webSocketSenderApi).sendObject(
                eq(UserTypeEnum.ADMIN.getValue()), eq(1L), eq(ImGroupMessageDTO.TYPE), eq(dto));
        verify(webSocketSenderApi).sendObject(
                eq(UserTypeEnum.ADMIN.getValue()), eq(2L), eq(ImGroupMessageDTO.TYPE), eq(dto));
        verifyNoMoreInteractions(webSocketSenderApi);
    }

    @Test
    public void testSendPrivateMessageAsync_exceptionSwallowed() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            // 准备：sender 抛异常
            ImPrivateMessageDTO dto = new ImPrivateMessageDTO().setSenderId(1L).setReceiverId(2L);
            doThrow(new RuntimeException("user offline"))
                    .when(webSocketSenderApi).sendObject(anyInt(), anyLong(), anyString(), any());

            // 调用：异常应被吞掉，不向上抛
            imWebSocketService.sendPrivateMessageAsync(2L, dto);

            verify(webSocketSenderApi).sendObject(anyInt(), eq(2L), anyString(), any());
        }
    }

    @Test
    public void testSendGroupMessageAsync_singleUserDefaultOverload() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            ImGroupMessageDTO dto = new ImGroupMessageDTO();
            dto.setGroupId(10L);

            imWebSocketService.sendGroupMessageAsync(42L, dto);

            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(42L), eq(ImGroupMessageDTO.TYPE), eq(dto));
        }
    }

}
