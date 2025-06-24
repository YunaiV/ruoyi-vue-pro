package cn.iocoder.yudao.module.huawei.smarthome.service.event;

import cn.iocoder.yudao.module.huawei.smarthome.dto.notification.NotificationEventDTO;

import javax.validation.Valid;
import java.util.List;

/**
 * 华为智能家居 - 事件通知处理 Service 接口
 *
 * @author Jules
 */
public interface HuaweiNotificationService {

    /**
     * 处理接收到的华为智能家居事件通知
     *
     * @param events 事件通知列表
     */
    void handleNotifications(@Valid List<NotificationEventDTO> events);

}
