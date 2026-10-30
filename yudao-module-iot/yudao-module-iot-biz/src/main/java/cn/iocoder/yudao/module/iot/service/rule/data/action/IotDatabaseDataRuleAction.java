package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkDatabaseConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Component;

/**
 * Database 的 {@link IotDataRuleAction} 实现类
 *
 * 通过 JDBC 连接数据库，将设备消息写入指定表。
 * 支持 MySQL、PostgreSQL、Oracle、SQL Server、DM 达梦等数据库，
 * HikariCP 会根据 JDBC URL 自动加载对应的驱动。
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotDatabaseDataRuleAction extends
        IotDataRuleCacheableAction<IotDataSinkDatabaseConfig, JdbcTemplate> {

    /**
     * 不支持 NOW()、改用 CURRENT_TIMESTAMP 的数据库产品名，与 JDBC 驱动的 DatabaseMetaData#getDatabaseProductName 一致
     */
    private static final String[] CURRENT_TIMESTAMP_PRODUCT_NAMES = {"Oracle", "Microsoft SQL Server"};

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.DATABASE.getType();
    }

    @Override
    public void execute(IotDeviceMessage message, IotDataSinkDatabaseConfig config) throws Exception {
        try {
            // 0. 校验表名：执行时再次校验，避免历史保存的非法表名拼接到 SQL 中
            if (!ReUtil.isMatch(IotDataSinkDatabaseConfig.TABLE_NAME_REGEX, config.getTableName())) {
                throw new IllegalArgumentException(StrUtil.format("数据库目的的表名({}) 不合法", config.getTableName()));
            }
            // 1. 获取或创建 JdbcTemplate
            JdbcTemplate jdbcTemplate = getProducer(config);

            // 2. 构建并执行 INSERT SQL
            String messageJson = JsonUtils.toJsonString(message);
            ArgumentPreparedStatementSetter parameterSetter = new ArgumentPreparedStatementSetter(new Object[]{
                    message.getId(),
                    message.getDeviceId(),
                    message.getTenantId(),
                    message.getMethod(),
                    message.getReportTime(),
                    messageJson});
            // 2.1 在执行 INSERT 的同一外部连接上识别数据库产品，选择 create_time 使用的数据库时间函数
            PreparedStatementCreator statementCreator = connection -> connection.prepareStatement(
                    buildInsertSql(config.getTableName(), connection.getMetaData().getDatabaseProductName()));
            // 2.2 Statement 由 JdbcTemplate 创建后再绑定参数：绑定或执行失败时，由 JdbcTemplate 关闭 Statement、释放连接
            PreparedStatementCallback<Integer> statementCallback = statement -> {
                try {
                    parameterSetter.setValues(statement);
                    return statement.executeUpdate();
                } finally {
                    parameterSetter.cleanupParameters();
                }
            };
            jdbcTemplate.execute(statementCreator, statementCallback);
            log.info("[execute][message({}) config({}) 写入数据库成功，table: {}]",
                    message.getId(), config, config.getTableName());
        } catch (Exception e) {
            log.error("[execute][message({}) config({}) 写入数据库失败]", message, config, e);
            throw e;
        }
    }

    /**
     * 构建 INSERT SQL
     *
     * create_time 使用外部数据库的时间：Oracle、SQL Server 不支持 NOW()，使用 CURRENT_TIMESTAMP；其它数据库保持 NOW()
     *
     * @param tableName 已校验的表名
     * @param databaseProductName 外部数据库的产品名，即 DatabaseMetaData#getDatabaseProductName
     * @return INSERT SQL
     */
    private static String buildInsertSql(String tableName, String databaseProductName) {
        String now = StrUtil.equalsAny(databaseProductName, CURRENT_TIMESTAMP_PRODUCT_NAMES) ? "CURRENT_TIMESTAMP" : "NOW()";
        return StrUtil.format(
                "INSERT INTO {} (id, device_id, tenant_id, method, report_time, data, create_time) VALUES (?, ?, ?, ?, ?, ?, {})",
                tableName, now);
    }

    @Override
    protected JdbcTemplate initProducer(IotDataSinkDatabaseConfig config) throws Exception {
        // 使用 HikariCP 连接池，HikariCP 会根据 JDBC URL 自动检测并加载对应的数据库驱动
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        // 连接池配置
        hikariConfig.setMaximumPoolSize(5); // 数据流转场景，不需要太多连接
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setConnectionTimeout(10000); // 连接超时 10 秒
        hikariConfig.setIdleTimeout(300000); // 空闲超时 5 分钟
        hikariConfig.setMaxLifetime(600000); // 最大生命周期 10 分钟
        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        log.info("[initProducer][数据库连接池创建成功，jdbcUrl: {}]", config.getJdbcUrl());
        return new JdbcTemplate(dataSource);
    }

    @Override
    protected void closeProducer(JdbcTemplate producer) throws Exception {
        if (producer.getDataSource() instanceof HikariDataSource) {
            ((HikariDataSource) producer.getDataSource()).close();
        }
    }

}
