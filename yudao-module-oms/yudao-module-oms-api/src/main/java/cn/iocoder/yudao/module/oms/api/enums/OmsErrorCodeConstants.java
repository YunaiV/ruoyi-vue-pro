package cn.iocoder.yudao.module.oms.api.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * OMS 错误码枚举类
 *
 * @author Administrator
 */
public interface OmsErrorCodeConstants {
    ErrorCode OMS_SHOP_NOT_EXISTS = new ErrorCode(1_040_500_000, "店铺不存在");

    ErrorCode OMS_SYNC_SHOP_INFO_LACK = new ErrorCode(1_040_501_000, "OMS同步缺少店铺信息");

    ErrorCode OMS_SYNC_SHOP_PRODUCT_LACK = new ErrorCode(1_040_501_001, "OMS同步缺少店铺产品信息");

    ErrorCode OMS_SYNC_SHOPIFY_SHOP_INFO_FIRST = new ErrorCode(1_040_501_002, "请先同步Shopify店铺信息");

    ErrorCode OMS_SHOP_CAN_NOT_CREATE_ONLINE_SHOP = new ErrorCode(1_040_501_003, "不能创建线上店铺");

    ErrorCode OMS_SHOP_PRODUCT_ITEM_NOT_EXISTS = new ErrorCode(1_040_501_004, "店铺产品关联不存在");

    ErrorCode OMS_SHOP_PRODUCT_NOT_EXISTS = new ErrorCode(1_040_501_005, "店铺产品不存在");

    ErrorCode OMS_SHOP_NAME_EXISTS = new ErrorCode(1_040_501_006, "店铺别名已存在");
}
