package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster;

import com.ghgande.j2mod.modbus.procimg.*;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;

/**
 * Modbus TCP 从站模拟器
 *
 * 用于测试 Modbus TCP 网关的连接和数据读写功能
 *
 * @author 芋道源码
 */
public class ModbusTcpSlaveSimulatorTest {

    private static final int PORT = 5020;
    private static final int SLAVE_ID = 1;

    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait", "CommentedOutCode"})
    public static void main(String[] args) throws Exception {
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

        System.out.println("===================================================");
        System.out.println("Modbus TCP 从站模拟器已启动");
        System.out.println("端口: " + PORT);
        System.out.println("从站地址 (Slave ID): " + SLAVE_ID);
        System.out.println("===================================================");
        System.out.println("可用寄存器:");
        System.out.println("  - 线圈 (Coil, 功能码 01/05): 地址 0-9");
        System.out.println("  - 离散输入 (Discrete Input, 功能码 02): 地址 0-9");
        System.out.println("  - 保持寄存器 (Holding Register, 功能码 03/06/16): 地址 0-19");
        System.out.println("  - 输入寄存器 (Input Register, 功能码 04): 地址 0-19");
        System.out.println("===================================================");
        System.out.println("按 Ctrl+C 停止模拟器");

        // 4. 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在关闭模拟器...");
            slave.close();
            System.out.println("模拟器已关闭");
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
//            System.out.println("[" + java.time.LocalTime.now() + "] 数据已更新, counter=" + counter
//                    + ", 保持寄存器[0]=" + (counter * 100)
//                    + ", 输入寄存器[0]=" + (1 + counter));
        }
    }

}
