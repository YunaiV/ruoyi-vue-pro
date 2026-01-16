package cn.iocoder.yudao.module.iot.gateway.protocol.modbustcp;

import cn.iocoder.yudao.module.iot.core.biz.dto.IotModbusPointRespDTO;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.util.BitVector;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IoT Modbus TCP 客户端
 *
 * 负责：
 * 1. 封装 Modbus 读写操作
 * 2. 根据功能码执行对应的 Modbus 请求
 *
 * @author 芋道源码
 */
// TODO @AI：希望它的初始化，在 configuration 里；
@Component
@RequiredArgsConstructor // TODO @AI：这个注解，是不是可以去掉？
@Slf4j
public class IotModbusTcpClient {

    /**
     * 读取 Modbus 数据
     *
     * @param connection Modbus 连接
     * @param slaveId    从站地址
     * @param point      点位配置
     * @return 原始值（int 数组）
     */
    public Future<int[]> read(IotModbusTcpConnectionManager.ModbusConnection connection,
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
                // TODO @AI：抛出异常时，增加更多的上下文信息，比如设备、点位等
                throw new RuntimeException("Modbus 读取失败", e);
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
    public Future<Boolean> write(IotModbusTcpConnectionManager.ModbusConnection connection,
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
                // TODO @AI：抛出异常时，增加更多的上下文信息，比如设备、点位等；
                throw new RuntimeException("Modbus 写入失败", e);
            }
        });
    }

    /**
     * 创建读取请求
     */
    private ModbusRequest createReadRequest(Integer functionCode, Integer address, Integer count) {
        // TODO @AI：1、2、3、4 能不能有枚举哈？这样 1、2、3、4 那的注释就不用写；
        switch (functionCode) {
            case 1: // ReadCoils
                return new ReadCoilsRequest(address, count);
            case 2: // ReadDiscreteInputs
                return new ReadInputDiscretesRequest(address, count);
            case 3: // ReadHoldingRegisters
                return new ReadMultipleRegistersRequest(address, count);
            case 4: // ReadInputRegisters
                return new ReadInputRegistersRequest(address, count);
            default:
                throw new IllegalArgumentException("不支持的功能码: " + functionCode);
        }
    }

    /**
     * 创建写入请求
     */
    private ModbusRequest createWriteRequest(Integer functionCode, Integer address, Integer count, int[] values) {
        // TODO @AI：5、6、15、16 能不能有枚举哈？这样 5、6、15、16 那的注释就不用写；
        switch (functionCode) {
            case 1: // WriteCoils (使用 5 或 15)
                if (count == 1) {
                    return new WriteCoilRequest(address, values[0] != 0);
                } else {
                    // 多线圈写入
                    BitVector bv = new BitVector(count);
                    for (int i = 0; i < Math.min(values.length, count); i++) {
                        bv.setBit(i, values[i] != 0);
                    }
                    return new WriteMultipleCoilsRequest(address, bv);
                }
            case 3: // WriteHoldingRegisters (使用 6 或 16)
                if (count == 1) {
                    return new WriteSingleRegisterRequest(address, new SimpleRegister(values[0]));
                } else {
                    Register[] registers = new com.ghgande.j2mod.modbus.procimg.SimpleRegister[count];
                    for (int i = 0; i < count; i++) {
                        registers[i] = new SimpleRegister(i < values.length ? values[i] : 0);
                    }
                    return new WriteMultipleRegistersRequest(address, registers);
                }
            case 2: // ReadDiscreteInputs - 只读
            case 4: // ReadInputRegisters - 只读
                return null;
            default:
                throw new IllegalArgumentException("不支持的功能码: " + functionCode);
        }
    }

    /**
     * 从响应中提取值
     */
    private int[] extractValues(ModbusResponse response, Integer functionCode) {
        // TODO @AI：1、2、3、4 能不能有枚举哈？这样 1、2、3、4 那的注释就不用写；
        switch (functionCode) {
            case 1: // ReadCoils
                ReadCoilsResponse coilsResponse = (ReadCoilsResponse) response;
                int bitCount = coilsResponse.getBitCount();
                int[] coilValues = new int[bitCount];
                for (int i = 0; i < bitCount; i++) {
                    coilValues[i] = coilsResponse.getCoilStatus(i) ? 1 : 0;
                }
                return coilValues;
            case 2: // ReadDiscreteInputs
                ReadInputDiscretesResponse discretesResponse = (ReadInputDiscretesResponse) response;
                int discreteCount = discretesResponse.getBitCount();
                int[] discreteValues = new int[discreteCount];
                for (int i = 0; i < discreteCount; i++) {
                    discreteValues[i] = discretesResponse.getDiscreteStatus(i) ? 1 : 0;
                }
                return discreteValues;
            case 3: // ReadHoldingRegisters
                ReadMultipleRegistersResponse holdingResponse = (ReadMultipleRegistersResponse) response;
                InputRegister[] holdingRegisters = holdingResponse.getRegisters();
                int[] holdingValues = new int[holdingRegisters.length];
                for (int i = 0; i < holdingRegisters.length; i++) {
                    holdingValues[i] = holdingRegisters[i].getValue();
                }
                return holdingValues;
            case 4: // ReadInputRegisters
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
