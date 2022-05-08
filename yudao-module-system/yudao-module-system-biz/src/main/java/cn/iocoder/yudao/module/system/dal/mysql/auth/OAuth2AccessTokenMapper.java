package cn.iocoder.yudao.module.system.dal.mysql.auth;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2AccessTokenDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2AccessTokenMapper extends BaseMapperX<OAuth2AccessTokenDO> {

    default OAuth2AccessTokenDO selectByAccessToken(String accessToken) {
        return selectOne(OAuth2AccessTokenDO::getAccessToken, accessToken);
    }

//    default OAuth2AccessTokenDO selectByUserIdAndUserType(Integer userId, Integer userType) {
//        return selectOne(new QueryWrapper<OAuth2AccessTokenDO>()
//                .eq("user_id", userId).eq("user_type", userType));
//    }
//
//    default int deleteByUserIdAndUserType(Integer userId, Integer userType) {
//        return delete(new QueryWrapper<OAuth2AccessTokenDO>()
//                .eq("user_id", userId).eq("user_type", userType));
//    }
//
//    default int deleteByRefreshToken(String refreshToken) {
//        return delete(new QueryWrapper<OAuth2AccessTokenDO>().eq("refresh_token", refreshToken));
//    }
//
//    default List<OAuth2AccessTokenDO> selectListByRefreshToken(String refreshToken) {
//        return selectList(new QueryWrapper<OAuth2AccessTokenDO>().eq("refresh_token", refreshToken));
//    }

}
