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
 * {@link DatabaseTableDAO} 的 PostgreSQL 实现类
 *
 * @author 芋道源码
 */
@Repository
public class DatabaseTablePostgreSQLDAOImpl implements DatabaseTableDAO {

    @Override
    public List<DatabaseTableDO> selectTableList(Connection connection, String tableNameLike, String tableCommentLike) {
        // 拼接 SQL
        String sql = "SELECT tbl.tablename, obj_description(c.oid)" +
                " FROM pg_tables tbl, pg_class c" +
                " WHERE tbl.schemaname = CURRENT_SCHEMA()" +
                " AND tbl.tablename = c.relname";
        if (StrUtil.isNotEmpty(tableNameLike)) {
            sql += StrUtil.format(" AND tbl.tablename LIKE '%{}%'", tableNameLike);
        }
        if (StrUtil.isNotEmpty(tableCommentLike)) {
            sql += StrUtil.format(" AND obj_description(c.oid) LIKE '%{}%'", tableCommentLike);
        }
        // 执行并返回结果
        return JdbcUtils.query(connection, sql, (rs, rowNum) -> DatabaseTableDO.builder()
                .tableName(rs.getString("tablename"))
                .tableComment(rs.getString("obj_description"))
                .build());
    }

    @Override
    public List<DatabaseColumnDO> selectColumnList(Connection connection, String tableName) {
        // 拼接 SQL
        String sql = "SELECT table_name, column_name, data_type, column_comment, ordinal_position," +
                " (CASE WHEN is_nullable = 'yes' THEN '1' ELSE '0' END) AS nullable," +
                " (CASE WHEN column_key = 'PRI' THEN '1' ELSE '0' END) AS primary_key," +
                " (CASE WHEN extra = 'auto_increment' THEN '1' ELSE '0' END) AS auto_increment" +
                " FROM information_schema.COLUMNS" +
                " WHERE table_schema = (SELECT DATABASE())" +
                String.format(" AND table_name = '%s'", tableName);
        // 执行并返回结果
        return JdbcUtils.query(connection, sql, (rs, rowNum) -> DatabaseColumnDO.builder()
                .tableName(rs.getString("table_name"))
                .columnName(rs.getString("column_name"))
                .dataType(rs.getString("data_type"))
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
