package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

/**
 * TD 引擎的表 Mapper
 */
@Mapper
@DS("tdengine")
public interface TdEngineTableMapper {

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
    @InterceptorIgnore(tenantLine = "true")
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
    @InterceptorIgnore(tenantLine = "true")
    void createTableWithTags(TdTableDO table);

}
