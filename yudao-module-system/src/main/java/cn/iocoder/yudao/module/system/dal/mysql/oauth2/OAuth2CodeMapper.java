package cn.iocoder.yudao.module.system.dal.mysql.oauth2;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2CodeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface OAuth2CodeMapper extends BaseMapperX<OAuth2CodeDO> {

    default OAuth2CodeDO selectByCode(String code) {
        return selectOne(OAuth2CodeDO::getCode, code);
    }

    default List<OAuth2CodeDO> selectListByUserIdsAndUserType(Collection<Long> userIds, Integer userType) {
        return selectList(new LambdaQueryWrapperX<OAuth2CodeDO>()
                .select(OAuth2CodeDO::getId)
                .in(OAuth2CodeDO::getUserId, userIds)
                .eq(OAuth2CodeDO::getUserType, userType));
    }

}
