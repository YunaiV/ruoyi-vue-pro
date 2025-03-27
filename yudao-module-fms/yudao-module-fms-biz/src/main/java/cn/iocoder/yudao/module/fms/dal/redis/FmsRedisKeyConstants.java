package cn.iocoder.yudao.module.fms.dal.redis;

/**
 * ERP Redis Key 枚举类
 *
 * @author 芋道源码
 */
public interface FmsRedisKeyConstants {

    /**
     * 序号的缓存
     * <p>
     * KEY 格式：trade_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String FMS_NO = "fms:seq_no:";
    /**
     * 财务主体集合的缓存
     */
    String FINANCE_SUBJECT_LIST = "erp:erp_finance_subject:list";
}
