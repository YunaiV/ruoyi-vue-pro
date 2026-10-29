/**
 * IoT 数据解析脚本：网关所有协议与 biz 调试共用
 * <ul>
 *     <li>本包：定义。{@link cn.iocoder.yudao.module.iot.core.script.IotScriptExecutor} 执行器接口、
 *         {@link cn.iocoder.yudao.module.iot.core.script.IotScriptUtils} 脚本契约（入参构造、输出校验）、
 *         {@link cn.iocoder.yudao.module.iot.core.script.IotScriptException} 脚本异常；</li>
 *     <li>dto：调用双方传递的数据。{@link cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource} 脚本、
 *         {@link cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult} 结果；</li>
 *     <li>rhino：Rhino 实现。{@link cn.iocoder.yudao.module.iot.core.script.rhino.IotRhinoScriptExecutor}，
 *         包内其余类（运行时、ContextFactory、执行预算）为包私有的内部实现。</li>
 * </ul>
 */
package cn.iocoder.yudao.module.iot.core.script;
