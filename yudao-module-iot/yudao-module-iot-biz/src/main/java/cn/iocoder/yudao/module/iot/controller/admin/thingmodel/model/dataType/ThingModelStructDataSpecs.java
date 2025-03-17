package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType;

import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelAccessModeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    /**
     * 属性标识符
     */
    private String identifier;
    /**
     * 属性名称
     */
    private String name;
    /**
     * 云端可以对该属性进行的操作类型
     *
     * 枚举 {@link IotThingModelAccessModeEnum}
     */
    private String accessMode;
    /**
     * 是否是标准品类的必选服务
     */
    private Boolean required;
    /**
     * struct 数据的数据类型
     */
    private String childDataType;
    /**
     * 数据类型（dataType）为非列表型（int、float、double、text、date、array）的数据规范存储在 dataSpecs 中
     */
    private ThingModelDataSpecs dataSpecs;
    /**
     * 数据类型（dataType）为列表型（enum、bool、struct）的数据规范存储在 dataSpecsList 中
     */
    private List<ThingModelDataSpecs> dataSpecsList;

}

