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


@Getter
@AllArgsConstructor
public enum DbTypeEnum {
    /**
     * MySQL
     */
    MY_SQL("MySQL", "FIND_IN_SET('#{value}', #{column}) <> 0", DbType.MYSQL),

    /**
     * Oracle
     */
    ORACLE("Oracle", "FIND_IN_SET('#{value}', #{column}) <> 0", DbType.ORACLE),

    /**
     * PostgreSQL
     */
    POSTGRE_SQL("PostgreSQL", "POSITION('#{value}' IN #{column}) <> 0", DbType.POSTGRE_SQL),

    /**
     * SQL Server
     */
    SQL_SERVER("Microsoft SQL Server", "CHARINDEX(',' + #{value} + ',', ',' + #{column} + ',') <> 0", DbType.SQL_SERVER),

    /**
     * 达梦
     */
    DM("DM DBMS", "FIND_IN_SET('#{value}', #{column}) <> 0", DbType.DM),

    /**
     * 人大金仓
     */
    KINGBASE_ES("KingbaseES", "POSITION('#{value}' IN #{column}) <> 0", DbType.KINGBASE_ES),

    // 华为openGauss 使用ProductName 与 PostgreSQL相同
    ;

    public static final Map<String, DbTypeEnum> MAP_BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(DbTypeEnum::getName, Function.identity()));

    public static final Map<DbType, DbTypeEnum> MAP_BY_MP = Arrays.stream(values())
            .collect(Collectors.toMap(DbTypeEnum::getMpDbType, Function.identity()));


    private final String name;
    private final String findInSetTemplate;
    private final DbType mpDbType;

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
