package cn.iocoder.yudao.module.social.dal.mysql.user;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.social.dal.dataobject.user.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * C 端用户 Mapper。
 *
 * 类名特意带 App 前缀，避免与芋道 AdminUserServiceImpl 中按变量名 userMapper 注入的
 * AdminUserMapper 发生 Spring bean 名冲突。
 */
@Mapper
public interface AppUserMapper extends BaseMapperX<UserDO> {

    default UserDO selectByOpenid(String openid) {
        return selectOne(UserDO::getOpenid, openid);
    }

}
