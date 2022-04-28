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
 * {@link DatabaseTableDAO} 的 Oracle 实现类
 *
 * @author 芋道源码
 */
@Repository
public class DatabaseTableOracleDAOImpl implements DatabaseTableDAO {

    @Override
    public List<DatabaseTableDO> selectTableList(Connection connection, String tableNameLike, String tableCommentLike) {
        // 拼接 SQL
        String sql = "SELECT tbl.table_name, col.comments, obj.created" +
                " FROM user_tables tbl, user_tab_comments col, user_objects obj" +
                " WHERE tbl.table_name = col.table_name" +
                " AND tbl.table_name = obj.object_name" +
                " AND obj.object_type = 'TABLE'";
        if (StrUtil.isNotEmpty(tableNameLike)) {
            sql += StrUtil.format(" AND tbl.table_name LIKE '%{}%'", tableNameLike);
        }
        if (StrUtil.isNotEmpty(tableCommentLike)) {
            sql += StrUtil.format(" AND col.comments LIKE '%{}%'", tableCommentLike);
        }
        // 执行并返回结果
        return JdbcUtils.query(connection, sql, (rs, rowNum) -> DatabaseTableDO.builder()
                .tableName(rs.getString("table_name"))
                .tableComment(rs.getString("comments"))
                .createTime(rs.getDate("created"))
                .build());
    }

    @Override
    public List<DatabaseColumnDO> selectColumnList(Connection connection, String tableName) {
        // 拼接 SQL
        String sql = String.format("SELECT table_name, column_name, data_type, comments, column_id," +
                "  (CASE WHEN nullable = 'Y' THEN '1' ELSE '0' END) AS nullable," +
                "  (CASE WHEN constraint_type = 'P' THEN '1' ELSE '0' END) AS primary_key" +
                " FROM" +
                " (" +
                "  SELECT col.*, comments, constraint_type," +
                "   row_number ( ) over ( partition BY col.column_name ORDER BY constraint_type DESC ) AS row_flag" +
                "  FROM user_tab_columns col" +
                "  LEFT JOIN user_col_comments ON user_col_comments.table_name = col.table_name" +
                "   AND user_col_comments.column_name = col.column_name" +
                "  LEFT JOIN user_cons_columns ON user_cons_columns.table_name = col.table_name" +
                "  AND user_cons_columns.column_name = col.column_name" +
                "  LEFT JOIN user_constraints ON user_constraints.constraint_name = user_cons_columns.constraint_name" +
                "   WHERE col.table_name = '%s'" +
                " )" +
                "WHERE row_flag = 1", tableName);
        // 执行并返回结果
        return JdbcUtils.query(connection, sql, (rs, rowNum) -> DatabaseColumnDO.builder()
                .tableName(rs.getString("table_name"))
                .columnName(rs.getString("column_name"))
                .dataType(rs.getString("data_type"))
                .columnComment(rs.getString("comments"))
                .nullable(rs.getBoolean("nullable"))
                .primaryKey(rs.getBoolean("primary_key"))
                .autoIncrement(false) // TODO 芋艿：oracle？？？
                .ordinalPosition(rs.getInt("column_id"))
                .build());
    }

    @Override
    public DbType getType() {
        return DbType.ORACLE;
    }

}
