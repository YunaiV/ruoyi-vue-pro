package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkDatabaseConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * {@link IotDatabaseDataRuleAction} 的单元测试
 *
 * @author HUIHUI
 */
class IotDatabaseDataRuleActionTest {

    private IotDatabaseDataRuleAction databaseDataRuleAction;

    @Mock
    private JdbcTemplate jdbcTemplate;

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
            IotDataSinkDatabaseConfig config = buildConfig(tableName);
            IotDatabaseDataRuleAction action = spy(databaseDataRuleAction);
            doReturn(jdbcTemplate).when(action).getProducer(same(config));

            // 调用
            action.execute(buildMessage(), config);

            // 断言：表名原样拼接，值使用参数绑定
            verify(jdbcTemplate).update(eq("INSERT INTO " + tableName
                            + " (id, device_id, tenant_id, method, report_time, data, create_time) VALUES (?, ?, ?, ?, ?, ?, NOW())"),
                    any(), any(), any(), any(), any(), any());
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

    private static IotDataSinkDatabaseConfig buildConfig(String tableName) {
        IotDataSinkDatabaseConfig config = new IotDataSinkDatabaseConfig();
        config.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/iot_data");
        config.setUsername("root");
        config.setPassword("123456");
        config.setTableName(tableName);
        return config;
    }

    private static IotDeviceMessage buildMessage() {
        return new IotDeviceMessage().setId("message_01").setDeviceId(1L).setTenantId(1L)
                .setMethod("thing.property.post").setReportTime(LocalDateTime.now());
    }

}
