package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * IoT 物模型数据类型为数组的 DataSpec 定义
 *
 * @author HUIHUI
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"dataType"}) // 忽略子类中的 dataType 字段，从而避免重复
public class ThingModelArrayDataSpecs extends ThingModelDataSpecs {

    @NotNull(message = "数组元素个数不能为空")
    private Integer size;

    @NotEmpty(message = "数组元素的数据类型不能为空")
    @Pattern(regexp = "^(struct|int|float|double|text)$", message = "数组元素的数据类型必须为：struct、int、float、double 或 text")
    private String childDataType;
    /**
     * 数据类型（childDataType）为列表型 struct 的数据规范存储在 dataSpecsList 中
     * 此时 struct 取值范围为：int、float、double、text、date、enum、bool
     */
    @Valid
    private List<ThingModelDataSpecs> dataSpecsList;

}

