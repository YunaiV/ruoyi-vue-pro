package cn.iocoder.yudao.module.infra.api.config;

/**
 * 参数配置 API 接口
 *
 * @author 芋道源码
 */
public interface ConfigApi {

    /**
     * 根据参数键查询参数值
     *
     * @param key 参数键
     * @return 参数值
     */
    String getConfigValueByKey(String key);

}
