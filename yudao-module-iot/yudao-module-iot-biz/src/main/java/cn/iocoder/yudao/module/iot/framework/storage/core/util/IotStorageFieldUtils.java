package cn.iocoder.yudao.module.iot.framework.storage.core.util;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * IoT 时序存储的字段名工具类
 *
 * 属性标识符（identifier）统一归一化为小写下划线的“中立字段名”，各存储后端在此基础上映射物理列名：
 * 1. TDengine：直接使用中立字段名
 * 2. IoTDB：使用 p_ + 中立字段名，避免与 time 等内置列冲突
 *
 * @author 芋道源码
 */
public class IotStorageFieldUtils {

    /**
     * 系统字段 - 存储时间
     */
    public static final String FIELD_TS = "ts";
    /**
     * 系统字段 - 上报时间
     */
    public static final String FIELD_REPORT_TIME = "report_time";
    /**
     * 系统字段 - 设备编号
     */
    public static final String FIELD_DEVICE_ID = "device_id";

    /**
     * 保留字段名：属性标识符归一化后，不允许与它们相同
     */
    public static final Set<String> RESERVED_FIELD_NAMES = SetUtils.asSet(FIELD_TS, FIELD_REPORT_TIME, FIELD_DEVICE_ID);

    /**
     * 中立字段名的格式：只允许小写字母、数字、下划线，且以字母开头
     *
     * 动态拼接到 SQL 结构名（列名）前，必须通过该格式校验
     */
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9_]{0,63}$");

    /**
     * 构建中立字段名：转为小写下划线，需要和建表、写入、查询保持一致。
     * 例如：PT -> pt，PfT -> pf_t。
     *
     * @param identifier 属性标识符
     * @return 中立字段名
     */
    public static String buildFieldName(String identifier) {
        if (StrUtil.isBlank(identifier)) {
            return identifier;
        }
        return StrUtil.toUnderlineCase(identifier).toLowerCase(Locale.ROOT);
    }

    /**
     * 判断属性标识符，归一化后是否为合法的属性字段名（格式合法，且不是保留字段）
     *
     * @param identifier 属性标识符
     * @return 是否合法
     */
    public static boolean isPropertyFieldName(String identifier) {
        String fieldName = buildFieldName(identifier);
        return fieldName != null && FIELD_NAME_PATTERN.matcher(fieldName).matches()
                && !RESERVED_FIELD_NAMES.contains(fieldName);
    }

    /**
     * 校验属性标识符列表，可以作为存储字段。define、insert 拼接 SQL 之前，都需要校验原始标识符：
     * 1. 归一化后格式合法
     * 2. 归一化后不是 ts、report_time、device_id 保留字段
     * 3. 归一化后不重复，例如说 fooBar 与 foo_bar
     *
     * @param identifiers 属性标识符列表
     */
    public static void validatePropertyIdentifiers(Collection<String> identifiers) {
        Map<String, String> fieldNames = new HashMap<>();
        for (String identifier : identifiers) {
            String fieldName = buildFieldName(identifier);
            if (fieldName == null || !FIELD_NAME_PATTERN.matcher(fieldName).matches()) {
                throw exception(DEVICE_PROPERTY_FIELD_NAME_INVALID, identifier);
            }
            if (RESERVED_FIELD_NAMES.contains(fieldName)) {
                throw exception(DEVICE_PROPERTY_FIELD_NAME_RESERVED, identifier, fieldName);
            }
            String existsIdentifier = fieldNames.putIfAbsent(fieldName, identifier);
            if (existsIdentifier != null) {
                throw exception(DEVICE_PROPERTY_FIELD_NAME_DUPLICATE, identifier, existsIdentifier, fieldName);
            }
        }
    }

}
