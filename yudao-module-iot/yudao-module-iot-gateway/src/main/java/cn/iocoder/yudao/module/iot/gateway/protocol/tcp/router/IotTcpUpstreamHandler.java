package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.router;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.iot.core.biz.IotDeviceCommonApi;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotDeviceRespDTO;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.config.IotGatewayProperties;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.client.TcpDeviceClient;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.manager.TcpDeviceConnectionManager;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataDecoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataEncoder;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataPackage;
import cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol.TcpDataReader;
import cn.iocoder.yudao.module.iot.gateway.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.parsetools.RecordParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * IoT 网关 TCP 上行消息处理器
 * <p>
 * 核心负责：
 * 1. 【设备注册】设备连接后发送注册消息，注册成功后可以进行通信
 * 2. 【心跳处理】定期接收设备心跳消息，维持连接状态
 * 3. 【数据上报】接收设备数据上报和事件上报
 * 4. 【连接管理】管理连接的建立、维护和清理
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotTcpUpstreamHandler implements Handler<NetSocket> {

    private final IotGatewayProperties.TcpProperties tcpConfig;

    // TODO @haohao：可以把 TcpDeviceConnectionManager 能力放大一点：1）handle 里的 client 初始化，可以拿到 TcpDeviceConnectionManager 里；2）handleDeviceRegister 也是；
    private final TcpDeviceConnectionManager connectionManager;

    private final IotDeviceService deviceService;

    private final IotDeviceMessageService messageService;

    private final IotDeviceCommonApi deviceApi;

    private final String serverId;

    @Override
    public void handle(NetSocket socket) {
        log.info("[handle][收到设备连接: {}]", socket.remoteAddress());

        // 创建客户端 ID 和设备客户端
        // TODO @haohao：clientid 给 TcpDeviceClient 生成会简洁一点；减少 upsteramhanlder 的非核心逻辑；
        String clientId = IdUtil.simpleUUID() + "_" + socket.remoteAddress();
        TcpDeviceClient client = new TcpDeviceClient(clientId, tcpConfig.getKeepAliveTimeoutMs());

        try {
            // 设置连接异常和关闭处理
            socket.exceptionHandler(ex -> {
                // TODO @haohao：这里的日志，可能把 clientid 都打上？因为 address 会重复么？
                log.error("[handle][连接({})异常]", socket.remoteAddress(), ex);
                handleConnectionClose(client);
            });
            socket.closeHandler(v -> {
                log.info("[handle][连接({})关闭]", socket.remoteAddress());
                handleConnectionClose(client);
            });
            client.setSocket(socket);

            // 设置解析器
            RecordParser parser = TcpDataReader.createParser(buffer -> {
                try {
                    handleDataPackage(client, buffer);
                } catch (Exception e) {
                    log.error("[handle][处理数据包异常]", e);
                }
            });
            client.setParser(parser);

            // TODO @haohao：socket.remoteAddress()) 打印进去
            log.info("[handle][设备连接处理器初始化完成: {}]", clientId);
        } catch (Exception e) {
            // TODO @haohao：socket.remoteAddress()) 打印进去
            log.error("[handle][初始化连接处理器失败]", e);
            client.shutdown();
        }
    }

    /**
     * 处理数据包
     *
     * @param client 设备客户端
     * @param buffer 数据缓冲区
     */
    private void handleDataPackage(TcpDeviceClient client, io.vertx.core.buffer.Buffer buffer) {
        try {
            // 解码数据包
            TcpDataPackage dataPackage = TcpDataDecoder.decode(buffer);
            log.info("[handleDataPackage][接收数据包] 设备地址: {}, 功能码: {}, 消息序号: {}",
                    dataPackage.getAddr(), dataPackage.getCodeDescription(), dataPackage.getMid());

            // 根据功能码处理不同类型的消息
            switch (dataPackage.getCode()) {
                // TODO @haohao：【重要】code 要不要改成 opCode。这样和 data 里的 code 好区分；
                case TcpDataPackage.CODE_REGISTER:
                    handleDeviceRegister(client, dataPackage);
                    break;
                case TcpDataPackage.CODE_HEARTBEAT:
                    handleHeartbeat(client, dataPackage);
                    break;
                case TcpDataPackage.CODE_DATA_UP:
                    handleDataUp(client, dataPackage);
                    break;
                case TcpDataPackage.CODE_EVENT_UP:
                    handleEventUp(client, dataPackage);
                    break;
                default:
                    log.warn("[handleDataPackage][未知功能码: {}]", dataPackage.getCode());
                    break;
            }
        } catch (Exception e) {
            // TODO @haohao：最好有 client 标识；
            log.error("[handleDataPackage][处理数据包失败]", e);
        }
    }

    /**
     * 处理设备注册
     *
     * @param client      设备客户端
     * @param dataPackage 数据包
     */
    private void handleDeviceRegister(TcpDeviceClient client, TcpDataPackage dataPackage) {
        try {
            String deviceAddr = dataPackage.getAddr();
            String productKey = dataPackage.getPayload();
            log.info("[handleDeviceRegister][设备注册] 设备地址: {}, 产品密钥: {}", deviceAddr, productKey);

            // 获取设备信息
            IotDeviceRespDTO device = deviceService.getDeviceFromCache(productKey, deviceAddr);
            if (device == null) {
                log.error("[handleDeviceRegister][设备不存在: {} - {}]", productKey, deviceAddr);
                sendRegisterReply(client, dataPackage, false);
                return;
            }

            // 更新客户端信息
            // TODO @haohao：一个 set 方法，统一处理掉会好点哈；
            client.setProductKey(productKey);
            client.setDeviceName(deviceAddr);
            client.setDeviceId(device.getId());
            client.setAuthenticated(true);

            // 添加到连接管理器
            connectionManager.addClient(deviceAddr, client);
            connectionManager.setDeviceIdMapping(deviceAddr, device.getId());

            // 发送设备上线消息
            IotDeviceMessage onlineMessage = IotDeviceMessage.buildStateUpdateOnline();
            messageService.sendDeviceMessage(onlineMessage, productKey, deviceAddr, serverId);

            // 发送注册成功回复
            sendRegisterReply(client, dataPackage, true);

            log.info("[handleDeviceRegister][设备注册成功] 设备地址: {}, 设备ID: {}", deviceAddr, device.getId());
        } catch (Exception e) {
            log.error("[handleDeviceRegister][设备注册失败]", e);
            sendRegisterReply(client, dataPackage, false);
        }
    }

    /**
     * 处理心跳
     *
     * @param client      设备客户端
     * @param dataPackage 数据包
     */
    private void handleHeartbeat(TcpDeviceClient client, TcpDataPackage dataPackage) {
        try {
            String deviceAddr = dataPackage.getAddr();
            log.debug("[handleHeartbeat][收到心跳] 设备地址: {}", deviceAddr);

            // 更新心跳时间
            client.keepAlive();

            // 发送心跳回复（可选）
            // sendHeartbeatReply(client, dataPackage);

        } catch (Exception e) {
            log.error("[handleHeartbeat][处理心跳失败]", e);
        }
    }

    /**
     * 处理数据上报
     *
     * @param client      设备客户端
     * @param dataPackage 数据包
     */
    private void handleDataUp(TcpDeviceClient client, TcpDataPackage dataPackage) {
        try {
            String deviceAddr = dataPackage.getAddr();
            String payload = dataPackage.getPayload();

            log.info("[handleDataUp][数据上报] 设备地址: {}, 数据: {}", deviceAddr, payload);

            // 检查设备是否已认证
            if (!client.isAuthenticated()) {
                log.warn("[handleDataUp][设备未认证，忽略数据上报: {}]", deviceAddr);
                return;
            }

            // 使用 IotDeviceMessageService 解码消息
            try {
                // 1. 将 TCP 数据包重新编码为字节数组
                Buffer buffer = TcpDataEncoder.encode(dataPackage);
                byte[] messageBytes = buffer.getBytes();

                // 2. 使用 messageService 解码消息
                IotDeviceMessage message = messageService.decodeDeviceMessage(
                        messageBytes, client.getProductKey(), client.getDeviceName());

                // 3. 发送解码后的消息
                messageService.sendDeviceMessage(message, client.getProductKey(), client.getDeviceName(), serverId);
            } catch (Exception e) {
                log.warn("[handleDataUp][使用编解码器解码失败，降级使用原始解析] 错误: {}", e.getMessage());

                // 降级处理：使用原始方式解析数据
                JSONObject dataJson = JSONUtil.parseObj(payload);
                IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", dataJson);
                messageService.sendDeviceMessage(message, client.getProductKey(), client.getDeviceName(), serverId);
            }

            // 发送数据上报回复
            sendDataUpReply(client, dataPackage);
        } catch (Exception e) {
            log.error("[handleDataUp][处理数据上报失败]", e);
        }
    }

    /**
     * 处理事件上报
     *
     * @param client      设备客户端
     * @param dataPackage 数据包
     */
    private void handleEventUp(TcpDeviceClient client, TcpDataPackage dataPackage) {
        try {
            String deviceAddr = dataPackage.getAddr();
            String payload = dataPackage.getPayload();

            log.info("[handleEventUp][事件上报] 设备地址: {}, 数据: {}", deviceAddr, payload);

            // 检查设备是否已认证
            if (!client.isAuthenticated()) {
                log.warn("[handleEventUp][设备未认证，忽略事件上报: {}]", deviceAddr);
                return;
            }

            // 使用 IotDeviceMessageService 解码消息
            try {
                // 1. 将 TCP 数据包重新编码为字节数组
                Buffer buffer = TcpDataEncoder.encode(dataPackage);
                byte[] messageBytes = buffer.getBytes();

                // 2. 使用 messageService 解码消息
                IotDeviceMessage message = messageService.decodeDeviceMessage(
                        messageBytes, client.getProductKey(), client.getDeviceName());

                // 3. 发送解码后的消息
                messageService.sendDeviceMessage(message, client.getProductKey(), client.getDeviceName(), serverId);
            } catch (Exception e) {
                log.warn("[handleEventUp][使用编解码器解码失败，降级使用原始解析] 错误: {}", e.getMessage());

                // 降级处理：使用原始方式解析数据
                // TODO @芋艿：降级处理逻辑；
                JSONObject eventJson = JSONUtil.parseObj(payload);
                IotDeviceMessage message = IotDeviceMessage.requestOf("thing.event.post", eventJson);
                messageService.sendDeviceMessage(message, client.getProductKey(), client.getDeviceName(), serverId);
            }

            // 发送事件上报回复
            sendEventUpReply(client, dataPackage);
        } catch (Exception e) {
            log.error("[handleEventUp][处理事件上报失败]", e);
        }
    }

    /**
     * 发送注册回复
     *
     * @param client      设备客户端
     * @param dataPackage 原始数据包
     * @param success     是否成功
     */
    private void sendRegisterReply(TcpDeviceClient client, TcpDataPackage dataPackage, boolean success) {
        try {
            io.vertx.core.buffer.Buffer replyBuffer = TcpDataEncoder.createRegisterReply(
                    dataPackage.getAddr(), dataPackage.getMid(), success);
            client.sendMessage(replyBuffer);

            log.debug("[sendRegisterReply][发送注册回复] 设备地址: {}, 结果: {}",
                    dataPackage.getAddr(), success ? "成功" : "失败");
        } catch (Exception e) {
            log.error("[sendRegisterReply][发送注册回复失败]", e);
        }
    }

    /**
     * 发送数据上报回复
     *
     * @param client      设备客户端
     * @param dataPackage 原始数据包
     */
    private void sendDataUpReply(TcpDeviceClient client, TcpDataPackage dataPackage) {
        try {
            TcpDataPackage replyPackage = TcpDataPackage.builder()
                    .addr(dataPackage.getAddr())
                    .code(TcpDataPackage.CODE_DATA_UP)
                    .mid(dataPackage.getMid())
                    .payload("0") // 0 表示成功 TODO @haohao：最好枚举到 TcpDataPackage 里？
                    .build();

            io.vertx.core.buffer.Buffer replyBuffer = TcpDataEncoder.encode(replyPackage);
            client.sendMessage(replyBuffer);
        } catch (Exception e) {
            // TODO @haohao：可以有个 client id
            log.error("[sendDataUpReply][发送数据上报回复失败]", e);
        }
    }

    /**
     * 发送事件上报回复
     *
     * @param client      设备客户端
     * @param dataPackage 原始数据包
     */
    private void sendEventUpReply(TcpDeviceClient client, TcpDataPackage dataPackage) {
        try {
            TcpDataPackage replyPackage = TcpDataPackage.builder()
                    .addr(dataPackage.getAddr())
                    .code(TcpDataPackage.CODE_EVENT_UP)
                    .mid(dataPackage.getMid())
                    .payload("0") // 0 表示成功
                    .build();

            io.vertx.core.buffer.Buffer replyBuffer = TcpDataEncoder.encode(replyPackage);
            client.sendMessage(replyBuffer);
        } catch (Exception e) {
            log.error("[sendEventUpReply][发送事件上报回复失败]", e);
        }
    }

    /**
     * 处理连接关闭
     *
     * @param client 设备客户端
     */
    private void handleConnectionClose(TcpDeviceClient client) {
        try {
            String deviceAddr = client.getDeviceAddr();

            // 发送设备离线消息
            if (client.isAuthenticated()) {
                IotDeviceMessage offlineMessage = IotDeviceMessage.buildStateOffline();
                messageService.sendDeviceMessage(offlineMessage,
                        client.getProductKey(), client.getDeviceName(), serverId);
            }

            // 从连接管理器移除
            if (deviceAddr != null) {
                connectionManager.removeClient(deviceAddr);
            }

            log.info("[handleConnectionClose][处理连接关闭完成] 设备地址: {}", deviceAddr);
        } catch (Exception e) {
            log.error("[handleConnectionClose][处理连接关闭失败]", e);
        }
    }
}