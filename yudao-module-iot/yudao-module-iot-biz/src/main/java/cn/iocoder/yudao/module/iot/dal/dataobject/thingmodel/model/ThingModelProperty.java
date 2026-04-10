package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDataSpecs;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelAccessModeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * IoT 物模型中的属性
 *
 * dataSpecs 和 dataSpecsList 之中必须传入且只能传入一个
 *
 * @author HUIHUI
 */
@Data
public class ThingModelProperty {

    /**
     * 属性标识符
     */
    @NotEmpty(message = "属性标识符不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,31}$", message = "属性标识符只能由字母、数字和下划线组成，必须以字母开头，长度不超过 32 个字符")
    private String identifier;
    /**
     * 属性名称
     */
    @NotEmpty(message = "属性名称不能为空")
    private String name;
    /**
     * 云端可以对该属性进行的操作类型
     *
     * 枚举 {@link IotThingModelAccessModeEnum}
     */
    @NotEmpty(message = "操作类型不能为空")
    @InEnum(IotThingModelAccessModeEnum.class)
    private String accessMode;
    /**
     * 是否是标准品类的必选服务
     */
    private Boolean required;
    /**
     * 参数值的数据类型，与 dataSpecs 的 dataType 保持一致
     *
     * 枚举 {@link IotDataSpecsDataTypeEnum}
     */
    @NotEmpty(message = "数据类型不能为空")
    @InEnum(IotDataSpecsDataTypeEnum.class)
    private String dataType;
    /**
     * 数据类型（dataType）为非列表型（int、float、double、text、date、array）的数据规范存储在 dataSpecs 中
     */
    private ThingModelDataSpecs dataSpecs;
    /**
     * 数据类型（dataType）为列表型（enum、bool、struct）的数据规范存储在 dataSpecsList 中
     */
    private List<ThingModelDataSpecs> dataSpecsList;

}
