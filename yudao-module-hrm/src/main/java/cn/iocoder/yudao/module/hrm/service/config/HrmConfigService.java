package cn.iocoder.yudao.module.hrm.service.config;

import java.util.List;

/**
 * HRM 配置 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmConfigService {

    /**
     * 获得指定类型的配置值列表
     *
     * @param type 配置类型
     * @return 配置值列表
     */
    List<String> getConfigValueList(Integer type);

    /**
     * 替换指定类型的配置值列表
     *
     * @param type 配置类型
     * @param values 配置值列表
     */
    void replaceConfigValueList(Integer type, List<String> values);

}
