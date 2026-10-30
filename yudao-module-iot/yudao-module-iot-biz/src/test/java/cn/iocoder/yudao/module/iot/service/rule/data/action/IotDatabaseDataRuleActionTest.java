package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkDatabaseConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * {@link IotDatabaseDataRuleAction} 的单元测试
 *
 * @author HUIHUI
 */
class IotDatabaseDataRuleActionTest {

    private static final String TABLE_NAME = "iot_device_message";

    private IotDatabaseDataRuleAction databaseDataRuleAction;

    @Mock
    private JdbcTemplate jdbcTemplate;

    /**
     * 外部数据库：由真实的 {@link JdbcTemplate} 管理连接、Statement 的生命周期
     */
    private DataSource externalDataSource;
    private Connection externalConnection;
    private DatabaseMetaData externalMetaData;
    private PreparedStatement externalStatement;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        databaseDataRuleAction = new IotDatabaseDataRuleAction();
    }

    @Test
    public void testGetType() {
        // 调用 & 断言：返回 Database 类型枚举值
        assertEquals(IotDataSinkTypeEnum.DATABASE.getType(), databaseDataRuleAction.getType());
    }

    @Test
    public void testExecute_tableNameValid() throws Exception {
        // 准备参数：普通表名，以及 schema.table 格式
        for (String tableName : Arrays.asList("iot_device_message", "_message01", "iot_data.device_message")) {
            mockExternalDatabase("MySQL");
            IotDataSinkDatabaseConfig config = buildConfig(tableName);
            IotDatabaseDataRuleAction action = spyAction(config);

            // 调用
            action.execute(buildMessage(), config);

            // 断言：表名原样拼接，值使用参数绑定
            verify(externalConnection).prepareStatement(buildInsertSql(tableName, "NOW()"));
        }
    }

    @Test
    public void testExecute_tableNameInvalid() throws Exception {
        // 准备参数：模拟历史已保存的非法表名，执行时也必须拦截
        for (String tableName : Arrays.asList(null, "", "1message", "device message",
                "message; DROP TABLE system_users", "message -- ", "message/**/", "`message`", "\"message\"",
                "message(id)", "a.b.c", ".message", "message.", "表名")) {
            IotDataSinkDatabaseConfig config = buildConfig(tableName);
            IotDatabaseDataRuleAction action = spy(databaseDataRuleAction);

            // 调用，并断言异常
            assertThrows(IllegalArgumentException.class, () -> action.execute(buildMessage(), config));
            // 断言：校验在获取连接之前，不会创建连接池、执行 SQL
            verify(action, never()).getProducer(any());
        }
        verifyNoInteractions(jdbcTemplate);
    }

    @Test
    public void testExecute_createTimeByDatabaseProduct() throws Exception {
        // 准备参数：数据库产品名 -> create_time 使用的时间函数
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("Oracle", "CURRENT_TIMESTAMP");
        cases.put("Microsoft SQL Server", "CURRENT_TIMESTAMP");
        cases.put("MySQL", "NOW()");
        cases.put("PostgreSQL", "NOW()");
        cases.put("DM DBMS", "NOW()"); // 未登记的产品，保持原 NOW()
        cases.put(null, "NOW()");
        for (Map.Entry<String, String> entry : cases.entrySet()) {
            mockExternalDatabase(entry.getKey());
            IotDeviceMessage message = buildMessage();
            IotDataSinkDatabaseConfig config = buildConfig(TABLE_NAME);
            IotDatabaseDataRuleAction action = spyAction(config);

            // 调用
            action.execute(message, config);

            // 断言：只借用一次外部连接，在同一连接上读取产品名、创建 Statement
            verify(externalDataSource, times(1)).getConnection();
            InOrder inOrder = inOrder(externalConnection, externalMetaData, externalStatement);
            inOrder.verify(externalConnection).getMetaData();
            inOrder.verify(externalMetaData).getDatabaseProductName();
            inOrder.verify(externalConnection).prepareStatement(buildInsertSql(TABLE_NAME, entry.getValue()));
            // 断言：原 6 个参数的值、绑定方式、顺序不变
            inOrder.verify(externalStatement).setString(1, "message_01");
            inOrder.verify(externalStatement).setObject(2, 1L);
            inOrder.verify(externalStatement).setObject(3, 2L);
            inOrder.verify(externalStatement).setString(4, "thing.property.post");
            inOrder.verify(externalStatement).setObject(5, message.getReportTime());
            inOrder.verify(externalStatement).setString(6, JsonUtils.toJsonString(message));
            inOrder.verify(externalStatement).executeUpdate();
            // 断言：Statement、连接由 JdbcTemplate 关闭、归还
            inOrder.verify(externalStatement).close();
            inOrder.verify(externalConnection).close();
            verify(externalConnection, times(1)).prepareStatement(anyString());
        }
    }

    @Test
    public void testExecute_getMetaDataFailure() throws Exception {
        // 准备参数：读取外部数据库元数据失败
        mockExternalDatabase("Oracle");
        SQLException metaDataException = new SQLException("metadata failure");
        when(externalConnection.getMetaData()).thenThrow(metaDataException);
        IotDataSinkDatabaseConfig config = buildConfig(TABLE_NAME);
        IotDatabaseDataRuleAction action = spyAction(config);

        // 调用，并断言异常：不吞掉原始异常
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> action.execute(buildMessage(), config));
        assertSame(metaDataException, exception.getCause());
        // 断言：未创建 Statement，连接被归还
        verify(externalConnection, never()).prepareStatement(anyString());
        verify(externalConnection, times(1)).close();
        verify(externalDataSource, times(1)).getConnection();
    }

    @Test
    public void testExecute_bindFailure() throws Exception {
        // 准备参数：绑定参数失败
        mockExternalDatabase("Microsoft SQL Server");
        SQLException bindException = new SQLException("bind failure");
        doThrow(bindException).when(externalStatement).setString(1, "message_01");
        IotDataSinkDatabaseConfig config = buildConfig(TABLE_NAME);
        IotDatabaseDataRuleAction action = spyAction(config);

        // 调用，并断言异常：不吞掉原始异常
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> action.execute(buildMessage(), config));
        assertSame(bindException, exception.getCause());
        // 断言：未执行 SQL，已创建的 Statement 被关闭，连接被归还
        verify(externalStatement, never()).executeUpdate();
        verify(externalStatement, times(1)).close();
        verify(externalConnection, times(1)).close();
        verify(externalDataSource, times(1)).getConnection();
    }

    @Test
    public void testExecute_executeUpdateFailure() throws Exception {
        // 准备参数：执行 SQL 失败
        mockExternalDatabase("PostgreSQL");
        SQLException executeException = new SQLException("execute failure");
        when(externalStatement.executeUpdate()).thenThrow(executeException);
        IotDataSinkDatabaseConfig config = buildConfig(TABLE_NAME);
        IotDatabaseDataRuleAction action = spyAction(config);

        // 调用，并断言异常：不吞掉原始异常
        DataAccessException exception = assertThrows(DataAccessException.class,
                () -> action.execute(buildMessage(), config));
        assertSame(executeException, exception.getCause());
        // 断言：Statement 被关闭，连接被归还
        verify(externalConnection).prepareStatement(buildInsertSql(TABLE_NAME, "NOW()"));
        verify(externalStatement, times(1)).close();
        verify(externalConnection, times(1)).close();
        verify(externalDataSource, times(1)).getConnection();
    }

    @Test
    public void testCloseProducer_whenHikari() throws Exception {
        // 准备：底层是 HikariDataSource
        HikariDataSource hikari = mock(HikariDataSource.class);
        when(jdbcTemplate.getDataSource()).thenReturn(hikari);

        // 调用
        databaseDataRuleAction.closeProducer(jdbcTemplate);

        // 断言：HikariDataSource 被关闭
        verify(hikari, times(1)).close();
    }

    @Test
    public void testCloseProducer_whenNotHikari() throws Exception {
        // 准备：底层不是 HikariDataSource，避免误调 close
        DataSource other = mock(DataSource.class);
        when(jdbcTemplate.getDataSource()).thenReturn(other);

        // 调用 & 断言：不抛异常，且不会尝试关闭非 Hikari 数据源
        assertDoesNotThrow(() -> databaseDataRuleAction.closeProducer(jdbcTemplate));
        verifyNoInteractions(other);
    }

    /**
     * 模拟外部数据库：DataSource 每次返回同一个 Connection，产品名来自该 Connection 的元数据
     */
    private void mockExternalDatabase(String databaseProductName) throws SQLException {
        externalDataSource = mock(DataSource.class);
        externalConnection = mock(Connection.class);
        externalMetaData = mock(DatabaseMetaData.class);
        externalStatement = mock(PreparedStatement.class);
        when(externalDataSource.getConnection()).thenReturn(externalConnection);
        when(externalConnection.getMetaData()).thenReturn(externalMetaData);
        when(externalMetaData.getDatabaseProductName()).thenReturn(databaseProductName);
        when(externalConnection.prepareStatement(anyString())).thenReturn(externalStatement);
        when(externalStatement.executeUpdate()).thenReturn(1);
    }

    /**
     * 创建 Action，其 producer 为基于外部数据库的真实 {@link JdbcTemplate}
     */
    private IotDatabaseDataRuleAction spyAction(IotDataSinkDatabaseConfig config) throws Exception {
        IotDatabaseDataRuleAction action = spy(databaseDataRuleAction);
        doReturn(new JdbcTemplate(externalDataSource)).when(action).getProducer(same(config));
        return action;
    }

    private static String buildInsertSql(String tableName, String now) {
        return "INSERT INTO " + tableName
                + " (id, device_id, tenant_id, method, report_time, data, create_time) VALUES (?, ?, ?, ?, ?, ?, " + now + ")";
    }

    private static IotDataSinkDatabaseConfig buildConfig(String tableName) {
        IotDataSinkDatabaseConfig config = new IotDataSinkDatabaseConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/iot_data");
        config.setUsername("root");
        config.setPassword("123456");
        config.setTableName(tableName);
        return config;
    }

    private static IotDeviceMessage buildMessage() {
        return new IotDeviceMessage().setId("message_01").setDeviceId(1L).setTenantId(2L)
                .setMethod("thing.property.post").setReportTime(LocalDateTime.now());
    }

}
