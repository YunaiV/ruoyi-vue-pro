package cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.model;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelServiceEventTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

// TODO @puhui999：感觉这个，是不是放到 dal 里会好点？（讨论下，先不改哈）
/**
 * IoT 物模型中的事件
 *
 * @author HUIHUI
 */
@Data
public class ThingModelEvent {

    /**
     * 事件标识符
     */
    @NotEmpty(message = "事件标识符不能为空")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]{0,31}$", message = "事件标识符只能由字母、数字和下划线组成，必须以字母开头，长度不超过 32 个字符")
    private String identifier;
    /**
     * 事件名称
     */
    @NotEmpty(message = "事件名称不能为空")
    private String name;
    /**
     * 是否是标准品类的必选事件
     */
    private Boolean required;
    /**
     * 事件类型
     *
     * 枚举 {@link IotThingModelServiceEventTypeEnum}
     */
    @NotEmpty(message = "事件类型不能为空")
    @InEnum(IotThingModelServiceEventTypeEnum.class)
    private String type;
    /**
     * 事件的输出参数
     *
     * 输出参数定义事件调用后返回的结果或反馈信息，用于确认操作结果或提供额外的信息。
     */
    @Valid
    private List<ThingModelParam> outputParams;
    /**
     * 标识设备需要执行的具体操作
     */
    private String method;

}
