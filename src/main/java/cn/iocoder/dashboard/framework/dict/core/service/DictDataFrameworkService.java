package cn.iocoder.dashboard.framework.dict.core.service;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictDataDO;

import java.util.List;

public interface DictDataFrameworkService {

    /**
     * 获得指定的字典数据，从缓存中
     *
     * @param type 字典类型
     * @param value 字典数据值
     * @return 字典数据
     */
    SysDictDataDO getDictDataFromCache(String type, String value);

    /**
     * 解析获得指定的字典数据，从缓存中
     *
     * @param type 字典类型
     * @param label 字典数据标签
     * @return 字典数据
     */
    SysDictDataDO parseDictDataFromCache(String type, String label);

    /**
     * 获得指定类型的字典数据，从缓存中
     *
     * @param type 字典类型
     * @return 字典数据列表
     */
    List<SysDictDataDO> listDictDatasFromCache(String type);

}
