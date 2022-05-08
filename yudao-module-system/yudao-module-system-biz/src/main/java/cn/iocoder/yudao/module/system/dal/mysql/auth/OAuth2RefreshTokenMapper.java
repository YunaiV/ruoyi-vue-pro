package cn.iocoder.yudao.module.system.dal.mysql.auth;

import cn.iocoder.yudao.module.system.dal.dataobject.auth.OAuth2RefreshTokenDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OAuth2RefreshTokenMapper extends BaseMapper<OAuth2RefreshTokenDO> {

    default int deleteByUserIdAndUserType(Integer userId, Integer userType) {
        return delete(new QueryWrapper<OAuth2RefreshTokenDO>()
                .eq("user_id", userId).eq("user_type", userType));
    }

}
