package cn.iocoder.yudao.module.infra.dal.mysql.db;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseTableDO;
import com.baomidou.mybatisplus.annotation.DbType;

import java.sql.Connection;
import java.util.List;

/**
 * 数据库 Table DAO 接口
 *
 * @author 芋道源码
 */
public interface DatabaseTableDAO {

    /**
     * 获得表列表，基于表名称 + 表描述进行模糊匹配
     *
     * @param connection 数据库连接
     * @param tableNameLike 表名称，模糊匹配
     * @param tableCommentLike 表描述，模糊匹配
     * @return 表列表
     */
    List<DatabaseTableDO> selectTableList(Connection connection, String tableNameLike, String tableCommentLike);

    /**
     * 获得指定表名
     *
     * @param connection 数据库连接
     * @param tableName 表名称
     * @return 表
     */
    default DatabaseTableDO selectTable(Connection connection, String tableName) {
        // 考虑到对性能没有要求，直接查询列表，然后内存过滤到记录
        List<DatabaseTableDO> tables = selectTableList(connection, tableName, null);
        return CollUtil.findOne(tables, table -> table.getTableName().equalsIgnoreCase(tableName));
    }

    /**
     * 获得指定表的字段列表
     *
     * @param connection 数据库连接
     * @param tableName 表名称
     * @return 字段列表
     */
    List<DatabaseColumnDO> selectColumnList(Connection connection, String tableName);

    /**
     * 获得数据库的类型
     *
     * @return 数据库的类型
     */
    DbType getType();

}
