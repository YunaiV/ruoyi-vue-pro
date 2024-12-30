package cn.iocoder.yudao.module.iot.controller.admin.plugin.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 插件信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class PluginInfoRespVO {

    @Schema(description = "主键 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "11546")
    @ExcelProperty("主键 ID")
    private Long id;

    @Schema(description = "插件包标识符", requiredMode = Schema.RequiredMode.REQUIRED, example = "24627")
    @ExcelProperty("插件包标识符")
    private String pluginKey;

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
    private String fileName;

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