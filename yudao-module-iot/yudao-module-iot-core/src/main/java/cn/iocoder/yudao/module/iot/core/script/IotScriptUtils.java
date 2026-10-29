package cn.iocoder.yudao.module.iot.core.script;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.script.dto.IotScriptResult;
import cn.iocoder.yudao.module.iot.core.util.IotDeviceMessageUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * IoT 数据解析脚本的契约：入参构造与输出校验
 * <p>
 * 网关所有协议和 biz 调试共用同一份规则，结果一致。任何一项不合法就整体拒绝（{@link IotScriptResult.Status#OUTPUT_INVALID}），不截断、不部分写出
 *
 * @author 芋道源码
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IotScriptUtils {

    public static final String FUNCTION_DECODE = "decode";
    public static final String FUNCTION_ENCODE = "encode";

    /**
     * 消息 params / data 序列化后的最大 UTF-8 字节数
     */
    public static final int MAX_FIELD_BYTES = 8192;
    public static final int MAX_REQUEST_ID_LENGTH = 50;
    public static final int MAX_IDENTIFIER_LENGTH = 100;
    public static final int MAX_MSG_LENGTH = 256;

    private static final Set<String> TOP_ENCODE_KEYS = new HashSet<>(Arrays.asList("bytes", "standard"));
    /**
     * decode 输出的消息允许的键：身份、路由与可信字段（deviceId、tenantId、serverId、productKey、deviceName、id、reportTime、raw）
     * 和其它未知键都不允许
     */
    private static final Set<String> MESSAGE_KEYS = new HashSet<>(Arrays.asList(
            "method", "params", "requestId", "code", "msg", "data"));
    /**
     * decode 允许的请求方法
     */
    private static final Set<String> DECODE_REQUEST_METHODS = new HashSet<>(Arrays.asList(
            IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(), IotDeviceMessageMethodEnum.EVENT_POST.getMethod()));

    // ========== 入参构造 ==========

    /**
     * 构造 decode 的入参 JSON
     *
     * @param protocol   协议类型
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @param receivedAt 接收时间（毫秒时间戳）
     * @param bytes      原始字节
     * @param request    encode 下行的原请求（协议回复设备的实际响应时）；普通上行为 null
     * @return JSON
     */
    public static String buildDecodeContext(String protocol, String productKey, String deviceName,
                                            long receivedAt, byte[] bytes, IotDeviceMessage request) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("direction", FUNCTION_DECODE);
        ctx.put("protocol", protocol);
        ctx.put("productKey", productKey);
        ctx.put("deviceName", deviceName);
        ctx.put("receivedAt", receivedAt);
        ctx.put("bytes", toUnsignedArray(bytes));
        ctx.put("hex", HexUtil.encodeHexStr(bytes, false));
        if (request != null) {
            ctx.put("request", buildMessageContext(request));
        }
        return JsonUtils.toJsonString(ctx);
    }

    /**
     * 构造 encode 的入参 JSON
     *
     * @param protocol   协议类型
     * @param productKey 产品标识
     * @param deviceName 设备名称
     * @param message    下行指令（property.set / service.invoke）
     * @return JSON
     */
    public static String buildEncodeContext(String protocol, String productKey, String deviceName,
                                            IotDeviceMessage message) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("direction", FUNCTION_ENCODE);
        ctx.put("protocol", protocol);
        ctx.put("productKey", productKey);
        ctx.put("deviceName", deviceName);
        ctx.put("message", buildMessageContext(message));
        return JsonUtils.toJsonString(ctx);
    }

    private static Map<String, Object> buildMessageContext(IotDeviceMessage message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("method", message.getMethod());
        map.put("requestId", message.getRequestId());
        map.put("params", message.getParams());
        return map;
    }

    private static int[] toUnsignedArray(byte[] bytes) {
        return IntStream.range(0, bytes.length).map(i -> bytes[i] & 0xFF).toArray();
    }

    // ========== 输出校验 ==========

    /**
     * 校验 decode 输出：一个标准消息对象（method、params、requestId、code、msg、data），与阿里云 rawDataToProtocol 一致
     *
     * @param json    脚本输出；null 表示没有定义 decode，或返回了 null / undefined，即没有业务消息
     * @param request encode 下行的原请求（协议回复设备的实际响应时）：此时必须返回回执，方法和 requestId 必须与原请求一致；普通上行为 null
     * @return 业务消息（编号、时间、身份、raw 由调用方可信设置）；null 表示没有业务消息（只有普通上行允许）
     * @throws IotScriptException 输出不合法
     */
    public static IotDeviceMessage validateDecode(String json, IotDeviceMessage request) throws IotScriptException {
        // 1. 没有业务消息：普通上行允许（只记录原始消息）；事务响应必须有回执
        if (json == null) {
            if (request != null) {
                throw IotScriptException.outputInvalid("事务响应必须返回回执");
            }
            return null;
        }
        // 2. 校验消息
        IotDeviceMessage message = validateMessage(parseObject(json, MESSAGE_KEYS));
        if (request != null) {
            validateTransactionReply(message, request);
        }
        return message;
    }

    /**
     * 校验 encode 输出：脚本输出的结构，以及透传字节是否符合协议的规则（{@link IotDeviceMessageUtils#validateRawPayload}，Modbus 必须是合法的请求 PDU）
     * <p>
     * 网关的 encode、biz 的脚本调试共用；raw.push 不经过 encode，由调用方自己校验
     *
     * @param json     脚本输出；null 表示没有定义 encode，或返回了 null / undefined
     * @param protocol 协议类型：透传字节按它的规则校验
     * @return null 表示没有生成下行数据（调用方只记日志，不写设备）
     * @throws IotScriptException 输出不合法
     */
    public static EncodeOutput validateEncode(String json, IotProtocolTypeEnum protocol) throws IotScriptException {
        // 1. 没有生成下行数据
        if (json == null) {
            return null;
        }
        // 2. 解析对象：只允许 bytes、standard，且只能返回一个
        JsonNode root = parseObject(json, TOP_ENCODE_KEYS);
        JsonNode standard = root.get("standard");
        JsonNode bytesNode = root.get("bytes");
        if (standard != null && bytesNode != null) {
            throw IotScriptException.outputInvalid("bytes 和 standard 只能返回一个");
        }
        // 3.1 走现有的标准路径
        if (standard != null) {
            if (!standard.isBoolean() || !standard.booleanValue()) {
                throw IotScriptException.outputInvalid("standard 只能是 true");
            }
            return new EncodeOutput(true, null);
        }
        // 3.2 透传字节：按协议的规则校验
        byte[] bytes = parseBytes(bytesNode, "bytes");
        try {
            IotDeviceMessageUtils.validateRawPayload(protocol, bytes);
        } catch (IllegalArgumentException e) {
            throw IotScriptException.outputInvalid(e.getMessage());
        }
        return new EncodeOutput(false, bytes);
    }

    private static JsonNode parseObject(String json, Set<String> allowedKeys) throws IotScriptException {
        // 1. 解析 JSON
        JsonNode root;
        try {
            root = JsonUtils.parseTree(json);
        } catch (RuntimeException e) {
            throw IotScriptException.outputInvalid("脚本输出不是合法的 JSON");
        }
        // 2. 必须是对象，且只包含允许的键
        if (root == null || !root.isObject()) {
            throw IotScriptException.outputInvalid("脚本输出必须是对象");
        }
        validateKeys(root, allowedKeys, "脚本输出");
        return root;
    }

    private static void validateKeys(JsonNode node, Set<String> allowedKeys, String name) throws IotScriptException {
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            if (!allowedKeys.contains(key)) {
                throw IotScriptException.outputInvalid(name + " 不允许包含字段 " + StrUtil.maxLength(key, 50));
            }
        }
    }

    private static IotDeviceMessage validateMessage(JsonNode node) throws IotScriptException {
        // 1.1 method 必填
        String name = "message";
        String method = textOrNull(node, "method", name);
        if (StrUtil.isEmpty(method)) {
            throw IotScriptException.outputInvalid(name + ".method 不能为空");
        }
        // 1.2 requestId、msg 的长度
        String requestId = textOrNull(node, "requestId", name);
        if (requestId != null && requestId.length() > MAX_REQUEST_ID_LENGTH) {
            throw IotScriptException.outputInvalid(name + ".requestId 不能超过 " + MAX_REQUEST_ID_LENGTH + " 个字符");
        }
        String msg = textOrNull(node, "msg", name);
        if (msg != null && msg.length() > MAX_MSG_LENGTH) {
            throw IotScriptException.outputInvalid(name + ".msg 不能超过 " + MAX_MSG_LENGTH + " 个字符");
        }
        // 1.3 params、data 序列化后的大小
        JsonNode params = nullIfJsonNull(node.get("params"));
        JsonNode data = nullIfJsonNull(node.get("data"));
        validateFieldSize(params, name + ".params");
        validateFieldSize(data, name + ".data");
        // 1.4 code：有值表示回执
        JsonNode codeNode = nullIfJsonNull(node.get("code"));
        Integer code = null;
        if (codeNode != null) {
            if (!codeNode.isIntegralNumber() || !codeNode.canConvertToInt()) {
                throw IotScriptException.outputInvalid(name + ".code 必须是整数");
            }
            code = codeNode.intValue();
        }
        // 2. 按请求、回执分别校验方法
        if (code == null) {
            // 2.1 请求：属性上报、事件上报；事件必须带 identifier
            if (!DECODE_REQUEST_METHODS.contains(method)) {
                throw IotScriptException.outputInvalid(name + ".method 不支持 " + StrUtil.maxLength(method, 50));
            }
            if (params == null || !params.isObject()) {
                throw IotScriptException.outputInvalid(name + ".params 必须是对象");
            }
            if (IotDeviceMessageMethodEnum.EVENT_POST.getMethod().equals(method)) {
                JsonNode identifier = params.get("identifier");
                if (identifier == null || !identifier.isTextual() || StrUtil.isEmpty(identifier.textValue())
                        || identifier.textValue().length() > MAX_IDENTIFIER_LENGTH) {
                    throw IotScriptException.outputInvalid(name + ".params.identifier 必须是 1~" + MAX_IDENTIFIER_LENGTH + " 个字符");
                }
            }
        } else {
            // 2.2 回执：属性设置、服务调用；requestId 不能为空
            if (!IotDeviceMessageUtils.isCommandMethod(method)) {
                throw IotScriptException.outputInvalid(name + ".method 不支持回执 " + StrUtil.maxLength(method, 50));
            }
            if (StrUtil.isEmpty(requestId)) {
                throw IotScriptException.outputInvalid(name + ".requestId 不能为空");
            }
        }
        return IotDeviceMessage.of(requestId, method, toValue(params), toValue(data), code, msg);
    }

    private static void validateTransactionReply(IotDeviceMessage reply, IotDeviceMessage request)
            throws IotScriptException {
        if (reply.getCode() == null || !Objects.equals(reply.getMethod(), request.getMethod())
                || !Objects.equals(reply.getRequestId(), request.getRequestId())) {
            throw IotScriptException.outputInvalid("事务响应的回执方法和 requestId 必须与原请求一致");
        }
    }

    private static String textOrNull(JsonNode node, String field, String name) throws IotScriptException {
        JsonNode value = nullIfJsonNull(node.get(field));
        if (value == null) {
            return null;
        }
        if (!value.isTextual()) {
            throw IotScriptException.outputInvalid(name + "." + field + " 必须是字符串");
        }
        return value.textValue();
    }

    private static void validateFieldSize(JsonNode value, String name) throws IotScriptException {
        if (value != null && value.toString().getBytes(StandardCharsets.UTF_8).length > MAX_FIELD_BYTES) {
            throw IotScriptException.outputInvalid(name + " 不能超过 " + MAX_FIELD_BYTES + " 字节");
        }
    }

    /**
     * 解析字节数组：每个元素都是 0~255 的整数；长度之后按协议的规则校验（脚本输出最多 65536 字符，数组不会过大）
     */
    private static byte[] parseBytes(JsonNode node, String name) throws IotScriptException {
        node = nullIfJsonNull(node);
        if (node == null) {
            throw IotScriptException.outputInvalid(name + " 不能为空");
        }
        if (!node.isArray()) {
            throw IotScriptException.outputInvalid(name + " 必须是数组");
        }
        byte[] bytes = new byte[node.size()];
        for (int i = 0; i < node.size(); i++) {
            JsonNode item = node.get(i);
            if (!item.isIntegralNumber() || !item.canConvertToInt() || item.intValue() < 0 || item.intValue() > 255) {
                throw IotScriptException.outputInvalid(name + "[" + i + "] 必须是 0~255 的整数");
            }
            bytes[i] = (byte) item.intValue();
        }
        return bytes;
    }

    private static JsonNode nullIfJsonNull(JsonNode node) {
        return node == null || node.isNull() ? null : node;
    }

    private static Object toValue(JsonNode node) {
        return node == null ? null : JsonUtils.getObjectMapper().convertValue(node, Object.class);
    }

    /**
     * encode 输出：二选一
     */
    @Getter
    @AllArgsConstructor
    public static final class EncodeOutput {

        /**
         * true = 走现有的标准路径
         */
        private final boolean standard;
        /**
         * 下行字节
         */
        private final byte[] bytes;

    }

}
