package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IoT 物模型数据类型为布尔型或枚举型的 DataSpec 定义
 *
 * 数据类型，取值为 bool 或 enum。
 *
 * @author HUIHUI
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"dataType"}) // 忽略子类中的 dataType 字段，从而避免重复
public class ThingModelBoolOrEnumDataSpecs extends ThingModelDataSpecs {

    // TODO @puhui999：要不写下参数校验？这样，注释可以简洁一点
    /**
     * 枚举项的名称。
     * 可包含中文、大小写英文字母、数字、下划线（_）和短划线（-）
     * 必须以中文、英文字母或数字开头，长度不超过 20 个字符
     */
    private String name;
    /**
     * 枚举值。
     */
    private Integer value;

}
