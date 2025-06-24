package cn.iocoder.yudao.module.huawei.smarthome.service.event;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.huawei.smarthome.dto.notification.NotificationEventDTO;
import cn.iocoder.yudao.module.huawei.smarthome.enums.HuaweiNotificationTypeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@Slf4j
public class HuaweiNotificationServiceImpl implements HuaweiNotificationService {

    @Override
    public void handleNotifications(List<NotificationEventDTO> events) {
        if (CollectionUtils.isEmpty(events)) {
            log.info("[handleNotifications] 接收到的事件列表为空，无需处理。");
            return;
        }

        for (NotificationEventDTO event : events) {
            String dataTypeStr = event.getDataType();
            JsonNode payload = event.getPayload();
            HuaweiNotificationTypeEnum notificationType = HuaweiNotificationTypeEnum.ofType(dataTypeStr);

            if (notificationType == null) {
                log.warn("[handleNotifications] 未知的通知类型: {}, payload: {}", dataTypeStr, JsonUtils.toJsonString(payload));
                continue;
            }

            log.info("[handleNotifications] 开始处理通知类型: {}, payload: {}", notificationType, JsonUtils.toJsonString(payload));

            try {
                switch (notificationType) {
                    case DEVICE_EVENT:
                        handleDeviceEvent(payload);
                        break;
                    case DEVICE_STATUS:
                        handleDeviceStatus(payload);
                        break;
                    case SCENARIO_LOG:
                        handleScenarioLog(payload);
                        break;
                    case DEVICE_REGISTER_STATUS:
                        handleDeviceRegisterStatus(payload);
                        break;
                    case SCENARIO_IMPORT_RESULT:
                        handleScenarioImportResult(payload);
                        break;
                    case SUB_SYSTEM_DATA_EVENT:
                        handleSubSystemDataEvent(payload);
                        break;
                    default:
                        // Should not happen if ofType handles all cases or returns null for unknown
                        log.warn("[handleNotifications] 未覆盖的已知通知类型: {}", notificationType);
                }
            } catch (Exception e) {
                log.error("[handleNotifications] 处理通知类型 {} 失败, payload: {}",
                        notificationType, JsonUtils.toJsonString(payload), e);
                // TODO: 可以考虑将失败的事件存入死信队列或进行其他错误处理
            }
        }
    }

    // 以下为各种事件类型的具体处理方法，目前仅打印日志，后续根据业务需求实现

    private void handleDeviceEvent(JsonNode payload) {
        // TODO: 解析 payload 并实现具体业务逻辑
        // 例如: payload 是一个JSON数组，每个元素代表一个设备的事件
        // List<DeviceDataChangeEvent> deviceDataChanges = JsonUtils.parseArray(payload.toString(), DeviceDataChangeEvent.class);
        log.info("[handleDeviceEvent] 接收到设备数据变更通知: {}", JsonUtils.toJsonString(payload));
    }

    private void handleDeviceStatus(JsonNode payload) {
        // TODO: 解析 payload 并实现具体业务逻辑
        log.info("[handleDeviceStatus] 接收到设备状态变更通知: {}", JsonUtils.toJsonString(payload));
    }

    private void handleScenarioLog(JsonNode payload) {
        // TODO: 解析 payload 并实现具体业务逻辑
        log.info("[handleScenarioLog] 接收到场景执行日志通知: {}", JsonUtils.toJsonString(payload));
    }

    private void handleDeviceRegisterStatus(JsonNode payload) {
        // TODO: 解析 payload 并实现具体业务逻辑
        log.info("[handleDeviceRegisterStatus] 接收到设备注册状态变更通知: {}", JsonUtils.toJsonString(payload));
    }

    private void handleScenarioImportResult(JsonNode payload) {
        // TODO: 解析 payload 并实现具体业务逻辑
        log.info("[handleScenarioImportResult] 接收到场景导入结果通知: {}", JsonUtils.toJsonString(payload));
    }

    private void handleSubSystemDataEvent(JsonNode payload) {
        // TODO: 解析 payload 并实现具体业务逻辑
        log.info("[handleSubSystemDataEvent] 接收到子系统数据变更通知: {}", JsonUtils.toJsonString(payload));
    }
}
