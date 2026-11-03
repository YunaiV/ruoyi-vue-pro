package cn.iocoder.yudao.module.report.dal.redis;

/**
 * Report Redis Key 常量
 *
 * @author 芋道源码
 */
public interface RedisKeyConstants {

    /**
     * UReport2 报表模板的版本号
     *
     * 模板保存、删除时更新为新的随机值，各实例据此判断本地缓存的模板定义是否过期；不设置过期时间，避免过期后误判为未修改
     *
     * KEY 格式：report:ureport2_file_version:{tenantId}:{file}
     * VALUE 数据格式：String 随机版本号
     */
    String UREPORT2_FILE_VERSION = "report:ureport2_file_version:%d:%s";

}
