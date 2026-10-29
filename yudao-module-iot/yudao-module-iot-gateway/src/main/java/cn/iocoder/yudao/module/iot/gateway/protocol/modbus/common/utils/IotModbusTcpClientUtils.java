package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.ErrorCode;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.manager.IotModbusTx;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.handler.downstream.IotModbusRawRequest;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.ModbusIOException;
import com.ghgande.j2mod.modbus.ModbusSlaveException;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import io.vertx.core.Future;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.DEVICE_OFFLINE_OR_WRITE_FAILED;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_RESPONSE_INVALID;
import static cn.iocoder.yudao.module.iot.gateway.enums.ErrorCodeConstants.MODBUS_TIMEOUT;
import static cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils.IotModbusCommonUtils.*;

/**
 * IoT Modbus TCP 客户端工具类
 * <p>
 * 封装基于 j2mod 的 Modbus TCP 读写操作：
 * 1. 根据功能码创建对应的 Modbus 读/写请求
 * 2. 通过 {@link IotModbusTcpClientConnectionManager.ModbusConnection} 执行事务
 * 3. 从响应中提取原始值
 *
 * @author 芋道源码
 */
@UtilityClass
@Slf4j
public class IotModbusTcpClientUtils {

    /**
     * 读取 Modbus 数据
     *
     * @param connection Modbus 连接
     * @param slaveId    从站地址
     * @param point      点位配置
     * @return 原始值（int 数组）
     */
    public static Future<int[]> read(IotModbusTcpClientConnectionManager.ModbusConnection connection,
                                     Integer slaveId,
                                     IotModbusPointRespDTO point) {
        return read(connection, slaveId, point.getFunctionCode(),
                point.getRegisterAddress(), point.getRegisterCount(), point.getIdentifier());
    }

    /**
     * 读取 Modbus 数据
     *
     * @param connection      Modbus 连接
     * @param slaveId         从站地址
     * @param functionCode    功能码
     * @param registerAddress 寄存器起始地址
     * @param registerCount   寄存器数量
     * @return 原始值（int 数组）
     */
    public static Future<int[]> read(IotModbusTcpClientConnectionManager.ModbusConnection connection,
                                     Integer slaveId,
                                     Integer functionCode,
                                     Integer registerAddress,
                                     Integer registerCount) {
        return read(connection, slaveId, functionCode, registerAddress, registerCount, null);
    }

