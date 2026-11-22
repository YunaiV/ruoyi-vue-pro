package cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.core.enums.opcua.IotOpcUaCollectModeEnum;
import cn.iocoder.yudao.module.iot.core.util.IotOpcUaUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import lombok.Data;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备 OPC UA 点位配置新增/修改 Request VO")
@Data
public class IotDeviceOpcUaPointSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "设备编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "设备编号不能为空")
    private Long deviceId;

    @Schema(description = "物模型属性编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @NotNull(message = "物模型属性编号不能为空")
    private Long thingModelId;

    @Schema(description = "NodeId（推荐 nsu= 写法）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "nsu=urn:example;s=Temperature")
    @NotEmpty(message = "NodeId 不能为空")
    @Size(max = IotOpcUaUtils.NODE_ID_MAX_LENGTH, message = "NodeId 长度不能超过 512 个字符")
    private String nodeId;

    @Schema(description = "采集方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "采集方式不能为空")
    @InEnum(IotOpcUaCollectModeEnum.class)
    private Integer collectMode;

    @Schema(description = "采集间隔（毫秒）", requiredMode = Schema.RequiredMode.REQUIRED, example = "5000")
    @NotNull(message = "采集间隔不能为空")
    @Min(value = 100, message = "采集间隔不能小于 100 毫秒")
    @Max(value = 86400000, message = "采集间隔不能大于 86400000 毫秒")
    private Integer collectInterval;

    @Schema(description = "是否可写（默认 false；物模型属性需要为读写）", example = "false")
    private Boolean writable;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

}
