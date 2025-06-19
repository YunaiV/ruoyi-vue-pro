package cn.iocoder.yudao.module.iot.dal.dataobject.device;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IoT 设备消息数据 DO
 *
 * 目前使用 TDengine 存储
 *
 * @author alwayssuper
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotDeviceMessageDO {

    /**
     * 消息编号
     */
    private String id;
    /**
     * 上报时间戳
     */
    private Long reportTime;
    /**
     * 存储时间戳
     */
    private Long ts;

    /**
     * 设备编号
     *
     * 关联 {@link IotDeviceDO#getId()}
     */
    private Long deviceId;
    /**
     * 租户编号
     */
    private Long tenantId;

    /**
     * 服务编号，该消息由哪个 server 发送
     */
    private String serverId;

    /**
     * 是否上行消息
     *
     * 由 {@link cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils#isUpstreamMessage(IotDeviceMessage)} 计算。
     * 计算并存储的目的：方便计算多少条上行、多少条下行
     */
    private Boolean upstream;
    /**
     * 是否回复消息
     *
     * 由 {@link cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils#isReplyMessage(IotDeviceMessage)} 计算。
     * 计算并存储的目的：方便计算多少条请求、多少条回复
     */
    private Boolean reply;
    /**
     * 标识符
     *
     * 例如说：{@link IotThingModelDO#getIdentifier()}
     * 目前，只有事件上报、服务调用才有！！！
     */
    private String identifier;

    // ========== codec（编解码）字段 ==========

    /**
     * 请求编号
     *
     * 由设备生成，对应阿里云 IoT 的 Alink 协议中的 id、华为云 IoTDA 协议的 request_id
     */
    private String requestId;
    /**
     * 请求方法
     *
     * 枚举 {@link IotDeviceMessageMethodEnum}
     * 例如说：thing.property.report 属性上报
     */
    private String method;
    /**
     * 请求参数
     *
     * 例如说：属性上报的 properties、事件上报的 params
     */
    private Object params;
    /**
     * 响应结果
     */
    private Object data;
    /**
     * 响应错误码
     */
    private Integer code;
    /**
     * 响应提示
     */
    private String msg;

}
