package cn.iocoder.yudao.module.iot.gateway.service.device.message;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotSerializeTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptSource;
import cn.iocoder.yudao.module.iot.gateway.service.device.message.dto.IotDownstreamEncodeRespDTO;

/**
 * IoT 设备消息 Service 接口
 *
 * @author 芋道源码
 */
public interface IotDeviceMessageService {

    /**
     * 序列化消息
     *
     * @param message    消息
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 序列化后的消息内容
     */
    byte[] serializeDeviceMessage(IotDeviceMessage message,
                                  String productKey, String deviceName);

    /**
     * 序列化消息
     *
     * @param message       消息
     * @param serializeType 序列化类型
     * @return 序列化后的消息内容
     */
    byte[] serializeDeviceMessage(IotDeviceMessage message,
                                  IotSerializeTypeEnum serializeType);

    /**
     * 反序列化消息
     *
     * @param bytes      消息内容
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @return 反序列化后的消息内容
     */
    IotDeviceMessage deserializeDeviceMessage(byte[] bytes,
                                              String productKey, String deviceName);

    /**
     * 反序列化消息
     *
     * @param bytes         消息内容
     * @param serializeType 序列化类型
     * @return 反序列化后的消息内容
     */
    IotDeviceMessage deserializeDeviceMessage(byte[] bytes, IotSerializeTypeEnum serializeType);

    /**
     * 发送消息
     * <p>
     * 设备经标准路径上报的消息：不接受透传方法；编号、时间由服务端生成，raw 由服务端清空（只有透传上行才可信赋值）
     *
     * @param message    消息
     * @param productKey 产品 Key
     * @param deviceName 设备名称
     * @param serverId   设备连接的 serverId
     */
    void sendDeviceMessage(IotDeviceMessage message,
                           String productKey, String deviceName, String serverId);

    // DONE @AI：deadlineNanos 是不是收敛掉到 service 里？已收进来：协议只传入收到的时刻（入队之前取得），
    //  截止 = 收到时刻 + gateway.script.deadline，由本方法计算；协议不再读取截止配置
    /**
     * 发送透传（raw）上行：与阿里云一致，按产品的数据解析脚本 decode 为一条标准消息；只投递一条消息
     * <ul>
     *     <li>解析成功：投递解析出的标准消息，raw 字段带上原始字节；</li>
     *     <li>正常未解析（功能开关关闭、脚本停用 / 未配置、decode 返回 null）：
     *         投递一条 thing.raw.post，raw 字段带上原始字节；</li>
     *     <li>解析失败（已启用但内容为空、执行失败、超时、繁忙、输出不合法、超过截止）：不投递，只记错误日志，抛出对应的错误码。</li>
     * </ul>
     * 截止时间 = receivedNanos + gateway.script.deadline，覆盖排队、执行脚本，直到投递。
     * 会阻塞（加载设备信息、执行脚本、发送总线）：只能在 worker 线程调用，不能在 event loop 上调用
     *
     * @param payload       原始字节
     * @param productKey    产品 Key
     * @param deviceName    设备名称
     * @param serverId      设备连接的 serverId
     * @param protocol      协议类型
     * @param receivedNanos 协议收到透传上报的时刻（System.nanoTime）：在交给 worker 排队之前取得，排队的时间也计入截止
     * @return 投递的消息编号
     * @throws cn.iocoder.yudao.framework.common.exception.ServiceException 载荷长度不合法（不投递）；
     *         已启用但内容为空、脚本失败、超时、繁忙、输出不合法、超过截止（都不投递）
     */
    String sendRawMessage(byte[] payload, String productKey, String deviceName, String serverId,
                          IotProtocolTypeEnum protocol, long receivedNanos);

    // DONE @AI：deadlineNanos 是不是收敛掉到 service 里？已收进来：下行订阅者只传入收到指令的时刻（按设备排队之前取得），
    //  截止 = 收到时刻 + gateway.script.deadline，由本方法计算；订阅者不再读取截止配置
    /**
     * 下行指令（property.set / service.invoke）按产品的数据解析脚本 encode：决定走标准路径，还是透传写出 encode 的字节；
     * 由协议的下行订阅者按结果下发（本方法不写设备）
     * <ul>
     *     <li>设备不存在（或查询失败）、产品脚本停用 / 未配置：走标准路径；</li>
     *     <li>不能下发（脚本已启用但内容为空、执行失败、输出不合法、没有输出、超过截止）：只记日志，返回 null，不写设备，也不改走标准路径。</li>
     * </ul>
     * 只在功能开关开启、协议支持透传下发时调用。截止时间 = receivedNanos + gateway.script.deadline，覆盖排队、执行脚本，
     * 直到开始写设备：encode 之后、返回之前再核对一次，调用方拿到结果立即下发。
     * 会阻塞（加载设备信息、执行脚本）：只能在 worker 线程调用
     *
     * @param message       下行指令
     * @param protocol      协议类型：encode 的入参与字节规则按它区分
     * @param receivedNanos 下行订阅者收到指令的时刻（System.nanoTime）：在按设备排队之前取得，排队的时间也计入截止
     * @return encode 的结果；null 表示不下发
     */
    IotDownstreamEncodeRespDTO encodeDownstreamCommand(IotDeviceMessage message, IotProtocolTypeEnum protocol,
                                                       long receivedNanos);

    /**
     * 回复透传下行（raw.push，或 encode 的指令）的设备实际响应：只投递一条回复，raw 字段带上响应字节
     * <ul>
     *     <li>raw.push：直接回复，code / msg 取协议判定的设备错误（没有为成功）；</li>
     *     <li>encode 的指令：用 encode 时固定的脚本执行 decode，得到与原请求一致的一条回复；期间脚本被停用、解析失败只记日志。</li>
     * </ul>
     * 只用于设备的实际响应：网关判定的失败（超时、断线、写失败等）由协议只记日志，不调用本方法。
     * 会阻塞（执行脚本、发送总线）：只能在 worker 线程调用
     *
     * @param request     原请求
     * @param source      encode 时固定的脚本；raw.push 为 null
     * @param payload     设备的响应字节
     * @param deviceError 协议判定的设备错误（例如 Modbus 异常响应）；正常响应为 null。只用于 raw.push 的回复
     * @param serverId    所属协议实例的 serverId
     */
    void sendRawReply(IotDeviceMessage request, IotScriptSource source, byte[] payload, ErrorCode deviceError, String serverId);

}
