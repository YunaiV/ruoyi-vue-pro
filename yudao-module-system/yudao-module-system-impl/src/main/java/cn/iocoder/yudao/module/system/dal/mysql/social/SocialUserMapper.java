package cn.iocoder.yudao.module.system.dal.mysql.social;

import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SocialUserMapper extends BaseMapperX<SocialUserDO> {

    default List<SocialUserDO> selectListByTypeAndUnionId(Integer userType, Collection<Integer> types, String unionId) {
        return selectList(new QueryWrapper<SocialUserDO>().eq("user_type", userType)
                .in("type", types).eq("union_id", unionId));
    }

    default List<SocialUserDO> selectListByTypeAndUserId(Integer userType, Collection<Integer> types, Long userId) {
        return selectList(new QueryWrapper<SocialUserDO>().eq("user_type", userType)
                .in("type", types).eq("user_id", userId));
    }

    default List<SocialUserDO> selectListByUserId(Integer userType, Long userId) {
        return selectList(new QueryWrapper<SocialUserDO>().eq("user_type", userType).eq("user_id", userId));
    }

}
