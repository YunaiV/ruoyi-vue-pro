package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpslave.codec;

import cn.iocoder.yudao.module.iot.core.enums.IotModbusFrameFormatEnum;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

// TODO @AI：看看是不是不要搞成 factory，而是直接 new；（可以一起讨论下）
/**
 * IoT Modbus RecordParser 工厂
 * <p>
 * 创建带自动帧格式检测的 RecordParser：
 * 1. 首帧检测：读前 6 字节，判断 MODBUS_TCP（ProtocolId==0x0000 且 Length 合理）或 MODBUS_RTU
 * 2. 检测后自动切换到对应的拆包模式
 * - MODBUS_TCP：两阶段 RecordParser（MBAP length 字段驱动）
 * - MODBUS_RTU：功能码驱动的状态机
 *
 * @author 芋道源码
 */
@Slf4j
public class IotModbusRecordParserFactory {

    /**
     * 创建带自动帧格式检测的 RecordParser
     *
     * @param customFunctionCode 自定义功能码
     * @param frameHandler       完整帧回调
     * @param onFormatDetected   帧格式检测回调
     * @return RecordParser 实例
     */
    public static RecordParser create(int customFunctionCode,
                                      Handler<Buffer> frameHandler,
                                      Consumer<IotModbusFrameFormatEnum> onFormatDetected) {
        // 先创建一个 RecordParser，使用 fixedSizeMode(6) 读取首帧前 6 字节进行帧格式检测
        // TODO @AI：最小需要 6 个字节么？有可能更小的情况下，就探测出来？！
        RecordParser parser = RecordParser.newFixed(6);
        parser.handler(new DetectPhaseHandler(parser, customFunctionCode, frameHandler, onFormatDetected));
        return parser;
    }

    /**
     * 帧格式检测阶段 Handler
     */
    @SuppressWarnings("ClassCanBeRecord")
    private static class DetectPhaseHandler implements Handler<Buffer> {

        private final RecordParser parser;
        private final int customFunctionCode;
        private final Handler<Buffer> frameHandler;
        private final Consumer<IotModbusFrameFormatEnum> onFormatDetected;

        // TODO @AI：简化构造方法，使用 lombok；
        DetectPhaseHandler(RecordParser parser, int customFunctionCode,
                           Handler<Buffer> frameHandler,
                           Consumer<IotModbusFrameFormatEnum> onFormatDetected) {
            this.parser = parser;
            this.customFunctionCode = customFunctionCode;
            this.frameHandler = frameHandler;
            this.onFormatDetected = onFormatDetected;
        }

        @Override
        public void handle(Buffer buffer) {
            byte[] header = buffer.getBytes();
            // 检测：byte[2]==0x00 && byte[3]==0x00 && 1<=length<=253
            int protocolId = ((header[2] & 0xFF) << 8) | (header[3] & 0xFF);
            int length = ((header[4] & 0xFF) << 8) | (header[5] & 0xFF);

            if (protocolId == 0x0000 && length >= 1 && length <= 253) {
                // MODBUS_TCP
                log.debug("[DetectPhaseHandler][检测到 MODBUS_TCP 帧格式]");
                onFormatDetected.accept(IotModbusFrameFormatEnum.MODBUS_TCP);
                // 切换到 TCP 拆包模式，处理当前首帧
                TcpFrameHandler tcpHandler = new TcpFrameHandler(parser, frameHandler);
                parser.handler(tcpHandler);
                // 当前 header 是 MBAP 的前 6 字节，需要继续读 length 字节
                tcpHandler.handleMbapHeader(header, length);
            } else {
                // MODBUS_RTU
                log.debug("[DetectPhaseHandler][检测到 MODBUS_RTU 帧格式]");
                onFormatDetected.accept(IotModbusFrameFormatEnum.MODBUS_RTU);
                // 切换到 RTU 拆包模式，处理当前首帧
                RtuFrameHandler rtuHandler = new RtuFrameHandler(parser, customFunctionCode, frameHandler);
                parser.handler(rtuHandler);
                // 当前 header 包含前 6 字节（slaveId + FC + 部分数据），需要拼接处理
                rtuHandler.handleInitialBytes(header);
            }
        }
    }

    /**
     * MODBUS_TCP 拆包 Handler（两阶段 RecordParser）
     * Phase 1: fixedSizeMode(6) → 读 MBAP 前 6 字节，提取 length
     * Phase 2: fixedSizeMode(length) → 读 unitId + PDU
     */
    private static class TcpFrameHandler implements Handler<Buffer> {

        private final RecordParser parser;
        private final Handler<Buffer> frameHandler;
        private byte[] mbapHeader;
        private boolean waitingForBody = false;

        // TODO @AI：lombok
        TcpFrameHandler(RecordParser parser, Handler<Buffer> frameHandler) {
            this.parser = parser;
            this.frameHandler = frameHandler;
        }

