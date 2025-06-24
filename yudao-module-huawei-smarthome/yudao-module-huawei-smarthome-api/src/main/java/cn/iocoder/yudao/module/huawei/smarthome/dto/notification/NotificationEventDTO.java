package cn.iocoder.yudao.module.huawei.smarthome.dto.notification;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@ApiModel("华为智能家居 - 统一事件通知 DTO (单个事件)")
@Data
public class NotificationEventDTO {

    @ApiModelProperty(value = "通知类型", required = true, example = "deviceEvent",
            notes = "枚举值：deviceEvent, deviceStatus, scenarioLog, deviceRegisterStatus, scenarioImportResult, subSystemDataEvent")
    @NotEmpty(message = "通知类型不能为空")
    private String dataType;

    @ApiModelProperty(value = "通知消息体 (JSON字符串或已解析的JSON对象)", required = true)
    @NotNull(message = "通知消息体不能为空")
    private JsonNode payload; // 使用JsonNode以灵活处理不同类型的payload. 华为文档中payload是一个字符串，但其内容是JSON数组。
    // 为了方便controller直接接收，这里定义为JsonNode，然后在service中处理。
    // 或者，更符合华为文档的定义是 `private String payload;` 然后在Service层用JsonUtils.parseArray(payload, SpecificEventPayload.class)
    // 考虑到华为的示例 `POST https://xxxx [ { "dataType": "deviceEvent", "payload":"…" }, ... ]`
    // 这表明外部系统发送的是一个JSON数组，数组的每个元素是 NotificationEventDTO 的结构。
    // 因此，Controller应该接收 List<NotificationEventDTO>。
    // NotificationReceiveReqDTO 应该被移除或修改。我们直接让Controller接收 List<NotificationEventDTO>。
}
