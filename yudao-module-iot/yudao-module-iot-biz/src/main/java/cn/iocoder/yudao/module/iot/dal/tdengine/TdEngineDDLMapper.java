package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 专门处理 DDL（数据定义语言）操作，包含所有的数据库和表的定义操作，例如创建数据库、创建超级表、创建子表等
 */
@Mapper
@DS("tdengine")
public interface TdEngineDDLMapper {

    /**
     * 创建数据库
     * SQL：CREATE DATABASE [IF NOT EXISTS] db_name [database_options];
     *
     * @param dataBaseName 数据库名称
     */
    @TenantIgnore
    void createDatabase(@Param("dataBaseName") String dataBaseName);

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
    @TenantIgnore
    void createSuperTable(TdTableDO superTable);

    /**
     * 查看超级表 - 显示当前数据库下的所有超级表信息
     * SQL：SHOW STABLES [LIKE tb_name_wildcard];
     *
     * @param superTable 超级表信息
     *                   dataBaseName 数据库名称
     *                   superTableName 超级表名称
     */
    @TenantIgnore
    List<Map<String, Object>> showSuperTables(TdTableDO superTable);

    /**
     * 查看超级表 - 获取超级表的结构信息
     * SQL：DESCRIBE [db_name.]stb_name;
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     */
    @TenantIgnore
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
    @TenantIgnore
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
    @TenantIgnore
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
    @TenantIgnore
    void modifyColumnWidthForSuperTable(TdTableDO superTable);


    /**
     * 修改超级表 - 为超级表添加标签
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   tag            标签信息
     */
    @TenantIgnore
    void addTagForSuperTable(TdTableDO superTable);

    /**
     * 修改超级表 - 为超级表删除标签
     *
     * @param superTable 超级表信息
     *                   dataBaseName   数据库名称
     *                   superTableName 超级表名称
     *                   tag            标签信息
     */
    @TenantIgnore
    void dropTagForSuperTable(TdTableDO superTable);

    /**
     * 创建子表 - 创建子表
     * SQL：CREATE TABLE [IF NOT EXISTS] tb_name USING stb_name TAGS (tag_value1, ...);
     *
     * @param table 表信息
     *              dataBaseName   数据库名称
     *              superTableName 超级表名称
     *              tableName      子表名称
     *              tags           TAG 字段
     */
    @TenantIgnore
    void createTable(TdTableDO table);

    /**
     * 创建子表 - 创建子表并指定标签的值
     * SQL：CREATE TABLE [IF NOT EXISTS] tb_name USING stb_name (tag_name1, ...) TAGS (tag_value1, ...);
     *
     * @param table 表信息
     *              dataBaseName   数据库名称
     *              superTableName 超级表名称
     *              tableName      子表名称
     *              tags           TAG 字段
     */
    @TenantIgnore
    void createTableWithTags(TdTableDO table);

}
