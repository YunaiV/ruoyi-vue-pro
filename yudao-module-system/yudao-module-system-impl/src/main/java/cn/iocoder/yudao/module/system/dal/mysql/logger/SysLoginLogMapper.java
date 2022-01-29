package cn.iocoder.yudao.module.system.dal.mysql.logger;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.logger.SysLoginLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.logger.vo.loginlog.SysLoginLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.logger.vo.loginlog.SysLoginLogPageReqVO;
import cn.iocoder.yudao.module.system.enums.logger.SysLoginResultEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysLoginLogMapper extends BaseMapperX<SysLoginLogDO> {

    default PageResult<SysLoginLogDO> selectPage(SysLoginLogPageReqVO reqVO) {
        QueryWrapperX<SysLoginLogDO> query = new QueryWrapperX<SysLoginLogDO>()
                .likeIfPresent("user_ip", reqVO.getUserIp())
                .likeIfPresent("username", reqVO.getUsername())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime());
        if (Boolean.TRUE.equals(reqVO.getStatus())) {
            query.eq("result", SysLoginResultEnum.SUCCESS.getResult());
        } else if (Boolean.FALSE.equals(reqVO.getStatus())) {
            query.gt("result", SysLoginResultEnum.SUCCESS.getResult());
        }
        query.orderByDesc("id"); // 降序
        return selectPage(reqVO, query);
    }

    default List<SysLoginLogDO> selectList(SysLoginLogExportReqVO reqVO) {
        QueryWrapperX<SysLoginLogDO> query = new QueryWrapperX<SysLoginLogDO>()
                .likeIfPresent("user_ip", reqVO.getUserIp())
                .likeIfPresent("username", reqVO.getUsername())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime());
        if (Boolean.TRUE.equals(reqVO.getStatus())) {
            query.eq("result", SysLoginResultEnum.SUCCESS.getResult());
        } else if (Boolean.FALSE.equals(reqVO.getStatus())) {
            query.gt("result", SysLoginResultEnum.SUCCESS.getResult());
        }
        query.orderByDesc("id"); // 降序
        return selectList(query);
    }

}
