package cn.iocoder.yudao.framework.mybatis.core.type;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Boolean 的类型转换器实现类，对应数据库的整数类型（tinyint、smallint、int2、number 等）
 *
 * 写入时 true 存 1、false 存 0；读取时非 0 为 true、0 为 false、NULL 为 null。
 * 用于数据库列是整数、而 Java 字段是 Boolean 的场景，避免 PostgreSQL 等数据库不允许 boolean 与整数隐式转换。
 *
 * 使用时在字段上配置 {@code @TableField(typeHandler = IntegerBooleanTypeHandler.class)}，
 * 并在实体上开启 {@code @TableName(autoResultMap = true)}，使查询结果也使用该转换器。
 * 自定义 SQL 或 Wrapper 的独立参数需显式指定此转换器。
 *
 * 注意：仅按字段使用，
 * 不要注册为全局的 Boolean 类型转换器，否则会影响真正的 boolean / bit 列。
 *
 * @author 芋道源码
 */
public class IntegerBooleanTypeHandler extends BaseTypeHandler<Boolean> {

    @Override
    public void setParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            // 未显式指定 jdbcType 时，按整数类型写入 NULL，避免 JdbcType.OTHER 在部分驱动上无法推断类型
            ps.setNull(i, jdbcType == null || jdbcType == JdbcType.OTHER ? Types.INTEGER : jdbcType.TYPE_CODE);
            return;
        }
        setNonNullParameter(ps, i, parameter, jdbcType);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Boolean parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter ? 1 : 0);
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value != 0;
    }

    @Override
    public Boolean getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int value = rs.getInt(columnIndex);
        return rs.wasNull() ? null : value != 0;
    }

    @Override
    public Boolean getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int value = cs.getInt(columnIndex);
        return cs.wasNull() ? null : value != 0;
    }

}
