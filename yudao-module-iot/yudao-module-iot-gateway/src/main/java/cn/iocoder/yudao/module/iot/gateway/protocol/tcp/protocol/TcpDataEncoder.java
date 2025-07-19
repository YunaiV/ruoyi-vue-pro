package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol;

import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

/**
 * TCP 数据编码器
 * <p>
 * 负责将 TcpDataPackage 对象编码为字节流
 * <p>
 * 数据包格式：
 * 包头(4字节长度) | 设备地址长度(2字节) | 设备地址(不定长) | 功能码(2字节) | 消息序号(2字节) | 包体(不定长)
 *
 * @author 芋道源码
 */
@Slf4j
public class TcpDataEncoder {

    /**
     * 编码数据包
     *
     * @param dataPackage 数据包对象
     * @return 编码后的字节流
     * @throws IllegalArgumentException 如果数据包对象不正确
     */
    public static Buffer encode(TcpDataPackage dataPackage) {
        if (dataPackage == null) {
            throw new IllegalArgumentException("数据包对象不能为空");
        }
        if (dataPackage.getAddr() == null || dataPackage.getAddr().isEmpty()) {
            throw new IllegalArgumentException("设备地址不能为空");
        }
        if (dataPackage.getPayload() == null) {
            dataPackage.setPayload("");
        }

        try {
            Buffer buffer = Buffer.buffer();

            // 1. 计算包体长度（除了包头 4 字节）
            int payloadLength = dataPackage.getPayload().getBytes().length;
            int totalLength = 2 + dataPackage.getAddr().length() + 2 + 2 + payloadLength;

            // 2.1 写入包头：总长度（4 字节）
            buffer.appendInt(totalLength);
            // 2.2 写入设备地址长度（2 字节）
            buffer.appendShort((short) dataPackage.getAddr().length());
            // 2.3 写入设备地址（不定长）
            buffer.appendBytes(dataPackage.getAddr().getBytes());
            // 2.4 写入功能码（2 字节）
            buffer.appendShort(dataPackage.getCode());
            // 2.5 写入消息序号（2 字节）
            buffer.appendShort(dataPackage.getMid());
            // 2.6 写入包体数据（不定长）
            buffer.appendBytes(dataPackage.getPayload().getBytes());

            log.debug("[encode][编码成功] 设备地址: {}, 功能码: {}, 消息序号: {}, 总长度: {}",
                    dataPackage.getAddr(), dataPackage.getCodeDescription(),
                    dataPackage.getMid(), buffer.length());
            return buffer;
        } catch (Exception e) {
            log.error("[encode][编码失败] 数据包: {}", dataPackage, e);
            throw new IllegalArgumentException("数据包编码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建注册回复数据包
     *
     * @param addr    设备地址
     * @param mid     消息序号
     * @param success 是否成功
     * @return 编码后的数据包
     */
    public static Buffer createRegisterReply(String addr, short mid, boolean success) {
        // TODO @haohao：payload 默认成功、失败，最好讴有个枚举
        String payload = success ? "0" : "1"; // 0 表示成功，1 表示失败
        TcpDataPackage dataPackage = TcpDataPackage.builder()
                .addr(addr)
                .code(TcpDataPackage.CODE_REGISTER_REPLY)
                .mid(mid)
                .payload(payload)
                .build();
        return encode(dataPackage);
    }

    /**
     * 创建数据下发数据包
     *
     * @param addr 设备地址
     * @param mid  消息序号
     * @param data 下发数据
     * @return 编码后的数据包
     */
    public static Buffer createDataDownPackage(String addr, short mid, String data) {
        TcpDataPackage dataPackage = TcpDataPackage.builder()
                .addr(addr)
                .code(TcpDataPackage.CODE_DATA_DOWN)
                .mid(mid)
                .payload(data)
                .build();
        return encode(dataPackage);
    }

    /**
     * 创建服务调用数据包
     *
     * @param addr        设备地址
     * @param mid         消息序号
     * @param serviceData 服务数据
     * @return 编码后的数据包
     */
    public static Buffer createServiceInvokePackage(String addr, short mid, String serviceData) {
        TcpDataPackage dataPackage = TcpDataPackage.builder()
                .addr(addr)
                .code(TcpDataPackage.CODE_SERVICE_INVOKE)
                .mid(mid)
                .payload(serviceData)
                .build();
        return encode(dataPackage);
    }

    /**
     * 创建属性设置数据包
     *
     * @param addr         设备地址
     * @param mid          消息序号
     * @param propertyData 属性数据
     * @return 编码后的数据包
     */
    public static Buffer createPropertySetPackage(String addr, short mid, String propertyData) {
        TcpDataPackage dataPackage = TcpDataPackage.builder()
                .addr(addr)
                .code(TcpDataPackage.CODE_PROPERTY_SET)
                .mid(mid)
                .payload(propertyData)
                .build();
        return encode(dataPackage);
    }

    /**
     * 创建属性获取数据包
     *
     * @param addr          设备地址
     * @param mid           消息序号
     * @param propertyNames 属性名称列表
     * @return 编码后的数据包
     */
    public static Buffer createPropertyGetPackage(String addr, short mid, String propertyNames) {
        TcpDataPackage dataPackage = TcpDataPackage.builder()
                .addr(addr)
                .code(TcpDataPackage.CODE_PROPERTY_GET)
                .mid(mid)
                .payload(propertyNames)
                .build();
        return encode(dataPackage);
    }

}