package cn.iocoder.yudao.module.iot.controller.admin.plugin.vo.config;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - IoT 插件配置状态 Request VO")
@Data
public class PluginConfigStatusReqVO {

    @Schema(description = "主键编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11546")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @InEnum(IotPluginStatusEnum.class)
    private Integer status;

}