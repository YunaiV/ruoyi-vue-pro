package cn.iocoder.yudao.module.im.service.websocket;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.im.enums.ImContentTypeEnum;
import cn.iocoder.yudao.module.im.enums.ImConversationTypeEnum;
import cn.iocoder.yudao.module.im.service.websocket.notification.ImNotificationWebSocketDTO;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImGroupMessageNotification;
import cn.iocoder.yudao.module.im.service.websocket.notification.message.ImPrivateMessageNotification;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * {@link ImWebSocketServiceImpl} 的单元测试
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

    // ========== 私聊推送 ==========

    @Test
    public void testSendNotificationAsync_private_noTransactionSendsImmediately() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            // 准备
            ImPrivateMessageNotification dto = new ImPrivateMessageNotification().setSenderId(1L).setReceiverId(2L)
                    .setType(ImContentTypeEnum.TEXT.getType());

            // 调用：无事务，应立即发送
            imWebSocketService.sendNotificationAsync(2L, ImConversationTypeEnum.PRIVATE.getType(),
                    ImContentTypeEnum.TEXT.getType(), dto);

            // 断言
            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(2L), eq(ImNotificationWebSocketDTO.TYPE),
                    argThat(actual -> isNotification(actual, ImConversationTypeEnum.PRIVATE,
                            ImContentTypeEnum.TEXT, dto)));
        }
    }

    @Test
    public void testSendNotificationAsync_private_inTransactionDeferredUntilCommit() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            // 准备：开启事务同步
            TransactionSynchronizationManager.initSynchronization();
            try {
                ImPrivateMessageNotification dto = new ImPrivateMessageNotification().setSenderId(1L).setReceiverId(2L)
                        .setType(ImContentTypeEnum.TEXT.getType());

                // 调用
                imWebSocketService.sendNotificationAsync(2L, ImConversationTypeEnum.PRIVATE.getType(),
                        ImContentTypeEnum.TEXT.getType(), dto);

                // 断言：事务未提交，未推送
                verify(webSocketSenderApi, never()).sendObject(anyInt(), anyLong(), anyString(), any());

                // 模拟事务提交
                List<TransactionSynchronization> syncs =
                        TransactionSynchronizationManager.getSynchronizations();
                assertEquals(1, syncs.size());
                syncs.forEach(TransactionSynchronization::afterCommit);

                // 断言：提交后推送
                verify(webSocketSenderApi).sendObject(
                        eq(UserTypeEnum.ADMIN.getValue()), eq(2L), eq(ImNotificationWebSocketDTO.TYPE),
                        argThat(actual -> isNotification(actual, ImConversationTypeEnum.PRIVATE,
                                ImContentTypeEnum.TEXT, dto)));
            } finally {
                TransactionSynchronizationManager.clear();
            }
        }
    }

    // ========== 群聊推送 ==========

    @Test
    public void testSendNotificationAsync_group_fanOutToAllUsers() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            ImGroupMessageNotification dto = new ImGroupMessageNotification();
            dto.setGroupId(10L);
            dto.setSenderId(1L);
            dto.setType(ImContentTypeEnum.TEXT.getType());

            imWebSocketService.sendNotificationAsync(ListUtil.of(1L, 2L, 3L),
                    ImConversationTypeEnum.GROUP.getType(), ImContentTypeEnum.TEXT.getType(), dto);

            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(1L), eq(ImNotificationWebSocketDTO.TYPE),
                    argThat(actual -> isNotification(actual, ImConversationTypeEnum.GROUP,
                            ImContentTypeEnum.TEXT, dto)));
            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(2L), eq(ImNotificationWebSocketDTO.TYPE),
                    argThat(actual -> isNotification(actual, ImConversationTypeEnum.GROUP,
                            ImContentTypeEnum.TEXT, dto)));
            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(3L), eq(ImNotificationWebSocketDTO.TYPE),
                    argThat(actual -> isNotification(actual, ImConversationTypeEnum.GROUP,
                            ImContentTypeEnum.TEXT, dto)));
        }
    }

    @Test
    public void testSendNotificationAsync_group_senderExceptionDoesNotBreakOthers() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            ImGroupMessageNotification dto = new ImGroupMessageNotification();
            dto.setGroupId(10L);
            // 给 1 号用户推送时抛异常，不能影响 2/3 号
            doThrow(new RuntimeException("user offline"))
                    .when(webSocketSenderApi).sendObject(anyInt(), eq(1L), anyString(), any());

            imWebSocketService.sendNotificationAsync(ListUtil.of(1L, 2L, 3L),
                    ImConversationTypeEnum.GROUP.getType(), ImContentTypeEnum.TEXT.getType(), dto);

            // 2L 和 3L 也都被推送
            verify(webSocketSenderApi).sendObject(anyInt(), eq(2L), anyString(), any());
            verify(webSocketSenderApi).sendObject(anyInt(), eq(3L), anyString(), any());
        }
    }

    @Test
    public void testDoSendNotification_emptyUserIds_noSend() {
        ImGroupMessageNotification dto = new ImGroupMessageNotification();
        dto.setGroupId(10L);
        dto.setType(ImContentTypeEnum.TEXT.getType());
        ImNotificationWebSocketDTO notification = buildNotification(ImConversationTypeEnum.GROUP,
                ImContentTypeEnum.TEXT, dto);

        imWebSocketService.doSendNotification(Collections.emptyList(), notification);
        imWebSocketService.doSendNotification(null, notification);

        verifyNoInteractions(webSocketSenderApi);
    }

    @Test
    public void testDoSendNotification_distinctUserIds() {
        ImGroupMessageNotification dto = new ImGroupMessageNotification();
        dto.setGroupId(10L);
        dto.setType(ImContentTypeEnum.TEXT.getType());
        ImNotificationWebSocketDTO notification = buildNotification(ImConversationTypeEnum.GROUP,
                ImContentTypeEnum.TEXT, dto);

        imWebSocketService.doSendNotification(Arrays.asList(1L, 2L, 1L, null), notification);

        verify(webSocketSenderApi).sendObject(
                eq(UserTypeEnum.ADMIN.getValue()), eq(1L), eq(ImNotificationWebSocketDTO.TYPE),
                argThat(actual -> isNotification(actual, ImConversationTypeEnum.GROUP,
                        ImContentTypeEnum.TEXT, dto)));
        verify(webSocketSenderApi).sendObject(
                eq(UserTypeEnum.ADMIN.getValue()), eq(2L), eq(ImNotificationWebSocketDTO.TYPE),
                argThat(actual -> isNotification(actual, ImConversationTypeEnum.GROUP,
                        ImContentTypeEnum.TEXT, dto)));
        verifyNoMoreInteractions(webSocketSenderApi);
    }

    @Test
    public void testSendNotificationAsync_private_exceptionSwallowed() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            // 准备：sender 抛异常
            ImPrivateMessageNotification dto = new ImPrivateMessageNotification().setSenderId(1L).setReceiverId(2L)
                    .setType(ImContentTypeEnum.TEXT.getType());
            doThrow(new RuntimeException("user offline"))
                    .when(webSocketSenderApi).sendObject(anyInt(), anyLong(), anyString(), any());

            // 调用：异常应被吞掉，不向上抛
            imWebSocketService.sendNotificationAsync(2L, ImConversationTypeEnum.PRIVATE.getType(),
                    ImContentTypeEnum.TEXT.getType(), dto);

            verify(webSocketSenderApi).sendObject(anyInt(), eq(2L), anyString(), any());
        }
    }

    @Test
    public void testSendNotificationAsync_group_singleUserDefaultOverload() {
        try (MockedStatic<SpringUtil> springUtilMockedStatic = mockStatic(SpringUtil.class)) {
            springUtilMockedStatic.when(() -> SpringUtil.getBean(eq(ImWebSocketServiceImpl.class)))
                    .thenReturn(imWebSocketService);

            ImGroupMessageNotification dto = new ImGroupMessageNotification();
            dto.setGroupId(10L);
            dto.setType(ImContentTypeEnum.TEXT.getType());

            imWebSocketService.sendNotificationAsync(42L, ImConversationTypeEnum.GROUP.getType(),
                    ImContentTypeEnum.TEXT.getType(), dto);

            verify(webSocketSenderApi).sendObject(
                    eq(UserTypeEnum.ADMIN.getValue()), eq(42L), eq(ImNotificationWebSocketDTO.TYPE),
                    argThat(actual -> isNotification(actual, ImConversationTypeEnum.GROUP,
                            ImContentTypeEnum.TEXT, dto)));
        }
    }

    private static boolean isNotification(Object object, ImConversationTypeEnum conversationType,
                                          ImContentTypeEnum contentType, Object payload) {
        if (!(object instanceof ImNotificationWebSocketDTO notification)) {
            return false;
        }
        return conversationType.getType().equals(notification.getConversationType())
                && contentType.getType().equals(notification.getContentType())
                && payload == notification.getPayload();
    }

    private static ImNotificationWebSocketDTO buildNotification(ImConversationTypeEnum conversationType,
                                                                ImContentTypeEnum contentType, Object payload) {
        return new ImNotificationWebSocketDTO()
                .setConversationType(conversationType.getType())
                .setContentType(contentType.getType())
                .setPayload(payload);
    }

}
