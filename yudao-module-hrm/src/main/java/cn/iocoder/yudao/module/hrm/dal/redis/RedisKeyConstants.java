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

}