    private static Future<int[]> read(IotModbusTcpClientConnectionManager.ModbusConnection connection,
                                      Integer slaveId,
                                      Integer functionCode,
                                      Integer registerAddress,
                                      Integer registerCount,
                                      String identifier) {
        return connection.executeBlocking(tcpConnection -> {
            try {
                // 1. 创建请求
                ModbusRequest request = createReadRequest(functionCode, registerAddress, registerCount);
                request.setUnitID(slaveId);

                // 2. 执行事务（请求）
                ModbusTCPTransaction transaction = new ModbusTCPTransaction(tcpConnection);
                transaction.setRequest(request);
                transaction.execute();

                // 3. 解析响应
                ModbusResponse response = transaction.getResponse();
                return extractValues(response, functionCode);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Modbus 读取失败 [slaveId=%d, identifier=%s, functionCode=%d, address=%d, count=%d]",
                        slaveId, identifier, functionCode, registerAddress, registerCount), e);
            }
        });
    }

    /**
     * 写入 Modbus 数据
     *
     * @param connection Modbus 连接
     * @param slaveId    从站地址
     * @param point      点位配置
     * @param values     要写入的值
     * @return 是否成功
     */
    public static Future<Boolean> write(IotModbusTcpClientConnectionManager.ModbusConnection connection,
                                        Integer slaveId,
                                        IotModbusPointRespDTO point,
                                        int[] values) {
        return connection.executeBlocking(tcpConnection -> {
            try {
                // 1. 创建请求
                ModbusRequest request = createWriteRequest(point.getFunctionCode(),
                        point.getRegisterAddress(), point.getRegisterCount(), values);
                if (request == null) {
                    throw new RuntimeException("功能码 " + point.getFunctionCode() + " 不支持写操作");
                }
                request.setUnitID(slaveId);

                // 2. 执行事务（请求）
                ModbusTCPTransaction transaction = new ModbusTCPTransaction(tcpConnection);
                transaction.setRequest(request);
                transaction.execute();
                return true;
            } catch (Exception e) {
                throw new RuntimeException(String.format("Modbus 写入失败 [slaveId=%d, identifier=%s, address=%d]",
                        slaveId, point.getIdentifier(), point.getRegisterAddress()), e);
            }
        });
    }

    /**
     * 执行透传请求（raw.push / encode 输出的 PDU）
     * <p>
     * 设备的正常、异常响应都从 transaction.getResponse() 取真实响应，先核对事务号、unit、功能码，再还原 PDU：
     * 异常响应还原为 [fc | 0x80, 异常码] 作为成功结果；在有序队列里排队超过截止 → 110；
     * 身份不一致 → 在本有序任务内关闭连接再 114（下一个任务开始时重连，不会读到错位的响应）；格式与请求不一致 → 114；
     * 传输失败由 executeRawBlocking 的守卫先断开连接再抛出（只作用于透传；标准读写超时不断开共享连接）
     *
     * @param connection Modbus 连接
     * @param slaveId    从站地址
     * @param tx         透传事务
     * @return 响应 PDU
     */
    public static Future<byte[]> executeRaw(IotModbusTcpClientConnectionManager.ModbusConnection connection,
                                            Integer slaveId, IotModbusTx tx) {
        return connection.executeRawBlocking(tcpConnection -> {
            // 1. 在有序队列里排得太久（超过本连接的 timeout）：不再执行
            if (tx.isExpired()) {
                throw exception(MODBUS_TIMEOUT);
            }
            // 2.1 构造事务：retries = 1 表示只尝试 1 次（0 会被 j2mod 当成默认的 5 次；写功能码重试会重复写出）
            IotModbusRawRequest request = new IotModbusRawRequest(tx.getPdu());
            request.setUnitID(slaveId);
            ModbusTCPTransaction transaction = new ModbusTCPTransaction(tcpConnection);
            transaction.setRetries(1);
            transaction.setRequest(request);
            // 2.2 保存发出的身份：setRequest 分配事务号，execute 结束后 j2mod 会把请求的事务号自增
            int transactionId = request.getTransactionID();
            int unitId = request.getUnitID();
            int functionCode = request.getFunctionCode();
            // 2.3 执行事务
            try {
                transaction.execute();
            } catch (ModbusSlaveException e) {
                // 设备异常响应：不在这里拼异常 PDU，下面用 transaction.getResponse() 的真实异常响应核对身份
                log.debug("[executeRaw][设备异常响应, slaveId={}, functionCode={}]", slaveId, functionCode);
            } catch (ModbusException e) {
                throw new RuntimeException(String.format("Modbus 透传失败 [slaveId=%d, functionCode=%d]",
                        slaveId, functionCode), e);
            }
            // 3.1 核对真实响应的身份：不一致说明读到的不是这次请求的响应，在本有序任务内关闭连接
            ModbusResponse response = transaction.getResponse();
            if (!IotModbusRawRequest.isResponseOf(response, transactionId, unitId, functionCode)) {
                log.warn("[executeRaw][响应与请求不一致, 关闭连接, 请求(tid={}, unit={}, fc={}), 响应(tid={}, unit={}, fc={})]",
                        transactionId, unitId, functionCode,
                        response != null ? response.getTransactionID() : null,
                        response != null ? response.getUnitID() : null,
                        response != null ? response.getFunctionCode() : null);
                tcpConnection.close();
                throw exception(MODBUS_RESPONSE_INVALID);
            }
            // 3.2 还原响应 PDU，并核对响应格式
            return request.buildResponsePdu(response);
        });
    }

    /**
     * 是否是传输失败（读超时、IO 异常）：沿 cause 链查找 ModbusIOException / IOException；
     * 遇到 ModbusSlaveException（设备异常响应，说明链路正常）返回 false
     *
     * @param e 异常
     * @return 是否传输失败
     */
    public static boolean isTransportFailure(Throwable e) {
        int depth = 0;
        for (Throwable cause = e; cause != null && depth < 16; cause = cause.getCause(), depth++) {
            if (cause instanceof ModbusSlaveException) {
                return false;
            }
            if (cause instanceof ModbusIOException || cause instanceof IOException) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否是读超时：j2mod 的 ModbusTCPTransport.readResponse 把 SocketTimeoutException 转为
     * ModbusIOException("Socket timeout reading response - ...")，不保留 cause，所以同时按消息判断
     *
     * @param e 异常
     * @return 是否读超时
     */
    public static boolean isTimeoutFailure(Throwable e) {
        int depth = 0;
        for (Throwable cause = e; cause != null && depth < 16; cause = cause.getCause(), depth++) {
            if (cause instanceof SocketTimeoutException) {
                return true;
            }
            if (cause instanceof ModbusIOException && StrUtil.containsIgnoreCase(cause.getMessage(), "timeout")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 把透传任务的失败转换为网关判定的错误码：ServiceException 保留其错误码（110 / 114 / 115），读超时 → 110，其它 → 106
     *
     * @param e 异常
     * @return 错误码
     */
    public static ErrorCode toRawFailure(Throwable e) {
        if (e instanceof ServiceException) {
            return new ErrorCode(((ServiceException) e).getCode(), e.getMessage());
        }
        if (isTimeoutFailure(e)) {
            return MODBUS_TIMEOUT;
        }
        return DEVICE_OFFLINE_OR_WRITE_FAILED;
    }

    /**
     * 创建读取请求
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private static ModbusRequest createReadRequest(Integer functionCode, Integer address, Integer count) {
        switch (functionCode) {
            case FC_READ_COILS:
                return new ReadCoilsRequest(address, count);
            case FC_READ_DISCRETE_INPUTS:
                return new ReadInputDiscretesRequest(address, count);
            case FC_READ_HOLDING_REGISTERS:
                return new ReadMultipleRegistersRequest(address, count);
            case FC_READ_INPUT_REGISTERS:
                return new ReadInputRegistersRequest(address, count);
            default:
                throw new IllegalArgumentException("不支持的功能码: " + functionCode);
        }
    }

    /**
     * 创建写入请求
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private static ModbusRequest createWriteRequest(Integer functionCode, Integer address, Integer count, int[] values) {
        switch (functionCode) {
            case FC_READ_COILS: // 写线圈（使用功能码 5 或 15）
                if (count == 1) {
                    return new WriteCoilRequest(address, values[0] != 0);
                } else {
                    BitVector bv = new BitVector(count);
                    for (int i = 0; i < Math.min(values.length, count); i++) {
                        bv.setBit(i, values[i] != 0);
                    }
                    return new WriteMultipleCoilsRequest(address, bv);
                }
            case FC_READ_HOLDING_REGISTERS: // 写保持寄存器（使用功能码 6 或 16）
                if (count == 1) {
                    return new WriteSingleRegisterRequest(address, new SimpleRegister(values[0]));
                } else {
                    Register[] registers = new SimpleRegister[count];
                    for (int i = 0; i < count; i++) {
                        registers[i] = new SimpleRegister(i < values.length ? values[i] : 0);
                    }
                    return new WriteMultipleRegistersRequest(address, registers);
                }
            case FC_READ_DISCRETE_INPUTS: // 只读
            case FC_READ_INPUT_REGISTERS: // 只读
                return null;
            default:
                throw new IllegalArgumentException("不支持的功能码: " + functionCode);
        }
    }

    /**
     * 从响应中提取值
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    private static int[] extractValues(ModbusResponse response, Integer functionCode) {
        switch (functionCode) {
            case FC_READ_COILS:
                ReadCoilsResponse coilsResponse = (ReadCoilsResponse) response;
                int bitCount = coilsResponse.getBitCount();
                int[] coilValues = new int[bitCount];
                for (int i = 0; i < bitCount; i++) {
                    coilValues[i] = coilsResponse.getCoilStatus(i) ? 1 : 0;
                }
                return coilValues;
            case FC_READ_DISCRETE_INPUTS:
                ReadInputDiscretesResponse discretesResponse = (ReadInputDiscretesResponse) response;
                int discreteCount = discretesResponse.getBitCount();
                int[] discreteValues = new int[discreteCount];
                for (int i = 0; i < discreteCount; i++) {
                    discreteValues[i] = discretesResponse.getDiscreteStatus(i) ? 1 : 0;
                }
                return discreteValues;
            case FC_READ_HOLDING_REGISTERS:
                ReadMultipleRegistersResponse holdingResponse = (ReadMultipleRegistersResponse) response;
                InputRegister[] holdingRegisters = holdingResponse.getRegisters();
                int[] holdingValues = new int[holdingRegisters.length];
                for (int i = 0; i < holdingRegisters.length; i++) {
                    holdingValues[i] = holdingRegisters[i].getValue();
                }
                return holdingValues;
            case FC_READ_INPUT_REGISTERS:
                ReadInputRegistersResponse inputResponse = (ReadInputRegistersResponse) response;
                InputRegister[] inputRegisters = inputResponse.getRegisters();
                int[] inputValues = new int[inputRegisters.length];
                for (int i = 0; i < inputRegisters.length; i++) {
                    inputValues[i] = inputRegisters[i].getValue();
                }
                return inputValues;
            default:
                throw new IllegalArgumentException("不支持的功能码: " + functionCode);
        }
    }

}
