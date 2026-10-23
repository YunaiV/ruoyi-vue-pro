package cn.iocoder.yudao.module.bpm;

import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.DmDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.core.BooleanType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.lang.reflect.Proxy;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Flowable 6.8.0 的达梦 Liquibase SPI 测试
 *
 * @author 芋道源码
 */
class DmDatabaseTest {

    @AfterEach
    void tearDown() {
        DatabaseFactory.reset();
    }

    @Test
    void testDriverAndOtherDatabases() throws Exception {
        DmDatabase database = new DmDatabase();
        assertEquals("dm", database.getShortName());
        assertEquals("dm.jdbc.driver.DmDriver", database.getDefaultDriver("jdbc:dm://localhost:5236"));
        assertNull(database.getDefaultDriver(null));
        assertNull(database.getDefaultDriver("jdbc:mysql://localhost/test"));
        assertEquals(5236, database.getDefaultPort());
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:dm_spi_regression", "sa", "");
        try (Connection connection = dataSource.getConnection()) {
            assertInstanceOf(H2Database.class, DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection)));
        }
        assertEquals(new BooleanType().toDatabaseDataType(new MySQLDatabase()).toString(),
                DataTypeFactory.getInstance().fromDescription("boolean", new MySQLDatabase())
                        .toDatabaseDataType(new MySQLDatabase()).toString());
    }

    @Test
    void testOracleAndDmRegistrationOrder() throws Exception {
        for (boolean dmFirst : new boolean[]{true, false}) {
            DatabaseFactory factory = DatabaseFactory.getInstance();
            factory.clearRegistry();
            factory.register(dmFirst ? new DmDatabase() : new OracleDatabase());
            factory.register(dmFirst ? new OracleDatabase() : new DmDatabase());
            assertInstanceOf(DmDatabase.class, factory.findCorrectDatabaseImplementation(metadata("DM DBMS")));
            assertEquals(OracleDatabase.class, factory.findCorrectDatabaseImplementation(metadata("Oracle")).getClass());
        }
    }

    @Test
    void testDmMetadataAndBooleanType() throws Exception {
        assumeTrue(System.getenv("DM_JDBC_URL") != null, "设置 DM_JDBC_URL 后执行真实达梦测试");
        DriverManagerDataSource dataSource = new DriverManagerDataSource(System.getenv("DM_JDBC_URL"),
                System.getenv("DM_JDBC_USER"), System.getenv("DM_JDBC_PASSWORD"));
        try (Connection connection = dataSource.getConnection()) {
            liquibase.database.Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            assertInstanceOf(DmDatabase.class, database);
            assertEquals("NUMBER(1)", DataTypeFactory.getInstance().fromDescription("boolean", database)
                    .toDatabaseDataType(database).toString());
        }
    }

    private DatabaseConnection metadata(String product) {
        return (DatabaseConnection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{DatabaseConnection.class}, (proxy, method, arguments) -> {
                    if ("getDatabaseProductName".equals(method.getName())) return product;
                    if ("getConnectionUserName".equals(method.getName())) return "TEST_USER";
                    if ("getURL".equals(method.getName())) return "DM DBMS".equals(product) ? "jdbc:dm://test" : "jdbc:oracle:thin:@test";
                    if (method.getReturnType() == int.class) return 8;
                    if (method.getReturnType() == boolean.class) return false;
                    return null;
                });
    }

}
