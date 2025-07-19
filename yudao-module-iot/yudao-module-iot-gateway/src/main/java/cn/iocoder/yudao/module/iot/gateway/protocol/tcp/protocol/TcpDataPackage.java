package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TCP 数据包协议定义
 * <p>
 * 数据包格式：
 * 包头(4 字节长度) | 设备地址长度(2 字节) | 设备地址(不定长) | 功能码(2 字节) | 消息序号(2 字节) | 包体(不定长)
 *
 * @author 芋道源码
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TcpDataPackage {

    // ==================== 功能码定义 ====================

    /**
     * 设备注册
     */
    public static final short CODE_REGISTER = 10;
    /**
     * 注册回复
     */
    public static final short CODE_REGISTER_REPLY = 11;
    // TODO @haohao：【重要】一般心跳，服务端会回复一条；回复要搞独立的 code 码，还是继续用原来的，因为 requestId 可以映射；
    /**
     * 心跳
     */
    public static final short CODE_HEARTBEAT = 20;
    // TODO @haohao：【重要】下面的，是不是融合成消息上行（client -> server），消息下行（server -> client）；然后把 method 放到 body 里？
    /**
     * 数据上报
     */
    public static final short CODE_DATA_UP = 30;
    /**
     * 事件上报
     */
    public static final short CODE_EVENT_UP = 40;
    /**
     * 数据下发
     */
    public static final short CODE_DATA_DOWN = 50;
    /**
     * 服务调用
     */
    public static final short CODE_SERVICE_INVOKE = 60;
    /**
     * 属性设置
     */
    public static final short CODE_PROPERTY_SET = 70;
    /**
     * 属性获取
     */
    public static final short CODE_PROPERTY_GET = 80;

    // ==================== 数据包字段 ====================

    // TODO @haohao：设备 addrLength、addr 是不是非必要呀？

    /**
     * 设备地址长度
     */
    private Integer addrLength;

    /**
     * 设备地址
     */
    private String addr;

    /**
     * 功能码
     */
    private short code;

    /**
     * 消息序号
     */
    private short mid;

    /**
     * 包体数据
     */
    private String payload;

    // ==================== 辅助方法 ====================

    // TODO @haohao：用不到的方法，可以清理掉哈；

    /**
     * 是否为注册消息
     */
    public boolean isRegisterMessage() {
        return code == CODE_REGISTER;
    }

    /**
     * 是否为心跳消息
     */
    public boolean isHeartbeatMessage() {
        return code == CODE_HEARTBEAT;
    }

    /**
     * 是否为数据上报消息
     */
    public boolean isDataUpMessage() {
        return code == CODE_DATA_UP;
    }

    /**
     * 是否为事件上报消息
     */
    public boolean isEventUpMessage() {
        return code == CODE_EVENT_UP;
    }

    /**
     * 是否为下行消息
     */
    public boolean isDownstreamMessage() {
        return code == CODE_DATA_DOWN || code == CODE_SERVICE_INVOKE ||
                code == CODE_PROPERTY_SET || code == CODE_PROPERTY_GET;
    }

    // TODO @haohao：这个是不是去掉呀？多了一些维护成本；
    /**
     * 获取功能码描述
     */
    public String getCodeDescription() {
        switch (code) {
            case CODE_REGISTER:
                return "设备注册";
            case CODE_REGISTER_REPLY:
                return "注册回复";
            case CODE_HEARTBEAT:
                return "心跳";
            case CODE_DATA_UP:
                return "数据上报";
            case CODE_EVENT_UP:
                return "事件上报";
            case CODE_DATA_DOWN:
                return "数据下发";
            case CODE_SERVICE_INVOKE:
                return "服务调用";
            case CODE_PROPERTY_SET:
                return "属性设置";
            case CODE_PROPERTY_GET:
                return "属性获取";
            default:
                return "未知功能码";
        }
    }
}