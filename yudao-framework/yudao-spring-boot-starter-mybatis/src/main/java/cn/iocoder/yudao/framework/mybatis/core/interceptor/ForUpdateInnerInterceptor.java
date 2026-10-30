package cn.iocoder.yudao.framework.mybatis.core.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.mybatis.core.enums.DbTypeEnum;
import cn.iocoder.yudao.framework.mybatis.core.injector.SelectForUpdateMethod;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserConstants;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.ForMode;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.PlainSelect;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.SqlSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// TODO DONE @AI：能不能不这么搞，而是 mapperx 根据数据库做封装噢？
// 选择执行路径已在 BaseMapperX 按数据库封装（Oracle 首行锁定走多次查询，其它单条语句）；
// SQL Server 的 WITH (UPDLOCK, ROWLOCK) 无法被 JSqlParser 解析，若在 Mapper 中直接生成，租户、数据权限等插件会解析失败，
// 因此只能在所有解析 SQL 的插件之后改写。本拦截器只处理 SelectForUpdateMethod 的内置查询，不再扫描其它 SQL。
/**
 * 锁定读的拦截器，需要位于拦截器链的末尾：在 prepare 阶段处理，此时租户、数据权限、分页等插件已完成 SQL 改写
 *
 * 只处理 SqlSource 为 {@link SelectForUpdateMethod.LockSqlSource} 的内置查询，其它 SQL 不做任何处理：
 * 1. 在实际执行的连接上校验事务：要求非自动提交；Oracle 首行锁定的主键查询（{@link SelectForUpdateMethod.Kind#FIRST_IDS}）
 *    还要求隔离级别为 READ COMMITTED，其它隔离级别下，锁定后的重新读取可能读不到其它事务已提交的记录
 * 2. SQL Server 不支持 FOR UPDATE、LIMIT：将末尾的 FOR UPDATE 改写为基表的 WITH (UPDLOCK, ROWLOCK)，首行的 LIMIT 1 改写为 TOP (1)；
 *    只按 SQL 的 token 修改，字符串中的 for update 不会被改写；仅支持单表，不支持时直接报错，不会退化为不加锁的 SQL
 * 3. Oracle 不支持 LIMIT 1 FOR UPDATE：首行加锁查询（{@link SelectForUpdateMethod.Kind#LOCK_FIRST}）直接报错，
 *    正常情况下 BaseMapperX 已按数据库类型改为多次查询
 *
 * @author 芋道源码
 */
public class ForUpdateInnerInterceptor implements InnerInterceptor {

