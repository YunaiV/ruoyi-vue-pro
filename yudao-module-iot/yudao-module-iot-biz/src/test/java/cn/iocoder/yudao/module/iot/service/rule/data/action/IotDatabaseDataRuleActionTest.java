package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

}
