package cn.iocoder.yudao.module.iot.dal.tdengine;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * TD 引擎的数据库 Mapper
 */
@Mapper
@DS("tdengine")
public interface TdEngineDatabaseMapper {

    /**
     * 创建数据库
     * SQL：CREATE DATABASE [IF NOT EXISTS] db_name [database_options];
     *
     * @param dataBaseName 数据库名称
     */
    @InterceptorIgnore(tenantLine = "true")
    void createDatabase(@Param("dataBaseName") String dataBaseName);
}
