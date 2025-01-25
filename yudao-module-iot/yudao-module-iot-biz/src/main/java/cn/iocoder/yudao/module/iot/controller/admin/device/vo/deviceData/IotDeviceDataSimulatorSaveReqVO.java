package cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

// TODO super： SaveReqVO => ReqVO
@Schema(description = "管理后台 - IoT 模拟设备数据 Request VO")
@Data
public class IotDeviceDataSimulatorSaveReqVO {

    // TODO @super：感觉后端随机更合适？
    @Schema(description = "消息 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "msg123")
    private String id;

    // TODO @super：不用传递 productKey，因为 deviceKey 可以推导出来
    // TODO 讨论: 日志记录的时候要记录一下productKey，目前是前端已经有productKey了，所以前端传入，如果不传入的话，后端要根据deviceKey查询productKey，感觉直传是不是效率高一些
    @Schema(description = "产品标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "product123")
    @NotEmpty(message = "产品标识不能为空")
    private String productKey;

    @Schema(description = "设备标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "device123")
    @NotEmpty(message = "设备标识不能为空")
    private String deviceKey;

    // TODO @super：type、subType，是不是不用传递，因为模拟只有属性？？？
    // TODO 讨论: 不只模拟属性
    @Schema(description = "消息/日志类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "property")
    @NotEmpty(message = "消息类型不能为空")
    private String type;

    @Schema(description = "标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "temperature")
    @NotEmpty(message = "标识符不能为空")
    private String subType;

    @Schema(description = "数据内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "{\"value\": 25.6}")
    @NotEmpty(message = "数据内容不能为空")
    private String content;

    // TODO @芋艿：需要讨论下，reportTime 到底以那个为准！
    @Schema(description = "上报时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long reportTime;

}
