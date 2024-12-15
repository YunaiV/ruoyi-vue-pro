package cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.plugin.IotPluginTypeEnum;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// TODO @haohao：只查询必要字段哈
@Schema(description = "管理后台 - IoT 插件信息分页 Request VO")
@Data
public class PluginInfoPageReqVO extends PageParam {

    @Schema(description = "插件包 ID ", example = "24627")
    private String pluginId;

    @Schema(description = "插件名称", example = "赵六")
    private String name;

    @Schema(description = "描述", example = "你猜")
    private String description;

    @Schema(description = "部署方式", example = "2")
    private Integer deployType;

    @Schema(description = "插件包文件名")
    private String file;

    @Schema(description = "插件版本")
    private String version;

    @Schema(description = "插件类型", example = "2")
    @InEnum(IotPluginTypeEnum.class)
    private Integer type;

    @Schema(description = "设备插件协议类型")
    private String protocol;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "插件配置项描述信息")
    private String configSchema;

    @Schema(description = "插件配置信息")
    private String config;

    @Schema(description = "插件脚本")
    private String script;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}