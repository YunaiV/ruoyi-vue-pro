package cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge;

import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - IoT 数据桥梁 Response VO")
@Data
@ExcelIgnoreUnannotated
public class IotDataBridgeRespVO {

    @Schema(description = "桥梁编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18564")
    @ExcelProperty("桥梁编号")
    private Long id;

    @Schema(description = "桥梁名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @ExcelProperty("桥梁名称")
    private String name;

    @Schema(description = "桥梁描述", example = "随便")
    @ExcelProperty("桥梁描述")
    private String description;

    @Schema(description = "桥梁状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "桥梁状态", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "桥梁方向", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "桥梁方向", converter = DictConvert.class)
    @DictFormat("iot_data_bridg_direction_enum") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer direction;

    @Schema(description = "桥梁类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty(value = "桥梁类型", converter = DictConvert.class)
    @DictFormat("iot_data_bridg_type_enum") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer type;

    @Schema(description = "桥梁配置")
    @ExcelProperty("桥梁配置")
    private String config;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}