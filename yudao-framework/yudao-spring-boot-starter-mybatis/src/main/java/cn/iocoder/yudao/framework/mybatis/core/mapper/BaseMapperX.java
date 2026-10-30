package cn.iocoder.yudao.framework.mybatis.core.mapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.SortablePageParam;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.common.util.object.PageUtils;
import cn.iocoder.yudao.framework.mybatis.core.injector.SelectForUpdateMethod;
import cn.iocoder.yudao.framework.mybatis.core.util.CaseInsensitiveResultMap;
import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import cn.iocoder.yudao.framework.mybatis.core.util.MyBatisUtils;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.MapperProxyMetadata;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.MybatisUtils;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.github.yulichang.base.MPJBaseMapper;
import com.github.yulichang.interfaces.MPJBaseJoin;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.exceptions.TooManyResultsException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 在 MyBatis Plus 的 BaseMapper 的基础上拓展，提供更多的能力
 *
 * 1. {@link BaseMapper} 为 MyBatis Plus 的基础接口，提供基础的 CRUD 能力
 * 2. {@link MPJBaseMapper} 为 MyBatis Plus Join 的基础接口，提供连表 Join 能力
 */
public interface BaseMapperX<T> extends MPJBaseMapper<T> {

    default PageResult<T> selectPage(SortablePageParam pageParam, @Param("ew") Wrapper<T> queryWrapper) {
        return selectPage(pageParam, pageParam.getSortingFields(), queryWrapper);
    }

    default PageResult<T> selectPage(PageParam pageParam, @Param("ew") Wrapper<T> queryWrapper) {
        return selectPage(pageParam, null, queryWrapper);
    }

