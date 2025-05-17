package cn.iocoder.yudao.module.system.api.dict;

import cn.iocoder.yudao.framework.common.biz.system.dict.DictDataCommonApi;

import java.util.Collection;

/**
 * 字典数据 API 接口
 *
 * @author 芋道源码
 */
public interface DictDataApi extends DictDataCommonApi {

    /**
     * 校验字典数据们是否有效。如下情况，视为无效：
     * 1. 字典数据不存在
     * 2. 字典数据被禁用
     *
     * @param dictType 字典类型
     * @param values   字典数据值的数组
     */
    void validateDictDataList(String dictType, Collection<String> values);

}