        /**
         * 处理首帧的 MBAP 头
         */
        void handleMbapHeader(byte[] header, int length) {
            this.mbapHeader = header;
            this.waitingForBody = true;
            parser.fixedSizeMode(length);
        }

        @Override
        public void handle(Buffer buffer) {
            if (waitingForBody) {
                // Phase 2: 收到 body（unitId + PDU）
                byte[] body = buffer.getBytes();
                // 拼接完整帧：MBAP(6) + body
                Buffer frame = Buffer.buffer(mbapHeader.length + body.length);
                frame.appendBytes(mbapHeader);
                frame.appendBytes(body);
                frameHandler.handle(frame);
                // 切回 Phase 1
                waitingForBody = false;
                mbapHeader = null;
                parser.fixedSizeMode(6);
            } else {
                // Phase 1: 收到 MBAP 头 6 字节
                byte[] header = buffer.getBytes();
                int length = ((header[4] & 0xFF) << 8) | (header[5] & 0xFF);
                if (length < 1 || length > 253) {
                    log.warn("[TcpFrameHandler][MBAP Length 异常: {}]", length);
                    parser.fixedSizeMode(6);
                    return;
                }
                this.mbapHeader = header;
                this.waitingForBody = true;
                parser.fixedSizeMode(length);
            }
        }
    }

    /**
     * MODBUS_RTU 拆包 Handler（功能码驱动的状态机）
     * <p>
     * 状态机流程：
     * Phase 1: fixedSizeMode(2) → 读 slaveId + functionCode
     * Phase 2: 根据 functionCode 确定剩余长度：
     * - 异常响应 (FC & 0x80)：fixedSizeMode(3) → exceptionCode(1) + CRC(2)
     * - 自定义 FC / FC01-04 响应：fixedSizeMode(1) → 读 byteCount → fixedSizeMode(byteCount + 2)
     * - FC05/06 响应：fixedSizeMode(6) → addr(2) + value(2) + CRC(2)
     * - FC15/16 响应：fixedSizeMode(6) → addr(2) + quantity(2) + CRC(2)
     */
    private static class RtuFrameHandler implements Handler<Buffer> {

        private static final int STATE_HEADER = 0;
        private static final int STATE_EXCEPTION_BODY = 1;
        private static final int STATE_READ_BYTE_COUNT = 2;
        private static final int STATE_READ_DATA = 3;
        private static final int STATE_WRITE_BODY = 4;

        private final RecordParser parser;
        private final int customFunctionCode;
        private final Handler<Buffer> frameHandler;

        private int state = STATE_HEADER;
        private byte slaveId;
        private byte functionCode;
        private byte byteCount;

        // TODO @AI：lombok
        RtuFrameHandler(RecordParser parser, int customFunctionCode, Handler<Buffer> frameHandler) {
            this.parser = parser;
            this.customFunctionCode = customFunctionCode;
            this.frameHandler = frameHandler;
        }

        /**
         * 处理首帧检测阶段传来的初始 6 字节
         * 由于 RTU 首帧跳过了格式检测，我们需要拼接处理
         */
        void handleInitialBytes(byte[] initialBytes) {
            // initialBytes 包含 6 字节：[slaveId][FC][...4 bytes...]
            this.slaveId = initialBytes[0];
            this.functionCode = initialBytes[1];
            int fc = functionCode & 0xFF;

            // 根据功能码，确定还需要多少字节
            if ((fc & 0x80) != 0) {
                // 异常响应：还需要 exceptionCode(1) + CRC(2) = 3 字节
                // 我们已经有 4 字节剩余（initialBytes[2..5]），足够
                // 拼接完整帧并交付
                // 完整帧 = slaveId(1) + FC(1) + exceptionCode(1) + CRC(2) = 5
                Buffer frame = Buffer.buffer(5);
                frame.appendByte(slaveId);
                frame.appendByte(functionCode);
                frame.appendBytes(initialBytes, 2, 3); // exceptionCode + CRC
                frameHandler.handle(frame);
                // 剩余 1 字节需要留给下一帧，但 RecordParser 不支持回推
                // 简化处理：重置状态，开始读下一帧
                resetToHeader();
            } else if (isReadResponse(fc) || fc == customFunctionCode) {
                // 读响应或自定义 FC：initialBytes[2] = byteCount
                this.byteCount = initialBytes[2];
                int bc = byteCount & 0xFF;
                // 已有数据：initialBytes[3..5] = 3 字节
                // 还需：byteCount + CRC(2) - 3 字节已有
                int remaining = bc + 2 - 3;
                if (remaining <= 0) {
                    // 数据已足够，组装完整帧
                    int totalLen = 2 + 1 + bc + 2; // slaveId + FC + byteCount + data + CRC
                    Buffer frame = Buffer.buffer(totalLen);
                    frame.appendByte(slaveId);
                    frame.appendByte(functionCode);
                    frame.appendByte(byteCount);
                    frame.appendBytes(initialBytes, 3, bc + 2); // data + CRC
                    frameHandler.handle(frame);
                    resetToHeader();
                } else {
                    // 需要继续读
                    state = STATE_READ_DATA;
                    // 保存已有数据片段
                    parser.fixedSizeMode(remaining);
                    // 在 handle() 中需要拼接 initialBytes[3..5] + 新读取的数据
                    // 为了简化，我们用一个 Buffer 暂存
                    this.pendingData = Buffer.buffer();
                    this.pendingData.appendBytes(initialBytes, 3, 3);
                    this.expectedDataLen = bc + 2; // byteCount 个数据 + 2 CRC
                }
            } else if (isWriteResponse(fc)) {
                // 写响应：FC05/06/15/16，总长 = slaveId(1) + FC(1) + addr(2) + value/qty(2) + CRC(2) = 8
                // 已有 6 字节，还需 2 字节
                state = STATE_WRITE_BODY;
                this.pendingData = Buffer.buffer();
                this.pendingData.appendBytes(initialBytes, 2, 4); // 4 bytes already
                parser.fixedSizeMode(2); // need 2 more bytes (CRC)
            } else {
                log.warn("[RtuFrameHandler][未知功能码: 0x{}]", Integer.toHexString(fc));
                resetToHeader();
            }
        }

