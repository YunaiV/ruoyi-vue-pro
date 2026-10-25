package cn.iocoder.yudao.module.bpm.framework.flowable.config;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.bpm.framework.flowable.core.behavior.BpmActivityBehaviorFactory;
import org.flowable.common.engine.api.delegate.FlowableFunctionDelegate;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BpmFlowableConfiguration} 的单元测试
 *
 * @author 芋道源码
 */
public class BpmFlowableConfigurationTest extends BaseMockitoUnitTest {

    @Mock
    private ObjectProvider<FlowableEventListener> listeners;
    @Mock
    private ObjectProvider<FlowableFunctionDelegate> customFlowableFunctionDelegates;

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private DatabaseMetaData metaData;

    @Test
    public void testBpmProcessEngineConfigurationConfigurer_gaussDB() throws SQLException {
        // mock 方法：GaussDB 原生驱动返回的 ProductName
        mockDatabaseProductName("GaussDB");
        // 准备参数
        SpringProcessEngineConfiguration configuration = buildConfiguration(null);

        // 调用
        configure(configuration);

        // 断言：按 PostgreSQL 处理，并关闭连接
        assertEquals(AbstractEngineConfiguration.DATABASE_TYPE_POSTGRES, configuration.getDatabaseType());
        verify(connection).close();
    }

    @Test
    public void testBpmProcessEngineConfigurationConfigurer_openGauss() throws SQLException {
        // mock 方法：org.opengauss 的 openGauss JDBC 驱动（jdbc:opengauss:）连接 MogDB 时返回的 ProductName
        mockDatabaseProductName("openGauss");
        // 准备参数
        SpringProcessEngineConfiguration configuration = buildConfiguration(null);

        // 调用
        configure(configuration);

        // 断言：按 PostgreSQL 处理，并关闭连接
        assertEquals(AbstractEngineConfiguration.DATABASE_TYPE_POSTGRES, configuration.getDatabaseType());
        verify(connection).close();
    }

    @Test
    public void testBpmProcessEngineConfigurationConfigurer_postgreSQL() throws SQLException {
        // mock 方法：PostgreSQL，以及 io.mogdb 的 openGauss JDBC 驱动连接 MogDB 时返回的 ProductName
        mockDatabaseProductName("PostgreSQL");
        // 准备参数
        SpringProcessEngineConfiguration configuration = buildConfiguration(null);

        // 调用
        configure(configuration);

        // 断言：不处理，仍由 Flowable 自动识别
        assertNull(configuration.getDatabaseType());
        verify(connection).close();
    }

    @Test
    public void testBpmProcessEngineConfigurationConfigurer_explicitDatabaseType() throws SQLException {
        // 准备参数：用户显式设置 databaseType
        SpringProcessEngineConfiguration configuration = buildConfiguration(AbstractEngineConfiguration.DATABASE_TYPE_MYSQL);

        // 调用
        configure(configuration);

        // 断言：不覆盖，且不获取连接
        assertEquals(AbstractEngineConfiguration.DATABASE_TYPE_MYSQL, configuration.getDatabaseType());
        verify(dataSource, never()).getConnection();
    }

    @Test
    public void testBpmProcessEngineConfigurationConfigurer_connectionError() throws SQLException {
        // mock 方法：获取连接失败
        when(dataSource.getConnection()).thenThrow(new SQLException("connection refused"));
        // 准备参数
        SpringProcessEngineConfiguration configuration = buildConfiguration(null);

        // 调用
        configure(configuration);

        // 断言：不处理，由 Flowable 自动识别时报错
        assertNull(configuration.getDatabaseType());
    }

    private void mockDatabaseProductName(String productName) throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn(productName);
    }

    private SpringProcessEngineConfiguration buildConfiguration(String databaseType) {
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setDatabaseType(databaseType);
        return configuration;
    }

    private void configure(SpringProcessEngineConfiguration configuration) {
        when(listeners.iterator()).thenReturn(Collections.emptyIterator());
        when(customFlowableFunctionDelegates.stream()).thenReturn(Stream.empty());
        new BpmFlowableConfiguration().bpmProcessEngineConfigurationConfigurer(listeners,
                customFlowableFunctionDelegates, new BpmActivityBehaviorFactory()).configure(configuration);
    }

}
