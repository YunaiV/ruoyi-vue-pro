package cn.iocoder.yudao.module.iot.core.util;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

import java.util.Base64;

/**
 * IoT OPC UA 工具类
 * <p>
 * NodeId 按 OPC UA Part 6 的字符串格式做语法校验：[ns=&lt;索引&gt;; | nsu=&lt;命名空间 URI&gt;;] 加上
 * i=&lt;数字&gt; | s=&lt;字符串&gt; | g=&lt;GUID&gt; | b=&lt;Base64&gt;。
 * 推荐 nsu= 写法：命名空间索引可能随服务端重启变化，nsu= 由网关在每次会话激活后按 NamespaceArray 重新解析。
 * <p>
 * 不复用 Milo 的 NodeId.parse：biz 不依赖 OPC UA SDK（Milo 依赖的 Netty 版本与项目 BOM 不同），且它的解析更宽松
 * （例如不限制索引范围）；GUID、数字用 Hutool 的正则校验，Base64 用 JDK 解码器（与前端 DeviceOpcUaPointForm 口径一致）
 *
 * @author 芋道源码
 */
public class IotOpcUaUtils {

    /**
     * NodeId 的最大长度
     */
    public static final int NODE_ID_MAX_LENGTH = 512;

    /**
     * 校验 NodeId 的语法
     *
     * @param nodeId NodeId 字符串
     * @return 是否合法
     */
    public static boolean isValidNodeId(String nodeId) {
        if (StrUtil.isBlank(nodeId) || nodeId.length() > NODE_ID_MAX_LENGTH || !nodeId.equals(nodeId.trim())) {
            return false;
        }
        // 1. 命名空间前缀：ns=<0-65535>; 或 nsu=<非空 URI>;
        String identifier = nodeId;
        if (nodeId.startsWith("ns=")) {
            int index = nodeId.indexOf(';');
            if (index < 0 || !isUnsignedInRange(nodeId.substring(3, index), 65535L)) {
                return false;
            }
            identifier = nodeId.substring(index + 1);
        } else if (nodeId.startsWith("nsu=")) {
            int index = nodeId.indexOf(';');
            if (index <= 4) {
                return false;
            }
            // 命名空间 URI 不能为空白，也不能带首尾空白
            String namespaceUri = nodeId.substring(4, index);
            if (StrUtil.isBlank(namespaceUri) || !namespaceUri.equals(namespaceUri.trim())) {
                return false;
            }
            identifier = nodeId.substring(index + 1);
        }
        // 2. 标识：i=、s=、g=、b=
        if (identifier.length() < 3 || identifier.charAt(1) != '=') {
            return false;
        }
        String value = identifier.substring(2);
        switch (identifier.charAt(0)) {
            case 'i':
                return isUnsignedInRange(value, 4294967295L);
            case 's':
                return !value.isEmpty();
            case 'g':
                return ReUtil.isMatch(PatternPool.UUID, value);
            case 'b':
                try {
                    return Base64.getDecoder().decode(value).length > 0;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            default:
                return false;
        }
    }

    /**
     * 是否使用命名空间索引（ns=）写法：索引可能随服务端重启变化，界面给出提示
     *
     * @param nodeId NodeId 字符串
     * @return 是否使用 ns= 写法
     */
    public static boolean isNamespaceIndexNodeId(String nodeId) {
        return nodeId != null && nodeId.startsWith("ns=");
    }

    /**
     * 是否为不超过 max 的无符号整数：只允许 ASCII 数字（PatternPool.NUMBERS 的 \d 不匹配全角、阿拉伯文等数字），
     * 不允许正负号；先限制长度，避免 Long.parseLong 溢出
     */
    private static boolean isUnsignedInRange(String value, long max) {
        return value.length() <= 10 && ReUtil.isMatch(PatternPool.NUMBERS, value) && Long.parseLong(value) <= max;
    }

}
