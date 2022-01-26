package cn.iocoder.yudao.coreservice.modules.system.service.dict;

import cn.iocoder.yudao.framework.dict.core.service.DictDataFrameworkService;

import java.util.Collection;

/**
 * 字典数据 Service 接口
 *
 * @author 芋道源码
 */
public interface SysDictDataCoreService extends DictDataFrameworkService {

    /**
     * 初始化字典数据的本地缓存
     */
    void initLocalCache();

    /**
     * 校验字典数据们是否有效。如下情况，视为无效：
     * 1. 字典数据不存在
     * 2. 字典数据被禁用
     *
     * @param dictType 字典类型
     * @param values 字典数据值的数组
     */
    void validDictDatas(String dictType, Collection<String> values);

}
