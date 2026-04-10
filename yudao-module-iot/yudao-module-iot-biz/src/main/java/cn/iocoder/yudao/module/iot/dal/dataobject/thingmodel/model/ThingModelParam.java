package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model.dataType.ThingModelDataSpecs;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotDataSpecsDataTypeEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelParamDirectionEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * IoT 产品物模型中的参数
 *
 * @author HUIHUI
 */
@Data
public class ThingModelParam {

    /**
     * 参数标识符
     */
    @NotEmpty(message = "参数标识符不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,31}$", message = "参数标识符只能由字母、数字和下划线组成，必须以字母开头，长度不超过 32 个字符")
    private String identifier;
    /**
     * 参数名称
     */
    @NotEmpty(message = "参数名称不能为空")
    private String name;
    /**
     * 用于区分输入或输出参数
     *
     * 枚举 {@link IotThingModelParamDirectionEnum}
     */
    @NotEmpty(message = "参数方向不能为空")
    @InEnum(IotThingModelParamDirectionEnum.class)
    private String direction;
    /**
     * 参数的序号。从 0 开始排序，且不能重复。
     *
     * TODO 考虑要不要序号，感觉是要的, 先留一手看看
     */
    private Integer paraOrder;
    /**
     * 参数值的数据类型，与 dataSpecs 的 dataType 保持一致
     *
     * 枚举 {@link IotDataSpecsDataTypeEnum}
     */
    @NotEmpty(message = "数据类型不能为空")
    @InEnum(IotDataSpecsDataTypeEnum.class)
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
