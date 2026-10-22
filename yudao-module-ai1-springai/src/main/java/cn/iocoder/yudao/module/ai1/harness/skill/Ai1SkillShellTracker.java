package cn.iocoder.yudao.module.ai1.harness.skill;

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.agent.common.exec.ExecBackend;
import org.springaicommunity.agent.common.exec.ExecHandle;
import org.springaicommunity.agent.common.exec.ExecResult;
import org.springaicommunity.agent.common.exec.ExecSpec;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI1 SKILL Shell 进程跟踪器
 *
 * 登记本次生成启动的后台进程，生成结束或终止时回收
 *
 * @author 芋道源码
 */
@Slf4j
public class Ai1SkillShellTracker implements ExecBackend {

    private final ExecBackend delegate;
    /**
     * 在册的后台进程
     */
    private final Set<ExecHandle> handles = ConcurrentHashMap.newKeySet();
    /**
     * 是否已终止
     */
    private volatile boolean cancelled = false;

    public Ai1SkillShellTracker(ExecBackend delegate) {
        this.delegate = delegate;
    }

    @Override
    public ExecResult run(ExecSpec spec) {
        return delegate.run(spec);
    }

    @Override
    public ExecHandle start(ExecSpec spec) {
        if (cancelled) {
            throw new IllegalStateException("生成已终止");
        }
        ExecHandle handle = delegate.start(spec);
        handles.add(handle);
        // 登记后才被终止时，立即回收；先置标志再遍历的 cancel 与此处必有一方看到该进程
        if (cancelled && handles.remove(handle)) {
            handle.kill();
        }
        return handle;
    }

    /**
     * 终止并回收全部在册的后台进程（幂等，可并发调用）
     *
     * 按「移出集合」认领进程，每个进程只回收一次
     */
    public void cancel() {
        cancelled = true;
        for (ExecHandle handle : handles) {
            if (!handles.remove(handle)) {
                continue;
            }
            try {
                handle.kill();
            } catch (Exception e) {
                log.warn("[cancel][Shell 进程({}) 回收失败]", handle.id(), e);
            }
        }
    }

}
