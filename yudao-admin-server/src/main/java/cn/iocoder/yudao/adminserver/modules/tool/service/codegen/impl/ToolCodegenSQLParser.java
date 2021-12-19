package cn.iocoder.yudao.adminserver.modules.tool.service.codegen.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.adminserver.modules.tool.dal.dataobject.codegen.ToolSchemaColumnDO;
import cn.iocoder.yudao.adminserver.modules.tool.dal.dataobject.codegen.ToolSchemaTableDO;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLTableElement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.repository.SchemaRepository;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.alibaba.druid.sql.SQLUtils.normalize;

/**
 * SQL 解析器，将创建表的 SQL，解析成 {@link ToolSchemaTableDO} 和 {@link ToolSchemaColumnDO} 对象，
 * 后续可以基于它们，生成代码~
 *
 * @author 芋道源码
 */
public class ToolCodegenSQLParser {

    /**
     * 解析建表 SQL 语句，返回 {@link ToolSchemaTableDO} 和 {@link ToolSchemaColumnDO} 对象
     *
     * @param sql 建表 SQL 语句
     * @return 解析结果
     */
    public static KeyValue<ToolSchemaTableDO, List<ToolSchemaColumnDO>> parse(String sql) {
        // 解析 SQL 成 Statement
        SQLCreateTableStatement statement = parseCreateSQL(sql);
        // 解析 Table 表
        ToolSchemaTableDO table = parseTable(statement);
        // 解析 Column 字段
        List<ToolSchemaColumnDO> columns = parseColumns(statement);
        columns.forEach(column -> column.setTableName(table.getTableName()));
        // 返回
        return new DefaultKeyValue<>(table, columns);
    }

    /**
     * 使用 Druid 工具，建表 SQL 语句
     *
     * @param sql 建表 SQL 语句
     * @return 创建 Statement
     */
    private static SQLCreateTableStatement parseCreateSQL(String sql) {
        // 解析 SQL
        SchemaRepository repository = new SchemaRepository(DbType.mysql);
        repository.console(sql);
        // 获得该表对应的 MySqlCreateTableStatement 对象
        String tableName = CollUtil.getFirst(repository.getDefaultSchema().getObjects()).getName();
        return (MySqlCreateTableStatement) repository.findTable(tableName).getStatement();
    }

    private static ToolSchemaTableDO parseTable(SQLCreateTableStatement statement) {
        return ToolSchemaTableDO.builder()
                .tableName(statement.getTableSource().getTableName(true))
                .tableComment(getCommentText(statement))
                .build();
    }

    private static String getCommentText(SQLCreateTableStatement statement) {
        if (statement == null || statement.getComment() == null) {
            return "";
        }
        return ((SQLCharExpr) statement.getComment()).getText();
    }

    private static List<ToolSchemaColumnDO> parseColumns(SQLCreateTableStatement statement) {
        List<ToolSchemaColumnDO> columns = new ArrayList<>();
        statement.getTableElementList().forEach(element -> parseColumn(columns, element));
        return columns;
    }

    private static void parseColumn(List<ToolSchemaColumnDO> columns, SQLTableElement element) {
        // 处理主键
        if (element instanceof SQLPrimaryKey) {
            parsePrimaryKey(columns, (SQLPrimaryKey) element);
            return;
        }
        // 处理字段定义
        if (element instanceof SQLColumnDefinition) {
            parseColumnDefinition(columns, (SQLColumnDefinition) element);
        }
    }

    private static void parsePrimaryKey(List<ToolSchemaColumnDO> columns, SQLPrimaryKey primaryKey) {
        String columnName = normalize(primaryKey.getColumns().get(0).toString()); // 暂时不考虑联合主键
        // 匹配 columns 主键字段，设置为 primary
        columns.stream().filter(column -> column.getColumnName().equals(columnName))
            .forEach(column -> column.setPrimaryKey(true));
    }

    private static void parseColumnDefinition(List<ToolSchemaColumnDO> columns, SQLColumnDefinition definition) {
        String text = definition.toString().toUpperCase();
        columns.add(ToolSchemaColumnDO.builder()
                .columnName(normalize(definition.getColumnName()))
                .columnType(definition.getDataType().toString())
                .columnComment(Objects.isNull(definition.getComment()) ? ""
                        : normalize(definition.getComment().toString()))
                .nullable(!text.contains(" NOT NULL"))
                .primaryKey(false)
                .autoIncrement(text.contains("AUTO_INCREMENT"))
                .ordinalPosition(columns.size() + 1)
                .build());
    }

}
