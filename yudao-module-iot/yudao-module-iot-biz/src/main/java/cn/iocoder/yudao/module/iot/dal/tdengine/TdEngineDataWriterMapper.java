package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;

/**
 * TD 引擎的数据写入 Mapper
 */
@Mapper
@DS("tdengine")
public interface TdEngineDataWriterMapper {

    /**
     * 插入数据 - 指定列插入数据
     *
     * @param table 数据
     *              dataBaseName 数据库名
     *              tableName 表名
     *              columns 列
     */
    @InterceptorIgnore(tenantLine = "true")
    void insertData(TdTableDO table);
}
