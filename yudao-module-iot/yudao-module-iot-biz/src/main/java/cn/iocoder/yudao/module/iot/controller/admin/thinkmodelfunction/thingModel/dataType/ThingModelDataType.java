package cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.dataType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ThingModelIntType.class, name = "int"),
        @JsonSubTypes.Type(value = ThingModelFloatType.class, name = "float"),
        @JsonSubTypes.Type(value = ThingModelDoubleType.class, name = "double"),
        @JsonSubTypes.Type(value = ThingModelTextType.class, name = "text"),
        @JsonSubTypes.Type(value = ThingModelDateType.class, name = "date"),
        @JsonSubTypes.Type(value = ThingModelBoolType.class, name = "bool"),
        @JsonSubTypes.Type(value = ThingModelEnumType.class, name = "enum"),
        @JsonSubTypes.Type(value = ThingModelStructType.class, name = "struct"),
        @JsonSubTypes.Type(value = ThingModelArrayType.class, name = "array")
})
public abstract class ThingModelDataType {

    private String type;

}
