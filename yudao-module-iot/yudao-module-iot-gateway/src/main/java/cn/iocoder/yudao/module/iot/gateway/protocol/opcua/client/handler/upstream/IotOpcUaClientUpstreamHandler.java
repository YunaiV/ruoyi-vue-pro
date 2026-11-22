package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.handler.upstream;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotOpcUaDeviceConfigRespDTO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.manager.IotOpcUaClientSession;
import cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util.IotOpcUaValueConverter;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.IotDeviceMessageService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT OPC UA 上行数据处理器：把一次读取（或一次订阅通知）中同一设备的属性值合并为属性上报消息
 * <p>
 * 超过 maxPointsPerMessage 个点位，或属性 JSON 超过 maxMessageBytes 字节时，拆分为多条消息
 *
 * @author 芋道源码
 */
@Slf4j
public class IotOpcUaClientUpstreamHandler {

    private final IotDeviceMessageService messageService;
    private final String serverId;
    private final int maxPointsPerMessage;
    private final int maxMessageBytes;

    public IotOpcUaClientUpstreamHandler(IotDeviceMessageService messageService, String serverId,
                                         int maxPointsPerMessage, int maxMessageBytes) {
        this.messageService = messageService;
        this.serverId = serverId;
        this.maxPointsPerMessage = maxPointsPerMessage;
        this.maxMessageBytes = maxMessageBytes;
    }

    /**
     * 上报属性：发送到消息总线会阻塞，由调用方提交到会话的 worker 队列上执行
     *
     * @param config     设备配置
     * @param properties 属性值：identifier -> 值（已转换为 JSON 安全的值）
     */
    public void handlePropertyPost(IotOpcUaDeviceConfigRespDTO config, Map<String, Object> properties) {
        if (MapUtil.isEmpty(properties)) {
            return;
        }
        for (Map<String, Object> params : splitProperties(properties)) {
            try {
                IotDeviceMessage message = IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), params);
                // 采集由网关发起，没有设备确认；离线暂存失败时只记日志
                messageService.sendDeviceMessage(message, config.getProductKey(), config.getDeviceName(),
                        serverId, IotProtocolTypeEnum.OPC_UA_CLIENT);
            } catch (Exception e) {
                log.error("[handlePropertyPost][上报属性失败, deviceId={}, identifiers={}]",
                        config.getDeviceId(), params.keySet(), e);
            }
        }
    }

    /**
     * 收集一个读取结果（或订阅通知）的值：只接受 Good 状态、可以转换为 JSON 的值，其它跳过并每代告警一次
     * <p>
     * 在协议的 context 线程上调用
     *
     * @param session    会话
     * @param deviceId   设备 ID
     * @param identifier 物模型属性标识
     * @param dataValue  读取结果
     * @param target     收集到的属性值：identifier -> 值
     */
    public void collectValue(IotOpcUaClientSession session, Long deviceId, String identifier,
                             DataValue dataValue, Map<String, Object> target) {
        // 1. 状态：Bad / Uncertain 跳过
        StatusCode status = dataValue != null ? dataValue.getStatusCode() : null;
        if (status == null || !status.isGood()) {
            if (session.warnOnce("status:" + deviceId + ":" + identifier + ":" + status)) {
                log.warn("[collectValue][设备 {} 属性 {} 的状态不是 Good（{}），跳过]", deviceId, identifier, status);
            }
            return;
        }
        // 2. 转换：不支持的类型跳过
        Object raw = dataValue.getValue() != null ? dataValue.getValue().getValue() : null;
        Object value = IotOpcUaValueConverter.convert(raw);
        if (value == null) {
            if (session.warnOnce("type:" + deviceId + ":" + identifier)) {
                log.warn("[collectValue][设备 {} 属性 {} 的值类型不支持（{}），跳过]", deviceId, identifier,
                        raw != null ? raw.getClass().getSimpleName() : "null");
            }
            return;
        }
        target.put(identifier, value);
    }

    /**
     * 按点位数、JSON 字节数拆分属性
     *
     * @param properties 属性值
     * @return 拆分后的属性值列表；单个属性超过字节上限时丢弃并记日志
     */
    public List<Map<String, Object>> splitProperties(Map<String, Object> properties) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> current = new LinkedHashMap<>();
        // 当前 JSON 的字节数：{} 两个字节，每个属性 "key":value，属性之间一个逗号
        int currentBytes = 2;
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            int entryBytes = calculateEntryBytes(entry.getKey(), entry.getValue());
            if (entryBytes + 2 > maxMessageBytes) {
                log.warn("[splitProperties][属性 {} 的 JSON 为 {} 字节，超过单条消息上限 {}，丢弃]",
                        entry.getKey(), entryBytes, maxMessageBytes);
                continue;
            }
            int separatorBytes = current.isEmpty() ? 0 : 1;
            if (!current.isEmpty() && (current.size() >= maxPointsPerMessage
                    || currentBytes + separatorBytes + entryBytes > maxMessageBytes)) {
                result.add(current);
                current = new LinkedHashMap<>();
                currentBytes = 2;
                separatorBytes = 0;
            }
            current.put(entry.getKey(), entry.getValue());
            currentBytes += separatorBytes + entryBytes;
        }
        if (!current.isEmpty()) {
            result.add(current);
        }
        return result;
    }

    private static int calculateEntryBytes(String key, Object value) {
        // {"key":value} 去掉两侧的大括号
        return StrUtil.utf8Bytes(JsonUtils.toJsonString(MapUtil.of(key, value))).length - 2;
    }

}
