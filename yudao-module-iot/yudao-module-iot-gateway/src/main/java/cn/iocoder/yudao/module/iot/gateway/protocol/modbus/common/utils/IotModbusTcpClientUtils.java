package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.common.utils;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient.manager.IotModbusTcpClientConnectionManager;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import io.vertx.core.Future;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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
        return connection.executeBlocking(tcpConnection -> {
            try {
                // 1. 创建请求
                ModbusRequest request = createReadRequest(point.getFunctionCode(),
                        point.getRegisterAddress(), point.getRegisterCount());
                request.setUnitID(slaveId);

                // 2. 执行事务（请求）
                ModbusTCPTransaction transaction = new ModbusTCPTransaction(tcpConnection);
                transaction.setRequest(request);
                transaction.execute();

                // 3. 解析响应
                ModbusResponse response = transaction.getResponse();
                return extractValues(response, point.getFunctionCode());
            } catch (Exception e) {
                throw new RuntimeException(String.format("Modbus 读取失败 [slaveId=%d, identifier=%s, address=%d]",
                        slaveId, point.getIdentifier(), point.getRegisterAddress()), e);
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
