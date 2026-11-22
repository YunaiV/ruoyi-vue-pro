package cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT OPC UA Server 节点分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotOpcUaServerNodePageReqVO extends PageParam {

    @Schema(description = "配置编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "配置编号不能为空")
    private Long configId;

    @Schema(description = "设备编号", example = "1024")
    private Long deviceId;

    @Schema(description = "浏览名称（模糊匹配）", example = "temperature")
    private String browseName;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
