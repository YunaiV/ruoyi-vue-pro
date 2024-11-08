package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * TD 引擎的超级表 Mapper
 */
@Mapper
@DS("tdengine")
public interface TdEngineSuperTableMapper {

    /**
     * 创建超级表
     * SQL：CREATE STABLE [IF NOT EXISTS] stb_name (create_definition [, create_definition] ...) TAGS (create_definition [, create_definition] ...) [table_options];
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   columns        列信息
     *                   tags           标签信息
     */
    @InterceptorIgnore(tenantLine = "true")
    void createSuperTable(TdTableDO superTable);

    /**
     * 查看超级表 - 显示当前数据库下的所有超级表信息
     * SQL：SHOW STABLES [LIKE tb_name_wildcard];
     *
     * @param superTable 超级表信息
     *                   dataBaseName 数据库名称
     *                   superTableName 超级表名称
     */
    @InterceptorIgnore(tenantLine = "true")
    List<Map<String, Object>> showSuperTables(TdTableDO superTable);

    /**
     * 查看超级表 - 获取超级表的结构信息
     * SQL：DESCRIBE [db_name.]stb_name;
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     */
    @InterceptorIgnore(tenantLine = "true")
    List<Map<String, Object>> describeSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 增加列
     * SQL：ALTER STABLE stb_name ADD COLUMN col_name column_type;
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   column         列信息
     */
    @InterceptorIgnore(tenantLine = "true")
    void addColumnForSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 删除列
     * SQL：ALTER STABLE stb_name DROP COLUMN col_name;
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   column         列信息
     */
    @InterceptorIgnore(tenantLine = "true")
    void dropColumnForSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 修改列宽
     * SQL：ALTER STABLE stb_name MODIFY COLUMN col_name data_type(length);
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   column         列信息
     */
    @InterceptorIgnore(tenantLine = "true")
    void modifyColumnWidthForSuperTable(TdTableDO superTable);


    /**
     * 修改超级表 - 为超级表添加标签
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   tag            标签信息
     */
    @InterceptorIgnore(tenantLine = "true")
    void addTagForSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 为超级表删除标签
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   tag            标签信息
     */
    @InterceptorIgnore(tenantLine = "true")
    void dropTagForSuperTable(TdTableDO superTable);
}