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
     * H2
     */
    H2(DbType.H2, "H2", "POSITION(',' || CAST(#{value} AS VARCHAR) || ',' IN ',' || #{column} || ',') > 0"),

    /**
     * MySQL
     */
    MY_SQL(DbType.MYSQL, "MySQL", "FIND_IN_SET(#{value}, #{column}) <> 0"),

    /**
     * Oracle
     */
    ORACLE(DbType.ORACLE, "Oracle", "INSTR(',' || #{column} || ',', ',' || #{value} || ',') > 0"),

    /**
     * PostgreSQL
     *
     * 华为 openGauss（PostgreSQL JDBC 驱动）、MogDB（io.mogdb 的 openGauss JDBC 驱动）使用 ProductName 与 PostgreSQL 相同
     */
    POSTGRE_SQL(DbType.POSTGRE_SQL, "PostgreSQL", "POSITION(',' || CAST(#{value} AS VARCHAR) || ',' IN ',' || #{column} || ',') > 0"),

    /**
     * SQL Server
     */
    SQL_SERVER(DbType.SQL_SERVER, "Microsoft SQL Server", "CHARINDEX(',' + CAST(#{value} AS varchar(255)) + ',', ',' + #{column} + ',') > 0"),
    /**
     * SQL Server 2005
     */
    SQL_SERVER2005(DbType.SQL_SERVER2005, "Microsoft SQL Server 2005", "CHARINDEX(',' + CAST(#{value} AS varchar(255)) + ',', ',' + #{column} + ',') > 0"),

    /**
     * 达梦
     */
    DM(DbType.DM, "DM DBMS", "FIND_IN_SET(#{value}, #{column}) <> 0"),

    /**
     * 人大金仓
     */
    KINGBASE_ES(DbType.KINGBASE_ES, "KingbaseES", "POSITION(',' || CAST(#{value} AS VARCHAR) || ',' IN ',' || #{column} || ',') > 0"),

    /**
     * OceanBase
     */
    OCEAN_BASE(DbType.OCEAN_BASE, "OceanBase", "FIND_IN_SET(#{value}, #{column}) <> 0"),

    /**
     * 华为 GaussDB
     *
     * 原生驱动（jdbc:gaussdb:）使用的 ProductName 为 GaussDB，需要使用 PG 兼容模式（DBCOMPATIBILITY = 'PG'）创建的数据库
     */
    GAUSS_DB(DbType.GAUSS_DB, "GaussDB", "POSITION(',' || CAST(#{value} AS VARCHAR) || ',' IN ',' || #{column} || ',') > 0"),

    /**
     * openGauss
     *
     * org.opengauss 的 openGauss JDBC 驱动（jdbc:opengauss:）使用的 ProductName 为 openGauss，连接 MogDB 时也是如此；
     * 需要使用 PG 兼容模式（DBCOMPATIBILITY = 'PG'）创建的数据库
     */
    OPENGAUSS(DbType.OPENGAUSS, "openGauss", "POSITION(',' || CAST(#{value} AS VARCHAR) || ',' IN ',' || #{column} || ',') > 0")

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
        return Optional.ofNullable(MAP_BY_MP.get(dbType))
                .map(DbTypeEnum::getFindInSetTemplate)
                .filter(StrUtil::isNotBlank)
                .orElseThrow(() -> new IllegalArgumentException("FIND_IN_SET not supported"));
    }
}
