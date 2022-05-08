package cn.iocoder.yudao.module.system.service.auth;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2ClientDO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2RefreshTokenDO;
import cn.iocoder.yudao.module.system.dal.mysql.auth.OAuth2AccessTokenMapper;
import cn.iocoder.yudao.module.system.dal.mysql.auth.OAuth2RefreshTokenMapper;
import cn.iocoder.yudao.module.system.dal.redis.auth.OAuth2AccessTokenRedisDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * OAuth2.0 Token Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OAuth2TokenServiceImpl implements OAuth2TokenService {

    @Resource
    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
    @Resource
    private OAuth2RefreshTokenMapper oauth2RefreshTokenMapper;

    @Resource
    private OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;

    @Resource
    private OAuth2ClientService oauth2ClientService;

    @Override
    @Transactional
    public OAuth2AccessTokenDO createAccessToken(Long userId, Integer userType, Long clientId) {
        OAuth2ClientDO clientDO = oauth2ClientService.validOAuthClientFromCache(clientId);
        // 创建刷新令牌
        OAuth2RefreshTokenDO refreshTokenDO = createOAuth2RefreshToken(userId, userType, clientDO);
        // 创建访问令牌
        OAuth2AccessTokenDO accessTokenDO = createOAuth2AccessToken(refreshTokenDO, clientDO);
        // 记录到 Redis 中
        oauth2AccessTokenRedisDAO.set(accessTokenDO);
        return accessTokenDO;
    }

    @Override
    public OAuth2AccessTokenDO refreshAccessToken(String refreshToken) {
        return null;
    }

    @Override
    public OAuth2AccessTokenDO getAccessToken(String accessToken) {
        // 优先从 Redis 中获取
        OAuth2AccessTokenDO accessTokenDO = oauth2AccessTokenRedisDAO.get(accessToken);
        if (accessTokenDO != null) {
            return accessTokenDO;
        }

        // 获取不到，从 MySQL 中获取
        accessTokenDO = oauth2AccessTokenMapper.selectById(accessToken);
        // 如果在 MySQL 存在，则往 Redis 中写入
        if (accessTokenDO != null && !DateUtils.isExpired(accessTokenDO.getExpiresTime())) {
            oauth2AccessTokenRedisDAO.set(accessTokenDO);
        }
        return accessTokenDO;
    }

    @Override
    public OAuth2AccessTokenDO checkAccessToken(String accessToken) {
        OAuth2AccessTokenDO accessTokenDO = getAccessToken(accessToken);
        if (accessTokenDO == null) {
            throw exception(GlobalErrorCodeConstants.UNAUTHORIZED, "Token 不存在");
        }
        if (DateUtils.isExpired(accessTokenDO.getExpiresTime())) {
            throw exception(GlobalErrorCodeConstants.UNAUTHORIZED, "Token 已过期");
        }
        return accessTokenDO;
    }

    @Override
    public boolean removeAccessToken(String accessToken) {
        return false;
    }

//    @Override
//    @Transactional
//    public OAuth2AccessTokenRespDTO checkAccessToken(String accessToken) {
//        OAuth2AccessTokenDO accessTokenDO = this.getOAuth2AccessToken(accessToken);
//        if (accessTokenDO == null) { // 不存在
//            throw ServiceExceptionUtil.exception(OAUTH2_ACCESS_TOKEN_NOT_FOUND);
//        }
//        if (accessTokenDO.getExpiresTime().getTime() < System.currentTimeMillis()) { // 已过期
//            throw ServiceExceptionUtil.exception(OAUTH2_ACCESS_TOKEN_TOKEN_EXPIRED);
//        }
//        // 返回访问令牌
//        return OAuth2Convert.INSTANCE.convert(accessTokenDO);
//    }

//    @Override
//    @Transactional
//    public OAuth2AccessTokenRespDTO refreshAccessToken(OAuth2RefreshAccessTokenReqDTO refreshAccessTokenDTO) {
//        OAuth2RefreshTokenDO refreshTokenDO = oauth2RefreshTokenMapper.selectById(refreshAccessTokenDTO.getRefreshToken());
//        // 校验刷新令牌是否合法
//        if (refreshTokenDO == null) { // 不存在
//            throw ServiceExceptionUtil.exception(OAUTH2_REFRESH_TOKEN_NOT_FOUND);
//        }
//        if (refreshTokenDO.getExpiresTime().getTime() < System.currentTimeMillis()) { // 已过期
//            throw ServiceExceptionUtil.exception(OAUTH_REFRESH_TOKEN_EXPIRED);
//        }
//
//        // 标记 refreshToken 对应的 accessToken 都不合法
//        // 这块的实现，参考了 Spring Security OAuth2 的代码
//        List<OAuth2AccessTokenDO> accessTokenDOs = oauth2AccessTokenMapper.selectListByRefreshToken(refreshAccessTokenDTO.getRefreshToken());
//        accessTokenDOs.forEach(accessTokenDO -> deleteOAuth2AccessToken(accessTokenDO.getId()));
//
//        // 创建访问令牌
//        OAuth2AccessTokenDO oauth2AccessTokenDO = createOAuth2AccessToken(refreshTokenDO, refreshAccessTokenDTO.getCreateIp());
//        // 返回访问令牌
//        return OAuth2Convert.INSTANCE.convert(oauth2AccessTokenDO);
//    }
//
//    @Override
//    @Transactional
//    public void removeToken(OAuth2RemoveTokenByUserReqDTO removeTokenDTO) {
//        // 删除 Access Token
//        OAuth2AccessTokenDO accessTokenDO = oauth2AccessTokenMapper.selectByUserIdAndUserType(
//                removeTokenDTO.getUserId(), removeTokenDTO.getUserType());
//        if (accessTokenDO != null) {
//            this.deleteOAuth2AccessToken(accessTokenDO.getId());
//        }
//
//        // 删除 Refresh Token
//        oauth2RefreshTokenMapper.deleteByUserIdAndUserType(removeTokenDTO.getUserId(), removeTokenDTO.getUserType());
//    }

    private OAuth2AccessTokenDO createOAuth2AccessToken(OAuth2RefreshTokenDO refreshTokenDO, OAuth2ClientDO clientDO) {
        OAuth2AccessTokenDO accessToken = new OAuth2AccessTokenDO().setAccessToken(generateAccessToken())
                .setUserId(refreshTokenDO.getUserId()).setUserType(refreshTokenDO.getUserType()).setClientId(clientDO.getId())
                .setRefreshToken(refreshTokenDO.getRefreshToken())
                .setExpiresTime(DateUtils.addDate(Calendar.SECOND, clientDO.getAccessTokenValiditySeconds()));
        accessToken.setTenantId(TenantContextHolder.getTenantId()); // 手动设置租户编号，避免缓存到 Redis 的时候，无对应的租户编号
        oauth2AccessTokenMapper.insert(accessToken);
        return accessToken;
    }

    private OAuth2RefreshTokenDO createOAuth2RefreshToken(Long userId, Integer userType, OAuth2ClientDO clientDO) {
        OAuth2RefreshTokenDO refreshToken = new OAuth2RefreshTokenDO().setRefreshToken(generateRefreshToken())
                .setUserId(userId).setUserType(userType).setClientId(clientDO.getId())
                .setExpiresTime(DateUtils.addDate(Calendar.SECOND, clientDO.getRefreshTokenValiditySeconds()));
        oauth2RefreshTokenMapper.insert(refreshToken);
        return refreshToken;
    }


//    /**
//     * 删除 accessToken 的 MySQL 与 Redis 的数据
//     *
//     * @param accessToken 访问令牌
//     */
//    private void deleteOAuth2AccessToken(String accessToken) {
//        // 删除 MySQL
//        oauth2AccessTokenMapper.deleteById(accessToken);
//        // 删除 Redis
//        oauth2AccessTokenRedisDAO.delete(accessToken);
//    }
//
    private static String generateAccessToken() {
        return IdUtil.fastSimpleUUID();
    }

    private static String generateRefreshToken() {
        return IdUtil.fastSimpleUUID();
    }

}
