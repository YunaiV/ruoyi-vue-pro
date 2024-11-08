package cn.iocoder.yudao.module.iot.service.tdengine;

/**
 * TD 引擎的数据库 Service 接口
 */
public interface TdEngineDatabaseService {

    /**
     * 创建数据库
     *
     * @param dataBaseName 数据库名称
     */
    void createDatabase(String dataBaseName);
}