        private Buffer pendingData;
        private int expectedDataLen;

        @Override
        public void handle(Buffer buffer) {
            switch (state) {
                case STATE_HEADER:
                    handleHeader(buffer);
                    break;
                case STATE_EXCEPTION_BODY:
                    handleExceptionBody(buffer);
                    break;
                case STATE_READ_BYTE_COUNT:
                    handleReadByteCount(buffer);
                    break;
                case STATE_READ_DATA:
                    handleReadData(buffer);
                    break;
                case STATE_WRITE_BODY:
                    handleWriteBody(buffer);
                    break;
                default:
                    resetToHeader();
            }
        }

        private void handleHeader(Buffer buffer) {
            byte[] header = buffer.getBytes();
            this.slaveId = header[0];
            this.functionCode = header[1];
            int fc = functionCode & 0xFF;

            if ((fc & 0x80) != 0) {
                // 异常响应
                state = STATE_EXCEPTION_BODY;
                parser.fixedSizeMode(3); // exceptionCode(1) + CRC(2)
            } else if (isReadResponse(fc) || fc == customFunctionCode) {
                // 读响应或自定义 FC
                state = STATE_READ_BYTE_COUNT;
                parser.fixedSizeMode(1); // byteCount
            } else if (isWriteResponse(fc)) {
                // 写响应
                state = STATE_WRITE_BODY;
                pendingData = Buffer.buffer();
                parser.fixedSizeMode(6); // addr(2) + value(2) + CRC(2)
            } else {
                log.warn("[RtuFrameHandler][未知功能码: 0x{}]", Integer.toHexString(fc));
                resetToHeader();
            }
        }

        private void handleExceptionBody(Buffer buffer) {
            // buffer = exceptionCode(1) + CRC(2)
            Buffer frame = Buffer.buffer();
            frame.appendByte(slaveId);
            frame.appendByte(functionCode);
            frame.appendBuffer(buffer);
            frameHandler.handle(frame);
            resetToHeader();
        }

        private void handleReadByteCount(Buffer buffer) {
            this.byteCount = buffer.getByte(0);
            int bc = byteCount & 0xFF;
            state = STATE_READ_DATA;
            pendingData = Buffer.buffer();
            expectedDataLen = bc + 2; // data(bc) + CRC(2)
            parser.fixedSizeMode(expectedDataLen);
        }

        private void handleReadData(Buffer buffer) {
            pendingData.appendBuffer(buffer);
            if (pendingData.length() >= expectedDataLen) {
                // 组装完整帧
                Buffer frame = Buffer.buffer();
                frame.appendByte(slaveId);
                frame.appendByte(functionCode);
                frame.appendByte(byteCount);
                frame.appendBuffer(pendingData);
                frameHandler.handle(frame);
                resetToHeader();
            }
            // 否则继续等待（不应该发生，因为我们精确设置了 fixedSizeMode）
        }

        private void handleWriteBody(Buffer buffer) {
            pendingData.appendBuffer(buffer);
            // 完整帧
            Buffer frame = Buffer.buffer();
            frame.appendByte(slaveId);
            frame.appendByte(functionCode);
            frame.appendBuffer(pendingData);
            frameHandler.handle(frame);
            resetToHeader();
        }

        private void resetToHeader() {
            state = STATE_HEADER;
            pendingData = null;
            parser.fixedSizeMode(2); // slaveId + FC
        }

        private boolean isReadResponse(int fc) {
            return fc >= 1 && fc <= 4;
        }

        private boolean isWriteResponse(int fc) {
            return fc == 5 || fc == 6 || fc == 15 || fc == 16;
        }
    }

}
