package cn.iocoder.yudao.module.iot.service.tdengine;

import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.TdTableDO;

/**
 * TD 引擎的表 Service 接口
 */
public interface TdEngineTableService {

    /**
     * 创建表 - 创建超级表的子表
     *
     * @param table 表信息
     *              dataBaseName   数据库名称
     *              superTableName 超级表名称
     *              tableName      子表名称
     *              tags           TAG 字段
     */
    void createTable(TdTableDO table);

}
