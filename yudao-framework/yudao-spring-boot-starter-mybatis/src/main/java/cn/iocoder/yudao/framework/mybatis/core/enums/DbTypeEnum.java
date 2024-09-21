package cn.iocoder.yudao.framework.mybatis.core.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 针对 MyBatis Plus 的 {@link DbType} 增强，补充更多信息
 */
@Getter
@AllArgsConstructor
public enum DbTypeEnum {

    /**
     * MySQL
     */
    MY_SQL( DbType.MYSQL, "MySQL", "FIND_IN_SET('#{value}', #{column}) <> 0"),

    /**
     * Oracle
     */
    ORACLE(DbType.ORACLE, "Oracle", "FIND_IN_SET('#{value}', #{column}) <> 0"),

    /**
     * PostgreSQL
     *
     * 华为 openGauss 使用 ProductName 与 PostgreSQL 相同
     */
    POSTGRE_SQL(DbType.POSTGRE_SQL,"PostgreSQL", "POSITION('#{value}' IN #{column}) <> 0"),

    /**
     * SQL Server
     */
    SQL_SERVER(DbType.SQL_SERVER, "Microsoft SQL Server", "CHARINDEX(',' + #{value} + ',', ',' + #{column} + ',') <> 0"),
    /**
     * SQL Server 2005
     */
    SQL_SERVER2005(DbType.SQL_SERVER2005, "Microsoft SQL Server 2005", "CHARINDEX(',' + #{value} + ',', ',' + #{column} + ',') <> 0"),

    /**
     * 达梦
     */
    DM(DbType.DM, "DM DBMS", "FIND_IN_SET('#{value}', #{column}) <> 0"),

    /**
     * 人大金仓
     */
    KINGBASE_ES(DbType.KINGBASE_ES, "KingbaseES", "POSITION('#{value}' IN #{column}) <> 0"),
    ;

    public static final Map<String, DbTypeEnum> MAP_BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(DbTypeEnum::getProductName, Function.identity()));

    public static final Map<DbType, DbTypeEnum> MAP_BY_MP = Arrays.stream(values())
            .collect(Collectors.toMap(DbTypeEnum::getMpDbType, Function.identity()));

    /**
     * MyBatis Plus 类型
     */
    private final DbType mpDbType;
    /**
     * 数据库产品名
     */
    private final String productName;
    /**
     * SQL FIND_IN_SET 模板
     */
    private final String findInSetTemplate;

    public static DbType find(String databaseProductName) {
        if (StrUtil.isBlank(databaseProductName)) {
            return null;
        }
        return MAP_BY_NAME.get(databaseProductName).getMpDbType();
    }

    public static String getFindInSetTemplate(DbType dbType) {
        return Optional.of(MAP_BY_MP.get(dbType).getFindInSetTemplate())
                .orElseThrow(() -> new IllegalArgumentException("FIND_IN_SET not supported"));
    }
}
