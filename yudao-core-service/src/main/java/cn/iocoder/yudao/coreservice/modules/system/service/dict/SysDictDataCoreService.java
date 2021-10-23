package cn.iocoder.yudao.coreservice.modules.system.service.dict;

import cn.iocoder.yudao.framework.dict.core.service.DictDataFrameworkService;

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

}
