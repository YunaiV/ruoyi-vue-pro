package cn.iocoder.yudao.module.iot.controller.admin.plugin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - IoT 插件实例 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PluginInstanceRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "23864")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "插件主程序id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23738")
    @ExcelProperty("插件主程序id")
    private String mainId;

    @Schema(description = "插件id", requiredMode = Schema.RequiredMode.REQUIRED, example = "26498")
    @ExcelProperty("插件id")
    private Long pluginId;

    @Schema(description = "插件主程序所在ip", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("插件主程序所在ip")
    private String ip;

    @Schema(description = "插件主程序端口", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("插件主程序端口")
    private Integer port;

    @Schema(description = "心跳时间，心路时间超过30秒需要剔除", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("心跳时间，心路时间超过30秒需要剔除")
    private Long heartbeatAt;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}