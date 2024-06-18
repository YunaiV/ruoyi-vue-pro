package cn.iocoder.yudao.framework.mybatis.core.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据库产品名 => mp DbType 的映射关系
 *
 * @author dhb52
 */
@Getter
@AllArgsConstructor
public enum NameToTypeEnum {

    /**
     * MySQL
     */
    MY_SQL("MySQL", DbType.MYSQL),

    /**
     * Oracle
     */
    ORACLE("Oracle", DbType.ORACLE),

    /**
     * PostgreSQL
     */
    POSTGRE_SQL("PostgreSQL", DbType.POSTGRE_SQL),

    /**
     * SQL Server
     */
    SQL_SERVER("Microsoft SQL Server", DbType.SQL_SERVER),

    /**
     * 达梦
     */
    DM("DM DBMS", DbType.DM),

    /**
     * 人大金仓
     */
    KINGBASE_ES("KingbaseES", DbType.KINGBASE_ES),

    // 华为openGauss ProductName 与 PostgreSQL相同
    ;

    private final String name;
    private final DbType type;

    public static final Map<String, DbType> MAPS = Arrays.stream(values())
            .collect(Collectors.toMap(NameToTypeEnum::getName, NameToTypeEnum::getType));

    public static DbType find(String databaseProductName) {
        if (StrUtil.isBlank(databaseProductName)) {
            return null;
        }
        return MAPS.get(databaseProductName);
    }
}