package cn.iocoder.yudao.module.huawei.smarthome.controller.app.event;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.huawei.smarthome.dto.notification.NotificationEventDTO;
import cn.iocoder.yudao.module.huawei.smarthome.service.event.HuaweiNotificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "用户APP - 华为智能家居 - 事件通知接收")
@RestController
@RequestMapping("/huawei/smarthome/notification") // 定义一个基础路径，符合ISV提供给华为的URL
@Validated
@Slf4j
public class AppHuaweiNotificationController {

    @Resource
    private HuaweiNotificationService huaweiNotificationService;

    /**
     * 华为全屋智能平台将调用此接口将变更通知给三方云（即本项目）
     * URI: https://xxxx (ISV提供) - 此处的 xxxx 对应到部署后的 /huawei/smarthome/notification/receive
     * 请求方法: POST
     *
     * 根据华为文档2.7.1，请求体是一个JSON数组，每个元素包含dataType和payload。
     * [
     *   { "dataType": "deviceEvent", "payload":"…" },
     *   { "dataType": "deviceStatus", "payload":"…" }
     * ]
     */
    @PostMapping("/receive")
    @ApiOperation("接收华为智能家居平台的统一事件通知")
    public CommonResult<?> receiveNotifications(@Valid @RequestBody List<NotificationEventDTO> events) {
        // 华为平台期望的成功响应是 HTTP 200 OK，没有特定的响应体要求。
        // 如果处理失败，可以返回相应的HTTP错误码，但要注意华为平台的重试机制和错误处理逻辑。
        // 文档中错误码部分是针对调用华为API的，对于华为调用ISV接口，ISV应尽量返回200 OK。
        // 具体的错误处理（如日志、告警、数据修正）应在ISV内部完成。

        log.info("[receiveNotifications] 接收到华为事件通知，数量: {}", events.size());
        try {
            huaweiNotificationService.handleNotifications(events);
            return success(null); // 返回HTTP 200 OK
        } catch (Exception e) {
            // 即使内部处理失败，也尽量返回200 OK给华为平台，避免华为平台频繁重试。
            // 错误应在内部记录和处理。
            log.error("[receiveNotifications] 处理华为事件通知时发生内部错误", e);
            // 可以根据实际情况决定是否要向上抛出或返回特定错误给华为（但不推荐，除非华为文档有此要求）
            // return CommonResult.error(500, "内部处理错误: " + e.getMessage());
            return success(null); // 仍然返回成功，表示已接收，内部处理问题
        }
    }
}
