package cn.iocoder.yudao.module.iot.framework.tdengine.core;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

/**
 * TDEngine 表字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TDengineTableField {

    /**
     * 字段名 - TDengine 默认 ts 字段，默认会被 TDengine 创建
     */
    public static final String FIELD_TS = "ts";

    public static final String TYPE_TINYINT = "TINYINT";
    public static final String TYPE_INT = "INT";
    public static final String TYPE_FLOAT = "FLOAT";
    public static final String TYPE_DOUBLE = "DOUBLE";
    public static final String TYPE_BOOL = "BOOL";
    public static final String TYPE_NCHAR = "NCHAR";
    public static final String TYPE_VARCHAR = "VARCHAR";
    public static final String TYPE_TIMESTAMP = "TIMESTAMP";

    /**
     * 字段长度 - VARCHAR 默认长度
     */
    public static final int LENGTH_VARCHAR = 1024;

    /**
     * 注释 - TAG 字段
     */
    public static final String NOTE_TAG = "TAG";

    /**
     * 字段名
     */
    private String field;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 字段长度
     */
    private Integer length;

    /**
     * 注释
     */
    private String note;

    public TDengineTableField(String field, String type) {
        this.field = field;
        this.type = type;
    }

    /**
     * 构建字段名：TDengine 默认会将字段名转为小写，需要和建表、写入、查询保持一致。
     * 例如：PT -> pt，PfT -> pf_t。
     */
    public static String buildFieldName(String field) {
        if (StrUtil.isBlank(field)) {
            return field;
        }
        return StrUtil.toUnderlineCase(field).toLowerCase(Locale.ROOT);
    }

}
