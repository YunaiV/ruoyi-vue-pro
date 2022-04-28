package cn.iocoder.yudao.framework.mybatis.core.util;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.SneakyThrows;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC 工具类
 *
 * @author 芋道源码
 */
public class JdbcUtils {

    /**
     * 判断连接是否正确
     *
     * @param url      数据源连接
     * @param username 账号
     * @param password 密码
     * @return 是否正确
     */
    public static boolean isConnectionOK(String url, String username, String password) {
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 获得连接
     *
     * @param url      数据源连接
     * @param username 账号
     * @param password 密码
     * @return 是否正确
     */
    @SneakyThrows
    public static Connection getConnection(String url, String username, String password) {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 执行指定 SQL，返回查询列表
     *
     * 参考 {@link JdbcTemplate#query(String, RowMapper)} 实现，主要考虑 JdbcTemplate 不支持使用指定 Connection 查询
     *
     * @param connection 数据库连接
     * @param sql SQL
     * @param handler 行处理器
     * @return 列表
     */
    @SneakyThrows
    public static <T> List<T> query(Connection connection, String sql, RowMapper<T> handler) {
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            // 处理结果
            List<T> result = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                result.add(handler.mapRow(rs, rowNum++));
            }
            return result;
        }
    }

    /**
     * 获得 URL 对应的 DB 类型
     *
     * @param url URL
     * @return DB 类型
     */
    public static DbType getDbType(String url) {
        String name = com.alibaba.druid.util.JdbcUtils.getDbType(url, null);
        return DbType.getDbType(name);
    }

}
