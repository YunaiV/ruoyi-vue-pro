package cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - IoT OPC UA Server 配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IotOpcUaServerConfigPageReqVO extends PageParam {

    @Schema(description = "配置名称（模糊匹配）", example = "车间")
    private String name;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
