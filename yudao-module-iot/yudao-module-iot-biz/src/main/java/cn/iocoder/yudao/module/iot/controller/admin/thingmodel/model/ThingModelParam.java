package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model;

import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model.dataType.ThingModelDataSpecs;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelParamDirectionEnum;
import lombok.Data;

import java.util.List;

/**
 * IOT 产品物模型中的参数
 *
 * @author HUIHUI
 */
@Data
public class ThingModelParam {

    /**
     * 参数标识符
     */
    private String identifier;
    /**
     * 参数名称
     */
    private String name;
    /**
     * 用于区分输入或输出参数
     *
     * 枚举 {@link IotThingModelParamDirectionEnum}
     */
    private String direction;
    /**
     * 参数的序号。从 0 开始排序，且不能重复。
     *
     * TODO 考虑要不要序号，感觉是要的, 先留一手看看
     */
    private Integer paraOrder;
    /**
     * 参数值的数据类型，与 dataSpecs 的 dataType 保持一致
     */
    private String dataType;
    /**
     * 参数值的数据类型（dataType）为非列表型（int、float、double、text、date、array）的数据规范存储在 dataSpecs 中
     */
    private ThingModelDataSpecs dataSpecs;
    /**
     * 参数值的数据类型（dataType）为列表型（enum、bool、struct）的数据规范存储在 dataSpecsList 中
     */
    private List<ThingModelDataSpecs> dataSpecsList;

}
