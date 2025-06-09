package com.somle.kingdee.constant;

/**
 * kingdee 的 Redis Key 常量类
 */
public class KingdeeRedisKeyConstants {

    /**
     * 金蝶物料缓存
     * 格式：kingdee:material:{物料编号}
     */
    public static final String KINGDEE_MATERIAL = "somle:kingdee:material";

    /**
     * 金蝶供应商列表缓存
     * 格式：esb:kingdee:supplier:list:{查询参数hash}
     */
    public static final String KINGDEE_SUPPLIER_LIST = "somle:kingdee:supplier:list";

} 