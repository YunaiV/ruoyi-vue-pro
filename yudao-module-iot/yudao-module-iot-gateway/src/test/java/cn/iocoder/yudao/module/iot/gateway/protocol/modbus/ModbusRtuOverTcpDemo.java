package cn.iocoder.yudao.module.iot.gateway.protocol.modbus;

import com.ghgande.j2mod.modbus.io.ModbusRTUTCPTransport;
import com.ghgande.j2mod.modbus.msg.*;
import com.ghgande.j2mod.modbus.procimg.*;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Modbus RTU over TCP 完整 Demo
 *
 * 架构：Master（主站）启动 TCP Server 监听 → Slave（从站）主动 TCP 连接上来
 * 通信协议：RTU 帧格式（带 CRC）通过 TCP 传输，而非标准 MBAP 头
 *
 * 流程：
 * 1. Master 启动 TCP ServerSocket 监听端口
 * 2. Slave（从站模拟器）作为 TCP Client 连接到 Master
 * 3. Master 通过 accept 得到的 Socket，使用 {@link ModbusRTUTCPTransport} 发送读写请求
 *
 * 实现说明：
 * 因为 j2mod 的 ModbusSlave 只能以 TCP Server 模式运行（监听端口等待 Master 连接），
 * 不支持"Slave 作为 TCP Client 主动连接 Master"的模式。
 * 所以这里用一个 TCP 桥接（bridge）来模拟：
 * - Slave 在本地内部端口启动（RTU over TCP 模式）
 * - 一个桥接线程同时连接 Master Server 和 Slave 内部端口，做双向数据转发
 * - Master 视角：看到的是 Slave 主动连上来
 *
 * 依赖：j2mod 3.2.1（pom.xml 中已声明）
 *
 * @author 芋道源码
 */
@Deprecated // 仅技术演示，非是必须的
public class ModbusRtuOverTcpDemo {

    /**
     * Master（主站）TCP Server 监听端口
     */
    private static final int PORT = 5021;
    /**
     * Slave 内部端口（仅本地中转用，不对外暴露）
     */
    private static final int SLAVE_INTERNAL_PORT = PORT + 100;
    /**
     * Modbus 从站地址
     */
    private static final int SLAVE_ID = 1;

    public static void main(String[] args) throws Exception {
        // ===================== 第一步：Master 启动 TCP Server 监听 =====================
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("===================================================");
        System.out.println("[Master] TCP Server 已启动，监听端口: " + PORT);
        System.out.println("[Master] 等待 Slave 连接...");
        System.out.println("===================================================");

        // ===================== 第二步：后台启动 Slave，它会主动连接 Master =====================
        ModbusSlave slave = startSlaveInBackground();

        // Master accept Slave 的连接
        Socket slaveSocket = serverSocket.accept();
        System.out.println("[Master] Slave 已连接: " + slaveSocket.getRemoteSocketAddress());

        // ===================== 第三步：Master 通过 RTU over TCP 发送读写请求 =====================
        // 使用 ModbusRTUTCPTransport 包装 Socket（RTU 帧 = SlaveID + 功能码 + 数据 + CRC，无 MBAP 头）
        ModbusRTUTCPTransport transport = new ModbusRTUTCPTransport(slaveSocket);

        try {
            System.out.println("[Master] RTU over TCP 通道已建立\n");

            // 1. 读操作演示：4 种功能码
            demoReadCoils(transport);           // 功能码 01：读线圈
            demoReadDiscreteInputs(transport);  // 功能码 02：读离散输入
            demoReadHoldingRegisters(transport); // 功能码 03：读保持寄存器
            demoReadInputRegisters(transport);   // 功能码 04：读输入寄存器

            // 2. 写操作演示 + 读回验证
            demoWriteCoil(transport);            // 功能码 05：写单个线圈
            demoWriteRegister(transport);        // 功能码 06：写单个保持寄存器

            System.out.println("\n===================================================");
            System.out.println("所有 RTU over TCP 读写操作执行成功！");
            System.out.println("===================================================");
        } finally {
            // 清理资源
            transport.close();
            slaveSocket.close();
            serverSocket.close();
            slave.close();
            System.out.println("[Master] 资源已关闭");
        }
    }

    // ===================== Slave 设备模拟（作为 TCP Client 连接 Master） =====================

