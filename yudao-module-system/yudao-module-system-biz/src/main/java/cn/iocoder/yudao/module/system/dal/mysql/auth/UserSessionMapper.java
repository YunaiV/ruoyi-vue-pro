package cn.iocoder.yudao.module.system.dal.mysql.auth;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.UserSessionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface UserSessionMapper extends BaseMapperX<UserSessionDO> {

    default PageResult<UserSessionDO> selectPage(UserSessionPageReqVO reqVO, Collection<Long> userIds) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserSessionDO>()
                .inIfPresent(UserSessionDO::getUserId, userIds)
                .likeIfPresent(UserSessionDO::getUserIp, reqVO.getUserIp()));
    }

    default List<UserSessionDO> selectListBySessionTimoutLt() {
        return selectList(new LambdaQueryWrapperX<UserSessionDO>()
                .lt(UserSessionDO::getSessionTimeout, new Date()));
    }

    default void updateByToken(String token, UserSessionDO updateObj) {
        update(updateObj, new LambdaQueryWrapperX<UserSessionDO>()
                .eq(UserSessionDO::getToken, token));
    }

    default void deleteByToken(String token) {
        delete(new LambdaQueryWrapperX<UserSessionDO>().eq(UserSessionDO::getToken, token));
    }

}
