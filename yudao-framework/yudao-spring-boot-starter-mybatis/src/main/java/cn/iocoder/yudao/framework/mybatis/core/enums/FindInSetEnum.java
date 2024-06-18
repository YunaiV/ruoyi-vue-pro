package cn.iocoder.yudao.framework.mybatis.core.enums;


import com.baomidou.mybatisplus.annotation.DbType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * find_in_set函数的跨数据库实现
 *
 * @author dhb52
 */
@Getter
@AllArgsConstructor
public enum FindInSetEnum {

    // FIND_IN_SET: MySQL 类型
    MYSQL("FIND_IN_SET('#{value}', #{column}) <> 0", DbType.MYSQL),
    DM("FIND_IN_SET('#{value}', #{column}) <> 0", DbType.DM),

    // INSTR: Oracle 类型
    ORACLE("INSTR(','||#{column}||',' , ',#{value},') <> 0", DbType.ORACLE),

    // CHARINDEX: SQLServer
    SQLSERVER("CHARINDEX(',' + #{value} + ',', ',' + #{column} + ',')", DbType.SQL_SERVER),

    // POSITION: PostgreSQL 类型
    POSTGRE_SQL("POSITION('#{value}' IN #{column})", DbType.POSTGRE_SQL),
    KINGBASE_ES("POSITION('#{value}' IN #{column})", DbType.KINGBASE_ES),

    // LOCATE: 其他
    H2("LOCATE('#{value}' IN #{column})", DbType.H2),
    ;

    public static final Map<DbType, String> MAPS = Arrays.stream(values())
            .collect(Collectors.toMap(FindInSetEnum::getDbType, FindInSetEnum::getSqlTemplate));

    private String sqlTemplate;
    private DbType dbType;

    public static String getTemplate(DbType dbType) {
        return Optional.of(MAPS.get(dbType))
                .orElseThrow(() -> new IllegalArgumentException("FIND_IN_SET not supported"));
    }
}
