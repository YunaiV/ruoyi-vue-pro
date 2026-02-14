package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient;

import com.ghgande.j2mod.modbus.procimg.*;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Modbus TCP 从站模拟器（手动测试）
 *
 * <p>测试场景：模拟一个标准 Modbus TCP 从站设备，供 Modbus TCP Client 网关连接和读写数据
 *
 * <p>使用步骤：
 * <ol>
 *     <li>运行 {@link #testStartSlaveSimulator()} 启动模拟从站（默认端口 5020，从站地址 1）</li>
 *     <li>启动 yudao-module-iot-gateway 服务（需开启 modbus-tcp-client 协议）</li>
 *     <li>确保数据库有对应的 Modbus Client 设备配置（ip=127.0.0.1, port=5020, slaveId=1）</li>
 *     <li>网关会自动连接模拟从站并开始轮询读取寄存器数据</li>
 *     <li>模拟器每 5 秒自动更新输入寄存器和保持寄存器的值，模拟传感器数据变化</li>
 * </ol>
 *
 * <p>可用寄存器：
 * <ul>
 *     <li>线圈 (Coil, 功能码 01/05): 地址 0-9，交替 true/false</li>
 *     <li>离散输入 (Discrete Input, 功能码 02): 地址 0-9，每 3 个一个 true</li>
 *     <li>保持寄存器 (Holding Register, 功能码 03/06/16): 地址 0-19，初始值 0,100,200,...</li>
 *     <li>输入寄存器 (Input Register, 功能码 04): 地址 0-19，初始值 1,11,21,...</li>
 * </ul>
 *
 * @author 芋道源码
 */
@Slf4j
@Disabled
public class IoTModbusTcpClientIntegrationTest {

    private static final int PORT = 5020;
    private static final int SLAVE_ID = 1;

    /**
     * 启动 Modbus TCP 从站模拟器
     *
     * <p>模拟器会持续运行，每 5 秒更新一次寄存器数据，直到手动停止
     */
    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    @Test
    public void testStartSlaveSimulator() throws Exception {
        // 1. 创建进程映像（Process Image），存储寄存器数据
        SimpleProcessImage spi = new SimpleProcessImage(SLAVE_ID);

        // 2.1 初始化线圈（Coil，功能码 01/05）- 离散输出，可读写
        // 地址 0-9，共 10 个线圈
        for (int i = 0; i < 10; i++) {
            spi.addDigitalOut(new SimpleDigitalOut(i % 2 == 0)); // 交替 true/false
        }

        // 2.2 初始化离散输入（Discrete Input，功能码 02）- 只读
        // 地址 0-9，共 10 个离散输入
        for (int i = 0; i < 10; i++) {
            spi.addDigitalIn(new SimpleDigitalIn(i % 3 == 0)); // 每 3 个一个 true
        }

        // 2.3 初始化保持寄存器（Holding Register，功能码 03/06/16）- 可读写
        // 地址 0-19，共 20 个寄存器
        for (int i = 0; i < 20; i++) {
            spi.addRegister(new SimpleRegister(i * 100)); // 值为 0, 100, 200, ...
        }

        // 2.4 初始化输入寄存器（Input Register，功能码 04）- 只读
        // 地址 0-19，共 20 个寄存器
        SimpleInputRegister[] inputRegisters = new SimpleInputRegister[20];
        for (int i = 0; i < 20; i++) {
            inputRegisters[i] = new SimpleInputRegister(i * 10 + 1); // 值为 1, 11, 21, ...
            spi.addInputRegister(inputRegisters[i]);
        }

        // 3.1 创建 Modbus TCP 从站
        ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(PORT, 5);
        slave.addProcessImage(SLAVE_ID, spi);
        // 3.2 启动从站
        slave.open();

        log.info("[testStartSlaveSimulator][Modbus TCP 从站模拟器已启动, 端口: {}, 从站地址: {}]", PORT, SLAVE_ID);
        log.info("[testStartSlaveSimulator][可用寄存器: 线圈(01/05) 0-9, 离散输入(02) 0-9, " +
                "保持寄存器(03/06/16) 0-19, 输入寄存器(04) 0-19]");

        // 4. 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("[testStartSlaveSimulator][正在关闭模拟器...]");
            slave.close();
            log.info("[testStartSlaveSimulator][模拟器已关闭]");
        }));

        // 5. 保持运行，定时更新输入寄存器模拟数据变化
        int counter = 0;
        while (true) {
            Thread.sleep(5000); // 每 5 秒更新一次
            counter++;

            // 更新输入寄存器的值，模拟传感器数据变化
            for (int i = 0; i < 20; i++) {
                int newValue = (i * 10 + 1) + counter;
                inputRegisters[i].setValue(newValue);
            }

            // 更新保持寄存器的第一个值
            spi.getRegister(0).setValue(counter * 100);
            log.info("[testStartSlaveSimulator][数据已更新, counter={}, 保持寄存器[0]={}, 输入寄存器[0]={}]",
                    counter, counter * 100, 1 + counter);
        }
    }

}