    default PageResult<T> selectPage(PageParam pageParam, Collection<SortingField> sortingFields, @Param("ew") Wrapper<T> queryWrapper) {
        // 特殊：不分页，直接查询全部
        if (PageParam.PAGE_SIZE_NONE.equals(pageParam.getPageSize())) {
            MyBatisUtils.addOrder(queryWrapper, sortingFields);
            List<T> list = selectList(queryWrapper);
            return PageUtils.buildPageResult(pageParam, list);
        }

        // MyBatis Plus 查询
        IPage<T> mpPage = MyBatisUtils.buildPage(pageParam, sortingFields);
        selectPage(mpPage, queryWrapper);
        // 转换返回
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    default <D> PageResult<D> selectJoinPage(PageParam pageParam, Class<D> clazz, MPJLambdaWrapper<T> lambdaWrapper) {
        // 特殊：不分页，直接查询全部
        if (PageParam.PAGE_SIZE_NONE.equals(pageParam.getPageSize())) {
            List<D> list = selectJoinList(clazz, lambdaWrapper);
            return PageUtils.buildPageResult(pageParam, list);
        }

        // MyBatis Plus Join 查询
        IPage<D> mpPage = MyBatisUtils.buildPage(pageParam);
        mpPage = selectJoinPage(mpPage, clazz, lambdaWrapper);
        // 转换返回
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    /**
     * 执行分页查询并返回结果。
     *
     * @param pageParam 分页参数，包含页码、每页条数和排序字段信息。如果 pageSize 为 {@link PageParam#PAGE_SIZE_NONE}，则不分页，直接查询所有数据。
     * @param clazz     结果集的类类型
     * @param lambdaWrapper MyBatis Plus Join 查询条件包装器
     * @param <D>       结果集的泛型类型
     * @return 返回分页查询的结果，包括总记录数和当前页的数据列表
     */
    default <D> PageResult<D> selectJoinPage(SortablePageParam pageParam, Class<D> clazz, MPJLambdaWrapper<T> lambdaWrapper) {
        // 特殊：不分页，直接查询全部
        if (PageParam.PAGE_SIZE_NONE.equals(pageParam.getPageSize())) {
            List<D> list = selectJoinList(clazz, lambdaWrapper);
            return PageUtils.buildPageResult(pageParam, list);
        }

        // MyBatis Plus Join 查询
        IPage<D> mpPage = MyBatisUtils.buildPage(pageParam, pageParam.getSortingFields());
        mpPage = selectJoinPage(mpPage, clazz, lambdaWrapper);
        // 转换返回
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    default <DTO> PageResult<DTO> selectJoinPage(PageParam pageParam, Class<DTO> resultTypeClass, MPJBaseJoin<T> joinQueryWrapper) {
        IPage<DTO> mpPage = MyBatisUtils.buildPage(pageParam);
        selectJoinPage(mpPage, resultTypeClass, joinQueryWrapper);
        // 转换返回
        return new PageResult<>(mpPage.getRecords(), mpPage.getTotal());
    }

    @Override
    @SuppressWarnings("unchecked")
    default Map<String, Object> selectJoinMap(@Param(Constants.WRAPPER) MPJBaseJoin<T> wrapper) {
        return selectJoinOne(CaseInsensitiveResultMap.class, wrapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    default List<Map<String, Object>> selectJoinMaps(@Param(Constants.WRAPPER) MPJBaseJoin<T> wrapper) {
        return (List<Map<String, Object>>) ((Object) selectJoinList(CaseInsensitiveResultMap.class, wrapper));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    default <P extends IPage<Map<String, Object>>> P selectJoinMapsPage(P page, @Param(Constants.WRAPPER) MPJBaseJoin<T> wrapper) {
        return (P) selectJoinPage((IPage) page, CaseInsensitiveResultMap.class, wrapper);
    }

    default T selectOne(String field, Object value) {
        return selectOne(new QueryWrapper<T>().eq(field, value));
    }

    default T selectOne(SFunction<T, ?> field, Object value) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default T selectOne(String field1, Object value1, String field2, Object value2) {
        return selectOne(new QueryWrapper<T>().eq(field1, value1).eq(field2, value2));
    }

    default T selectOne(SFunction<T, ?> field1, Object value1, SFunction<T, ?> field2, Object value2) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2));
    }

    default T selectOne(SFunction<T, ?> field1, Object value1, SFunction<T, ?> field2, Object value2,
                        SFunction<T, ?> field3, Object value3) {
        return selectOne(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2).eq(field3, value3));
    }

    // ========== 锁定读：需要在事务中调用；Wrapper 不能使用 last、groupBy、having、参数别名 ==========

    /**
     * 获得满足条件的一条记录，并使用 FOR UPDATE 锁定。
     *
     * 注意：需要在事务中调用，否则锁会立即释放；多于 1 条时抛出 {@link TooManyResultsException}（已锁定的记录在事务结束时释放）
     *
     * @param queryWrapper 查询条件
     * @return 实体
     */
    default T selectOneForUpdate(LambdaQueryWrapper<T> queryWrapper) {
        List<T> list = selectListForUpdate(queryWrapper);
        if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOneForUpdate(), but found: " + list.size());
        }
        return CollUtil.getFirst(list);
    }

    default T selectOneForUpdate(SFunction<T, ?> field, Object value) {
        return selectOneForUpdate(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default T selectOneForUpdate(SFunction<T, ?> field1, Object value1, SFunction<T, ?> field2, Object value2) {
        return selectOneForUpdate(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2));
    }

    default T selectOneForUpdate(SFunction<T, ?> field1, Object value1, SFunction<T, ?> field2, Object value2,
                                 SFunction<T, ?> field3, Object value3) {
        return selectOneForUpdate(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2).eq(field3, value3));
    }

    /**
     * 获得满足条件的全部记录，并使用 FOR UPDATE 锁定。
     *
     * 注意：需要在事务中调用，否则锁会立即释放；返回顺序为 queryWrapper 的排序，未设置时不排序
     *
     * @param queryWrapper 查询条件
     * @return 实体列表
     */
    default List<T> selectListForUpdate(LambdaQueryWrapper<T> queryWrapper) {
        return selectListByForUpdateMethod(SelectForUpdateMethod.Kind.LOCK, queryWrapper, null);
    }

    default List<T> selectListForUpdate(SFunction<T, ?> field, Object value) {
        return selectListForUpdate(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default List<T> selectListForUpdate(SFunction<T, ?> field, Collection<?> values) {
        if (CollUtil.isEmpty(values)) {
            return CollUtil.newArrayList();
        }
        return selectListForUpdate(new LambdaQueryWrapper<T>().in(field, values));
    }

    /**
     * 获得满足条件的第 1 条记录，并使用 FOR UPDATE 锁定。
     *
     * 注意：需要在事务中调用，否则锁会立即释放；queryWrapper 未设置排序时，为任意一条满足条件的记录（只适合判断是否存在）
     *
     * Oracle 不支持 LIMIT 1 FOR UPDATE（Oracle 的 FOR UPDATE 也不能与行数限制一起使用），改为多次查询：
     * 1. 按原条件、原排序查询首行主键（候选），不加锁；不存在时返回 null
     * 2. 按原条件、且主键等于候选主键加锁查询，不限制行数；候选已不满足条件时重试
     * 3. 锁定后重新查询首行主键，与候选相同才返回；不同（例如其它事务已提交了排序更靠前的记录）时重试，最多
     *    5 次后抛出 {@link GlobalErrorCodeConstants#LOCKED}
     * 要求实际执行的连接隔离级别为 READ COMMITTED，否则锁定后无法读到其它事务已提交的记录，
     * 由 {@link cn.iocoder.yudao.framework.mybatis.core.interceptor.ForUpdateInnerInterceptor} 校验
     *
     * 其它数据库为单条 LIMIT 1 FOR UPDATE（SQL Server 改写为 TOP (1) 与 WITH (UPDLOCK, ROWLOCK)）
     *
     * @param queryWrapper 查询条件
     * @return 实体
     */
    // DONE @AI：方法内注释噢；（处理：已按“情况一 非 Oracle 单条锁定、情况二 Oracle 2.1～2.3 候选、加锁、重读”补充步骤注释）
    default T selectFirstOneForUpdate(LambdaQueryWrapper<T> queryWrapper) {
        // 情况一：非 Oracle，单条 LIMIT 1 FOR UPDATE 锁定首行（SQL Server 由 ForUpdateInnerInterceptor 改写）
        DbType dbType = JdbcUtils.getDbType();
        // DONE @AI：ObjectUtils notequals any；（处理：已改为 ObjectUtils.notEqualsAny，dbType 为 null 时与原取反写法一致，均走单条锁定）
        if (ObjectUtils.notEqualsAny(dbType, DbType.ORACLE, DbType.ORACLE_12C)) {
            return CollUtil.getFirst(selectListByForUpdateMethod(SelectForUpdateMethod.Kind.LOCK_FIRST, queryWrapper, null));
        }

        // 情况二：Oracle，按“查询候选主键、按主键加锁、重新读取首行主键”重试，最多 5 次
        int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            // DONE @AI：getFirst 是不是可以简化下；看看其它地方，是不是也有地方；（处理：候选、重读主键与返回的锁定行均改为 CollUtil.getFirst；是否锁定到候选仍按列表是否为空判断）
            // 2.1 查询首行主键作为候选，不加锁；主键不为 null，取不到时即没有满足条件的记录
            Object id = CollUtil.getFirst(selectListByForUpdateMethod(SelectForUpdateMethod.Kind.FIRST_IDS, queryWrapper, null));
            if (id == null) {
                return null;
            }
            // 2.2 按原条件、且主键等于候选加锁；列表为空说明候选已不满足条件，重试
            // 注意：以列表是否为空判断是否锁定到候选，不能以元素判断：查询列均为 NULL 时，MyBatis 返回的元素为 null
            List<T> lockRows = selectListByForUpdateMethod(SelectForUpdateMethod.Kind.LOCK, queryWrapper, id);
            if (CollUtil.isEmpty(lockRows)) {
                continue;
            }
            // 2.3 锁定后重新读取首行主键，与候选相同才返回锁定的记录（查询列均为 NULL 时为 null）；不同时重试
            Object latestId = CollUtil.getFirst(selectListByForUpdateMethod(SelectForUpdateMethod.Kind.FIRST_IDS, queryWrapper, null));
            if (Objects.equals(id, latestId)) {
                return CollUtil.getFirst(lockRows);
            }
        }
        throw ServiceExceptionUtil.exception(GlobalErrorCodeConstants.LOCKED);
    }

    // DONE @AI：方法内注释噢；（处理：已补充校验、构建参数、执行内置查询三步注释）
    /**
     * 执行锁定读的内置查询，见 {@link SelectForUpdateMethod}
     *
     * @param kind         内置查询的类型
     * @param queryWrapper 查询条件
     * @param id           主键；仅 {@link SelectForUpdateMethod.Kind#LOCK} 使用，为 null 时不限制
     * @return 实体或主键列表
     */
    private <E> List<E> selectListByForUpdateMethod(SelectForUpdateMethod.Kind kind, LambdaQueryWrapper<T> queryWrapper,
                                                    Object id) {
        // 1. 校验查询条件
        checkForUpdateWrapper(queryWrapper);
        // 2. 构建参数：查询条件、主键；首行主键查询由分页插件限制为 1 条，不查询总数
        MapperMethod.ParamMap<Object> param = new MapperMethod.ParamMap<>();
        param.put(Constants.WRAPPER, queryWrapper);
        param.put(SelectForUpdateMethod.PARAM_ID, id);
        if (kind == SelectForUpdateMethod.Kind.FIRST_IDS) {
            param.put("page", MyBatisUtils.buildPage(0, 1, false));
        }
        // 3. 通过 SqlSession 执行内置查询：未在 Mapper 接口声明，MappedStatement 编号为 Mapper 接口名 + 方法名
        MapperProxyMetadata mapperProxyMetadata = MybatisUtils.getMapperProxy(this);
        return mapperProxyMetadata.getSqlSession().selectList(
                mapperProxyMetadata.getMapperInterface().getName() + Constants.DOT + kind.getMethod(), param);
    }

    // DONE @AI：方法内注释噢；（处理：已补充 last、groupBy/having、参数别名的校验注释）
    // DONE @AI：notequals；isNotEmpty 等等，减少取反；（处理：已改为 ObjectUtil.notEqual、CollUtil.isNotEmpty）
    /**
     * 校验锁定读的查询条件：内置查询只使用实体条件、普通条件与排序
     *
     * @param queryWrapper 查询条件
     */
    private void checkForUpdateWrapper(LambdaQueryWrapper<T> queryWrapper) {
        if (queryWrapper == null) {
            throw new IllegalArgumentException("锁定读的查询条件不能为空");
        }
        // 校验 last：设置 last 时，Wrapper 的 SQL 片段比条件表达式多出 last 部分
        MergeSegments expression = queryWrapper.getExpression();
        if (ObjectUtil.notEqual(queryWrapper.getSqlSegment(), expression.getSqlSegment())) {
            throw new IllegalArgumentException("锁定读的查询条件不能使用 last");
        }
        // 校验 groupBy、having：内置查询不生成这两部分
        if (CollUtil.isNotEmpty(expression.getGroupBy()) || CollUtil.isNotEmpty(expression.getHaving())) {
            throw new IllegalArgumentException("锁定读的查询条件不能使用 groupBy、having");
        }
        // 校验参数别名：内置查询的模板固定使用 ew 作为参数名
        if (ObjectUtil.notEqual(Constants.WRAPPER, queryWrapper.getParamAlias())) {
            throw new IllegalArgumentException("锁定读的查询条件不能设置参数别名");
        }
    }

    /**
     * 获取满足条件的第 1 条记录
     *
     * 目的：解决并发场景下，插入多条记录后，使用 selectOne 会报错的问题
     *
     * @param field 字段名
     * @param value 字段值
     * @return 实体
     */
    default T selectFirstOne(SFunction<T, ?> field, Object value) {
        List<T> list = selectList(new LambdaQueryWrapper<T>().eq(field, value));
        return CollUtil.getFirst(list);
    }

    default T selectFirstOne(SFunction<T, ?> field1, Object value1, SFunction<T, ?> field2, Object value2) {
        List<T> list = selectList(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2));
        return CollUtil.getFirst(list);
    }

    default T selectFirstOne(SFunction<T,?> field1, Object value1, SFunction<T,?> field2, Object value2,
                             SFunction<T,?> field3, Object value3) {
        List<T> list = selectList(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2).eq(field3, value3));
        return CollUtil.getFirst(list);
    }

