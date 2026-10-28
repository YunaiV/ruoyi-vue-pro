package cn.iocoder.yudao.module.iot.core.script;

import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;

import java.util.concurrent.TimeUnit;

/**
 * IoT 数据解析脚本执行器：网关的 decode / encode，biz 的保存语法检查、调试共用
 * <p>
 * 目前只有 Rhino 实现 {@link cn.iocoder.yudao.module.iot.core.script.rhino.IotRhinoScriptExecutor}，由使用方直接创建
 *
 * @author 芋道源码
 */
public interface IotScriptExecutor {

    /**
     * 执行一次脚本调用：调用方线程阻塞等待，最长到截止时间
     *
     * @param source        脚本
     * @param function      "decode" 或 "encode"
     * @param ctxJson       平台构造的入参 JSON
     * @param deadlineNanos 截止时间（System.nanoTime）：覆盖排队与执行
     * @return 结果，状态见 {@link IotScriptResult.Status}
     */
    IotScriptResult execute(IotScriptSource source, String function, String ctxJson, long deadlineNanos);

    /**
     * 只做语法检查（不执行任何用户代码）：调用方线程有界等待
     *
     * @param content 脚本内容
     * @param timeout 等待时间
     * @param unit    时间单位
     * @return 结果：SUCCESS / ERROR（带行号）/ REJECTED / TIMEOUT
     */
    IotScriptResult compileOnly(String content, long timeout, TimeUnit unit);

    /**
     * 预热：启动时、接收流量之前调用一次
     */
    void warmUp();

    /**
     * 停止：不再接收新任务
     */
    void shutdown();

}
