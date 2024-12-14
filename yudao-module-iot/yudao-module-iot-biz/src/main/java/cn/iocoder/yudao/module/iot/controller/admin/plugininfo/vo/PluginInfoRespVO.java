package cn.iocoder.yudao.module.iot.controller.admin.plugininfo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - IoT 插件信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PluginInfoRespVO {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11546")
    @ExcelProperty("主键ID")
    private Long id;

    @Schema(description = "插件包id", requiredMode = Schema.RequiredMode.REQUIRED, example = "24627")
    @ExcelProperty("插件包id")
    private String pluginId;

    @Schema(description = "插件名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("插件名称")
    private String name;

    @Schema(description = "描述", example = "你猜")
    @ExcelProperty("描述")
    private String description;

    @Schema(description = "部署方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("部署方式")
    private Integer deployType;

    @Schema(description = "插件包文件名", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("插件包文件名")
    private String file;

    @Schema(description = "插件版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("插件版本")
    private String version;

    @Schema(description = "插件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("插件类型")
    private Integer type;

    @Schema(description = "设备插件协议类型")
    @ExcelProperty("设备插件协议类型")
    private String protocol;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "插件配置项描述信息")
    @ExcelProperty("插件配置项描述信息")
    private String configSchema;

    @Schema(description = "插件配置信息")
    @ExcelProperty("插件配置信息")
    private String config;

    @Schema(description = "插件脚本")
    @ExcelProperty("插件脚本")
    private String script;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}