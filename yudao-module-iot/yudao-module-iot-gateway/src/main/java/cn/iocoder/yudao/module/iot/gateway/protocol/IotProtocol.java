package cn.iocoder.yudao.module.iot.gateway.protocol;

import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import io.vertx.core.Vertx;

/**
 * IoT 协议接口
 *
 * 定义传输层协议的生命周期管理
 *
 * @author 芋道源码
 */
public interface IotProtocol {

    /**
     * 获取协议实例 ID
     *
     * @return 协议实例 ID，如 "http-alink"、"tcp-binary"
     */
    String getId();

    /**
     * 获取服务器 ID（用于消息追踪，全局唯一）
     *
     * @return 服务器 ID
     */
    String getServerId();

    /**
     * 获取协议类型
     *
     * @return 协议类型枚举
     */
    IotProtocolTypeEnum getType();

    /**
     * 启动协议服务
     */
    void start();

    /**
     * 停止协议服务
     */
    void stop();

    /**
     * 检查协议服务是否正在运行
     *
     * @return 是否正在运行
     */
    boolean isRunning();

    /**
     * 获取协议的 Vert.x 实例：阻塞的处理（数据解析脚本、发送消息）切到它的 worker 上执行
     *
     * @return Vert.x 实例；没有使用 Vert.x 的协议（CoAP）返回 null
     */
    default Vertx getVertx() {
        return null;
    }

}
