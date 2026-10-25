package cn.iocoder.yudao.framework.mybatis.config;

import cn.iocoder.yudao.framework.mybatis.core.util.JdbcUtils;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.incrementer.PostgreKeyGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link IdTypeEnvironmentPostProcessor} 的单元测试
 *
 * @author 芋道源码
 */
public class IdTypeEnvironmentPostProcessorTest {

    private static final String ID_TYPE_KEY = "mybatis-plus.global-config.db-config.id-type";
    private static final String QUARTZ_DELEGATE_KEY = "spring.quartz.properties.org.quartz.jobStore.driverDelegateClass";
    private static final String POSTGRESQL_DELEGATE = "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";

    private final IdTypeEnvironmentPostProcessor processor = new IdTypeEnvironmentPostProcessor();

    @BeforeEach
    @AfterEach
    public void clearQuartzDelegate() {
        // Quartz Delegate 写入的是 JVM 系统属性，需要清理，避免影响其它用例
        System.clearProperty(QUARTZ_DELEGATE_KEY);
    }

    @Test
    public void testPostProcessEnvironment_highGoNativeUrl() {
        // 准备参数
        StandardEnvironment environment = buildEnvironment("jdbc:highgo://127.0.0.1:5866/ruoyi-vue-pro", null);

        // 调用
        processor.postProcessEnvironment(environment, null);

        // 断言：与 PostgreSQL 一致，使用序列主键及 PostgreSQLDelegate
        assertEquals("INPUT", environment.getProperty(ID_TYPE_KEY));
        assertEquals(POSTGRESQL_DELEGATE, environment.getProperty(QUARTZ_DELEGATE_KEY));
        assertInstanceOf(PostgreKeyGenerator.class, new YudaoMybatisAutoConfiguration().keyGenerator(environment));
    }

    @Test
    public void testPostProcessEnvironment_gaussDBNativeUrl() {
        // 准备参数
        StandardEnvironment environment = buildEnvironment("jdbc:gaussdb://127.0.0.1:8000/ruoyi-vue-pro", null);

        // 调用
        processor.postProcessEnvironment(environment, null);

        // 断言：GaussDB（PG 兼容模式）与 PostgreSQL 一致，使用序列主键及 PostgreSQLDelegate
        assertEquals("INPUT", environment.getProperty(ID_TYPE_KEY));
        assertEquals(POSTGRESQL_DELEGATE, environment.getProperty(QUARTZ_DELEGATE_KEY));
        assertInstanceOf(PostgreKeyGenerator.class, new YudaoMybatisAutoConfiguration().keyGenerator(environment));
    }

    @Test
    public void testPostProcessEnvironment_openGaussNativeUrl() {
        // 准备参数：openGauss、MogDB 使用 openGauss JDBC 驱动
        StandardEnvironment environment = buildEnvironment("jdbc:opengauss://127.0.0.1:15432/ruoyi-vue-pro", null);

        // 调用
        processor.postProcessEnvironment(environment, null);

        // 断言：与 PostgreSQL 一致，使用序列主键及 PostgreSQLDelegate
        assertEquals("INPUT", environment.getProperty(ID_TYPE_KEY));
        assertEquals(POSTGRESQL_DELEGATE, environment.getProperty(QUARTZ_DELEGATE_KEY));
        assertInstanceOf(PostgreKeyGenerator.class, new YudaoMybatisAutoConfiguration().keyGenerator(environment));
    }

    @Test
    public void testPostProcessEnvironment_notNormalizedNativeUrls() {
        // 海量 Vastbase 原生驱动、神通、PolarDB（兼容 Oracle）未按 PostgreSQL 处理：保持自增主键，且不设置 Quartz Delegate
        for (String url : Arrays.asList("jdbc:vastbase://127.0.0.1:5432/ruoyi-vue-pro",
                "jdbc:oscar://127.0.0.1:2003/OSRDB", "jdbc:polardb://127.0.0.1:1521/ruoyi-vue-pro")) {
            // 准备参数
            StandardEnvironment environment = buildEnvironment(url, null);

            // 调用
            processor.postProcessEnvironment(environment, null);

            // 断言
            assertEquals("AUTO", environment.getProperty(ID_TYPE_KEY), url);
            assertNull(environment.getProperty(QUARTZ_DELEGATE_KEY), url);
        }
    }

