package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelAccessModeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * IoT 物模型数据类型为 struct 的 DataSpec 定义
 *
 * @author HUIHUI
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"dataType"}) // 忽略子类中的 dataType 字段，从而避免重复
public class ThingModelStructDataSpecs extends ThingModelDataSpecs {

    @NotEmpty(message = "属性标识符不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,31}$", message = "属性标识符只能由字母、数字和下划线组成，必须以字母开头，长度不超过 32 个字符")
    private String identifier;

    @NotEmpty(message = "属性名称不能为空")
    private String name;

    @NotEmpty(message = "操作类型不能为空")
    @InEnum(IotThingModelAccessModeEnum.class)
    private String accessMode;

    /**
     * 是否是标准品类的必选服务
     */
    private Boolean required;

    @NotEmpty(message = "数据类型不能为空")
    @Pattern(regexp = "^(int|float|double|text|date|enum|bool)$", message = "数据类型必须为：int、float、double、text、date、enum、bool")
    private String childDataType;

    /**
     * 数据类型（dataType）为非列表型（int、float、double、text、date、array）的数据规范存储在 dataSpecs 中
     */
    @Valid
    private ThingModelDataSpecs dataSpecs;

    /**
     * 数据类型（dataType）为列表型（enum、bool、struct）的数据规范存储在 dataSpecsList 中
     */
    @Valid
    private List<ThingModelDataSpecs> dataSpecsList;

}

