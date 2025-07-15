package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.client.TcpDeviceClient;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.TcpDeviceConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataDecoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataPackage;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import com.alibaba.fastjson.JSON;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 下行消息处理器
 * <p>
 * 负责处理从业务系统发送到设备的下行消息，包括：
 * 1. 属性设置
 * 2. 服务调用
 * 3. 属性获取
 * 4. 配置下发
 * 5. OTA 升级
 *
 * @author 芋道源码
 */
@Slf4j
public class IotTcpDownstreamHandler {

    private final TcpDeviceConnectionManager connectionManager;

    private final IotDeviceMessageService messageService;

    public IotTcpDownstreamHandler(TcpDeviceConnectionManager connectionManager,
                                   IotDeviceMessageService messageService) {
        this.connectionManager = connectionManager;
        this.messageService = messageService;
    }

    /**
     * 处理下行消息
     *
     * @param message 设备消息
     */
    public void handle(IotDeviceMessage message) {
        try {
            log.info("[handle][处理下行消息] 设备ID: {}, 方法: {}, 消息ID: {}",
                    message.getDeviceId(), message.getMethod(), message.getId());

            // 1. 获取设备连接
            TcpDeviceClient client = connectionManager.getClientByDeviceId(message.getDeviceId());
            if (client == null || !client.isOnline()) {
                log.error("[handle][设备({})不在线，无法发送下行消息]", message.getDeviceId());
                return;
            }

            // 2. 根据消息方法处理不同类型的下行消息
            switch (message.getMethod()) {
                case "thing.property.set":
                    handlePropertySet(client, message);
                    break;
                case "thing.property.get":
                    handlePropertyGet(client, message);
                    break;
                case "thing.service.invoke":
                    handleServiceInvoke(client, message);
                    break;
                case "thing.config.push":
                    handleConfigPush(client, message);
                    break;
                case "thing.ota.upgrade":
                    handleOtaUpgrade(client, message);
                    break;
                default:
                    log.warn("[handle][未知的下行消息方法: {}]", message.getMethod());
                    break;
            }

        } catch (Exception e) {
            log.error("[handle][处理下行消息失败]", e);
        }
    }

    /**
     * 处理属性设置
     *
     * @param client  设备客户端
     * @param message 设备消息
     */
    private void handlePropertySet(TcpDeviceClient client, IotDeviceMessage message) {
        try {
            log.info("[handlePropertySet][属性设置] 设备地址: {}, 属性: {}",
                    client.getDeviceAddr(), message.getParams());

            // 使用编解码器发送消息，降级处理使用原始编码
            sendMessageWithCodec(client, message, "handlePropertySet", () -> {
                String payload = JSON.toJSONString(message.getParams());
                short mid = generateMessageId();

                Buffer buffer = TcpDataEncoder.createPropertySetPackage(
                        client.getDeviceAddr(), mid, payload);
                client.sendMessage(buffer);

                log.debug("[handlePropertySet][属性设置消息已发送(降级)] 设备地址: {}, 消息序号: {}",
                        client.getDeviceAddr(), mid);
            });

        } catch (Exception e) {
            log.error("[handlePropertySet][属性设置失败]", e);
        }
    }

    /**
     * 处理属性获取
     *
     * @param client  设备客户端
     * @param message 设备消息
     */
    private void handlePropertyGet(TcpDeviceClient client, IotDeviceMessage message) {
        try {
            log.info("[handlePropertyGet][属性获取] 设备地址: {}, 属性列表: {}",
                    client.getDeviceAddr(), message.getParams());

            // 使用编解码器发送消息，降级处理使用原始编码
            sendMessageWithCodec(client, message, "handlePropertyGet", () -> {
                String payload = JSON.toJSONString(message.getParams());
                short mid = generateMessageId();

                Buffer buffer = TcpDataEncoder.createPropertyGetPackage(
                        client.getDeviceAddr(), mid, payload);
                client.sendMessage(buffer);

                log.debug("[handlePropertyGet][属性获取消息已发送(降级)] 设备地址: {}, 消息序号: {}",
                        client.getDeviceAddr(), mid);
            });

        } catch (Exception e) {
            log.error("[handlePropertyGet][属性获取失败]", e);
        }
    }

    /**
     * 处理服务调用
     *
     * @param client  设备客户端
     * @param message 设备消息
     */
    private void handleServiceInvoke(TcpDeviceClient client, IotDeviceMessage message) {
        try {
            log.info("[handleServiceInvoke][服务调用] 设备地址: {}, 服务参数: {}",
                    client.getDeviceAddr(), message.getParams());

            // 1. 构建服务调用数据包
            String payload = JSON.toJSONString(message.getParams());
            short mid = generateMessageId();

            Buffer buffer = TcpDataEncoder.createServiceInvokePackage(
                    client.getDeviceAddr(), mid, payload);

            // 2. 发送消息
            client.sendMessage(buffer);

            log.debug("[handleServiceInvoke][服务调用消息已发送] 设备地址: {}, 消息序号: {}",
                    client.getDeviceAddr(), mid);

        } catch (Exception e) {
            log.error("[handleServiceInvoke][服务调用失败]", e);
        }
    }

    /**
     * 处理配置推送
     *
     * @param client  设备客户端
     * @param message 设备消息
     */
    private void handleConfigPush(TcpDeviceClient client, IotDeviceMessage message) {
        try {
            log.info("[handleConfigPush][配置推送] 设备地址: {}, 配置: {}",
                    client.getDeviceAddr(), message.getParams());

            // 1. 构建配置推送数据包
            String payload = JSON.toJSONString(message.getParams());
            short mid = generateMessageId();

            Buffer buffer = TcpDataEncoder.createDataDownPackage(
                    client.getDeviceAddr(), mid, payload);

            // 2. 发送消息
            client.sendMessage(buffer);

            log.debug("[handleConfigPush][配置推送消息已发送] 设备地址: {}, 消息序号: {}",
                    client.getDeviceAddr(), mid);

        } catch (Exception e) {
            log.error("[handleConfigPush][配置推送失败]", e);
        }
    }

