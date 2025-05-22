package cn.iocoder.yudao.framework.common.biz.system.tenant;

import java.util.List;

/**
 * 多租户的 API 接口
 *
 * @author 芋道源码
 */
public interface TenantCommonApi {

    /**
     * 获得所有租户
     *
     * @return 租户编号数组
     */
    List<Long> getTenantIdList();

    /**
     * 校验租户是否合法
     *
     * @param id 租户编号
     */
    void validateTenant(Long id);

}
