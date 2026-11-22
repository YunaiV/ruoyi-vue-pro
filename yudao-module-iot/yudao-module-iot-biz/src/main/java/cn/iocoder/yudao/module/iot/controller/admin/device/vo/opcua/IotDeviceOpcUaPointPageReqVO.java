package cn.iocoder.yudao.module.iot.controller.admin.device.vo.opcua;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author 芋道源码
 */
@Schema(description = "管理后台 - IoT 设备 OPC UA 点位配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotDeviceOpcUaPointPageReqVO extends PageParam {

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "NodeId（模糊匹配）", example = "Temperature")
    private String nodeId;

    @Schema(description = "采集方式", example = "1")
    private Integer collectMode;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
