package cn.iocoder.yudao.framework.dict.core.service;

import cn.iocoder.yudao.framework.dict.core.dto.DictDataRespDTO;

import java.util.List;

public interface DictDataFrameworkService {

    /**
     * 获得指定的字典数据，从缓存中
     *
     * @param type 字典类型
     * @param value 字典数据值
     * @return 字典数据
     */
    DictDataRespDTO getDictDataFromCache(String type, String value);

    /**
     * 解析获得指定的字典数据，从缓存中
     *
     * @param type 字典类型
     * @param label 字典数据标签
     * @return 字典数据
     */
    DictDataRespDTO parseDictDataFromCache(String type, String label);

    /**
     * 获得指定类型的字典数据，从缓存中
     *
     * @param type 字典类型
     * @return 字典数据列表
     */
    List<DictDataRespDTO> listDictDatasFromCache(String type);

}
