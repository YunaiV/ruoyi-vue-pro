package cn.iocoder.yudao.module.system.dal.mysql.auth;

import cn.iocoder.yudao.module.system.controller.admin.auth.vo.session.UserSessionPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.auth.UserSessionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface UserSessionMapper extends BaseMapperX<UserSessionDO> {

    default PageResult<UserSessionDO> selectPage(UserSessionPageReqVO reqVO, Collection<Long> userIds) {
        return selectPage(reqVO, new QueryWrapperX<UserSessionDO>()
                .inIfPresent("user_id", userIds)
                .likeIfPresent("user_ip", reqVO.getUserIp()));
    }

    default List<UserSessionDO> selectListBySessionTimoutLt() {
        return selectList(new QueryWrapperX<UserSessionDO>().lt("session_timeout",new Date()));
    }

}
