package cn.iocoder.yudao.module.erp.dal.redis;

/**
 * ERP Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface ErpRedisKeyConstants {

    /**
     * 序号的缓存
     * <p>
     * KEY 格式：trade_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String NO = "erp:seq_no:";

    /**
     * 单个产品的缓存
     */
    String PRODUCT = "erp:product:";
    /**
     * 产品的集合+分页的缓存
     */
    String PRODUCT_LIST = "erp:product_list:";

}
