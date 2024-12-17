package cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.model.dataType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * 抽象类 ThinkModelDataSpecs
 *
 * 用于表示物模型数据的通用类型，根据具体的 "dataType" 字段动态映射到对应的子类。
 * 提供多态支持，适用于不同类型的数据结构序列化和反序列化场景。
 *
 * @author HUIHUI
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "dataType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ThinkModelNumericDataSpec.class, name = "int"),
        @JsonSubTypes.Type(value = ThinkModelNumericDataSpec.class, name = "float"),
        @JsonSubTypes.Type(value = ThinkModelNumericDataSpec.class, name = "double"),
        @JsonSubTypes.Type(value = ThinkModelDateOrTextDataSpecs.class, name = "text"),
        @JsonSubTypes.Type(value = ThinkModelDateOrTextDataSpecs.class, name = "date"),
        @JsonSubTypes.Type(value = ThinkModelBoolOrEnumDataSpecs.class, name = "bool"),
        @JsonSubTypes.Type(value = ThinkModelBoolOrEnumDataSpecs.class, name = "enum"),
        @JsonSubTypes.Type(value = ThinkModelArrayDataSpecs.class, name = "array"),
        @JsonSubTypes.Type(value = ThinkModelStructDataSpecs.class, name = "struct")
})
public abstract class ThinkModelDataSpecs {

    /**
     * 数据类型
     */
    private String dataType;

}