    @Test
    public void testPostProcessEnvironment_postgresqlUrl() {
        // 准备参数
        StandardEnvironment environment = buildEnvironment("jdbc:postgresql://127.0.0.1:5866/ruoyi-vue-pro", null);

        // 调用
        processor.postProcessEnvironment(environment, null);

        // 断言
        assertEquals("INPUT", environment.getProperty(ID_TYPE_KEY));
        assertEquals(POSTGRESQL_DELEGATE, environment.getProperty(QUARTZ_DELEGATE_KEY));
        assertInstanceOf(PostgreKeyGenerator.class, new YudaoMybatisAutoConfiguration().keyGenerator(environment));
    }

    @Test
    public void testPostProcessEnvironment_mysqlUrl() {
        // 准备参数
        StandardEnvironment environment = buildEnvironment("jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro", null);

        // 调用
        processor.postProcessEnvironment(environment, null);

        // 断言：MySQL 保持自增主键，且不设置 Quartz Delegate
        assertEquals("AUTO", environment.getProperty(ID_TYPE_KEY));
        assertNull(environment.getProperty(QUARTZ_DELEGATE_KEY));
    }

    @Test
    public void testPostProcessEnvironment_highGoNativeUrlWithExplicitConfig() {
        // 准备参数：用户显式配置主键策略及 Quartz Delegate
        Map<String, Object> explicit = new HashMap<>();
        explicit.put(ID_TYPE_KEY, "ASSIGN_ID");
        explicit.put(QUARTZ_DELEGATE_KEY, "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        StandardEnvironment environment = buildEnvironment("jdbc:highgo://127.0.0.1:5866/ruoyi-vue-pro", explicit);

        // 调用
        processor.postProcessEnvironment(environment, null);

        // 断言：不覆盖用户配置
        assertEquals("ASSIGN_ID", environment.getProperty(ID_TYPE_KEY));
        assertEquals("org.quartz.impl.jdbcjobstore.StdJDBCDelegate", environment.getProperty(QUARTZ_DELEGATE_KEY));
        assertNull(System.getProperty(QUARTZ_DELEGATE_KEY));
    }

    @Test
    public void testGetDbType_normalizePostgreSQLCompatible() {
        // 按 PostgreSQL 处理：瀚高、openGauss（MogDB）、GaussDB 的原生驱动
        assertEquals(DbType.POSTGRE_SQL, JdbcUtils.getDbType("jdbc:highgo://127.0.0.1:5866/ruoyi-vue-pro"));
        assertEquals(DbType.POSTGRE_SQL, JdbcUtils.getDbType("jdbc:opengauss://127.0.0.1:15432/ruoyi-vue-pro"));
        assertEquals(DbType.POSTGRE_SQL, JdbcUtils.getDbType("jdbc:gaussdb://127.0.0.1:8000/ruoyi-vue-pro"));
        assertEquals(DbType.POSTGRE_SQL, JdbcUtils.getDbType("jdbc:postgresql://127.0.0.1:5432/ruoyi-vue-pro"));
        // 保持 MyBatis Plus 的识别结果
        assertEquals(DbType.MYSQL, JdbcUtils.getDbType("jdbc:mysql://127.0.0.1:3306/ruoyi-vue-pro"));
        assertEquals(DbType.KINGBASE_ES, JdbcUtils.getDbType("jdbc:kingbase8://127.0.0.1:54321/test"));
        assertEquals(DbType.DM, JdbcUtils.getDbType("jdbc:dm://127.0.0.1:5236?schema=RUOYI_VUE_PRO"));
        assertEquals(DbType.VASTBASE, JdbcUtils.getDbType("jdbc:vastbase://127.0.0.1:5432/ruoyi-vue-pro"));
        assertEquals(DbType.OSCAR, JdbcUtils.getDbType("jdbc:oscar://127.0.0.1:2003/OSRDB"));
        assertEquals(DbType.OTHER, JdbcUtils.getDbType("jdbc:polardb://127.0.0.1:1521/ruoyi-vue-pro"));
    }

    private static StandardEnvironment buildEnvironment(String url, Map<String, Object> extra) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.dynamic.primary", "master");
        properties.put("spring.datasource.dynamic.datasource.master.url", url);
        properties.put(ID_TYPE_KEY, "NONE");
        if (extra != null) {
            properties.putAll(extra);
        }
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new MapPropertySource("test", properties));
        return environment;
    }

}
