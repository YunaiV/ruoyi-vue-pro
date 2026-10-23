package cn.iocoder.yudao.module.ai1.harness.skill;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springaicommunity.agent.common.exec.ExecBackend;
import org.springaicommunity.agent.common.exec.ExecHandle;
import org.springaicommunity.agent.common.exec.ExecResult;
import org.springaicommunity.agent.common.exec.ExecSpec;
import org.springaicommunity.agent.exec.LocalExecBackend;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link Ai1SkillShellTracker} 的单元测试
 *
 * @author 芋道源码
 */
public class Ai1SkillShellTrackerTest extends BaseMockitoUnitTest {

    private static final ExecSpec SPEC = ExecSpec.of("sleep 60");

    @Mock
    private ExecBackend delegate;
    @Mock
    private ExecHandle handle1;
    @Mock
    private ExecHandle handle2;

    @Test
    public void testRun_delegate() {
        // mock 方法
        ExecResult result = ExecResult.completed(0, "ok", "");
        when(delegate.run(SPEC)).thenReturn(result);
        Ai1SkillShellTracker tracker = new Ai1SkillShellTracker(delegate);

        // 调用，并断言：同步命令自带超时，不登记
        assertSame(result, tracker.run(SPEC));
        tracker.cancel();
        verifyNoInteractions(handle1);
    }

    @Test
    public void testCancel_killAll() {
        // mock 方法：第一个进程回收失败，不影响其他进程
        when(delegate.start(SPEC)).thenReturn(handle1, handle2);
        doThrow(new IllegalStateException("进程已退出")).when(handle1).kill();
        Ai1SkillShellTracker tracker = new Ai1SkillShellTracker(delegate);
        tracker.start(SPEC);
        tracker.start(SPEC);

        // 调用：重复终止幂等
        tracker.cancel();
        tracker.cancel();
        // 断言
        verify(handle1).kill();
        verify(handle2).kill();
    }

    @Test
    @Timeout(30)
    public void testCancel_concurrentKillOnce() throws Exception {
        // mock 方法：第一个进程回收期间，另一个线程（如 worker 结束）并发回收
        when(delegate.start(SPEC)).thenReturn(handle1, handle2);
        Ai1SkillShellTracker tracker = new Ai1SkillShellTracker(delegate);
        tracker.start(SPEC);
        tracker.start(SPEC);
        AtomicBoolean first = new AtomicBoolean(true);
        Answer<Void> concurrentCancel = invocation -> {
            if (first.getAndSet(false)) {
                Thread other = new Thread(tracker::cancel);
                other.start();
                other.join(5000);
            }
            return null;
        };
        doAnswer(concurrentCancel).when(handle1).kill();
        doAnswer(concurrentCancel).when(handle2).kill();

        // 调用
        tracker.cancel();
        // 断言：每个进程只回收一次
        verify(handle1, times(1)).kill();
        verify(handle2, times(1)).kill();
    }

    @Test
    public void testStart_afterCancel() {
        // 准备参数
        Ai1SkillShellTracker tracker = new Ai1SkillShellTracker(delegate);
        tracker.cancel();

        // 调用，并断言异常：终止后拒绝再启动
        assertThrows(IllegalStateException.class, () -> tracker.start(SPEC));
        verify(delegate, never()).start(any());
    }

    @Test
    public void testStart_cancelledDuringStart() {
        // mock 方法：进程启动期间被终止
        Ai1SkillShellTracker tracker = new Ai1SkillShellTracker(delegate);
        when(delegate.start(SPEC)).thenAnswer(invocation -> {
            tracker.cancel();
            return handle1;
        });

        // 调用
        assertSame(handle1, tracker.start(SPEC));
        // 断言：登记后发现已终止，立即回收
        verify(handle1).kill();
    }

    @Test
    @Timeout(30)
    @EnabledOnOs({OS.MAC, OS.LINUX})
    public void testCancel_realProcess(@TempDir Path workDir) throws Exception {
        // 准备参数：Shell 写入自身 PID 后 exec 为 sleep，PID 不变
        Ai1SkillShellTracker tracker = new Ai1SkillShellTracker(LocalExecBackend.builder().workingDirectory(workDir).build());
        ExecHandle handle = tracker.start(ExecSpec.of("echo $$ > pid.tmp && mv pid.tmp pid && exec sleep 60"));
        ProcessHandle process = null;
        try {
            process = awaitDescendant(workDir.resolve("pid"));
            assertTrue(process.isAlive());

            // 调用
            tracker.cancel();
            // 断言
            process.onExit().get(10, TimeUnit.SECONDS);
            assertFalse(process.isAlive());
            assertFalse(handle.isAlive());
        } finally {
            // 只清理本用例启动的进程
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
            if (handle.isAlive()) {
                handle.kill();
            }
        }
    }

    // ========== 测试数据 ==========

    /**
     * 等待 PID 文件写入，且该进程属于当前 JVM 的子孙进程
     */
    private static ProcessHandle awaitDescendant(Path pidFile) throws Exception {
        long deadline = System.currentTimeMillis() + 5_000;
        while (System.currentTimeMillis() < deadline) {
            if (Files.exists(pidFile)) {
                long pid = Long.parseLong(Files.readString(pidFile).trim());
                Optional<ProcessHandle> process = ProcessHandle.current().descendants()
                        .filter(descendant -> descendant.pid() == pid).findFirst();
                if (process.isPresent()) {
                    return process.get();
                }
            }
            Thread.sleep(50);
        }
        return fail("未找到本用例启动的 Shell 进程");
    }

}
