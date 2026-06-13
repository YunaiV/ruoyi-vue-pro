package cn.iocoder.yudao.module.iot.service.rule.data.action;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.config.IotDataSinkDatabaseConfig;
import cn.iocoder.yudao.module.iot.enums.rule.IotDataSinkTypeEnum;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Override
    public Integer getType() {
        return IotDataSinkTypeEnum.DATABASE.getType();
    }

    @Override
    public void execute(IotDeviceMessage message, IotDataSinkDatabaseConfig config) throws Exception {
        try {
            // 1. 获取或创建 JdbcTemplate
            JdbcTemplate jdbcTemplate = getProducer(config);

            // 2. 构建并执行 INSERT SQL
            String sql = StrUtil.format(
                    "INSERT INTO {} (id, device_id, tenant_id, method, report_time, data, create_time) VALUES (?, ?, ?, ?, ?, ?, NOW())",
                    config.getTableName());
            String messageJson = JsonUtils.toJsonString(message);
            jdbcTemplate.update(sql,
                    message.getId(),
                    message.getDeviceId(),
                    message.getTenantId(),
                    message.getMethod(),
                    message.getReportTime(),
                    messageJson);
            log.info("[execute][message({}) config({}) 写入数据库成功，table: {}]",
                    message.getId(), config, config.getTableName());
        } catch (Exception e) {
            log.error("[execute][message({}) config({}) 写入数据库失败]", message, config, e);
            throw e;
        }
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
