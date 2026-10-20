package cn.iocoder.yudao.framework.mybatis.core.util;

import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Locale;

/**
 * key 大小写不敏感的 MyBatis 查询结果 Map
 *
 * 背景：使用 Map 接收查询结果时，key 为 JDBC 返回的列标签。PostgreSQL 等会将未加引号的列名、别名折叠为小写，
 * Oracle、达梦等会折叠为大写，导致 map.get("conversationId") 这类按固定 key 取值的代码，在 MySQL 以外的数据库取不到值。
 *
 * 实现：基于 {@link LinkedCaseInsensitiveMap}，get / containsKey / remove 时忽略 key 的大小写，
 * 同时保留原始的列标签作为 key（遍历、序列化时不变）。固定使用 {@link Locale#ROOT}，避免服务器默认 Locale（例如土耳其语）影响 key 的匹配。
 *
 * 使用方式：由 MyBatis 直接创建结果行，Mapper 的返回类型仍声明为 Map，业务无需感知该类
 * 1. MyBatis Plus 的 selectMaps：由 YudaoSqlInjector 注入，自动使用该类
 * 2. MyBatis Plus Join 的 selectJoinMaps 等：由 BaseMapperX 覆盖，自动使用该类
 * 3. XML SQL：将 resultType 设置为该类
 * 4. 注解 SQL：添加 @ResultMap({@link #RESULT_MAP_ID})
 *
 * 注意：仅大小写不同的多个 key，会合并为一个，保留后写入的 key 和值
 *
 * @author 芋道源码
 * @see <a href="https://t.zsxq.com/kPvqm">星球讨论：Map 查询结果的跨数据库兼容问题</a>
 */
public class CaseInsensitiveResultMap<V> extends LinkedCaseInsensitiveMap<V> {

    /**
     * 注解 SQL 使用的 resultMap 编号
     *
     * 由 YudaoMybatisAutoConfiguration 注册到 MyBatis 全局配置；包含“.”，所以不会拼接 Mapper 的 namespace
     */
    public static final String RESULT_MAP_ID = "cn.iocoder.yudao.framework.mybatis.core.util.CaseInsensitiveResultMap";

    /**
     * 创建空的结果 Map
     *
     * 用途：MyBatis 通过无参构造方法，实例化结果行
     */
    public CaseInsensitiveResultMap() {
        super(Locale.ROOT);
    }

}
