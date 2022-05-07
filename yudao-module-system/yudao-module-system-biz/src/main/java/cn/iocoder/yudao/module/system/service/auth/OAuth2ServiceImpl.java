package cn.iocoder.yudao.module.system.service.auth;

import org.springframework.stereotype.Service;

/**
 * OAuth2.0 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class OAuth2ServiceImpl implements OAuth2Service {

//    @Autowired
//    private SystemBizProperties systemBizProperties;
//
//    @Autowired
//    private OAuth2AccessTokenMapper oauth2AccessTokenMapper;
//    @Autowired
//    private OAuth2RefreshTokenMapper oauth2RefreshTokenMapper;
//
//    @Autowired
//    private OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;
//
//    @Override
//    @Transactional
//    public OAuth2AccessTokenRespDTO createAccessToken(OAuth2CreateAccessTokenReqDTO createAccessTokenDTO) {
//        // 创建刷新令牌 + 访问令牌
//        OAuth2RefreshTokenDO refreshTokenDO = createOAuth2RefreshToken(createAccessTokenDTO.getUserId(),
//                createAccessTokenDTO.getUserType(), createAccessTokenDTO.getCreateIp());
//        OAuth2AccessTokenDO accessTokenDO = createOAuth2AccessToken(refreshTokenDO, createAccessTokenDTO.getCreateIp());
//        // 返回访问令牌
//        return OAuth2Convert.INSTANCE.convert(accessTokenDO);
//    }
//
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
//
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
//
//    private OAuth2AccessTokenDO createOAuth2AccessToken(OAuth2RefreshTokenDO refreshTokenDO, String createIp) {
//        OAuth2AccessTokenDO accessToken = new OAuth2AccessTokenDO()
//                .setId(generateAccessToken())
//                .setUserId(refreshTokenDO.getUserId()).setUserType(refreshTokenDO.getUserType())
//                .setRefreshToken(refreshTokenDO.getId())
//                .setExpiresTime(new Date(System.currentTimeMillis() + systemBizProperties.getAccessTokenExpireTimeMillis()))
//                .setCreateIp(createIp);
//        oauth2AccessTokenMapper.insert(accessToken);
//        return accessToken;
//    }
//
//    private OAuth2RefreshTokenDO createOAuth2RefreshToken(Integer userId, Integer userType, String createIp) {
//        OAuth2RefreshTokenDO refreshToken = new OAuth2RefreshTokenDO()
//                .setId(generateRefreshToken())
//                .setUserId(userId).setUserType(userType)
//                .setExpiresTime(new Date(System.currentTimeMillis() + systemBizProperties.getRefreshTokenExpireTimeMillis()))
//                .setCreateIp(createIp);
//        oauth2RefreshTokenMapper.insert(refreshToken);
//        return refreshToken;
//    }
//
//    private OAuth2AccessTokenDO getOAuth2AccessToken(String accessToken) {
//        // 优先从 Redis 中获取
//        OAuth2AccessTokenDO accessTokenDO = oauth2AccessTokenRedisDAO.get(accessToken);
//        if (accessTokenDO != null) {
//            return accessTokenDO;
//        }
//
//        // 获取不到，从 MySQL 中获取
//        accessTokenDO = oauth2AccessTokenMapper.selectById(accessToken);
//        // 如果在 MySQL 存在，则往 Redis 中写入
//        if (accessTokenDO != null) {
//            oauth2AccessTokenRedisDAO.set(accessTokenDO);
//        }
//        return accessTokenDO;
//    }
//
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
//    private static String generateAccessToken() {
//        return StringUtils.uuid(true);
//    }
//
//    private static String generateRefreshToken() {
//        return StringUtils.uuid(true);
//    }

}
