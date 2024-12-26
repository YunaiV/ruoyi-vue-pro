package cn.iocoder.yudao.module.iot.controller.admin.thingmodel.model;

import cn.iocoder.yudao.module.iot.enums.thingmodel.IotProductThingModelServiceCallTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * 物模型中的服务
 *
 * @author HUIHUI
 */
@Data
public class ThingModelService {

    /**
     * 服务标识符
     */
    private String identifier;
    /**
     * 服务名称
     */
    private String name;
    /**
     * 是否是标准品类的必选服务。
     *
     * - true：是
     * - false：否
     */
    private Boolean required;
    /**
     * 调用类型
     *
     * 关联枚举 {@link IotProductThingModelServiceCallTypeEnum}
     */
    private String callType;
    /**
     * 服务的输入参数
     *
     * 输入参数定义服务调用时所需提供的信息，用于控制设备行为或执行特定任务
     */
    private List<ThingModelInputOutputParam> inputParams;
    /**
     * 服务的输出参数
     *
     * 输出参数定义服务调用后返回的结果或反馈信息，用于确认操作结果或提供额外的信息。
     */
    private List<ThingModelInputOutputParam> outputParams;
    /**
     * 标识设备需要执行的具体操作
     */
    private String method;

}
