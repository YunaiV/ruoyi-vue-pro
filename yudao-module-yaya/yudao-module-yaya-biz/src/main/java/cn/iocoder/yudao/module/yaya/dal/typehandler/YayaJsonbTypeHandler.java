package cn.iocoder.yudao.module.yaya.dal.typehandler;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * JSON handler that writes PostgreSQL jsonb with {@link Types#OTHER} and keeps H2 tests simple.
 */
public class YayaJsonbTypeHandler extends BaseTypeHandler<Object> {

    private final Class<?> javaType;

    public YayaJsonbTypeHandler(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        String json = JsonUtils.toJsonString(parameter);
        String url = ps.getConnection().getMetaData().getURL();
        if (url != null && url.startsWith("jdbc:postgresql:")) {
            ps.setObject(i, json, Types.OTHER);
            return;
        }
        ps.setString(i, json);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private Object parse(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JsonUtils.parseObject(json, javaType);
    }

}
