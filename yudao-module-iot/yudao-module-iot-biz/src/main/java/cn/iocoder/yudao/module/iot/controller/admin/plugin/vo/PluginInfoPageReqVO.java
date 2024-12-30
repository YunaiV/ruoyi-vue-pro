package cn.iocoder.yudao.module.iot.controller.admin.plugin.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 插件信息分页 Request VO")
@Data
public class PluginInfoPageReqVO extends PageParam {

    @Schema(description = "插件名称", example = "http")
    private String name;

    @Schema(description = "状态")
    private Integer status;

}