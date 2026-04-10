package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IoT 物模型数据类型为数值的 DataSpec 定义
 *
 * 数据类型，取值为 int、float 或 double
 *
 * @author HUIHUI
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"dataType"}) // 忽略子类中的 dataType 字段，从而避免重复
public class ThingModelNumericDataSpec extends ThingModelDataSpecs {

    /**
     * 最大值，需转为字符串类型。值必须与 dataType 类型一致
     */
    @NotEmpty(message = "最大值不能为空")
    @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "最大值必须为数值类型")
    private String max;
    /**
     * 最小值，需转为字符串类型。值必须与 dataType 类型一致
     */
    @NotEmpty(message = "最小值不能为空")
    @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "最小值必须为数值类型")
    private String min;
    /**
     * 步长，需转为字符串类型。值必须与 dataType 类型一致
     */
    @NotEmpty(message = "步长不能为空")
    @Pattern(regexp = "^-?\\d+(\\.\\d+)?$", message = "步长必须为数值类型")
    private String step;
    /**
     * 精度。当 dataType 为 float 或 double 时可选传入
     */
    private String precise;
    /**
     * 默认值，可传入用于存储的默认值
     */
    private String defaultValue;
    /**
     * 单位的符号
     */
    private String unit;
    /**
     * 单位的名称
     */
    private String unitName;

}
