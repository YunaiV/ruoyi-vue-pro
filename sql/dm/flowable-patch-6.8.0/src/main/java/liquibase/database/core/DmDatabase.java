package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

/**
 * 达梦数据库的 Liquibase 适配，复用 Oracle 的字段类型与建表语法
 *
 * @author 芋道源码
 */
public class DmDatabase extends OracleDatabase {

    @Override
    public String getShortName() {
        // 独立注册达梦，避免与 Oracle 的 SPI 注册键及优先级冲突
        return "dm";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "DM DBMS";
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection connection) throws DatabaseException {
        return "DM DBMS".equalsIgnoreCase(connection.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        return url != null && url.startsWith("jdbc:dm:") ? "dm.jdbc.driver.DmDriver" : null;
    }

    @Override
    public Integer getDefaultPort() {
        return 5236;
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        // 特殊：部分达梦驱动返回非数字次版本号，避免 Oracle 版本判断时解析失败
        try {
            return super.getDatabaseMinorVersion();
        } catch (DatabaseException e) {
            return 0;
        }
    }

}
