package cn.iocoder.yudao.framework.mybatis.core.type;

import cn.hutool.core.lang.Assert;
import cn.hutool.extra.spring.SpringUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jasypt.encryption.StringEncryptor;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 字段字段的 TypeHandler 实现类，基于 {@link StringEncryptor} 实现
 * 可通过 jasypt.encryptor.password 配置项，设置密钥
 *
 * @author 芋道源码
 */
public class EncryptTypeHandler extends BaseTypeHandler<String> {

    private static StringEncryptor encryptor;

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, getEncryptor().encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decrypt(value);
    }

    private static String decrypt(String value) {
        if (value == null) {
            return null;
        }
        return getEncryptor().decrypt(value);
    }

    public static String encrypt(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        return getEncryptor().encrypt(rawValue);
    }

    private static StringEncryptor getEncryptor() {
        if (encryptor != null) {
            return encryptor;
        }
        encryptor = SpringUtil.getBean(StringEncryptor.class);
        Assert.notNull(encryptor, "StringEncryptor 不能为空");
        return encryptor;
    }

}
