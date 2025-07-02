package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IoT 物模型数据类型为时间型或文本型的 DataSpec 定义
 *
 * 数据类型，取值为 date 或 text
 *
 * @author HUIHUI
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"dataType"}) // 忽略子类中的 dataType 字段，从而避免重复
public class ThingModelDateOrTextDataSpecs extends ThingModelDataSpecs {

    /**
     * 数据长度，单位为字节。取值不能超过 2048
     *
     * 当 dataType 为 text 时，需传入该参数
     */
    @Max(value = 2048, message = "数据长度不能超过 2048")
    private Integer length;
    /**
     * 默认值，可选参数，用于存储默认值
     */
    private String defaultValue;

}

