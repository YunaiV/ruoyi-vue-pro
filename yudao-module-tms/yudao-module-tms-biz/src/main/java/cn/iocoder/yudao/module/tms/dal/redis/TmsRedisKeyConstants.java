package cn.iocoder.yudao.module.tms.dal.redis;

public interface TmsRedisKeyConstants {


    /**
     * TmsCustomProductService 服务的缓存单体
     */
    String TMS_CUSTOM_PRODUCT = "tms:custom_product_service:";
    /**
     * TmsCustomProductService 服务的缓存集合+分页+list
     */
    String TMS_CUSTOM_PRODUCT_LIST = "tms:custom_product_service:list:";
}
