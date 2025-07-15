package cn.iocoder.yudao.module.iot.gateway.protocol.tcp.protocol;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * TCP 数据读取器
 * <p>
 * 负责从 TCP 流中读取完整的数据包
 * <p>
 * 数据包格式：
 * 包头(4字节长度) | 设备地址长度(2字节) | 设备地址(不定长) | 功能码(2字节) | 消息序号(2字节) | 包体(不定长)
 *
 * @author 芋道源码
 */
@Slf4j
public class TcpDataReader {

    /**
     * 创建数据包解析器
     *
     * @param receiveHandler 接收处理器
     * @return RecordParser 解析器
     */
    public static RecordParser createParser(Consumer<Buffer> receiveHandler) {
        // 首先读取4字节的长度信息
        RecordParser parser = RecordParser.newFixed(4);

        // 设置处理器
        parser.setOutput(new Handler<Buffer>() {
            // 当前数据包的长度，-1表示还没有读取到长度信息
            private int dataLength = -1;

            @Override
            public void handle(Buffer buffer) {
                try {
                    // 如果还没有读取到长度信息
                    if (dataLength == -1) {
                        // 从包头中读取数据长度
                        dataLength = buffer.getInt(0);

                        // 校验数据长度
                        if (dataLength <= 0 || dataLength > 1024 * 1024) { // 最大1MB
                            log.error("[handle][无效的数据包长度: {}]", dataLength);
                            reset();
                            return;
                        }

                        // 切换到读取数据模式
                        parser.fixedSizeMode(dataLength);

                        log.debug("[handle][读取到数据包长度: {}]", dataLength);
                    } else {
                        // 读取到完整的数据包
                        log.debug("[handle][读取到完整数据包，长度: {}]", buffer.length());

                        // 处理数据包
                        try {
                            receiveHandler.accept(buffer);
                        } catch (Exception e) {
                            log.error("[handle][处理数据包失败]", e);
                        }

                        // 重置状态，准备读取下一个数据包
                        reset();
                    }
                } catch (Exception e) {
                    log.error("[handle][数据包处理异常]", e);
                    reset();
                }
            }

            /**
             * 重置解析器状态
             */
            private void reset() {
                dataLength = -1;
                parser.fixedSizeMode(4);
            }
        });

        return parser;
    }

    /**
     * 创建带异常处理的数据包解析器
     *
     * @param receiveHandler   接收处理器
     * @param exceptionHandler 异常处理器
     * @return RecordParser 解析器
     */
    public static RecordParser createParserWithExceptionHandler(
            Consumer<Buffer> receiveHandler,
            Consumer<Throwable> exceptionHandler) {

        RecordParser parser = RecordParser.newFixed(4);

        parser.setOutput(new Handler<Buffer>() {
            private int dataLength = -1;

            @Override
            public void handle(Buffer buffer) {
                try {
                    if (dataLength == -1) {
                        dataLength = buffer.getInt(0);

                        if (dataLength <= 0 || dataLength > 1024 * 1024) {
                            throw new IllegalArgumentException("无效的数据包长度: " + dataLength);
                        }

                        parser.fixedSizeMode(dataLength);
                        log.debug("[handle][读取到数据包长度: {}]", dataLength);
                    } else {
                        log.debug("[handle][读取到完整数据包，长度: {}]", buffer.length());

                        try {
                            receiveHandler.accept(buffer);
                        } catch (Exception e) {
                            exceptionHandler.accept(e);
                        }

                        reset();
                    }
                } catch (Exception e) {
                    exceptionHandler.accept(e);
                    reset();
                }
            }

            private void reset() {
                dataLength = -1;
                parser.fixedSizeMode(4);
            }
        });

        return parser;
    }

    /**
     * 创建简单的数据包解析器（用于测试）
     *
     * @param receiveHandler 接收处理器
     * @return RecordParser 解析器
     */
    public static RecordParser createSimpleParser(Consumer<TcpDataPackage> receiveHandler) {
        return createParser(buffer -> {
            try {
                TcpDataPackage dataPackage = TcpDataDecoder.decode(buffer);
                receiveHandler.accept(dataPackage);
            } catch (Exception e) {
                log.error("[createSimpleParser][解码数据包失败]", e);
            }
        });
    }
}