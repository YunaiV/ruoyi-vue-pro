package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TCP 数据包协议定义
 * <p>
 * 数据包格式：
 * 包头(4字节长度) | 设备地址长度(2字节) | 设备地址(不定长) | 功能码(2字节) | 消息序号(2字节) | 包体(不定长)
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
    /**
     * 心跳
     */
    public static final short CODE_HEARTBEAT = 20;
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