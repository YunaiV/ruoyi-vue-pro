package cn.iocoder.dashboard.framework.dict.core.util;

import cn.iocoder.dashboard.framework.dict.core.service.DictDataFrameworkService;
import lombok.extern.slf4j.Slf4j;

/**
 * 字典工具类
 */
@Slf4j
public class DictUtils {

    private static DictDataFrameworkService service;

    public static void init(DictDataFrameworkService service) {
        DictUtils.service = service;
        log.info("[init][初始化 DictUtils 成功]");
    }

}
