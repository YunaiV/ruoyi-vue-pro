package cn.iocoder.yudao.module.system.dal.mysql.logger;

import cn.iocoder.yudao.module.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog.LoginLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.loginlog.LoginLogPageReqVO;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LoginLogMapper extends BaseMapperX<SysLoginLogDO> {

    default PageResult<SysLoginLogDO> selectPage(LoginLogPageReqVO reqVO) {
        QueryWrapperX<SysLoginLogDO> query = new QueryWrapperX<SysLoginLogDO>()
                .likeIfPresent("user_ip", reqVO.getUserIp())
                .likeIfPresent("username", reqVO.getUsername())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime());
        if (Boolean.TRUE.equals(reqVO.getStatus())) {
            query.eq("result", LoginResultEnum.SUCCESS.getResult());
        } else if (Boolean.FALSE.equals(reqVO.getStatus())) {
            query.gt("result", LoginResultEnum.SUCCESS.getResult());
        }
        query.orderByDesc("id"); // 降序
        return selectPage(reqVO, query);
    }

    default List<SysLoginLogDO> selectList(LoginLogExportReqVO reqVO) {
        QueryWrapperX<SysLoginLogDO> query = new QueryWrapperX<SysLoginLogDO>()
                .likeIfPresent("user_ip", reqVO.getUserIp())
                .likeIfPresent("username", reqVO.getUsername())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime());
        if (Boolean.TRUE.equals(reqVO.getStatus())) {
            query.eq("result", LoginResultEnum.SUCCESS.getResult());
        } else if (Boolean.FALSE.equals(reqVO.getStatus())) {
            query.gt("result", LoginResultEnum.SUCCESS.getResult());
        }
        query.orderByDesc("id"); // 降序
        return selectList(query);
    }

}
