package cn.iocoder.yudao.module.report.service.goview;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.module.report.controller.admin.goview.vo.data.GoViewDataRespVO;
import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.SQLParserFeature;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.druid.wall.WallCheckResult;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallProvider;
import com.alibaba.druid.wall.spi.MySqlWallProvider;
import com.alibaba.druid.wall.spi.PGWallProvider;
import com.baomidou.dynamic.datasource.tx.TransactionContext;
import com.google.common.collect.Maps;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.SqlRowSetResultSetExtractor;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.report.enums.ErrorCodeConstants.*;

/**
 * GoView 数据 Service 实现类
 *
 * 补充说明：
 * 1. 目前默认使用 jdbcTemplate 查询项目配置的数据源。如果你想查询其它数据源，可以新建对应数据源的 jdbcTemplate 来实现。
 * 2. 默认数据源是 MySQL 关系数据源，可能数据量比较大的情况下，会比较慢，可以考虑后续使用 Click House 等等。
 * 3. SQL 查询只允许读操作，与数据库账号是否有写权限无关：
 *    3.1 使用 Druid 的 SQL 防火墙（WallProvider）校验为单条 SELECT，并执行 Druid 输出的 SQL，见 {@link #validateSql(String, DbType)}
 *    3.2 在 REQUIRES_NEW 的独立事务中，由数据库强制只读，仅支持 MySQL、MariaDB、PostgreSQL；
 *        其它数据库（包括 Oracle：自治事务函数可以绕过只读事务）未经验证，直接拒绝，不会退化为普通查询
 *    3.3 查询语句超时 {@link #QUERY_TIMEOUT_SECONDS} 秒，最多 {@link #MAX_ROWS} 行，只对当前语句设置，不修改共享的 jdbcTemplate、事务管理器
 * 4. 只读不等于数据权限：不限制可以读取的表、列与租户（jdbcTemplate 也不经过 MyBatis 的租户、数据权限插件），
 *    读取范围仍取决于 report:go-view-data:get-by-sql 菜单权限与数据库账号的权限
 *
 * @author 芋道源码
 */
@Service
@Validated
public class GoViewDataServiceImpl implements GoViewDataService {

    /**
     * 查询语句的超时时间，单位：秒
     */
    static final int QUERY_TIMEOUT_SECONDS = 300;
    /**
     * 查询结果的最大行数
     */
    static final int MAX_ROWS = 100000;
    /**
     * SQL 的最大长度，单位：字符
     */
    static final int MAX_SQL_LENGTH = 100000;

    static final String SET_TRANSACTION_READ_ONLY_SQL = "SET TRANSACTION READ ONLY";
    static final String START_TRANSACTION_READ_ONLY_SQL = "START TRANSACTION READ ONLY";
    /**
     * MySQL 驱动的 setMaxRows 通过会话变量 SQL_SELECT_LIMIT 实现，查询后需要复原，避免影响连接池中该连接的后续查询
     */
    static final String RESET_SELECT_LIMIT_SQL = "SET SQL_SELECT_LIMIT = DEFAULT";

    /**
     * 支持只读事务的数据库：key 为 DatabaseMetaData#getDatabaseProductName；MariaDB 使用 MySQL 的语法
     */
    private static final Map<String, DbType> DB_TYPES = MapUtil.<String, DbType>builder()
            .put("MySQL", DbType.mysql).put("MariaDB", DbType.mysql).put("PostgreSQL", DbType.postgresql).build();

