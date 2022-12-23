package cn.iocoder.yudao.framework.datapermission.core.db;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.SetUtils;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRule;
import cn.iocoder.yudao.framework.datapermission.core.rule.DataPermissionRuleFactory;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.parser.JsqlParserSupport;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限拦截器，通过 {@link DataPermissionRule} 数据权限规则，重写 SQL 的方式来实现
 * 主要的 SQL 重写方法，可见 {@link #builderExpression(Expression, List)} 方法
 *
 * 整体的代码实现上，参考 {@link com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor} 实现。
 * 所以每次 MyBatis Plus 升级时，需要 Review 下其具体的实现是否有变更！
 *
 * @author 芋道源码
 */
@RequiredArgsConstructor
public class DataPermissionDatabaseInterceptor extends JsqlParserSupport implements InnerInterceptor {

    private final DataPermissionRuleFactory ruleFactory;

    @Getter
    private final MappedStatementCache mappedStatementCache = new MappedStatementCache();

    @Override // SELECT 场景
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 获得 Mapper 对应的数据权限的规则
        List<DataPermissionRule> rules = ruleFactory.getDataPermissionRule(ms.getId());
        if (mappedStatementCache.noRewritable(ms, rules)) { // 如果无需重写，则跳过
            return;
        }

        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        try {
            // 初始化上下文
            ContextHolder.init(rules);
            // 处理 SQL
            mpBs.sql(parserSingle(mpBs.sql(), null));
        } finally {
            // 添加是否需要重写的缓存
            addMappedStatementCache(ms);
            // 清空上下文
            ContextHolder.clear();
        }
    }

    @Override // 只处理 UPDATE / DELETE 场景，不处理 INSERT 场景（因为 INSERT 不需要数据权限)
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        MappedStatement ms = mpSh.mappedStatement();
        SqlCommandType sct = ms.getSqlCommandType();
        if (sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
            // 获得 Mapper 对应的数据权限的规则
            List<DataPermissionRule> rules = ruleFactory.getDataPermissionRule(ms.getId());
            if (mappedStatementCache.noRewritable(ms, rules)) { // 如果无需重写，则跳过
                return;
            }

            PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
            try {
                // 初始化上下文
                ContextHolder.init(rules);
                // 处理 SQL
                mpBs.sql(parserMulti(mpBs.sql(), null));
            } finally {
                // 添加是否需要重写的缓存
                addMappedStatementCache(ms);
                // 清空上下文
                ContextHolder.clear();
            }
        }
    }

    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        processSelectBody(select.getSelectBody());
        List<WithItem> withItemsList = select.getWithItemsList();
        if (!CollectionUtils.isEmpty(withItemsList)) {
            withItemsList.forEach(this::processSelectBody);
        }
    }

    /**
     * update 语句处理
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        final Table table = update.getTable();
        update.setWhere(this.builderExpression(update.getWhere(), table));
    }

    /**
     * delete 语句处理
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        delete.setWhere(this.builderExpression(delete.getWhere(), delete.getTable()));
    }

    // ========== 和 TenantLineInnerInterceptor 一致的逻辑 ==========

    protected void processSelectBody(SelectBody selectBody) {
        if (selectBody == null) {
            return;
        }
        if (selectBody instanceof PlainSelect) {
            processPlainSelect((PlainSelect) selectBody);
        } else if (selectBody instanceof WithItem) {
            WithItem withItem = (WithItem) selectBody;
            processSelectBody(withItem.getSubSelect().getSelectBody());
        } else {
            SetOperationList operationList = (SetOperationList) selectBody;
            List<SelectBody> selectBodyList = operationList.getSelects();
            if (CollectionUtils.isNotEmpty(selectBodyList)) {
                selectBodyList.forEach(this::processSelectBody);
            }
        }
    }

    /**
     * 处理 PlainSelect
     */
    protected void processPlainSelect(PlainSelect plainSelect) {
        //#3087 github
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        if (CollectionUtils.isNotEmpty(selectItems)) {
            selectItems.forEach(this::processSelectItem);
        }

        // 处理 where 中的子查询
        Expression where = plainSelect.getWhere();
        processWhereSubSelect(where);

        // 处理 fromItem
        FromItem fromItem = plainSelect.getFromItem();
        List<Table> list = processFromItem(fromItem);
        List<Table> mainTables = new ArrayList<>(list);

        // 处理 join
        List<Join> joins = plainSelect.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            mainTables = processJoins(mainTables, joins);
        }

        // 当有 mainTable 时，进行 where 条件追加
        if (CollectionUtils.isNotEmpty(mainTables)) {
            plainSelect.setWhere(builderExpression(where, mainTables));
        }
    }

    private List<Table> processFromItem(FromItem fromItem) {
        // 处理括号括起来的表达式
        while (fromItem instanceof ParenthesisFromItem) {
            fromItem = ((ParenthesisFromItem) fromItem).getFromItem();
        }

        List<Table> mainTables = new ArrayList<>();
        // 无 join 时的处理逻辑
        if (fromItem instanceof Table) {
            Table fromTable = (Table) fromItem;
            mainTables.add(fromTable);
        } else if (fromItem instanceof SubJoin) {
            // SubJoin 类型则还需要添加上 where 条件
            List<Table> tables = processSubJoin((SubJoin) fromItem);
            mainTables.addAll(tables);
        } else {
            // 处理下 fromItem
            processOtherFromItem(fromItem);
        }
        return mainTables;
    }

    /**
     * 处理where条件内的子查询
     * <p>
     * 支持如下:
     * 1. in
     * 2. =
     * 3. >
     * 4. <
     * 5. >=
     * 6. <=
     * 7. <>
     * 8. EXISTS
     * 9. NOT EXISTS
     * <p>
     * 前提条件:
     * 1. 子查询必须放在小括号中
     * 2. 子查询一般放在比较操作符的右边
     *
     * @param where where 条件
     */
    protected void processWhereSubSelect(Expression where) {
        if (where == null) {
            return;
        }
        if (where instanceof FromItem) {
            processOtherFromItem((FromItem) where);
            return;
        }
        if (where.toString().indexOf("SELECT") > 0) {
            // 有子查询
            if (where instanceof BinaryExpression) {
                // 比较符号 , and , or , 等等
                BinaryExpression expression = (BinaryExpression) where;
                processWhereSubSelect(expression.getLeftExpression());
                processWhereSubSelect(expression.getRightExpression());
            } else if (where instanceof InExpression) {
                // in
                InExpression expression = (InExpression) where;
                Expression inExpression = expression.getRightExpression();
                if (inExpression instanceof SubSelect) {
                    processSelectBody(((SubSelect) inExpression).getSelectBody());
                }
            } else if (where instanceof ExistsExpression) {
                // exists
                ExistsExpression expression = (ExistsExpression) where;
                processWhereSubSelect(expression.getRightExpression());
            } else if (where instanceof NotExpression) {
                // not exists
                NotExpression expression = (NotExpression) where;
                processWhereSubSelect(expression.getExpression());
            } else if (where instanceof Parenthesis) {
                Parenthesis expression = (Parenthesis) where;
                processWhereSubSelect(expression.getExpression());
            }
        }
    }

    protected void processSelectItem(SelectItem selectItem) {
        if (selectItem instanceof SelectExpressionItem) {
            SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
            if (selectExpressionItem.getExpression() instanceof SubSelect) {
                processSelectBody(((SubSelect) selectExpressionItem.getExpression()).getSelectBody());
            } else if (selectExpressionItem.getExpression() instanceof Function) {
                processFunction((Function) selectExpressionItem.getExpression());
            }
        }
    }

    /**
     * 处理函数
     * <p>支持: 1. select fun(args..) 2. select fun1(fun2(args..),args..)<p>
     * <p> fixed gitee pulls/141</p>
     *
     * @param function
     */
    protected void processFunction(Function function) {
        ExpressionList parameters = function.getParameters();
        if (parameters != null) {
            parameters.getExpressions().forEach(expression -> {
                if (expression instanceof SubSelect) {
                    processSelectBody(((SubSelect) expression).getSelectBody());
                } else if (expression instanceof Function) {
                    processFunction((Function) expression);
                }
            });
        }
    }

    /**
     * 处理子查询等
     */
    protected void processOtherFromItem(FromItem fromItem) {
        // 去除括号
        while (fromItem instanceof ParenthesisFromItem) {
            fromItem = ((ParenthesisFromItem) fromItem).getFromItem();
        }

        if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            if (subSelect.getSelectBody() != null) {
                processSelectBody(subSelect.getSelectBody());
            }
        } else if (fromItem instanceof ValuesList) {
            logger.debug("Perform a subQuery, if you do not give us feedback");
        } else if (fromItem instanceof LateralSubSelect) {
            LateralSubSelect lateralSubSelect = (LateralSubSelect) fromItem;
            if (lateralSubSelect.getSubSelect() != null) {
                SubSelect subSelect = lateralSubSelect.getSubSelect();
                if (subSelect.getSelectBody() != null) {
                    processSelectBody(subSelect.getSelectBody());
                }
            }
        }
    }

    /**
     * 处理 sub join
     *
     * @param subJoin subJoin
     * @return Table subJoin 中的主表
     */
    private List<Table> processSubJoin(SubJoin subJoin) {
        List<Table> mainTables = new ArrayList<>();
        if (subJoin.getJoinList() != null) {
            List<Table> list = processFromItem(subJoin.getLeft());
            mainTables.addAll(list);
            mainTables = processJoins(mainTables, subJoin.getJoinList());
        }
        return mainTables;
    }

    /**
     * 处理 joins
     *
     * @param mainTables 可以为 null
     * @param joins      join 集合
     * @return List<Table> 右连接查询的 Table 列表
     */
    private List<Table> processJoins(List<Table> mainTables, List<Join> joins) {
        // join 表达式中最终的主表
        Table mainTable = null;
        // 当前 join 的左表
        Table leftTable = null;

        if (mainTables == null) {
            mainTables = new ArrayList<>();
        } else if (mainTables.size() == 1) {
            mainTable = mainTables.get(0);
            leftTable = mainTable;
        }

        //对于 on 表达式写在最后的 join，需要记录下前面多个 on 的表名
        Deque<List<Table>> onTableDeque = new LinkedList<>();
        for (Join join : joins) {
            // 处理 on 表达式
            FromItem joinItem = join.getRightItem();

            // 获取当前 join 的表，subJoint 可以看作是一张表
            List<Table> joinTables = null;
            if (joinItem instanceof Table) {
                joinTables = new ArrayList<>();
                joinTables.add((Table) joinItem);
            } else if (joinItem instanceof SubJoin) {
                joinTables = processSubJoin((SubJoin) joinItem);
            }

            if (joinTables != null) {

                // 如果是隐式内连接
                if (join.isSimple()) {
                    mainTables.addAll(joinTables);
                    continue;
                }

                // 当前表是否忽略
                Table joinTable = joinTables.get(0);

                List<Table> onTables = null;
                // 如果不要忽略，且是右连接，则记录下当前表
                if (join.isRight()) {
                    mainTable = joinTable;
                    if (leftTable != null) {
                        onTables = Collections.singletonList(leftTable);
                    }
                } else if (join.isLeft()) {
                    onTables = Collections.singletonList(joinTable);
                } else if (join.isInner()) {
                    if (mainTable == null) {
                        onTables = Collections.singletonList(joinTable);
                    } else {
                        onTables = Arrays.asList(mainTable, joinTable);
                    }
                    mainTable = null;
                }

                mainTables = new ArrayList<>();
                if (mainTable != null) {
                    mainTables.add(mainTable);
                }

                // 获取 join 尾缀的 on 表达式列表
                Collection<Expression> originOnExpressions = join.getOnExpressions();
                // 正常 join on 表达式只有一个，立刻处理
                if (originOnExpressions.size() == 1 && onTables != null) {
                    List<Expression> onExpressions = new LinkedList<>();
                    onExpressions.add(builderExpression(originOnExpressions.iterator().next(), onTables));
                    join.setOnExpressions(onExpressions);
                    leftTable = joinTable;
                    continue;
                }
                // 表名压栈，忽略的表压入 null，以便后续不处理
                onTableDeque.push(onTables);
                // 尾缀多个 on 表达式的时候统一处理
                if (originOnExpressions.size() > 1) {
                    Collection<Expression> onExpressions = new LinkedList<>();
                    for (Expression originOnExpression : originOnExpressions) {
                        List<Table> currentTableList = onTableDeque.poll();
                        if (CollectionUtils.isEmpty(currentTableList)) {
                            onExpressions.add(originOnExpression);
                        } else {
                            onExpressions.add(builderExpression(originOnExpression, currentTableList));
                        }
                    }
                    join.setOnExpressions(onExpressions);
                }
                leftTable = joinTable;
            } else {
                processOtherFromItem(joinItem);
                leftTable = null;
            }
        }

        return mainTables;
    }

    // ========== 和 TenantLineInnerInterceptor 存在差异的逻辑：关键，实现权限条件的拼接 ==========

    /**
     * 处理条件
     *
     * @param currentExpression 当前 where 条件
     * @param table             单个表
     */
    protected Expression builderExpression(Expression currentExpression, Table table) {
        return this.builderExpression(currentExpression, Collections.singletonList(table));
    }

    /**
     * 处理条件
     *
     * @param currentExpression 当前 where 条件
     * @param tables 多个表
     */
    protected Expression builderExpression(Expression currentExpression, List<Table> tables) {
        // 没有表需要处理直接返回
        if (CollectionUtils.isEmpty(tables)) {
            return currentExpression;
        }

        // 第一步，获得 Table 对应的数据权限条件
        Expression dataPermissionExpression = null;
        for (Table table : tables) {
            // 构建每个表的权限 Expression 条件
            Expression expression = buildDataPermissionExpression(table);
            if (expression == null) {
                continue;
            }
            // 合并到 dataPermissionExpression 中
            dataPermissionExpression = dataPermissionExpression == null ? expression
                    : new AndExpression(dataPermissionExpression, expression);
        }

        // 第二步，合并多个 Expression 条件
        if (dataPermissionExpression == null) {
            return currentExpression;
        }
        if (currentExpression == null) {
            return dataPermissionExpression;
        }
        // ① 如果表达式为 Or，则需要 (currentExpression) AND dataPermissionExpression
        if (currentExpression instanceof OrExpression) {
            return new AndExpression(new Parenthesis(currentExpression), dataPermissionExpression);
        }
        // ② 如果表达式为 And，则直接返回 where AND dataPermissionExpression
        return new AndExpression(currentExpression, dataPermissionExpression);
    }

    /**
     * 构建指定表的数据权限的 Expression 过滤条件
     *
     * @param table 表
     * @return Expression 过滤条件
     */
    private Expression buildDataPermissionExpression(Table table) {
        // 生成条件
        Expression allExpression = null;
        for (DataPermissionRule rule : ContextHolder.getRules()) {
            // 判断表名是否匹配
            if (!rule.getTableNames().contains(table.getName())) {
                continue;
            }
            // 如果有匹配的规则，说明可重写。
            // 为什么不是有 allExpression 非空才重写呢？在生成 column = value 过滤条件时，会因为 value 不存在，导致未重写。
            // 这样导致第一次无 value，被标记成无需重写；但是第二次有 value，此时会需要重写。
            ContextHolder.setRewrite(true);

            // 单条规则的条件
            String tableName = MyBatisUtils.getTableName(table);
            Expression oneExpress = rule.getExpression(tableName, table.getAlias());
            // 拼接到 allExpression 中
            allExpression = allExpression == null ? oneExpress
                    : new AndExpression(allExpression, oneExpress);
        }

        return allExpression;
    }

    /**
     * 判断 SQL 是否重写。如果没有重写，则添加到 {@link MappedStatementCache} 中
     *
     * @param ms MappedStatement
     */
    private void addMappedStatementCache(MappedStatement ms) {
        if (ContextHolder.getRewrite()) {
            return;
        }
        // 无重写，进行添加
        mappedStatementCache.addNoRewritable(ms, ContextHolder.getRules());
    }

    /**
     * SQL 解析上下文，方便透传 {@link DataPermissionRule} 规则
     *
     * @author 芋道源码
     */
    static final class ContextHolder {

        /**
         * 该 {@link MappedStatement} 对应的规则
         */
        private static final ThreadLocal<List<DataPermissionRule>> RULES = new TransmittableThreadLocal<>();
        /**
         * SQL 是否进行重写
         */
        private static final ThreadLocal<Boolean> REWRITE = new TransmittableThreadLocal<>();

        public static void init(List<DataPermissionRule> rules) {
            RULES.set(rules);
            REWRITE.set(false);
        }

        public static void clear() {
            RULES.remove();
            REWRITE.remove();
        }

        public static boolean getRewrite() {
            return REWRITE.get();
        }

        public static void setRewrite(boolean rewrite) {
            REWRITE.set(rewrite);
        }

        public static List<DataPermissionRule> getRules() {
            return RULES.get();
        }

    }

    /**
     * {@link MappedStatement} 缓存
     * 目前主要用于，记录 {@link DataPermissionRule} 是否对指定 {@link MappedStatement} 无效
     * 如果无效，则可以避免 SQL 的解析，加快速度
     *
     * @author 芋道源码
     */
    static final class MappedStatementCache {

        /**
         * 指定数据权限规则，对指定 MappedStatement 无需重写（不生效)的缓存
         *
         * value：{@link MappedStatement#getId()} 编号
         */
        @Getter
        private final Map<Class<? extends DataPermissionRule>, Set<String>> noRewritableMappedStatements = new ConcurrentHashMap<>();

        /**
         * 判断是否无需重写
         * ps：虽然有点中文式英语，但是容易读懂即可
         *
         * @param ms MappedStatement
         * @param rules 数据权限规则数组
         * @return 是否无需重写
         */
        public boolean noRewritable(MappedStatement ms, List<DataPermissionRule> rules) {
            // 如果规则为空，说明无需重写
            if (CollUtil.isEmpty(rules)) {
                return true;
            }
            // 任一规则不在 noRewritableMap 中，则说明可能需要重写
            for (DataPermissionRule rule : rules) {
                Set<String> mappedStatementIds = noRewritableMappedStatements.get(rule.getClass());
                if (!CollUtil.contains(mappedStatementIds, ms.getId())) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 添加无需重写的 MappedStatement
         *
         * @param ms MappedStatement
         * @param rules 数据权限规则数组
         */
        public void addNoRewritable(MappedStatement ms, List<DataPermissionRule> rules) {
            for (DataPermissionRule rule : rules) {
                Set<String> mappedStatementIds = noRewritableMappedStatements.get(rule.getClass());
                if (CollUtil.isNotEmpty(mappedStatementIds)) {
                    mappedStatementIds.add(ms.getId());
                } else {
                    noRewritableMappedStatements.put(rule.getClass(), SetUtils.asSet(ms.getId()));
                }
            }
        }

        /**
         * 清空缓存
         * 目前主要提供给单元测试
         */
        public void clear() {
            noRewritableMappedStatements.clear();
        }

    }

}
