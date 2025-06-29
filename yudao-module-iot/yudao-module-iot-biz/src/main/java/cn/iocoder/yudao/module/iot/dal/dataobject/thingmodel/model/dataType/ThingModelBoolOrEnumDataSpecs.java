package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * IoT 物模型数据类型为布尔型或枚举型的 DataSpec 定义
 *
 * 数据类型，取值为 bool 或 enum
 *
 * @author HUIHUI
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"dataType"}) // 忽略子类中的 dataType 字段，从而避免重复
public class ThingModelBoolOrEnumDataSpecs extends ThingModelDataSpecs {

    @NotEmpty(message = "枚举项的名称不能为空")
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z0-9][\\u4e00-\\u9fa5a-zA-Z0-9_-]{0,19}$",
            message = "枚举项的名称只能包含中文、大小写英文字母、数字、下划线和短划线，必须以中文、英文字母或数字开头，长度不超过 20 个字符")
    private String name;

    @NotNull(message = "枚举值不能为空")
    private Integer value;

}
