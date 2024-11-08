package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;

/**
 * TD 引擎的数据写入 Service 接口
 */
public interface TdEngineDataWriterService {

    /**
     * 插入数据 - 指定列插入数据
     *
     * @param table 数据
     *              dataBaseName 数据库名
     *              tableName 表名
     *              columns 列
     */
    void insertData(TdTableDO table);
}
