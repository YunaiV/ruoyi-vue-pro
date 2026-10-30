package cn.iocoder.yudao.framework.mybatis.core.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;

/**
 * 锁定读使用的内置查询，见 BaseMapperX 的 selectOneForUpdate、selectListForUpdate、selectFirstOneForUpdate 方法
 *
 * 按 {@link Kind} 分为三类，均不在 Mapper 接口上声明，由 BaseMapperX 通过 SqlSession 调用：
 * 1. {@link Kind#LOCK}：按原条件（可选再加主键）加锁查询，末尾为 FOR UPDATE
 * 2. {@link Kind#LOCK_FIRST}：按原条件、原排序加锁查询首行，末尾为 LIMIT 1 FOR UPDATE
 * 3. {@link Kind#FIRST_IDS}：按原条件、原排序（未设置时按主键）查询首行主键，行数限制由分页插件生成，不加锁；
 *    仅用于 Oracle 的首行锁定（Oracle 的 FOR UPDATE 不能与 ROWNUM、FETCH FIRST 一起使用）
 *
 * SQL Server 不支持 FOR UPDATE、LIMIT，由 {@link cn.iocoder.yudao.framework.mybatis.core.interceptor.ForUpdateInnerInterceptor}
 * 在所有解析 SQL 的插件之后改写；该拦截器只处理 SqlSource 为 {@link LockSqlSource} 的内置查询
 *
 * 1. 条件：Wrapper 的实体条件、普通条件（整体加括号，OR 条件也均受主键限制）、逻辑删除，与 MyBatis Plus 的 selectList 一致；
 *    不使用 Wrapper 的 last、groupBy、having，由调用方校验
 * 2. 列与结果：加锁查询沿用 selectList 的查询列（含 Wrapper 的 select）、resultMap；主键查询的结果为主键类型
 * 3. 缓存：每次执行前清空一级缓存、且不使用二级缓存，避免锁定前后读到缓存的结果
 *
 * @author 芋道源码
 */
public class SelectForUpdateMethod extends AbstractMethod {

    /**
     * 加锁查询的主键参数名
     */
    public static final String PARAM_ID = "id";

    private final Kind kind;

    public SelectForUpdateMethod(Kind kind) {
        super(kind.getMethod());
        this.kind = kind;
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        // 没有主键的表，无法查询首行主键
        if (kind == Kind.FIRST_IDS && !tableInfo.havePK()) {
            return null;
        }
        String columns = kind == Kind.FIRST_IDS ? tableInfo.getKeyColumn() : sqlSelectColumns(tableInfo, true);
        String sql = "<script>" + sqlFirst() + " SELECT " + columns + " FROM " + tableInfo.getTableName()
                + NEWLINE + sqlWhere(tableInfo) + NEWLINE + sqlTail(tableInfo) + sqlComment() + NEWLINE + "</script>";
        SqlSource sqlSource = new LockSqlSource(kind, super.createSqlSource(configuration, sql, modelClass));
        if (kind == Kind.FIRST_IDS) {
            return addSelectMappedStatementForOther(mapperClass, methodName, sqlSource, tableInfo.getKeyType());
        }
        return addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
    }

    /**
     * 条件：实体条件 + 普通条件（加括号） + [主键] + 逻辑删除
     *
     * @param tableInfo 表信息
     * @return WHERE 脚本
     */
    private String sqlWhere(TableInfo tableInfo) {
        String entityWhere = SqlScriptUtils.convertIf(tableInfo.getAllSqlWhere(true, true, true, WRAPPER_ENTITY_DOT),
                WRAPPER_ENTITY + " != null", true);
        String normalWhere = SqlScriptUtils.convertIf(" AND (${" + WRAPPER_DOT + "expression.normal.sqlSegment})",
                WRAPPER_DOT + "nonEmptyOfNormal", true);
        String idWhere = kind == Kind.LOCK && tableInfo.havePK() ? NEWLINE + SqlScriptUtils.convertIf(
                " AND " + tableInfo.getKeyColumn() + " = " + SqlScriptUtils.safeParam(PARAM_ID), PARAM_ID + " != null", true) : "";
        return SqlScriptUtils.convertWhere(entityWhere + NEWLINE + normalWhere + idWhere
                + NEWLINE + tableInfo.getLogicDeleteSql(true, true));
    }