    /**
     * SQL Server 的行锁提示：UPDLOCK 持有更新锁到事务结束，与 FOR UPDATE 一样阻塞其它事务的加锁读与修改；ROWLOCK 尽量使用行锁
     */
    static final String SQL_SERVER_LOCK_HINTS = " WITH (UPDLOCK, ROWLOCK)";
    /**
     * SQL Server 的首行限制，替代 LIMIT 1
     */
    static final String SQL_SERVER_TOP_ONE = " TOP (1)";

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        SqlSource sqlSource = mpSh.mappedStatement().getSqlSource();
        if (!(sqlSource instanceof SelectForUpdateMethod.LockSqlSource)) {
            return;
        }
        SelectForUpdateMethod.Kind kind = ((SelectForUpdateMethod.LockSqlSource) sqlSource).getKind();
        // 1. 校验实际连接的事务
        checkTransaction(connection, kind);
        // 2. 按实际连接的数据库类型处理：首行主键查询不加锁，无需改写
        if (kind == SelectForUpdateMethod.Kind.FIRST_IDS) {
            return;
        }
        DbType dbType = getDbType(connection);
        if (JdbcUtils.isSQLServer(dbType)) {
            PluginUtils.MPBoundSql mpBoundSql = mpSh.mPBoundSql();
            mpBoundSql.sql(rewriteForSqlServer(mpBoundSql.sql(), kind == SelectForUpdateMethod.Kind.LOCK_FIRST));
            return;
        }
        if (kind == SelectForUpdateMethod.Kind.LOCK_FIRST && ObjectUtils.equalsAny(dbType, DbType.ORACLE, DbType.ORACLE_12C)) {
            throw new IllegalStateException("Oracle 不支持 LIMIT 1 FOR UPDATE，首行锁定需要使用多次查询");
        }
    }

    /**
     * 校验连接在事务中；首行主键查询还要求隔离级别为 READ COMMITTED
     *
     * @param connection 实际执行的连接
     * @param kind       内置查询的类型
     */
    static void checkTransaction(Connection connection, SelectForUpdateMethod.Kind kind) {
        try {
            if (connection.getAutoCommit()) {
                throw new IllegalStateException("锁定读需要在事务中执行，当前连接为自动提交");
            }
            if (kind != SelectForUpdateMethod.Kind.FIRST_IDS) {
                return;
            }
            int isolation = connection.getTransactionIsolation();
            if (isolation != Connection.TRANSACTION_READ_COMMITTED) {
                throw new IllegalStateException("首行锁定要求事务隔离级别为 READ COMMITTED，当前为：" + isolation);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("获取连接的事务状态失败", e);
        }
    }

    private static DbType getDbType(Connection connection) {
        try {
            return DbTypeEnum.find(connection.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            throw new IllegalStateException("获取数据库类型失败", e);
        }
    }

    /**
     * 将单表 SELECT 末尾的 FOR UPDATE 改写为表的 WITH (UPDLOCK, ROWLOCK)；首行时将 LIMIT 1 改写为 TOP (1)
     *
     * @param sql   SQL
     * @param first 是否为首行加锁查询
     * @return 改写后的 SQL
     */
    static String rewriteForSqlServer(String sql, boolean first) {
        Statement statement;
        try {
            statement = SqlTokenUtils.parse(sql);
        } catch (JSQLParserException e) {
            throw new IllegalStateException("解析行锁 SQL 失败：" + sql, e);
        }
        // 校验：仅支持单表、无修饰的 FOR UPDATE
        if (!(statement instanceof PlainSelect)) {
            throw unsupported("仅支持单个 SELECT", sql);
        }
        PlainSelect plainSelect = (PlainSelect) statement;
        if (plainSelect.getForMode() != ForMode.UPDATE || plainSelect.getForUpdateTable() != null
                || plainSelect.getWait() != null || plainSelect.isNoWait() || plainSelect.isSkipLocked()) {
            throw unsupported("仅支持 FOR UPDATE，不支持 OF、WAIT、NOWAIT、SKIP LOCKED 等", sql);
        }
        if (CollUtil.isNotEmpty(plainSelect.getWithItemsList())) {
            throw unsupported("不支持 WITH", sql);
        }
        if (!(plainSelect.getFromItem() instanceof Table) || CollUtil.isNotEmpty(plainSelect.getJoins())) {
            throw unsupported("仅支持单表", sql);
        }
        Table table = (Table) plainSelect.getFromItem();
        if (table.getSqlServerHints() != null || table.getIndexHint() != null || table.getSampleClause() != null
                || table.getPivot() != null || table.getUnPivot() != null) {
            throw unsupported("表已有提示等修饰", sql);
        }
        if (plainSelect.getTop() != null || plainSelect.getOffset() != null || plainSelect.getFetch() != null) {
            throw unsupported("不支持 TOP、OFFSET、FETCH", sql);
        }
        Limit limit = plainSelect.getLimit();
        if (first ? !isLimitOne(limit) || plainSelect.getDistinct() != null : limit != null) {
            throw unsupported(first ? "首行仅支持 LIMIT 1，且不支持 DISTINCT" : "不支持 LIMIT", sql);
        }
        SimpleNode tableNode = table.getASTNode();
        if (tableNode == null) {
            throw unsupported("无法定位表", sql);
        }

        // 末尾的 FOR UPDATE：表之后的 token 中，最后两个（不含分号）
        List<Token> tokens = new ArrayList<>();
        for (Token token = tableNode.jjtGetLastToken().next; token != null && token.kind != CCJSqlParserConstants.EOF;
             token = token.next) {
            tokens.add(token);
        }
        while (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == CCJSqlParserConstants.ST_SEMICOLON) {
            tokens.remove(tokens.size() - 1);
        }
        if (tokens.size() < 2 || tokens.get(tokens.size() - 2).kind != CCJSqlParserConstants.K_FOR
                || tokens.get(tokens.size() - 1).kind != CCJSqlParserConstants.K_UPDATE) {
            throw unsupported("FOR UPDATE 不在末尾", sql);
        }

        // 删除 FOR UPDATE，在表（含别名）之后增加提示
        int[] lineStarts = SqlTokenUtils.lineStarts(sql);
        Token forToken = tokens.get(tokens.size() - 2);
        Token updateToken = tokens.get(tokens.size() - 1);
        int tableEnd = SqlTokenUtils.end(sql, lineStarts, tableNode.jjtGetLastToken());
        List<SqlTokenUtils.Patch> patches = new ArrayList<>();
        patches.add(new SqlTokenUtils.Patch(SqlTokenUtils.begin(sql, lineStarts, forToken),
                SqlTokenUtils.end(sql, lineStarts, updateToken), ""));
        patches.add(new SqlTokenUtils.Patch(tableEnd, tableEnd, SQL_SERVER_LOCK_HINTS));
        // 首行：删除 LIMIT 1，在 SELECT 之后增加 TOP (1)
        if (first) {
            SimpleNode limitNode = limit.getASTNode();
            SimpleNode selectNode = plainSelect.getASTNode();
            if (limitNode == null || selectNode == null
                    || selectNode.jjtGetFirstToken().kind != CCJSqlParserConstants.K_SELECT) {
                throw unsupported("无法定位 SELECT、LIMIT", sql);
            }
            int selectEnd = SqlTokenUtils.end(sql, lineStarts, selectNode.jjtGetFirstToken());
            patches.add(new SqlTokenUtils.Patch(selectEnd, selectEnd, SQL_SERVER_TOP_ONE));
            patches.add(new SqlTokenUtils.Patch(SqlTokenUtils.begin(sql, lineStarts, limitNode.jjtGetFirstToken()),
                    SqlTokenUtils.end(sql, lineStarts, limitNode.jjtGetLastToken()), ""));
        }
        return SqlTokenUtils.apply(sql, patches);
    }

    private static boolean isLimitOne(Limit limit) {
        if (limit == null || limit.getOffset() != null) {
            return false;
        }
        Expression rowCount = limit.getRowCount();
        return rowCount instanceof LongValue && ((LongValue) rowCount).getValue() == 1L;
    }

    private static IllegalStateException unsupported(String reason, String sql) {
        return new IllegalStateException("SQL Server 不支持的行锁查询（" + reason + "）：" + sql);
    }

}
