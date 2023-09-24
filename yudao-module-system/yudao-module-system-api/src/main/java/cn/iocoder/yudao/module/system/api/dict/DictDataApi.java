package cn.iocoder.yudao.module.system.api.dict;

import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 字典数据 API 接口
 *
 * @author 芋道源码
 */
public interface DictDataApi {

    /**
     * 校验字典数据们是否有效。如下情况，视为无效：
     * 1. 字典数据不存在
     * 2. 字典数据被禁用
     *
     * @param dictType 字典类型
     * @param values 字典数据值的数组
     */
    void validateDictDataList(String dictType, Collection<String> values);

    /**
     * 获得指定的字典数据，从缓存中
     *
     * @param type 字典类型
     * @param value 字典数据值
     * @return 字典数据
     */
    DictDataRespDTO getDictData(String type, String value);

    /**
     * 获得指定类型的字典数据，从缓存中
     *
     * @param type 字典类型
     * @return 字典数据
     */
    List<DictDataRespDTO> getDictDataList(String type);

    /**
     * 获得指定类型的字典数据 标签字典，从缓存中
     * key：value, value: label
     *
     * @param type 字典类型
     * @return 字典数据
     */
    Map<String, String> getDictDataLabelMap(String type);

    /**
     * 解析获得指定的字典数据，从缓存中
     *
     * @param type 字典类型
     * @param label 字典数据标签
     * @return 字典数据
     */
    DictDataRespDTO parseDictData(String type, String label);

}
