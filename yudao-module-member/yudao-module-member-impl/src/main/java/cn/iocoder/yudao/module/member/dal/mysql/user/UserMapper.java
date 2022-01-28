package cn.iocoder.yudao.module.member.dal.mysql.user;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.member.dal.dataobject.user.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员 User Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface UserMapper extends BaseMapperX<UserDO> {

    default UserDO selectByMobile(String mobile) {
        return selectOne(UserDO::getMobile, mobile);
    }

}