    /**
     * 处理 OTA 升级
     *
     * @param client  设备客户端
     * @param message 设备消息
     */
    private void handleOtaUpgrade(TcpDeviceClient client, IotDeviceMessage message) {
        try {
            log.info("[handleOtaUpgrade][OTA升级] 设备地址: {}, 升级信息: {}",
                    client.getDeviceAddr(), message.getParams());

            // 1. 构建 OTA 升级数据包
            String payload = JSON.toJSONString(message.getParams());
            short mid = generateMessageId();

            Buffer buffer = TcpDataEncoder.createDataDownPackage(
                    client.getDeviceAddr(), mid, payload);

            // 2. 发送消息
            client.sendMessage(buffer);

            log.debug("[handleOtaUpgrade][OTA升级消息已发送] 设备地址: {}, 消息序号: {}",
                    client.getDeviceAddr(), mid);

        } catch (Exception e) {
            log.error("[handleOtaUpgrade][OTA升级失败]", e);
        }
    }

    /**
     * 处理自定义下行消息
     *
     * @param client  设备客户端
     * @param message 设备消息
     * @param code    功能码
     */
    private void handleCustomMessage(TcpDeviceClient client, IotDeviceMessage message, short code) {
        try {
            log.info("[handleCustomMessage][自定义消息] 设备地址: {}, 功能码: {}, 数据: {}",
                    client.getDeviceAddr(), code, message.getParams());

            // 1. 构建自定义数据包
            String payload = JSON.toJSONString(message.getParams());
            short mid = generateMessageId();

            TcpDataPackage dataPackage = TcpDataPackage.builder()
                    .addr(client.getDeviceAddr())
                    .code(code)
                    .mid(mid)
                    .payload(payload)
                    .build();

            Buffer buffer = TcpDataEncoder.encode(dataPackage);

            // 2. 发送消息
            client.sendMessage(buffer);

            log.debug("[handleCustomMessage][自定义消息已发送] 设备地址: {}, 功能码: {}, 消息序号: {}",
                    client.getDeviceAddr(), code, mid);

        } catch (Exception e) {
            log.error("[handleCustomMessage][自定义消息发送失败]", e);
        }
    }

    /**
     * 批量发送下行消息
     *
     * @param deviceIds 设备ID列表
     * @param message   设备消息
     */
    public void broadcastMessage(Long[] deviceIds, IotDeviceMessage message) {
        try {
            log.info("[broadcastMessage][批量发送消息] 设备数量: {}, 方法: {}",
                    deviceIds.length, message.getMethod());

            for (Long deviceId : deviceIds) {
                // 创建副本消息（避免消息ID冲突）
                IotDeviceMessage copyMessage = IotDeviceMessage.of(
                        message.getRequestId(),
                        message.getMethod(),
                        message.getParams(),
                        message.getData(),
                        message.getCode(),
                        message.getMsg());
                copyMessage.setDeviceId(deviceId);

                // 处理单个设备消息
                handle(copyMessage);
            }

        } catch (Exception e) {
            log.error("[broadcastMessage][批量发送消息失败]", e);
        }
    }

    /**
     * 检查设备是否支持指定方法
     *
     * @param client 设备客户端
     * @param method 消息方法
     * @return 是否支持
     */
    private boolean isMethodSupported(TcpDeviceClient client, String method) {
        // TODO: 可以根据设备类型或产品信息判断是否支持特定方法
        return IotDeviceMessageMethodEnum.of(method) != null;
    }

    /**
     * 生成消息序号
     *
     * @return 消息序号
     */
    private short generateMessageId() {
        return (short) (System.currentTimeMillis() % Short.MAX_VALUE);
    }

    /**
     * 使用编解码器发送消息
     *
     * @param client         设备客户端
     * @param message        设备消息
     * @param methodName     方法名称
     * @param fallbackAction 降级处理逻辑
     */
    private void sendMessageWithCodec(TcpDeviceClient client, IotDeviceMessage message,
                                      String methodName, Runnable fallbackAction) {
        try {
            // 1. 使用编解码器编码消息
            byte[] messageBytes = messageService.encodeDeviceMessage(
                    message, client.getProductKey(), client.getDeviceName());

            // 2. 解析编码后的数据包并设置设备地址和消息序号
            Buffer buffer = Buffer.buffer(messageBytes);
            TcpDataPackage dataPackage = TcpDataDecoder.decode(buffer);
            dataPackage.setAddr(client.getDeviceAddr());
            dataPackage.setMid(generateMessageId());

            // 3. 重新编码并发送
            Buffer finalBuffer = TcpDataEncoder.encode(dataPackage);
            client.sendMessage(finalBuffer);

            log.debug("[{}][消息已发送] 设备地址: {}, 消息序号: {}",
                    methodName, client.getDeviceAddr(), dataPackage.getMid());

        } catch (Exception e) {
            log.warn("[{}][使用编解码器编码失败，降级使用原始编码] 错误: {}",
                    methodName, e.getMessage());

            // 执行降级处理
            if (fallbackAction != null) {
                fallbackAction.run();
            }
        }
    }

    /**
     * 获取连接统计信息
     *
     * @return 连接统计信息
     */
    public String getHandlerStatistics() {
        return String.format("TCP下游处理器 - %s", connectionManager.getConnectionStatus());
    }
}