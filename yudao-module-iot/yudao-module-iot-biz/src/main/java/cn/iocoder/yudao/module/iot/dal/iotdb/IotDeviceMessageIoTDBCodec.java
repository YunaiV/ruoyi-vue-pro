package cn.iocoder.yudao.module.iot.dal.iotdb;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceMessageDO;

/**
 * IoTDB 设备消息的编解码器
 *
 * 为什么需要编码？IoTDB 相同 (device_id, time) 再次写入时，NULL 值不会清空旧值，导致“整行替换”的语义不成立，
 * 例如说同一设备、同一毫秒的两条消息（id 不同），会混合成一行。因此，所有 FIELD 都编码为非 NULL 值，每次写入都设置全部 FIELD
 * （id 为 FIELD，原样存储，不编码）：
 * 1. Boolean：-1 为 null，0 为 false，1 为 true
 * 2. 其它：n: 为 null，v: + 字符串为非 null 值。例如说：null -> "n:"，"" -> "v:"，123 -> "v:123"
 *
 * 查询条件中的值，也需要使用相同的编码
 *
 * @author 芋道源码
 */
public class IotDeviceMessageIoTDBCodec {

    public static final int BOOLEAN_NULL = -1;
    public static final int BOOLEAN_FALSE = 0;
    public static final int BOOLEAN_TRUE = 1;

    public static final String PREFIX_NULL = "n:";
    public static final String PREFIX_VALUE = "v:";

    // ========== 消息 ==========

    public static IotDeviceMessageIoTDBRow encode(IotDeviceMessageDO message) {
        Assert.notNull(message.getTs(), "消息的 ts 不能为空");
        Assert.notNull(message.getDeviceId(), "消息的 deviceId 不能为空");
        Assert.notEmpty(message.getId(), "消息的 id 不能为空");
        return new IotDeviceMessageIoTDBRow().setTs(message.getTs())
                .setDeviceId(encodeDeviceId(message.getDeviceId())).setId(message.getId())
                .setUpstream(encodeBoolean(message.getUpstream())).setReply(encodeBoolean(message.getReply()))
                .setMethod(encodeString(message.getMethod())).setIdentifier(encodeString(message.getIdentifier()))
                .setRequestId(encodeString(message.getRequestId())).setServerId(encodeString(message.getServerId()))
                .setParams(encodeString(toJsonString(message.getParams())))
                .setData(encodeString(toJsonString(message.getData())))
                .setMsg(encodeString(message.getMsg())).setReportTime(encodeString(message.getReportTime()))
                .setTenantId(encodeString(message.getTenantId())).setCode(encodeString(message.getCode()))
                .setRaw(encodeString(message.getRaw()));
    }

    public static IotDeviceMessageDO decode(IotDeviceMessageIoTDBRow row) {
        String reportTime = decodeString(row.getReportTime());
        String tenantId = decodeString(row.getTenantId());
        String code = decodeString(row.getCode());
        return new IotDeviceMessageDO().setTs(row.getTs())
                .setDeviceId(row.getDeviceId() != null ? Long.valueOf(row.getDeviceId()) : null).setId(row.getId())
                .setUpstream(decodeBoolean(row.getUpstream())).setReply(decodeBoolean(row.getReply()))
                .setMethod(decodeString(row.getMethod())).setIdentifier(decodeString(row.getIdentifier()))
                .setRequestId(decodeString(row.getRequestId())).setServerId(decodeString(row.getServerId()))
                .setParams(decodeString(row.getParams())).setData(decodeString(row.getData()))
                .setMsg(decodeString(row.getMsg()))
                .setReportTime(reportTime != null ? Long.valueOf(reportTime) : null)
                .setTenantId(tenantId != null ? Long.valueOf(tenantId) : null)
                .setCode(code != null ? Integer.valueOf(code) : null)
                .setRaw(decodeString(row.getRaw()));
    }

    /**
     * 转换 params、data 为字符串：Service 写入前已转为 JSON 字符串，这里兼容其它对象
     */
    private static String toJsonString(Object value) {
        if (value == null || value instanceof String) {
            return (String) value;
        }
        return JsonUtils.toJsonString(value);
    }

    // ========== 单个值 ==========

    public static String encodeDeviceId(Long deviceId) {
        return deviceId != null ? deviceId.toString() : null;
    }

    public static int encodeBoolean(Boolean value) {
        if (value == null) {
            return BOOLEAN_NULL;
        }
        return value ? BOOLEAN_TRUE : BOOLEAN_FALSE;
    }

    /**
     * 解码 Boolean
     *
     * @param value 存储值；为 null 时，说明该行不是本系统写入的，按 null 处理
     * @return Boolean
     */
    public static Boolean decodeBoolean(Integer value) {
        if (value == null || value == BOOLEAN_NULL) {
            return null;
        }
        if (value == BOOLEAN_TRUE) {
            return true;
        }
        if (value == BOOLEAN_FALSE) {
            return false;
        }
        throw new IllegalArgumentException(StrUtil.format("无法解码 Boolean 存储值({})", value));
    }

    public static String encodeString(Object value) {
        if (value == null) {
            return PREFIX_NULL;
        }
        return PREFIX_VALUE + value;
    }

    /**
     * 解码字符串
     *
     * @param value 存储值；为 null 时，说明该行不是本系统写入的，按 null 处理
     * @return 字符串
     */
    public static String decodeString(String value) {
        if (value == null || PREFIX_NULL.equals(value)) {
            return null;
        }
        if (value.startsWith(PREFIX_VALUE)) {
            return value.substring(PREFIX_VALUE.length());
        }
        throw new IllegalArgumentException(StrUtil.format("无法解码字符串存储值({})", value));
    }

}
