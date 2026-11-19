package cn.iocoder.yudao.framework.mybatis.core.type;

import org.apache.ibatis.type.ClobTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Oracle CLOB 参数的类型转换器实现类
 *
 * 写入时通过 {@link PreparedStatement#setClob(int, java.io.Reader)} 按 CLOB 绑定，读取沿用 {@link ClobTypeHandler}。
 * 用于 Oracle JSON_TABLE 等需要传入超长 JSON 字符串的场景，避免 setString / setCharacterStream 绑定超长参数时报 ORA-01460。
 *
 * 使用时在 Oracle 的 SQL 参数上显式指定 {@code typeHandler=cn.iocoder.yudao.framework.mybatis.core.type.OracleClobTypeHandler}。
 *
 * 注意：仅按参数使用，
 * 不要注册为全局的 String 类型转换器，否则会影响其它数据库及普通字符串参数。
 *
 * @author 芋道源码
 */
public class OracleClobTypeHandler extends ClobTypeHandler {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        // Reader 由驱动在语句执行时读取，此处不关闭
        ps.setClob(i, new StringReader(parameter));
    }

}
