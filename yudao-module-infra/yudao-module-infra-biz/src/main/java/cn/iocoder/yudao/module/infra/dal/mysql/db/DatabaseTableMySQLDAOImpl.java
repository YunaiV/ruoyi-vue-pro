package cn.iocoder.yudao.module.infra.dal.mysql.db;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseTableDO;
import com.baomidou.mybatisplus.annotation.DbType;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.util.List;

/**
 * {@link DatabaseTableDAO} 的 MySQL 实现类
 *
 * @author 芋道源码
 */
@Repository
public class DatabaseTableMySQLDAOImpl implements DatabaseTableDAO {

    @Override
    public List<DatabaseTableDO> selectTableList(Connection connection, String tableNameLike, String tableCommentLike) {
        // 拼接 SQL
        String sql = "SELECT table_name, table_comment, create_time" +
                " FROM information_schema.TABLES" +
                " WHERE table_schema = (SELECT DATABASE())";
        if (StrUtil.isNotEmpty(tableNameLike)) {
            sql += StrUtil.format(" AND table_name LIKE '%{}%'", tableNameLike);
        }
        if (StrUtil.isNotEmpty(tableCommentLike)) {
            sql += StrUtil.format(" AND table_comment LIKE '%{}%'", tableCommentLike);
        }
        // 执行并返回结果
        return JdbcUtils.query(connection, sql, (rs, rowNum) -> DatabaseTableDO.builder()
                .tableName(rs.getString("table_name"))
                .tableComment(rs.getString("table_comment"))
                .createTime(rs.getDate("create_time"))
                .build());
    }

    @Override
    public List<DatabaseColumnDO> selectColumnList(Connection connection, String tableName) {
        // 拼接 SQL
        String sql = "SELECT table_name, column_name, column_type, column_comment, ordinal_position" +
                " (CASE WHEN is_nullable = 'yes' THEN '1' ELSE '0' END) AS nullable," +
                " (CASE WHEN column_key = 'PRI' THEN '1' ELSE '0' END) AS primary_key," +
                " (CASE WHEN extra = 'auto_increment' THEN '1' ELSE '0' END) AS auto_increment," +
                " FROM information_schema.COLUMNS" +
                " WHERE table_schema = (SELECT DATABASE())" +
                String.format(" AND table_name = '%s'", tableName);
        // 执行并返回结果
        return JdbcUtils.query(connection, sql, (rs, rowNum) -> DatabaseColumnDO.builder()
                .tableName(rs.getString("table_name"))
                .columnName(rs.getString("column_name"))
                .columnType(rs.getString("column_type"))
                .columnComment(rs.getString("column_comment"))
                .nullable(rs.getBoolean("nullable"))
                .primaryKey(rs.getBoolean("primary_key"))
                .autoIncrement(rs.getBoolean("auto_increment"))
                .ordinalPosition(rs.getInt("ordinal_position"))
                .build());
    }

    @Override
    public DbType getType() {
        return DbType.MYSQL;
    }

}