    /**
     * 排序与锁定：
     * 1. 加锁查询：Wrapper 的排序（未设置时不排序，避免改变数据库的加锁范围） + FOR UPDATE；首行再加 LIMIT 1
     * 2. 主键查询：Wrapper 的排序；未设置时按主键排序，保证锁定前后读取的首行一致
     *
     * @param tableInfo 表信息
     * @return ORDER BY 等脚本
     */
    private String sqlTail(TableInfo tableInfo) {
        String orderBy = " ${" + WRAPPER_DOT + "expression.orderBy.sqlSegment}";
        if (kind == Kind.FIRST_IDS) {
            return SqlScriptUtils.convertChoose(WRAPPER_DOT + "expression.orderBy.isEmpty()",
                    " ORDER BY " + tableInfo.getKeyColumn(), orderBy);
        }
        return SqlScriptUtils.convertIf(orderBy, "!" + WRAPPER_DOT + "expression.orderBy.isEmpty()", true)
                + NEWLINE + (kind == Kind.LOCK_FIRST ? "LIMIT 1 FOR UPDATE" : "FOR UPDATE");
    }

    @Override
    protected MappedStatement addMappedStatement(Class<?> mapperClass, String id, SqlSource sqlSource,
                                                 SqlCommandType sqlCommandType, Class<?> parameterType,
                                                 String resultMap, Class<?> resultType, KeyGenerator keyGenerator,
                                                 String keyProperty, String keyColumn) {
        String statementName = mapperClass.getName() + DOT + id;
        if (configuration.hasStatement(statementName, false)) {
            logger.warn(LEFT_SQ_BRACKET + statementName + "] Has been loaded by XML or SqlProvider or Mybatis's Annotation, so ignoring this injection for [" + getClass() + RIGHT_SQ_BRACKET);
            return null;
        }
        // 与父类相比：flushCache 为 true、useCache 为 false
        return builderAssistant.addMappedStatement(id, sqlSource, StatementType.PREPARED, sqlCommandType,
                null, null, null, parameterType, resultMap, resultType,
                null, true, false, false, keyGenerator, keyProperty, keyColumn,
                configuration.getDatabaseId(), languageDriver, null);
    }

    /**
     * 内置查询的类型
     */
    @Getter
    @AllArgsConstructor
    public enum Kind {

        /**
         * 加锁查询：按原条件、可选的主键加锁，返回实体
         */
        LOCK("_forUpdateLock"),
        /**
         * 首行加锁查询：按原条件、原排序加锁第 1 条，返回实体
         */
        LOCK_FIRST("_forUpdateLockFirst"),
        /**
         * 首行主键查询：按原条件、原排序查询第 1 条的主键，不加锁，返回主键
         */
        FIRST_IDS("_forUpdateFirstIds");

        /**
         * 方法名，即 MappedStatement 编号中 Mapper 之后的部分
         */
        private final String method;

    }

    /**
     * 内置查询的 SqlSource：标记 MappedStatement 为锁定读的内置查询，SQL 由原 SqlSource 生成
     *
     * MyBatis Plus Join 等插件复制 MappedStatement 时保留同一个 SqlSource，标记仍然有效
     */
    @Getter
    @AllArgsConstructor
    public static class LockSqlSource implements SqlSource {

        /**
         * 内置查询的类型
         */
        private final Kind kind;
        /**
         * 原 SqlSource
         */
        private final SqlSource delegate;

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return delegate.getBoundSql(parameterObject);
        }

    }

}
