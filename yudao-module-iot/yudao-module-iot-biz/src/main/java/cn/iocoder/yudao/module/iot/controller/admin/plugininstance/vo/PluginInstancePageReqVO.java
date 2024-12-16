package cn.iocoder.yudao.module.iot.controller.admin.plugininstance.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

// TODO @haohao：建议搞个 plugin，然后里面分 info 和 instance。另外，是不是 info => config 会好点，插件配置？
@Schema(description = "管理后台 - IoT 插件实例分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PluginInstancePageReqVO extends PageParam {

    @Schema(description = "插件主程序id", example = "23738")
    private String mainId;

    @Schema(description = "插件id", example = "26498")
    private Long pluginId;

    @Schema(description = "插件主程序所在ip")
    private String ip;

    @Schema(description = "插件主程序端口")
    private Integer port;

    @Schema(description = "心跳时间，心路时间超过30秒需要剔除")
    private Long heartbeatAt;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}