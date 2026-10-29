package cn.iocoder.yudao.module.iot.dal.dataobject.rule.config;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * IoT Database 配置 {@link IotAbstractDataSinkConfig} 实现类
 *
 * 通过 JDBC 连接数据库，将设备消息写入指定表。
 * 支持 MySQL、PostgreSQL、Oracle、SQL Server、DM 达梦等数据库，
 * HikariCP 会根据 JDBC URL 自动加载对应的驱动。
 *
 * @author HUIHUI
 */
@Data
public class IotDataSinkDatabaseConfig extends IotAbstractDataSinkConfig {

    /**
     * 目标表名的格式：只允许字母、数字、下划线，且不以数字开头；支持可选的 schema.table 格式
     *
     * 目的：表名会拼接到 INSERT 语句中，保存和执行时都必须校验
     */
    public static final String TABLE_NAME_REGEX = "^[A-Za-z_][A-Za-z0-9_]{0,127}(\\.[A-Za-z_][A-Za-z0-9_]{0,127})?$";

    /**
     * JDBC 连接地址
     *
     * 例如：jdbc:mysql://localhost:3306/iot_data
     * 例如：jdbc:postgresql://localhost:5432/iot_data
     * 例如：jdbc:dm://localhost:5236/iot_data
     *
     * HikariCP 会根据 URL 自动检测并加载对应的 JDBC 驱动
     */
    private String jdbcUrl;
    /**
     * 数据库用户名
     */
    private String username;
    /**
     * 数据库密码
     */
    private String password;
    /**
     * 目标表名
     *
     * 设备消息将以固定结构写入该表
     */
    @NotEmpty(message = "目标表名不能为空")
    @Pattern(regexp = TABLE_NAME_REGEX, message = "目标表名格式不正确，只允许字母、数字、下划线，或 schema.table 格式")
    private String tableName;

}
