package cn.iocoder.yudao.module.iot.controller.admin.productthingmodel.thingmodel.dataType;

import lombok.Data;

/**
 * 物模型数据类型为数值的 DataSpec 定义
 *
 * 数据类型，取值为 int、float 或 double。
 *
 * @author HUIHUI
 */
@Data
public class ThingModelNumericDataSpec extends ThingModelDataSpecs {

    /**
     * 最大值，需转为字符串类型。值必须与 dataType 类型一致。
     * 例如，当 dataType 为 int 时，取值为 "200"，而不是 200。
     */
    private String max;
    /**
     * 最小值，需转为字符串类型。值必须与 dataType 类型一致。
     * 例如，当 dataType 为 int 时，取值为 "0"，而不是 0。
     */
    private String min;
    /**
     * 步长，需转为字符串类型。值必须与 dataType 类型一致。
     * 例如，当 dataType 为 int 时，取值为 "10"，而不是 10。
     */
    private String step;
    /**
     * 精度。当 dataType 为 float 或 double 时可选传入。
     */
    private String precise;
    /**
     * 默认值，可传入用于存储的默认值。
     */
    private String defaultValue;
    /**
     * 单位的符号。
     */
    private String unit;
    /**
     * 单位的名称。
     */
    private String unitName;

}
