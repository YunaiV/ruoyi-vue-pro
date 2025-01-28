package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model;

import cn.iocoder.yudao.module.iot.enums.thingmodel.IotThingModelServiceEventTypeEnum;
import lombok.Data;

import java.util.List;

// TODO @puhui999：必要的参数校验
/**
 * 物模型中的事件
 *
 * @author HUIHUI
 */
@Data
public class ThingModelEvent {

    /**
     * 事件标识符
     */
    private String identifier;
    /**
     * 事件名称
     */
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
    private String type;
    /**
     * 事件的输出参数
     *
     * 输出参数定义事件调用后返回的结果或反馈信息，用于确认操作结果或提供额外的信息。
     */
    private List<ThingModelParam> outputParams;
    /**
     * 标识设备需要执行的具体操作
     */
    private String method;

}
