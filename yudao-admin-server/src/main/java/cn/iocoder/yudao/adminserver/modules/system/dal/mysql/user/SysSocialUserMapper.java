package cn.iocoder.yudao.adminserver.modules.system.dal.mysql.user;

import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.user.SysSocialUserDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysSocialUserMapper extends BaseMapperX<SysSocialUserDO> {

    default List<SysSocialUserDO> selectListByTypeAndUnionId(Integer userType, Integer type, String unionId) {
        return selectList(new QueryWrapper<SysSocialUserDO>().eq("user_type", userType)
                .eq("type", type).eq("union_id", unionId));
    }

}
