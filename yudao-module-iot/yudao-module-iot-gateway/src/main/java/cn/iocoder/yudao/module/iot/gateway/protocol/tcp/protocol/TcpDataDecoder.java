package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol;

import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

// TODO @haohao：“设备地址长度”是不是不需要。
/**
 * TCP 数据解码器
 * <p>
 * 负责将字节流解码为 TcpDataPackage 对象
 * <p>
 * 数据包格式：
 * 包头(4 字节长度) | 设备地址长度(2 字节) | 设备地址(不定长) | 功能码(2 字节) | 消息序号(2 字节) | 包体(不定长)
 *
 * @author 芋道源码
 */
@Slf4j
public class TcpDataDecoder {

    /**
     * 解码数据包
     *
     * @param buffer 数据缓冲区
     * @return 解码后的数据包
     * @throws IllegalArgumentException 如果数据包格式不正确
     */
    public static TcpDataPackage decode(Buffer buffer) {
        if (buffer == null || buffer.length() < 8) {
            throw new IllegalArgumentException("数据包长度不足");
        }

        try {
            int index = 0;

            // 1.1 获取设备地址长度（2字节）
            short addrLength = buffer.getShort(index);
            index += 2;

            // 1.2 校验数据包长度
            int expectedLength = 2 + addrLength + 2 + 2; // 地址长度 + 地址 + 功能码 + 消息序号
            if (buffer.length() < expectedLength) {
                throw new IllegalArgumentException("数据包长度不足，期望至少 " + expectedLength + " 字节");
            }

            // 1.3 获取设备地址
            String addr = buffer.getBuffer(index, index + addrLength).toString();
            index += addrLength;

            // 1.4 获取功能码（2字节）
            short code = buffer.getShort(index);
            index += 2;

            // 1.5 获取消息序号（2字节）
            short mid = buffer.getShort(index);
            index += 2;

            // 1.6 获取包体数据
            String payload = "";
            if (index < buffer.length()) {
                payload = buffer.getString(index, buffer.length());
            }

            // 2. 构建数据包对象
            TcpDataPackage dataPackage = TcpDataPackage.builder()
                    .addrLength((int) addrLength)
                    .addr(addr)
                    .code(code)
                    .mid(mid)
                    .payload(payload)
                    .build();

            log.debug("[decode][解码成功] 设备地址: {}, 功能码: {}, 消息序号: {}, 包体长度: {}",
                    addr, dataPackage.getCodeDescription(), mid, payload.length());
            return dataPackage;
        } catch (Exception e) {
            log.error("[decode][解码失败] 数据: {}", buffer.toString(), e);
            throw new IllegalArgumentException("数据包解码失败: " + e.getMessage(), e);
        }
    }

    // TODO @haohao：这个要不去掉，暂时没用到；
    /**
     * 校验数据包格式
     *
     * @param buffer 数据缓冲区
     * @return 校验结果
     */
    public static boolean validate(Buffer buffer) {
        try {
            decode(buffer);
            return true;
        } catch (Exception e) {
            log.warn("[validate][数据包格式校验失败] 数据: {}, 错误: {}", buffer.toString(), e.getMessage());
            return false;
        }
    }

}