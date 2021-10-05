package cn.iocoder.yudao.userserver.modules.system.dict;

import cn.iocoder.yudao.framework.dict.core.service.DictDataFrameworkService;

/**
 * 字典数据 Service 接口
 *
 * @author ruoyi
 */
public interface SysDictDataService extends DictDataFrameworkService {

    /**
     * 初始化字典数据的本地缓存
     */
    void initLocalCache();

}
