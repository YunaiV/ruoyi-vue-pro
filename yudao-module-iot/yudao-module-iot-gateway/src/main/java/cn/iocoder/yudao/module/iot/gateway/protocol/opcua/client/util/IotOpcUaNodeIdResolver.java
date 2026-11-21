package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.util.IotOpcUaUtils;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;

// TODO @AI：搞到 utils 里？目前有一个呀？
/**
 * IoT OPC UA NodeId 解析器：把点位配置的 NodeId 字符串解析为本次会话的 NodeId
 * <p>
 * nsu= 写法按会话激活时读取的 NamespaceArray 解析出命名空间索引，服务端重启、命名空间顺序变化后重新解析；
 * ns= 写法直接使用配置的索引（可能随服务端变化，由调用方告警）
 *
 * @author 芋道源码
 */
public class IotOpcUaNodeIdResolver {

    /**
     * 解析 NodeId
     *
     * @param nodeId         点位配置的 NodeId 字符串
     * @param namespaceTable 本次会话的命名空间表
     * @return 解析结果；格式不合法，或 nsu= 的命名空间不在命名空间表中时返回 null
     */
    public static NodeId resolve(String nodeId, NamespaceTable namespaceTable) {
        if (StrUtil.isBlank(nodeId) || !IotOpcUaUtils.isValidNodeId(nodeId)) {
            return null;
        }
        try {
            ExpandedNodeId expandedNodeId = ExpandedNodeId.parse(nodeId);
            return expandedNodeId.toNodeId(namespaceTable).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 构建命名空间表
     *
     * @param namespaces 服务端的 NamespaceArray
     * @return 命名空间表
     */
    public static NamespaceTable buildNamespaceTable(String[] namespaces) {
        NamespaceTable table = new NamespaceTable();
        if (namespaces == null) {
            return table;
        }
        table.update(map -> {
            map.clear();
            for (int i = 0; i < namespaces.length; i++) {
                if (namespaces[i] != null) {
                    map.put(ushort(i), namespaces[i]);
                }
            }
        });
        return table;
    }

}
