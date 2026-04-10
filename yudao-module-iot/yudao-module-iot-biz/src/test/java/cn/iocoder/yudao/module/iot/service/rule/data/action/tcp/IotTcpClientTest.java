package cn.iocoder.yudao.module.iot.service.rule.data.action.tcp;

import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkTcpConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotTcpClient} 的单元测试
 * <p>
 * 测试 dataFormat 默认值行为
 * Property 1: TCP 客户端 dataFormat 默认值行为
 * Validates: Requirements 1.1, 1.2
 *
 * @author HUIHUI
 */
class IotTcpClientTest {

    @Test
    public void testConstructor_dataFormatNull() {
        // 准备参数
        String host = "localhost";
        Integer port = 8080;

        // 调用
        IotTcpClient client = new IotTcpClient(host, port, null, null, null, null);

        // 断言：dataFormat 为 null 时应使用默认值
        assertEquals(IotDataSinkTcpConfig.DEFAULT_DATA_FORMAT,
                ReflectUtil.getFieldValue(client, "dataFormat"));
    }

    @Test
    public void testConstructor_dataFormatEmpty() {
        // 准备参数
        String host = "localhost";
        Integer port = 8080;

        // 调用
        IotTcpClient client = new IotTcpClient(host, port, null, null, null, "");

        // 断言：dataFormat 为空字符串时应使用默认值
        assertEquals(IotDataSinkTcpConfig.DEFAULT_DATA_FORMAT,
                ReflectUtil.getFieldValue(client, "dataFormat"));
    }

    @Test
    public void testConstructor_dataFormatBlank() {
        // 准备参数
        String host = "localhost";
        Integer port = 8080;

        // 调用
        IotTcpClient client = new IotTcpClient(host, port, null, null, null, "   ");

        // 断言：dataFormat 为纯空白字符串时应使用默认值
        assertEquals(IotDataSinkTcpConfig.DEFAULT_DATA_FORMAT,
                ReflectUtil.getFieldValue(client, "dataFormat"));
    }

    @Test
    public void testConstructor_dataFormatValid() {
        // 准备参数
        String host = "localhost";
        Integer port = 8080;
        String dataFormat = "BINARY";

        // 调用
        IotTcpClient client = new IotTcpClient(host, port, null, null, null, dataFormat);

        // 断言：dataFormat 为有效值时应保持原值
        assertEquals(dataFormat, ReflectUtil.getFieldValue(client, "dataFormat"));
    }

    @Test
    public void testConstructor_defaultValues() {
        // 准备参数
        String host = "localhost";
        Integer port = 8080;

        // 调用
        IotTcpClient client = new IotTcpClient(host, port, null, null, null, null);

        // 断言：验证所有默认值
        assertEquals(host, ReflectUtil.getFieldValue(client, "host"));
        assertEquals(port, ReflectUtil.getFieldValue(client, "port"));
        assertEquals(IotDataSinkTcpConfig.DEFAULT_CONNECT_TIMEOUT_MS,
                ReflectUtil.getFieldValue(client, "connectTimeoutMs"));
        assertEquals(IotDataSinkTcpConfig.DEFAULT_READ_TIMEOUT_MS,
                ReflectUtil.getFieldValue(client, "readTimeoutMs"));
        assertEquals(IotDataSinkTcpConfig.DEFAULT_SSL,
                ReflectUtil.getFieldValue(client, "ssl"));
        assertEquals(IotDataSinkTcpConfig.DEFAULT_DATA_FORMAT,
                ReflectUtil.getFieldValue(client, "dataFormat"));
    }

    @Test
    public void testConstructor_customValues() {
        // 准备参数
        String host = "192.168.1.100";
        Integer port = 9090;
        Integer connectTimeoutMs = 3000;
        Integer readTimeoutMs = 8000;
        Boolean ssl = true;
        String dataFormat = "BINARY";

        // 调用
        IotTcpClient client = new IotTcpClient(host, port, connectTimeoutMs, readTimeoutMs, ssl, dataFormat);

        // 断言：验证自定义值
        assertEquals(host, ReflectUtil.getFieldValue(client, "host"));
        assertEquals(port, ReflectUtil.getFieldValue(client, "port"));
        assertEquals(connectTimeoutMs, ReflectUtil.getFieldValue(client, "connectTimeoutMs"));
        assertEquals(readTimeoutMs, ReflectUtil.getFieldValue(client, "readTimeoutMs"));
        assertEquals(ssl, ReflectUtil.getFieldValue(client, "ssl"));
        assertEquals(dataFormat, ReflectUtil.getFieldValue(client, "dataFormat"));
    }

    @Test
    public void testIsConnected_initialState() {
        // 准备参数
        String host = "localhost";
        Integer port = 8080;

        // 调用
        IotTcpClient client = new IotTcpClient(host, port, null, null, null, null);

        // 断言：初始状态应为未连接
        assertFalse(client.isConnected());
    }

    @Test
    public void testToString() {
        // 准备参数
        String host = "localhost";
        Integer port = 8080;

        // 调用
        IotTcpClient client = new IotTcpClient(host, port, null, null, null, null);
        String result = client.toString();

        // 断言
        assertNotNull(result);
        assertTrue(result.contains("host='localhost'"));
        assertTrue(result.contains("port=8080"));
        assertTrue(result.contains("dataFormat='JSON'"));
        assertTrue(result.contains("connected=false"));
    }

}
