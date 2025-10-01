package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkTcpConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.data.action.tcp.IotTcpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * TCP 的 {@link IotDataRuleAction} 实现类
 * <p>
 * 负责将设备消息发送到外部 TCP 服务器
 * 支持普通 TCP 和 SSL TCP 连接，支持 JSON 和 BINARY 数据格式
 * 使用连接池管理 TCP 连接，提高性能和资源利用率
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotTcpDataRuleAction extends
        IotDataRuleCacheableAction<IotDataSinkTcpConfig, IotTcpClient> {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(10);

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.TCP.getType();
    }

    @Override
    protected IotTcpClient initProducer(IotDataSinkTcpConfig config) throws Exception {
        // 1.1 参数校验
        if (config.getHost() == null || config.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("TCP 服务器地址不能为空");
        }
        if (config.getPort() == null || config.getPort() <= 0 || config.getPort() > 65535) {
            throw new IllegalArgumentException("TCP 服务器端口无效");
        }

        // 2.1 创建 TCP 客户端
        IotTcpClient tcpClient = new IotTcpClient(
                config.getHost(),
                config.getPort(),
                config.getConnectTimeoutMs(),
                config.getReadTimeoutMs(),
                config.getSsl(),
                config.getSslCertPath(),
                config.getDataFormat()
        );
        // 2.2 连接服务器
        tcpClient.connect();
        log.info("[initProducer][TCP 客户端创建并连接成功，服务器: {}:{}，SSL: {}，数据格式: {}]",
                config.getHost(), config.getPort(), config.getSsl(), config.getDataFormat());
        return tcpClient;
    }

    @Override
    protected void closeProducer(IotTcpClient producer) throws Exception {
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    protected void execute(IotDeviceMessage message, IotDataSinkTcpConfig config) throws Exception {
        try {
            // 1.1 获取或创建 TCP 客户端
            IotTcpClient tcpClient = getProducer(config);
            // 1.2 检查连接状态，如果断开则重新连接
            if (!tcpClient.isConnected()) {
                log.warn("[execute][TCP 连接已断开，尝试重新连接，服务器: {}:{}]", config.getHost(), config.getPort());
                tcpClient.connect();
            }

            // 2.1 发送消息并等待结果
            tcpClient.sendMessage(message);
            // 2.2 记录发送成功日志
            log.info("[execute][message({}) config({}) 发送成功，TCP 服务器: {}:{}]",
                    message, config, config.getHost(), config.getPort());
        } catch (Exception e) {
            log.error("[execute][message({}) config({}) 发送失败，TCP 服务器: {}:{}]",
                    message, config, config.getHost(), config.getPort(), e);
            throw e;
        }
    }

}