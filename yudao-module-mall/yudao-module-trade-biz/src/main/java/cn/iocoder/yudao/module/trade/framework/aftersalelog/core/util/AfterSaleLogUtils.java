package cn.iocoder.yudao.module.trade.framework.aftersalelog.core.util;


import cn.iocoder.yudao.module.trade.framework.aftersalelog.core.aop.AfterSaleLogAspect;

import java.util.Map;

/**
 * 操作日志工具类
 * 目前主要的作用，是提供给业务代码，记录操作明细和拓展字段
 *
 * @author 芋道源码
 */
public class AfterSaleLogUtils {

    public static void setBeforeStatus(Integer status) {
        AfterSaleLogAspect.setBeforeStatus(status);
    }

    public static void setAfterStatus(Integer status) {
        AfterSaleLogAspect.setAfterStatus(status);
    }

    public static void setAfterSaleInfo(Long id, Integer beforeStatus, Integer afterStatus) {
        setAfterSaleInfo(id, beforeStatus, afterStatus, null);
    }

    public static void setAfterSaleInfo(Long id, Integer beforeStatus, Integer afterStatus,
                                        Map<String, Object> exts) {
        // TODO 待实现
    }

}
