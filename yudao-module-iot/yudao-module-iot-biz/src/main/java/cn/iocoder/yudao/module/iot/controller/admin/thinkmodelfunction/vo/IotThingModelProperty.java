package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - IoT 产品物模型属性")
@Data
public class IotThingModelProperty {

    @Schema(description = "属性标识符")
    private String identifier;

    @Schema(description = "属性名称")
    private String name;

    @Schema(description = "访问模式 (r/rw)")
    private String accessMode;

    @Schema(description = "是否必需")
    private boolean required;

    @Schema(description = "数据类型")
    private DataType dataType;

    @Schema(description = "数据类型")
    @Data
    public static class DataType {

        @Schema(description = "数据类型（float, double, struct, enum等）")
        private String type;

        @Schema(description = "单一类型的规格（适用于float, double等）")
        private Specs specs;

        @Schema(description = "结构体字段（适用于struct类型）")
        private List<StructField> structSpecs;

        @Schema(description = "规格")
        @Data
        public static class Specs {

            @Schema(description = "最小值")
            private String min;

            @Schema(description = "最大值")
            private String max;

            @Schema(description = "单位符号")
            private String unit;

            @Schema(description = "单位名称")
            private String unitName;

            @Schema(description = "步进值")
            private String step;
        }

        @Schema(description = "结构体字段")
        @Data
        public static class StructField {

            @Schema(description = "字段标识符")
            private String identifier;

            @Schema(description = "字段名称")
            private String name;

            @Schema(description = "字段的数据类型")
            private DataType dataType;
        }
    }

    @Schema(description = "枚举规格")
    @Data
    public static class EnumSpecs {

        @Schema(description = "枚举值")
        private int value;

        @Schema(description = "枚举名称")
        private String name;

        public EnumSpecs(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }
}