    /**
     * 在 Druid 默认禁止的函数（sleep、benchmark、load_file、pg_sleep、set_config 等）之外，额外禁止的函数：
     * 只读事务拦不住它们的副作用
     */
    private static final Set<String> DENY_FUNCTIONS = SetUtils.asSet(
            // 修改会话中的自增 ID：回滚后仍保留在连接上
            "last_insert_id",
            // 会话级的锁：回滚后仍保留在连接池的连接上
            "get_lock", "release_lock", "release_all_locks",
            "pg_advisory_lock", "pg_advisory_lock_shared", "pg_try_advisory_lock", "pg_try_advisory_lock_shared",
            "pg_advisory_unlock", "pg_advisory_unlock_shared", "pg_advisory_unlock_all",
            // 另开连接执行，不受当前只读事务约束；dblink_* 由 validateTokens 统一禁止
            "dblink",
            // 字符串中的动态 SQL，不经过 Druid 校验
            "query_to_xml", "query_to_xml_and_xmlschema", "cursor_to_xml",
            // 服务端文件、管理操作
            "lo_import", "lo_export", "pg_file_write", "pg_file_unlink", "pg_file_rename",
            "pg_terminate_backend", "pg_cancel_backend", "pg_reload_conf");

    private static final WallProvider MYSQL_WALL_PROVIDER = initWallProvider(new MySqlWallProvider());
    private static final WallProvider POSTGRESQL_WALL_PROVIDER = initWallProvider(new PGWallProvider());

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    public GoViewDataRespVO getDataBySQL(String sql) {
        // 1. dynamic-datasource 本地事务（@DSTransactional）的连接由其代理，无法开启独立的只读事务
        if (StrUtil.isNotEmpty(TransactionContext.getXID())) {
            throw exception(GO_VIEW_SQL_TRANSACTION_NOT_SUPPORTED);
        }

        // 2. 在独立的只读事务中执行查询：不加入外层事务；结束后回滚，由事务管理器复原连接的只读、自动提交状态
        // 不设置事务超时：其截止时间会使查询超时后的 SQL_SELECT_LIMIT 复原语句无法执行；超时由查询语句自身设置
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setReadOnly(true);
        return transactionTemplate.execute(status -> {
            status.setRollbackOnly();
            // 2.1 校验数据库类型与 SQL；jdbcTemplate 与事务管理器需为同一数据源，保证下面的语句都在当前事务的同一连接上执行
            Assert.state(TransactionSynchronizationManager.hasResource(Objects.requireNonNull(jdbcTemplate.getDataSource())),
                    "jdbcTemplate 的数据源未绑定到当前事务");
            String productName = jdbcTemplate.execute((ConnectionCallback<String>) connection ->
                    connection.getMetaData().getDatabaseProductName());
            DbType dbType = DB_TYPES.get(productName);
            if (dbType == null) {
                throw exception(GO_VIEW_SQL_DB_NOT_SUPPORTED, productName);
            }
            String executeSql = validateSql(sql, dbType);
            // 2.2 由数据库强制只读；需要是事务中的第一条语句
            // MySQL、MariaDB 显式开始事务，避免不访问表的 SELECT 未开启事务，留下“下一事务只读”状态
            jdbcTemplate.execute(dbType == DbType.mysql ? START_TRANSACTION_READ_ONLY_SQL : SET_TRANSACTION_READ_ONLY_SQL);
            // 2.3 执行查询；无论如何退出，都复原 SQL_SELECT_LIMIT
            RuntimeException failure = null;
            try {
                SqlRowSet sqlRowSet = jdbcTemplate.execute(executeSql, (PreparedStatementCallback<SqlRowSet>) statement -> {
                    // 在 JdbcTemplate 应用其全局设置之后设置，只对当前语句生效；多保留一行，用于判断是否超过最大行数
                    statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                    statement.setMaxRows(MAX_ROWS + 1);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        return new SqlRowSetResultSetExtractor().extractData(resultSet);
                    }
                });
                return buildRespVO(sqlRowSet);
            } catch (RuntimeException ex) {
                // 记录查询的异常，复原失败时作为 suppressed，保留原始异常
                failure = ex;
                throw ex;
            } finally {
                if (dbType == DbType.mysql) {
                    resetSelectLimit(failure);
                }
            }
        });
    }

    /**
     * 校验 SQL 为单条只读 SELECT，返回实际执行的 SQL
     *
     * 1. Druid SQL 防火墙：单条语句，禁止 INTO、写语句（包括写入的 CTE）、Druid 默认禁止的系统变量（例如说 @@basedir）
     * 2. 执行 Druid 输出的 SQL，保证执行的语句与校验的语句一致；并通过 {@link #validateTokens(String, DbType, Set)} 检查输出
     *
     * 注意：这里无法覆盖所有数据库副作用（例如说自定义函数、视图），写入由数据库的只读事务保证
     *
     * @param sql    SQL
     * @param dbType 数据库类型
     * @return 实际执行的 SQL
     */
    static String validateSql(String sql, DbType dbType) {
        if (StrUtil.length(sql) > MAX_SQL_LENGTH) {
            throw exception(GO_VIEW_SQL_INVALID, "长度不能超过 " + MAX_SQL_LENGTH);
        }
        WallProvider provider = dbType == DbType.postgresql ? POSTGRESQL_WALL_PROVIDER : MYSQL_WALL_PROVIDER;
        // 1. Druid SQL 防火墙
        WallCheckResult result;
        try {
            result = provider.check(sql);
        } catch (RuntimeException ex) {
            throw exception(GO_VIEW_SQL_INVALID, "仅支持可解析的单条 SELECT 查询");
        }
        if (CollUtil.isNotEmpty(result.getViolations())) {
            // 截断提示，避免回显整段 SQL
            throw exception(GO_VIEW_SQL_INVALID, StrUtil.maxLength(CollUtil.getFirst(result.getViolations()).getMessage(), 100));
        }
        List<SQLStatement> statements = result.getStatementList();
        if (statements.size() != 1 || !(statements.get(0) instanceof SQLSelectStatement)) {
            throw exception(GO_VIEW_SQL_INVALID, "仅支持单条 SELECT 查询");
        }
        // 2. 执行 Druid 输出的 SQL，并检查输出
        String executeSql = SQLUtils.toSQLString(statements.get(0), dbType);
        validateTokens(executeSql, dbType, provider.getConfig().getDenyFunctions());
        return executeSql;
    }

    /**
     * 使用 Druid 的词法分析，检查实际执行的 SQL；字符串是单独的字面量记号，其中的内容不会误判
     *
     * 1. 注释、提示：Druid 可能在输出中保留注释；查询块上的提示（例如说 MySQL 的可执行注释、SET_VAR 优化器提示）不经过防火墙的检查，却会原样输出
     * 2. 赋值（:=）：MySQL、MariaDB 的用户变量赋值，回滚后仍保留在连接池的连接上
     * 3. 禁止的函数调用（包括整个 dblink_* 系列）：函数名（包括 schema 限定、加引号）后接左括号；防火墙、AST 访问器会遗漏部分位置，例如说窗口 frame 的起止表达式
     */
    private static void validateTokens(String sql, DbType dbType, Set<String> denyFunctions) {
        Lexer lexer = SQLParserUtils.createLexer(sql, dbType);
        lexer.config(SQLParserFeature.SkipComments, false);
        try {
            String name = null; // 上一个记号的名字：标识符、关键字；字面量为 null
            for (lexer.nextToken(); lexer.token() != Token.EOF; lexer.nextToken()) {
                Token token = lexer.token();
                if (token == Token.HINT || token == Token.MULTI_LINE_COMMENT || token == Token.LINE_COMMENT
                        || token == Token.ERROR) {
                    throw exception(GO_VIEW_SQL_INVALID, "不支持注释与提示");
                }
                if (token == Token.COLONEQ) {
                    throw exception(GO_VIEW_SQL_INVALID, "不支持变量赋值");
                }
                if (token == Token.LPAREN && name != null
                        && (denyFunctions.contains(name) || name.startsWith("dblink_"))) {
                    throw exception(GO_VIEW_SQL_INVALID, "deny function : " + StrUtil.maxLength(name, 64));
                }
                String text = token == Token.IDENTIFIER || token == Token.LITERAL_ALIAS ? lexer.stringVal() : token.name;
                name = text != null ? SQLUtils.normalize(text).toLowerCase(Locale.ROOT) : null;
            }
        } catch (ParserException ex) {
            throw exception(GO_VIEW_SQL_INVALID, "仅支持可解析的单条 SELECT 查询");
        }
    }

    /**
     * 初始化 SQL 防火墙：保留只读限制，允许自助查询中的常量条件等正常写法，并关闭 SQL 缓存
     *
     * @param provider 对应数据库的 SQL 防火墙
     * @return 初始化后的 SQL 防火墙
     */
    private static WallProvider initWallProvider(WallProvider provider) {
        WallConfig config = provider.getConfig();
        // 只读：禁止 SELECT INTO、提示，以及写入的 CTE 中嵌套的写语句；多语句，Druid 默认已禁止
        // 注意：防火墙不访问查询块上的提示，由 validateSql 检查 Druid 的输出
        config.setSelectIntoAllow(false);
        config.setHintAllow(false);
        config.setInsertAllow(false);
        config.setUpdateAllow(false);
        config.setDeleteAllow(false);
        config.setMergeAllow(false);
        config.setReplaceAllow(false);
        // 关闭防 SQL 注入的启发式检查：SQL 由有权限的用户直接编写，WHERE 1 = 1、常量 UNION、LIMIT 0 等是正常写法
        config.setSelectWhereAlwayTrueCheck(false);
        config.setSelectHavingAlwayTrueCheck(false);
        config.setConditionAndAlwayFalseAllow(true);
        config.setConditionDoubleConstAllow(true);
        config.setCaseConditionConstAllow(true);
        config.setSelectUnionCheck(false);
        config.setSelectMinusCheck(false);
        config.setSelectExceptCheck(false);
        config.setSelectIntersectCheck(false);
        config.setLimitZeroAllow(true);
        // 关闭系统库、系统表的检查：Druid 对顶层 FROM 系统库（例如说 information_schema）的查询放行，并清空已发现的违规（例如说 INTO）；
        // 读取元数据也是读操作，范围取决于数据库账号的权限
        config.setSchemaCheck(false);
        config.setTableCheck(false);
        config.getDenyTables().clear();
        config.getDenyFunctions().addAll(DENY_FUNCTIONS);
        // 关闭白名单、黑名单缓存：命中缓存时不返回解析后的语句；也避免缓存任意 SQL
        provider.setWhiteListEnable(false);
        provider.setBlackListEnable(false);
        return provider;
    }

    /**
     * MySQL、MariaDB 复原 SQL_SELECT_LIMIT
     *
     * @param failure 查询的 RuntimeException：存在时，复原的异常作为其 suppressed，保留原始异常；
     *                不存在时（查询成功，或以 Error 退出），抛出复原的异常
     */
    private void resetSelectLimit(RuntimeException failure) {
        try {
            jdbcTemplate.execute((StatementCallback<Boolean>) statement -> {
                // 只对当前语句设置超时
                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                return statement.execute(RESET_SELECT_LIMIT_SQL);
            });
        } catch (RuntimeException ex) {
            if (failure == null) {
                throw ex;
            }
            failure.addSuppressed(ex);
        }
    }

    /**
     * 将查询结果转换为 GoView 数据，超过最大行数时返回业务异常
     *
     * @param sqlRowSet 查询结果
     * @return GoView 数据
     */
    private static GoViewDataRespVO buildRespVO(SqlRowSet sqlRowSet) {
        GoViewDataRespVO respVO = new GoViewDataRespVO();
        // 1. 解析元数据
        SqlRowSetMetaData metaData = sqlRowSet.getMetaData();
        String[] columnNames = metaData.getColumnNames();
        respVO.setDimensions(Arrays.asList(columnNames));
        // 2. 解析数据明细
        respVO.setSource(new LinkedList<>()); // 由于数据量不确认，使用 LinkedList 虽然内存占用大一点，但是不存在扩容复制的问题
        while (sqlRowSet.next()) {
            if (respVO.getSource().size() >= MAX_ROWS) {
                throw exception(GO_VIEW_SQL_ROWS_EXCEED, MAX_ROWS);
            }
            Map<String, Object> data = Maps.newHashMapWithExpectedSize(columnNames.length);
            for (String columnName : columnNames) {
                data.put(columnName, sqlRowSet.getObject(columnName));
            }
            respVO.getSource().add(data);
        }
        return respVO;
    }

}
