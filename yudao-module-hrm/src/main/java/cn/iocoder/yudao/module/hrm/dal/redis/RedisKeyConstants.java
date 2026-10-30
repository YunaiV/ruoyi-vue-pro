package cn.iocoder.yudao.module.hrm.dal.redis;

/**
 * HRM Redis Key 常量
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * 标准参保类型的缓存
     *
     * KEY 格式：hrm:insurance_standard_type:{areaId}
     * VALUE 数据格式：标准参保类型列表
     */
    String INSURANCE_STANDARD_TYPE = "hrm:insurance_standard_type";

    /**
     * 标准参保项目的缓存
     *
     * KEY 格式：hrm:insurance_standard_project:{areaId}:{typeCode}
     * VALUE 数据格式：标准参保项目列表
     */
    String INSURANCE_STANDARD_PROJECT = "hrm:insurance_standard_project";

    /**
     * 月度社保表变更的分布式锁
     *
     * 全局唯一、不区分租户：首月创建、次月创建、删除互斥；各租户的数据仍按租户过滤
     *
     * KEY 格式：hrm:insurance_month_record_mutation_lock
     * VALUE 数据格式：Redisson 分布式锁
     */
    String INSURANCE_MONTH_RECORD_MUTATION_LOCK = "hrm:insurance_month_record_mutation_lock";

}
