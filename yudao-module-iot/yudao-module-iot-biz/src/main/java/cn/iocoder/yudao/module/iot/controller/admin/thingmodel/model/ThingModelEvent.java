package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model;

import cn.iocoder.yudao.module.iot.enums.thingmodel.IotProductThingModelServiceEventTypeEnum;
import lombok.Data;

import java.util.List;

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
     * 事件描述
     */
    // TODO @puhui999: 考虑移除
    private String description;
    /**
     * 是否是标准品类的必选事件。
     *
     * - true：是
     * - false：否
     */
    private Boolean required;
    /**
     * 事件类型
     *
     * 关联枚举 {@link IotProductThingModelServiceEventTypeEnum}
     */
    private String type;
    /**
     * 事件的输出参数
     *
     * 输出参数定义事件调用后返回的结果或反馈信息，用于确认操作结果或提供额外的信息。
     */
    private List<ThingModelInputOutputParam> outputParams;
    /**
     * 标识设备需要执行的具体操作
     */
    private String method;

}
