package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpserver.codec;

import cn.iocoder.yudao.module.iot.core.enums.modbus.IotModbusFrameFormatEnum;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

/**
 * IoT Modbus 帧解码器：集成 TCP 拆包 + 帧格式探测 + 帧解码，一条龙完成从 TCP 字节流到 IotModbusFrame 的转换。
 * <p>
 * 流程：
 * 1. 首帧检测：读前 6 字节，判断 MODBUS_TCP（ProtocolId==0x0000 且 Length 合理）或 MODBUS_RTU
 * 2. 检测后切换到对应的拆包 Handler，并将首包 6 字节通过 handleFirstBytes() 交给新 Handler 处理
 * 3. 拆包完成后解码为 IotModbusFrame，通过回调返回
 *      - MODBUS_TCP：两阶段 RecordParser（MBAP length 字段驱动）
 *      - MODBUS_RTU：功能码驱动的状态机
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
@Slf4j
public class IotModbusFrameDecoder {

    private static final Boolean REQUEST_MODE_DEFAULT = false;

    /**
     * 自定义功能码
     */
    private final int customFunctionCode;

    /**
     * 创建带自动帧格式检测的 RecordParser（默认响应模式）
     *
     * @param frameHandler 完整帧回调（解码后的 IotModbusFrame + 检测到的帧格式）
     * @return RecordParser 实例
     */
    public RecordParser createRecordParser(BiConsumer<IotModbusFrame, IotModbusFrameFormatEnum> frameHandler) {
        return createRecordParser(frameHandler, REQUEST_MODE_DEFAULT);
    }

    /**
     * 创建带自动帧格式检测的 RecordParser
     *
     * @param frameHandler 完整帧回调（解码后的 IotModbusFrame + 检测到的帧格式）
     * @param requestMode  是否为请求模式（true：接收方收到的是 Modbus 请求帧，FC01-04 按固定 8 字节解析；
     *                     false：接收方收到的是 Modbus 响应帧，FC01-04 按 byteCount 变长解析）
     * @return RecordParser 实例
     */
    public RecordParser createRecordParser(BiConsumer<IotModbusFrame, IotModbusFrameFormatEnum> frameHandler,
                                           boolean requestMode) {
        // 先创建一个 RecordParser：使用 fixedSizeMode(6) 读取首帧前 6 字节进行帧格式检测
        RecordParser parser = RecordParser.newFixed(6);
        parser.handler(new DetectPhaseHandler(parser, customFunctionCode, frameHandler, requestMode));
        return parser;
    }

    // ==================== 帧解码 ====================

    /**
     * 解码响应帧（拆包后的完整帧 byte[]）
     *
     * @param data   完整帧字节数组
     * @param format 帧格式
     * @return 解码后的 IotModbusFrame
     */
    private IotModbusFrame decodeResponse(byte[] data, IotModbusFrameFormatEnum format) {
        if (format == IotModbusFrameFormatEnum.MODBUS_TCP) {
            return decodeTcpResponse(data);
        } else {
            return decodeRtuResponse(data);
        }
    }

    /**
     * 解码 MODBUS_TCP 响应
     * 格式：[TransactionId(2)] [ProtocolId(2)] [Length(2)] [UnitId(1)] [FC(1)] [Data...]
     */
    private IotModbusFrame decodeTcpResponse(byte[] data) {
        if (data.length < 8) {
            log.warn("[decodeTcpResponse][数据长度不足: {}]", data.length);
            return null;
        }
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int transactionId = buf.getShort() & 0xFFFF;
        buf.getShort(); // protocolId：固定 0x0000，Modbus 协议标识
        buf.getShort(); // length：后续字节数（UnitId + PDU），拆包阶段已使用
        int slaveId = buf.get() & 0xFF;
        int functionCode = buf.get() & 0xFF;
        // 提取 PDU 数据（从 functionCode 之后到末尾）
        byte[] pdu = new byte[data.length - 8];
        System.arraycopy(data, 8, pdu, 0, pdu.length);
        // 构建 IotModbusFrame
        return buildFrame(slaveId, functionCode, pdu, transactionId);
    }

    /**
     * 解码 MODBUS_RTU 响应
     * 格式：[SlaveId(1)] [FC(1)] [Data...] [CRC(2)]
     */
    private IotModbusFrame decodeRtuResponse(byte[] data) {
        if (data.length < 4) {
            log.warn("[decodeRtuResponse][数据长度不足: {}]", data.length);
            return null;
        }
        // 校验 CRC
        if (!IotModbusCommonUtils.verifyCrc16(data)) {
            log.warn("[decodeRtuResponse][CRC 校验失败]");
            return null;
        }
        int slaveId = data[0] & 0xFF;
        int functionCode = data[1] & 0xFF;
        // PDU 数据（不含 slaveId、functionCode、CRC）
        byte[] pdu = new byte[data.length - 4];
        System.arraycopy(data, 2, pdu, 0, pdu.length);
        // 构建 IotModbusFrame
        return buildFrame(slaveId, functionCode, pdu, null);
    }

    /**
     * 构建 IotModbusFrame
     */
    private IotModbusFrame buildFrame(int slaveId, int functionCode, byte[] pdu, Integer transactionId) {
        IotModbusFrame frame = new IotModbusFrame()
                .setSlaveId(slaveId)
                .setFunctionCode(functionCode)
                .setPdu(pdu)
                .setTransactionId(transactionId);
        // 异常响应
        if (IotModbusCommonUtils.isExceptionResponse(functionCode)) {
            frame.setFunctionCode(IotModbusCommonUtils.extractOriginalFunctionCode(functionCode));
            if (pdu.length >= 1) {
                frame.setExceptionCode(pdu[0] & 0xFF);
            }
            return frame;
        }
        // 自定义功能码
        if (functionCode == customFunctionCode) {
            // data 区格式：[byteCount(1)] [JSON data(N)]
            if (pdu.length >= 1) {
                int byteCount = pdu[0] & 0xFF;
                if (pdu.length >= 1 + byteCount) {
                    frame.setCustomData(new String(pdu, 1, byteCount, StandardCharsets.UTF_8));
                }
            }
        }
        return frame;
    }

    // ==================== 拆包 Handler ====================

    /**
     * 帧格式检测阶段 Handler（仅处理首包，探测后切换到对应的拆包 Handler）
     */
    @RequiredArgsConstructor
    private class DetectPhaseHandler implements Handler<Buffer> {

        private final RecordParser parser;
        private final int customFunctionCode;
        private final BiConsumer<IotModbusFrame, IotModbusFrameFormatEnum> frameHandler;
        private final boolean requestMode;

        @Override
        public void handle(Buffer buffer) {
            // 检测帧格式：protocolId==0x0000 且 length 合法 → MODBUS_TCP，否则 → MODBUS_RTU
            byte[] bytes = buffer.getBytes();
            int protocolId = ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
            int length = ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF);

            // 分别处理 MODBUS_TCP、MODBUS_RTU 两种情况
            if (protocolId == 0x0000 && length >= 1 && length <= 253) {
                // MODBUS_TCP：切换到 TCP 拆包 Handler
                log.debug("[DetectPhaseHandler][检测到 MODBUS_TCP 帧格式]");
                TcpFrameHandler tcpHandler = new TcpFrameHandler(parser, frameHandler);
                parser.handler(tcpHandler);
                // 当前 bytes 就是 MBAP 的前 6 字节，直接交给 tcpHandler 处理
                tcpHandler.handleFirstBytes(bytes);
            } else {
                // MODBUS_RTU：切换到 RTU 拆包 Handler
                log.debug("[DetectPhaseHandler][检测到 MODBUS_RTU 帧格式]");
                RtuFrameHandler rtuHandler = new RtuFrameHandler(parser, frameHandler, customFunctionCode, requestMode);
                parser.handler(rtuHandler);
                // 当前 bytes 包含前 6 字节（slaveId + FC + 部分数据），交给 rtuHandler 处理
                rtuHandler.handleFirstBytes(bytes);
            }
        }
    }

    /**
     * MODBUS_TCP 拆包 Handler（两阶段 RecordParser）
     * <p>
     * Phase 1: fixedSizeMode(6) → 读 MBAP 前 6 字节，提取 length
     * Phase 2: fixedSizeMode(length) → 读 unitId + PDU
     */
    @RequiredArgsConstructor
    private class TcpFrameHandler implements Handler<Buffer> {

        private final RecordParser parser;
        private final BiConsumer<IotModbusFrame, IotModbusFrameFormatEnum> frameHandler;

        private byte[] mbapHeader;
        private boolean waitingForBody = false;

        /**
         * 处理探测阶段传来的首帧 6 字节（即 MBAP 头）
         *
         * @param bytes 探测阶段消费的 6 字节
         */
        void handleFirstBytes(byte[] bytes) {
            int length = ((bytes[4] & 0xFF) << 8) | (bytes[5] & 0xFF);
            this.mbapHeader = bytes;
            this.waitingForBody = true;
            parser.fixedSizeMode(length);
        }

        @Override
        public void handle(Buffer buffer) {
            if (waitingForBody) {
                // Phase 2: 收到 body（unitId + PDU）
                byte[] body = buffer.getBytes();
                // 拼接完整帧：MBAP(6) + body
                byte[] fullFrame = new byte[mbapHeader.length + body.length];
                System.arraycopy(mbapHeader, 0, fullFrame, 0, mbapHeader.length);
                System.arraycopy(body, 0, fullFrame, mbapHeader.length, body.length);
                // 解码并回调
                IotModbusFrame frame = decodeResponse(fullFrame, IotModbusFrameFormatEnum.MODBUS_TCP);
                if (frame != null) {
                    frameHandler.accept(frame, IotModbusFrameFormatEnum.MODBUS_TCP);
                }
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
     * - 异常响应 (FC & EXCEPTION_MASK)：fixedSizeMode(3) → exceptionCode(1) + CRC(2)
     * - 自定义 FC / FC01-04 响应：fixedSizeMode(1) → 读 byteCount → fixedSizeMode(byteCount + 2)
     * - FC05/06 响应：fixedSizeMode(6) → addr(2) + value(2) + CRC(2)
     * - FC15/16 响应：fixedSizeMode(6) → addr(2) + quantity(2) + CRC(2)
     * <p>
     * 请求模式（requestMode=true）时，FC01-04 按固定 8 字节解析（与写响应相同路径），
     * 因为读请求格式为 [SlaveId(1)][FC(1)][StartAddr(2)][Quantity(2)][CRC(2)]
     */
    @RequiredArgsConstructor
    private class RtuFrameHandler implements Handler<Buffer> {

        private static final int STATE_HEADER = 0;
        private static final int STATE_EXCEPTION_BODY = 1;
        private static final int STATE_READ_BYTE_COUNT = 2;
        private static final int STATE_READ_DATA = 3;
        private static final int STATE_WRITE_BODY = 4;

        private final RecordParser parser;
        private final BiConsumer<IotModbusFrame, IotModbusFrameFormatEnum> frameHandler;
        private final int customFunctionCode;
        /**
         * 请求模式：
         *    - true 表示接收方收到的是 Modbus 请求帧（如设备端收到网关下发的读请求），FC01-04 按固定 8 字节帧解析
         *    - false 表示接收方收到的是 Modbus 响应帧，FC01-04 按 byteCount 变长解析
         */
        private final boolean requestMode;

        private int state = STATE_HEADER;
        private byte slaveId;
        private byte functionCode;
        private byte byteCount;
        private Buffer pendingData;
        private int expectedDataLen;

        /**
         * 处理探测阶段传来的首帧 6 字节
         * <p>
         * 由于 RTU 首帧被探测阶段消费了 6 字节，这里需要从中提取 slaveId + FC 并根据 FC 处理剩余数据
         *
         * @param bytes 探测阶段消费的 6 字节：[slaveId][FC][...4 bytes...]
         */
        void handleFirstBytes(byte[] bytes) {
            this.slaveId = bytes[0];
            this.functionCode = bytes[1];
            int fc = functionCode & 0xFF;
            if (IotModbusCommonUtils.isExceptionResponse(fc)) {
                // 异常响应：完整帧 = slaveId(1) + FC(1) + exceptionCode(1) + CRC(2) = 5 字节
                // 已有 6 字节（多 1 字节），取前 5 字节组装
                Buffer frame = Buffer.buffer(5);
                frame.appendByte(slaveId);
                frame.appendByte(functionCode);
                frame.appendBytes(bytes, 2, 3); // exceptionCode + CRC
                emitFrame(frame);
                resetToHeader();
            } else if (IotModbusCommonUtils.isReadResponse(fc) && requestMode) {
                // 请求模式下的读请求：固定 8 字节 [SlaveId(1)][FC(1)][StartAddr(2)][Quantity(2)][CRC(2)]
                // 已有 6 字节，还需 2 字节（CRC）
                state = STATE_WRITE_BODY;
                this.pendingData = Buffer.buffer();
                this.pendingData.appendBytes(bytes, 2, 4); // 暂存已有的 4 字节（StartAddr + Quantity）
                parser.fixedSizeMode(2); // 还需 2 字节（CRC）
            } else if (IotModbusCommonUtils.isReadResponse(fc) || fc == customFunctionCode) {
                // 读响应或自定义 FC：bytes[2] = byteCount
                this.byteCount = bytes[2];
                int bc = byteCount & 0xFF;
                // 已有数据：bytes[3..5] = 3 字节
                // 还需：byteCount + CRC(2) - 3 字节已有
                int remaining = bc + 2 - 3;
                if (remaining <= 0) {
                    // 数据已足够，组装完整帧
                    int totalLen = 2 + 1 + bc + 2; // slaveId + FC + byteCount + data + CRC
                    Buffer frame = Buffer.buffer(totalLen);
                    frame.appendByte(slaveId);
                    frame.appendByte(functionCode);
                    frame.appendByte(byteCount);
                    frame.appendBytes(bytes, 3, bc + 2); // data + CRC
                    emitFrame(frame);
                    resetToHeader();
                } else {
                    // 需要继续读
                    state = STATE_READ_DATA;
                    this.pendingData = Buffer.buffer();
                    this.pendingData.appendBytes(bytes, 3, 3); // 暂存已有的 3 字节
                    this.expectedDataLen = bc + 2; // byteCount 个数据 + 2 CRC
                    parser.fixedSizeMode(remaining);
                }
            } else if (IotModbusCommonUtils.isWriteResponse(fc)) {
                // 写响应：总长 = slaveId(1) + FC(1) + addr(2) + value/qty(2) + CRC(2) = 8 字节
                // 已有 6 字节，还需 2 字节
                state = STATE_WRITE_BODY;
                this.pendingData = Buffer.buffer();
                this.pendingData.appendBytes(bytes, 2, 4); // 暂存已有的 4 字节
                parser.fixedSizeMode(2); // 还需 2 字节（CRC）
            } else {
                log.warn("[RtuFrameHandler][未知功能码: 0x{}]", Integer.toHexString(fc));
                resetToHeader();
            }
        }

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
            if (IotModbusCommonUtils.isExceptionResponse(fc)) {
                // 异常响应
                state = STATE_EXCEPTION_BODY;
                parser.fixedSizeMode(3); // exceptionCode(1) + CRC(2)
            } else if (IotModbusCommonUtils.isReadResponse(fc) && requestMode) {
                // 请求模式下的读请求：固定 8 字节，已读 2 字节（slaveId + FC），还需 6 字节
                state = STATE_WRITE_BODY;
                pendingData = Buffer.buffer();
                parser.fixedSizeMode(6); // StartAddr(2) + Quantity(2) + CRC(2)
            } else if (IotModbusCommonUtils.isReadResponse(fc) || fc == customFunctionCode) {
                // 读响应或自定义 FC
                state = STATE_READ_BYTE_COUNT;
                parser.fixedSizeMode(1); // byteCount
            } else if (IotModbusCommonUtils.isWriteResponse(fc)) {
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
            emitFrame(frame);
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
                emitFrame(frame);
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
            emitFrame(frame);
            resetToHeader();
        }

        /**
         * 发射完整帧：解码并回调
         */
        private void emitFrame(Buffer frameBuffer) {
            IotModbusFrame frame = decodeResponse(frameBuffer.getBytes(), IotModbusFrameFormatEnum.MODBUS_RTU);
            if (frame != null) {
                frameHandler.accept(frame, IotModbusFrameFormatEnum.MODBUS_RTU);
            }
        }

        private void resetToHeader() {
            state = STATE_HEADER;
            pendingData = null;
            parser.fixedSizeMode(2); // slaveId + FC
        }

    }

}