    /**
     * 获取满足条件的最新一条记录
     * <p>
     * 目的：解决并发场景下，插入多条记录后，使用 selectOne 会报错的问题
     *
     * @param queryWrapper 查询条件
     * @return 最新一条；不存在返回 null
     */
    default T selectLastOne(LambdaQueryWrapper<T> queryWrapper) {
        return CollUtil.getLast(selectList(queryWrapper));
    }

    /**
     * 获取满足条件的第 1 条记录，见 {@link #selectListLimitN(Wrapper, int)}
     *
     * @param queryWrapper 查询条件
     * @return 第 1 条；不存在返回 null
     */
    default T selectFirstOne(Wrapper<T> queryWrapper) {
        return CollUtil.getFirst(selectListLimitN(queryWrapper, 1));
    }

    /**
     * 获取满足条件的前 limit 条记录
     *
     * 使用 MyBatis Plus 分页插件，按执行时连接的数据库类型生成 LIMIT、ROWNUM、OFFSET FETCH 等语句，并且不执行 COUNT 查询；
     * 保留 queryWrapper 的排序与 select 投影，不能再通过 last 追加行数限制
     *
     * 注意：SQL Server 的分页语句要求有 ORDER BY，queryWrapper 需要设置排序
     *
     * @param queryWrapper 查询条件
     * @param limit        条数，不能为负数；为 0 时不查询，直接返回空列表
     * @return 记录列表
     */
    default List<T> selectListLimitN(Wrapper<T> queryWrapper, int limit) {
        if (limit < 0) {
            // 分页插件会把负数的 size 视为不分页，从而查询全表
            throw new IllegalArgumentException("limit 不能为负数：" + limit);
        }
        if (limit == 0) {
            return CollUtil.newArrayList();
        }
        return selectPage(MyBatisUtils.<T>buildPage(0, limit, false), queryWrapper).getRecords();
    }

