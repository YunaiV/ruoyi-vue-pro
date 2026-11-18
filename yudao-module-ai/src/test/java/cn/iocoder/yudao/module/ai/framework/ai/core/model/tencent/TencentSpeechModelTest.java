package cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent;

import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link TencentSpeechModel} 的单元测试类：提交一次、先交出任务编号、再在时限内等待结果
 *
 * @author 芋道源码
 */
public class TencentSpeechModelTest {

    private static final byte[] MP3 = new byte[]{'I', 'D', '3'};

    private final TencentTtsApi api = mock(TencentTtsApi.class);
    private final TencentSpeechModel speechModel = new TencentSpeechModel(api);

    @Test
    @SuppressWarnings("unchecked")
    public void testSynthesize_success() {
        // mock 方法
        when(api.createTask(eq("长文本"), eq("101001"), eq("mp3"), eq(1.2)))
                .thenReturn(new TencentTtsApi.TtsTask("task-1", "req-1"));
        when(api.waitTask(eq("task-1"), eq("mp3"), eq(TencentSpeechModel.WAIT_TIMEOUT), eq(TencentSpeechModel.POLL_INTERVAL)))
                .thenReturn(buildResult(TencentTtsApi.TtsTaskStatus.SUCCESS, MP3, null));
        Consumer<String> taskIdConsumer = mock(Consumer.class);

        // 调用
        byte[] audio = speechModel.synthesize("长文本", "101001", "mp3", 1.2, taskIdConsumer);
        // 断言：只提交一次；先交出任务编号，再等待结果
        assertSame(MP3, audio);
        InOrder inOrder = inOrder(api, taskIdConsumer);
        inOrder.verify(api).createTask(any(), any(), any(), any());
        inOrder.verify(taskIdConsumer).accept(eq("task-1"));
        inOrder.verify(api).waitTask(any(), any(), any(), any());
        verify(api, times(1)).createTask(any(), any(), any(), any());
    }

    @Test
    public void testSynthesize_fail() {
        // mock 方法：任务失败
        mockCreateTask();
        when(api.waitTask(eq("task-1"), any(), any(), any()))
                .thenReturn(buildResult(TencentTtsApi.TtsTaskStatus.FAILED, null, "VoiceType 不存在"));
        List<String> taskIds = new ArrayList<>();

        // 调用，并断言：抛出任务的错误信息；任务编号已经交出
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> speechModel.synthesize("长文本", "101001", "mp3", null, taskIds::add));
        assertEquals("VoiceType 不存在", ex.getMessage());
        assertEquals("task-1", taskIds.get(0));
    }

    @Test
    public void testSynthesize_waitTimeout() {
        // mock 方法：时限内仍在处理中；或者一直查询失败
        mockCreateTask();
        when(api.waitTask(eq("task-1"), any(), any(), any()))
                .thenReturn(buildResult(TencentTtsApi.TtsTaskStatus.RUNNING, null, null))
                .thenReturn(null);
        List<String> taskIds = new ArrayList<>();

        // 调用，并断言：返回 null，由调用方按任务编号继续查询；不重新提交
        assertNull(speechModel.synthesize("长文本", "101001", "mp3", null, taskIds::add));
        assertNull(speechModel.synthesize("长文本", "101001", "mp3", null, taskIds::add));
        assertEquals(2, taskIds.size());
        verify(api, times(2)).createTask(any(), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSynthesize_submitFail() {
        // mock 方法：提交失败
        when(api.createTask(any(), any(), any(), any())).thenThrow(new IllegalStateException("提交失败"));
        Consumer<String> taskIdConsumer = mock(Consumer.class);

        // 调用，并断言：异常直接抛出，不交出任务编号，也不等待
        assertThrows(IllegalStateException.class,
                () -> speechModel.synthesize("长文本", "101001", "mp3", null, taskIdConsumer));
        verifyNoInteractions(taskIdConsumer);
        verify(api, never()).waitTask(any(), any(), any(), any());
    }

    @Test
    public void testValidateOptions() {
        // 调用：委托给 TencentTtsApi 校验
        speechModel.validateOptions("你好", "101001", "mp3", 1.0);
        // 断言
        verify(api).validateOptions(eq("你好"), eq("101001"), eq("mp3"), eq(1.0));
        verifyNoMoreInteractions(api);
    }

    private void mockCreateTask() {
        when(api.createTask(any(), any(), any(), any())).thenReturn(new TencentTtsApi.TtsTask("task-1", "req-1"));
    }

    private static TencentTtsApi.TtsTaskResult buildResult(TencentTtsApi.TtsTaskStatus status, byte[] audio,
                                                          String errorMessage) {
        return new TencentTtsApi.TtsTaskResult("task-1", status, audio, errorMessage, "req-2");
    }

}
