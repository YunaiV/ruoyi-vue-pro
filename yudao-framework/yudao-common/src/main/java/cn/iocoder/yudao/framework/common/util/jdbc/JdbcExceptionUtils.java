package cn.iocoder.yudao.framework.common.util.jdbc;

import cn.hutool.core.util.StrUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * JDBC 异常工具类
 *
 * @author 芋道源码
 */
public class JdbcExceptionUtils {

    /**
     * 表示“表不存在”的 SQLState
     *
     * 1. 42S02：ANSI / ODBC 标准，MySQL（1146）、MariaDB、H2、SQL Server（jTDS）等使用
     * 2. S0002：X/Open 旧标准，SQL Server（208，mssql-jdbc）使用
     * 3. 42P01：PostgreSQL 的 undefined_table，openGauss、KingbaseES 等 PostgreSQL 兼容数据库同样使用
     */
    private static final Set<String> TABLE_NOT_EXISTS_SQL_STATES = new HashSet<>(Arrays.asList("42S02", "S0002", "42P01"));

    /**
     * Oracle 的“表或视图不存在”错误码：ORA-00942，对应 SQLState 为通用的 42000，需要同时匹配错误码
     */
    private static final int ORACLE_TABLE_NOT_EXISTS_ERROR_CODE = 942;
    private static final String ORACLE_TABLE_NOT_EXISTS_SQL_STATE = "42000";

    /**
     * 兜底的“表不存在”异常文案，兼容未携带 SQLState 的场景（MySQL 的 Table 'xxx' doesn't exist）
     */
    private static final String TABLE_NOT_EXISTS_MESSAGE = "doesn't exist";

    /**
     * 判断异常是否为“表不存在”
     *
     * 沿 cause 链、{@link SQLException#getNextException()} 链查找，优先识别 SQLState、错误码。
     * 仅在整条链都没有有效 SQLState 时兜底匹配异常文案，避免外层包装信息覆盖明确的数据库错误。
     * 列不存在、权限不足、语法错误等其它异常，都返回 false，由调用方原样抛出。
     *
     * @param throwable 异常
     * @return 是否表不存在
     */
    @SuppressWarnings("PatternVariableCanBeUsed") // 保持 JDK8 兼容性
    public static boolean isTableNotExists(Throwable throwable) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Throwable> queue = new ArrayDeque<>();
        if (throwable != null) {
            queue.add(throwable);
        }
        boolean hasSqlState = false;
        boolean hasLegacyMessage = false;
        while (!queue.isEmpty()) {
            Throwable current = queue.poll();
            if (!visited.add(current)) { // 避免循环引用
                continue;
            }
            if (current instanceof SQLException) {
                SQLException sqlException = (SQLException) current;
                if (isTableNotExists(sqlException)) {
                    return true;
                }
                hasSqlState |= StrUtil.isNotBlank(sqlException.getSQLState());
                addIfNotNull(queue, sqlException.getNextException());
            }
            hasLegacyMessage |= StrUtil.contains(current.getMessage(), TABLE_NOT_EXISTS_MESSAGE);
            addIfNotNull(queue, current.getCause());
        }
        return !hasSqlState && hasLegacyMessage;
    }

    private static boolean isTableNotExists(SQLException sqlException) {
        String sqlState = sqlException.getSQLState();
        if (TABLE_NOT_EXISTS_SQL_STATES.contains(sqlState)) {
            return true;
        }
        return sqlException.getErrorCode() == ORACLE_TABLE_NOT_EXISTS_ERROR_CODE
                && ORACLE_TABLE_NOT_EXISTS_SQL_STATE.equals(sqlState);
    }

    private static void addIfNotNull(Deque<Throwable> queue, Throwable throwable) {
        if (throwable != null) {
            queue.add(throwable);
        }
    }

}
