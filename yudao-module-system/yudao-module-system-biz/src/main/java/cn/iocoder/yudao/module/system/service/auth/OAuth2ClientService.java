package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2ClientDO;

/**
 * OAuth2.0 Client Service 接口
 *
 * 从功能上，和 JdbcClientDetailsService 的功能，提供客户端的操作
 *
 * @author 芋道源码
 */
public interface OAuth2ClientService {

    /**
     * 从缓存中，校验客户端是否合法
     *
     * @param id 客户端编号
     * @return 客户端
     */
    OAuth2ClientDO validOAuthClientFromCache(Long id);

}
