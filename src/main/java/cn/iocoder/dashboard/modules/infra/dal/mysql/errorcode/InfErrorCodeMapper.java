package cn.iocoder.dashboard.modules.infra.dal.mysql.errorcode;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodePageReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.errorcode.InfErrorCodeDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface InfErrorCodeMapper extends BaseMapperX<InfErrorCodeDO> {

    default PageResult<InfErrorCodeDO> selectPage(InfErrorCodePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<InfErrorCodeDO>()
                .eqIfPresent("type", reqVO.getType())
                .likeIfPresent("application_name", reqVO.getApplicationName())
                .eqIfPresent("code", reqVO.getCode())
                .likeIfPresent("message", reqVO.getMessage())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByAsc("application_name", "code"));
    }

    default List<InfErrorCodeDO> selectList(InfErrorCodeExportReqVO reqVO) {
        return selectList(new QueryWrapperX<InfErrorCodeDO>()
                .eqIfPresent("type", reqVO.getType())
                .likeIfPresent("application_name", reqVO.getApplicationName())
                .eqIfPresent("code", reqVO.getCode())
                .likeIfPresent("message", reqVO.getMessage())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByAsc("application_name", "code"));
    }

    default List<InfErrorCodeDO> selectListByCodes(Collection<Integer> codes) {
        return selectList(new QueryWrapper<InfErrorCodeDO>().in("code", codes));
    }

    default InfErrorCodeDO selectByCode(Integer code) {
        return selectOne(new QueryWrapper<InfErrorCodeDO>().eq("code", code));
    }

    default List<InfErrorCodeDO> selectListByApplicationNameAndUpdateTimeGt(String applicationName, Date minUpdateTime) {
        return selectList(new QueryWrapperX<InfErrorCodeDO>().eq("application_name", applicationName)
                .gtIfPresent("update_time", minUpdateTime));
    }

}