    /**
     * 在后台启动从站模拟器，并通过 TCP 桥接连到 Master Server
     *
     * @return ModbusSlave 实例（用于最后关闭资源）
     */
    private static ModbusSlave startSlaveInBackground() throws Exception {
        // 1. 创建进程映像，初始化寄存器数据
        SimpleProcessImage spi = new SimpleProcessImage(SLAVE_ID);
        // 1.1 线圈（Coil，功能码 01/05）- 可读写，地址 0~9
        for (int i = 0; i < 10; i++) {
            spi.addDigitalOut(new SimpleDigitalOut(i % 2 == 0));
        }
        // 1.2 离散输入（Discrete Input，功能码 02）- 只读，地址 0~9
        for (int i = 0; i < 10; i++) {
            spi.addDigitalIn(new SimpleDigitalIn(i % 3 == 0));
        }
        // 1.3 保持寄存器（Holding Register，功能码 03/06/16）- 可读写，地址 0~19
        for (int i = 0; i < 20; i++) {
            spi.addRegister(new SimpleRegister(i * 100));
        }
        // 1.4 输入寄存器（Input Register，功能码 04）- 只读，地址 0~19
        for (int i = 0; i < 20; i++) {
            spi.addInputRegister(new SimpleInputRegister(i * 10 + 1));
        }

        // 2. 启动 Slave（RTU over TCP 模式，在本地内部端口监听）
        ModbusSlave slave = ModbusSlaveFactory.createTCPSlave(SLAVE_INTERNAL_PORT, 5, true);
        slave.addProcessImage(SLAVE_ID, spi);
        slave.open();
        System.out.println("[Slave] 从站模拟器已启动（内部端口: " + SLAVE_INTERNAL_PORT + "）");

        // 3. 启动桥接线程：TCP Client 连接 Master Server，同时连接 Slave 内部端口，双向转发
        Thread bridgeThread = new Thread(() -> {
            try {
                Socket toMaster = new Socket("127.0.0.1", PORT);
                Socket toSlave = new Socket("127.0.0.1", SLAVE_INTERNAL_PORT);
                System.out.println("[Bridge] 已建立桥接: Master(" + PORT + ") <-> Slave(" + SLAVE_INTERNAL_PORT + ")");

                // 双向桥接：Master ↔ Bridge ↔ Slave
                Thread forward = new Thread(() -> bridge(toMaster, toSlave), "bridge-master→slave");
                Thread backward = new Thread(() -> bridge(toSlave, toMaster), "bridge-slave→master");
                forward.setDaemon(true);
                backward.setDaemon(true);
                forward.start();
                backward.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "bridge-setup");
        bridgeThread.setDaemon(true);
        bridgeThread.start();

        return slave;
    }

    /**
     * TCP 双向桥接：从 src 读取数据，写入 dst
     */
    private static void bridge(Socket src, Socket dst) {
        try {
            byte[] buf = new byte[1024];
            InputStream in = src.getInputStream();
            OutputStream out = dst.getOutputStream();
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                out.flush();
            }
        } catch (Exception ignored) {
            // 连接关闭时正常退出
        }
    }

    // ===================== Master 读写操作 =====================

    /**
     * 发送请求并接收响应（通用方法）
     */
    private static ModbusResponse sendRequest(ModbusRTUTCPTransport transport, ModbusRequest request) throws Exception {
        request.setUnitID(SLAVE_ID);
        transport.writeRequest(request);
        return transport.readResponse();
    }

    /**
     * 功能码 01：读线圈（Read Coils）
     */
    private static void demoReadCoils(ModbusRTUTCPTransport transport) throws Exception {
        ReadCoilsRequest request = new ReadCoilsRequest(0, 5);
        ReadCoilsResponse response = (ReadCoilsResponse) sendRequest(transport, request);

        StringBuilder sb = new StringBuilder("[功能码 01] 读线圈(0~4): ");
        for (int i = 0; i < 5; i++) {
            sb.append(response.getCoilStatus(i) ? "ON" : "OFF");
            if (i < 4) {
                sb.append(", ");
            }
        }
        System.out.println(sb);
    }

    /**
     * 功能码 02：读离散输入（Read Discrete Inputs）
     */
    private static void demoReadDiscreteInputs(ModbusRTUTCPTransport transport) throws Exception {
        ReadInputDiscretesRequest request = new ReadInputDiscretesRequest(0, 5);
        ReadInputDiscretesResponse response = (ReadInputDiscretesResponse) sendRequest(transport, request);

        StringBuilder sb = new StringBuilder("[功能码 02] 读离散输入(0~4): ");
        for (int i = 0; i < 5; i++) {
            sb.append(response.getDiscreteStatus(i) ? "ON" : "OFF");
            if (i < 4) {
                sb.append(", ");
            }
        }
        System.out.println(sb);
    }

    /**
     * 功能码 03：读保持寄存器（Read Holding Registers）
     */
    private static void demoReadHoldingRegisters(ModbusRTUTCPTransport transport) throws Exception {
        ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(0, 5);
        ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) sendRequest(transport, request);

        StringBuilder sb = new StringBuilder("[功能码 03] 读保持寄存器(0~4): ");
        for (int i = 0; i < response.getWordCount(); i++) {
            sb.append(response.getRegisterValue(i));
            if (i < response.getWordCount() - 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb);
    }

    /**
     * 功能码 04：读输入寄存器（Read Input Registers）
     */
    private static void demoReadInputRegisters(ModbusRTUTCPTransport transport) throws Exception {
        ReadInputRegistersRequest request = new ReadInputRegistersRequest(0, 5);
        ReadInputRegistersResponse response = (ReadInputRegistersResponse) sendRequest(transport, request);

        StringBuilder sb = new StringBuilder("[功能码 04] 读输入寄存器(0~4): ");
        for (int i = 0; i < response.getWordCount(); i++) {
            sb.append(response.getRegisterValue(i));
            if (i < response.getWordCount() - 1) {
                sb.append(", ");
            }
        }
        System.out.println(sb);
    }

    /**
     * 功能码 05：写单个线圈（Write Single Coil）+ 读回验证
     */
    private static void demoWriteCoil(ModbusRTUTCPTransport transport) throws Exception {
        int address = 0;

        // 1. 先读取当前值
        ReadCoilsRequest readReq = new ReadCoilsRequest(address, 1);
        ReadCoilsResponse readResp = (ReadCoilsResponse) sendRequest(transport, readReq);
        boolean beforeValue = readResp.getCoilStatus(0);

        // 2. 写入相反的值
        boolean writeValue = !beforeValue;
        WriteCoilRequest writeReq = new WriteCoilRequest(address, writeValue);
        sendRequest(transport, writeReq);

        // 3. 读回验证
        ReadCoilsResponse verifyResp = (ReadCoilsResponse) sendRequest(transport, readReq);
        boolean afterValue = verifyResp.getCoilStatus(0);

        System.out.println("[功能码 05] 写线圈: 地址=" + address
                + ", 写入前=" + (beforeValue ? "ON" : "OFF")
                + ", 写入值=" + (writeValue ? "ON" : "OFF")
                + ", 读回值=" + (afterValue ? "ON" : "OFF")
                + (afterValue == writeValue ? " ✓ 验证通过" : " ✗ 验证失败"));
    }

    /**
     * 功能码 06：写单个保持寄存器（Write Single Register）+ 读回验证
     */
    private static void demoWriteRegister(ModbusRTUTCPTransport transport) throws Exception {
        int address = 0;
        int writeValue = 12345;

        // 1. 先读取当前值
        ReadMultipleRegistersRequest readReq = new ReadMultipleRegistersRequest(address, 1);
        ReadMultipleRegistersResponse readResp = (ReadMultipleRegistersResponse) sendRequest(transport, readReq);
        int beforeValue = readResp.getRegisterValue(0);

        // 2. 写入新值
        WriteSingleRegisterRequest writeReq = new WriteSingleRegisterRequest(address, new SimpleRegister(writeValue));
        sendRequest(transport, writeReq);

        // 3. 读回验证
        ReadMultipleRegistersResponse verifyResp = (ReadMultipleRegistersResponse) sendRequest(transport, readReq);
        int afterValue = verifyResp.getRegisterValue(0);

        System.out.println("[功能码 06] 写保持寄存器: 地址=" + address
                + ", 写入前=" + beforeValue
                + ", 写入值=" + writeValue
                + ", 读回值=" + afterValue
                + (afterValue == writeValue ? " ✓ 验证通过" : " ✗ 验证失败"));
    }

}
