package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * IoT ThingModelDataSpecs 抽象类
 *
 * 用于表示物模型数据的通用类型，根据具体的 "dataType" 字段动态映射到对应的子类
 * 提供多态支持，适用于不同类型的数据结构序列化和反序列化场景
 *
 * @author HUIHUI
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "dataType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ThingModelNumericDataSpec.class, name = "int"),
        @JsonSubTypes.Type(value = ThingModelNumericDataSpec.class, name = "float"),
        @JsonSubTypes.Type(value = ThingModelNumericDataSpec.class, name = "double"),
        @JsonSubTypes.Type(value = ThingModelDateOrTextDataSpecs.class, name = "text"),
        @JsonSubTypes.Type(value = ThingModelDateOrTextDataSpecs.class, name = "date"),
        @JsonSubTypes.Type(value = ThingModelBoolOrEnumDataSpecs.class, name = "bool"),
        @JsonSubTypes.Type(value = ThingModelBoolOrEnumDataSpecs.class, name = "enum"),
        @JsonSubTypes.Type(value = ThingModelArrayDataSpecs.class, name = "array"),
        @JsonSubTypes.Type(value = ThingModelStructDataSpecs.class, name = "struct")
})
public abstract class ThingModelDataSpecs {

    /**
     * 数据类型
     */
    private String dataType;

}
