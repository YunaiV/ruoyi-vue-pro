package cn.iocoder.dashboard.modules.system.service.auth;

import io.jsonwebtoken.Claims;

import java.util.Map;

/**
 * Token Service 接口
 *
 * 提供访问 Token 令牌，目前基于 JWT 实现
 */
public interface SysTokenService {

    /**
     * 创建 Token
     *
     * @param subject 主体
     * @return Token 字符串
     */
    String createToken(String subject);

    /**
     * 解析 Token，返回 claims 数据声明
     *
     * @param token Token
     * @return claims
     */
    Claims parseToken(String token);

}
