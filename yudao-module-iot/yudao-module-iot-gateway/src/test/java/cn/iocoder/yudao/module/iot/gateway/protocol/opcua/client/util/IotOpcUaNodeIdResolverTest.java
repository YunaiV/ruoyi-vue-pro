package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util;

import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.junit.jupiter.api.Test;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link IotOpcUaNodeIdResolver} 的单元测试
 *
 * @author 芋道源码
 */
public class IotOpcUaNodeIdResolverTest {

    private static final String[] NAMESPACES = {"http://opcfoundation.org/UA/", "urn:server", "urn:yudao:test"};
    private static final String[] REORDERED = {"http://opcfoundation.org/UA/", "urn:server", "urn:extra", "urn:yudao:test"};

    @Test
    public void testResolve_namespaceUri() {
        // nsu= 写法：按会话的命名空间表解析索引，命名空间顺序变化后指向新的索引
        String nodeId = "nsu=urn:yudao:test;s=Temperature";
        NamespaceTable table = IotOpcUaNodeIdResolver.buildNamespaceTable(NAMESPACES);
        assertEquals(new NodeId(2, "Temperature"), IotOpcUaNodeIdResolver.resolve(nodeId, table));
        NamespaceTable reordered = IotOpcUaNodeIdResolver.buildNamespaceTable(REORDERED);
        assertEquals(new NodeId(3, "Temperature"), IotOpcUaNodeIdResolver.resolve(nodeId, reordered));
    }

    @Test
    public void testResolve_namespaceIndex() {
        // ns= 写法：直接使用配置的索引
        NamespaceTable table = IotOpcUaNodeIdResolver.buildNamespaceTable(REORDERED);
        assertEquals(new NodeId(2, uint(1001)), IotOpcUaNodeIdResolver.resolve("ns=2;i=1001", table));
        assertEquals(new NodeId(0, uint(2258)), IotOpcUaNodeIdResolver.resolve("i=2258", table));
    }

    @Test
    public void testResolve_invalid() {
        NamespaceTable table = IotOpcUaNodeIdResolver.buildNamespaceTable(NAMESPACES);
        assertNull(IotOpcUaNodeIdResolver.resolve(null, table));
        assertNull(IotOpcUaNodeIdResolver.resolve("x=1", table));
        // 命名空间不在服务端的命名空间表中
        assertNull(IotOpcUaNodeIdResolver.resolve("nsu=urn:not-exists;s=a", table));
    }

}
