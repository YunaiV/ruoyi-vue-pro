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
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
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
 * 主要的 SQL 重写方法，可见 {@link #builderExpression(Expression, Table)} 方法
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
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
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
            addMappedStatementCache(ms);
            ContextHolder.clear();
        }
    }

    @Override // 只处理 UPDATE / DELETE 场景，不处理 INSERT 场景
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
                addMappedStatementCache(ms);
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
            List<SelectBody> selectBodys = operationList.getSelects();
            if (CollectionUtils.isNotEmpty(selectBodys)) {
                selectBodys.forEach(this::processSelectBody);
            }
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

    /**
     * 处理 PlainSelect
     */
    protected void processPlainSelect(PlainSelect plainSelect) {
        FromItem fromItem = plainSelect.getFromItem();
        Expression where = plainSelect.getWhere();
        processWhereSubSelect(where);
        if (fromItem instanceof Table) {
            Table fromTable = (Table) fromItem;
            plainSelect.setWhere(builderExpression(where, fromTable));
        } else {
            processFromItem(fromItem);
        }
        //#3087 github
        List<SelectItem> selectItems = plainSelect.getSelectItems();
        if (CollectionUtils.isNotEmpty(selectItems)) {
            selectItems.forEach(this::processSelectItem);
        }
        List<Join> joins = plainSelect.getJoins();
        if (CollectionUtils.isNotEmpty(joins)) {
            processJoins(joins);
        }
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
            processFromItem((FromItem) where);
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
                ItemsList itemsList = expression.getRightItemsList();
                if (itemsList instanceof SubSelect) {
                    processSelectBody(((SubSelect) itemsList).getSelectBody());
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
     * @param function 函数
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
    protected void processFromItem(FromItem fromItem) {
        if (fromItem instanceof SubJoin) {
            SubJoin subJoin = (SubJoin) fromItem;
            if (subJoin.getJoinList() != null) {
                processJoins(subJoin.getJoinList());
            }
            if (subJoin.getLeft() != null) {
                processFromItem(subJoin.getLeft());
            }
        } else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            if (subSelect.getSelectBody() != null) {
                processSelectBody(subSelect.getSelectBody());
            }
        } else if (fromItem instanceof ValuesList) {
            logger.debug("Perform a subquery, if you do not give us feedback");
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
     * 处理 joins
     *
     * @param joins join 集合
     */
    private void processJoins(List<Join> joins) {
        //对于 on 表达式写在最后的 join，需要记录下前面多个 on 的表名
        Deque<Table> tables = new LinkedList<>();
        for (Join join : joins) {
            // 处理 on 表达式
            FromItem fromItem = join.getRightItem();
            if (fromItem instanceof Table) {
                Table fromTable = (Table) fromItem;
                // 获取 join 尾缀的 on 表达式列表
                Collection<Expression> originOnExpressions = join.getOnExpressions();
                // 正常 join on 表达式只有一个，立刻处理
                if (originOnExpressions.size() == 1) {
                    processJoin(join);
                    continue;
                }
                tables.push(fromTable);
                // 尾缀多个 on 表达式的时候统一处理
                if (originOnExpressions.size() > 1) {
                    Collection<Expression> onExpressions = new LinkedList<>();
                    for (Expression originOnExpression : originOnExpressions) {
                        Table currentTable = tables.poll();
                        onExpressions.add(builderExpression(originOnExpression, currentTable));
                    }
                    join.setOnExpressions(onExpressions);
                }
            } else {
                // 处理右边连接的子表达式
                processFromItem(fromItem);
            }
        }
    }

    /**
     * 处理联接语句
     */
    protected void processJoin(Join join) {
        if (join.getRightItem() instanceof Table) {
            Table fromTable = (Table) join.getRightItem();
            Expression originOnExpression = CollUtil.getFirst(join.getOnExpressions());
            originOnExpression = builderExpression(originOnExpression, fromTable);
            join.setOnExpressions(CollUtil.newArrayList(originOnExpression));
        }
    }

    /**
     * 处理条件
     */
    protected Expression builderExpression(Expression currentExpression, Table table) {
        // 获得 Table 对应的数据权限条件
        Expression equalsTo = buildDataPermissionExpression(table);
        if (equalsTo == null) { // 如果没条件，则返回 currentExpression 默认
            return currentExpression;
        }

        // 表达式为空，则直接返回 equalsTo
        if (currentExpression == null) {
            return equalsTo;
        }
        // 如果表达式为 Or，则需要 (currentExpression) AND equalsTo
        if (currentExpression instanceof OrExpression) {
            return new AndExpression(new Parenthesis(currentExpression), equalsTo);
        }
        // 如果表达式为 And，则直接返回 currentExpression AND equalsTo
        return new AndExpression(currentExpression, equalsTo);
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
