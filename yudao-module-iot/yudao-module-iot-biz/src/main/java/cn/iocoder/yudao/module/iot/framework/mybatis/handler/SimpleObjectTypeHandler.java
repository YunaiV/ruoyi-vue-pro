package cn.iocoder.yudao.module.iot.framework.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 简单的 Object 类型处理器
 * 直接返回字符串，避免 JSON 解析问题
 *
 * @author 芋道源码
 */
public class SimpleObjectTypeHandler extends BaseTypeHandler<Object> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName); // 直接返回字符串
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return rs.getString(columnIndex); // 直接返回字符串
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return cs.getString(columnIndex); // 直接返回字符串
    }
}