    default Long selectCount() {
        return selectCount(new QueryWrapper<>());
    }

    default Long selectCount(String field, Object value) {
        return selectCount(new QueryWrapper<T>().eq(field, value));
    }

    default Long selectCount(SFunction<T, ?> field, Object value) {
        return selectCount(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default List<T> selectList() {
        return selectList(new QueryWrapper<>());
    }

    default List<T> selectList(String field, Object value) {
        return selectList(new QueryWrapper<T>().eq(field, value));
    }

    default List<T> selectList(SFunction<T, ?> field, Object value) {
        return selectList(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default List<T> selectList(String field, Collection<?> values) {
        if (CollUtil.isEmpty(values)) {
            return CollUtil.newArrayList();
        }
        return selectList(new QueryWrapper<T>().in(field, values));
    }

    default List<T> selectList(SFunction<T, ?> field, Collection<?> values) {
        if (CollUtil.isEmpty(values)) {
            return CollUtil.newArrayList();
        }
        return selectList(new LambdaQueryWrapper<T>().in(field, values));
    }

    default List<T> selectList(SFunction<T, ?> field1, Object value1, SFunction<T, ?> field2, Object value2) {
        return selectList(new LambdaQueryWrapper<T>().eq(field1, value1).eq(field2, value2));
    }

    /**
     * 批量插入，适合大量数据插入
     *
     * @param entities 实体们
     */
    default Boolean insertBatch(Collection<T> entities) {
        // 特殊：SQL Server 批量插入后，获取 id 会报错，因此通过循环处理
        DbType dbType = JdbcUtils.getDbType();
        if (JdbcUtils.isSQLServer(dbType)) {
            entities.forEach(this::insert);
            return CollUtil.isNotEmpty(entities);
        }
        return Db.saveBatch(entities);
    }

    /**
     * 批量插入，适合大量数据插入
     *
     * @param entities 实体们
     * @param size     插入数量 Db.saveBatch 默认为 1000
     */
    default Boolean insertBatch(Collection<T> entities, int size) {
        // 特殊：SQL Server 批量插入后，获取 id 会报错，因此通过循环处理
        DbType dbType = JdbcUtils.getDbType();
        if (JdbcUtils.isSQLServer(dbType)) {
            entities.forEach(this::insert);
            return CollUtil.isNotEmpty(entities);
        }
        return Db.saveBatch(entities, size);
    }

    default int updateBatch(T update) {
        return update(update, new QueryWrapper<>());
    }

    default Boolean updateBatch(Collection<T> entities) {
        return Db.updateBatchById(entities);
    }

    default Boolean updateBatch(Collection<T> entities, int size) {
        return Db.updateBatchById(entities, size);
    }

    default int delete(String field, String value) {
        return delete(new QueryWrapper<T>().eq(field, value));
    }

    default int delete(SFunction<T, ?> field, Object value) {
        return delete(new LambdaQueryWrapper<T>().eq(field, value));
    }

    default int deleteBatch(SFunction<T, ?> field, Collection<?> values) {
        if (CollUtil.isEmpty(values)) {
            return 0;
        }
        return delete(new LambdaQueryWrapper<T>().in(field, values));
    }

}
