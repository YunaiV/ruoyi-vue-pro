package cn.iocoder.yudao.module.system.dal.mysql.errorcode;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.errorcode.vo.SysErrorCodeExportReqVO;
import cn.iocoder.yudao.module.system.controller.errorcode.vo.SysErrorCodePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.errorcode.SysErrorCodeDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface SysErrorCodeMapper extends BaseMapperX<SysErrorCodeDO> {

    default PageResult<SysErrorCodeDO> selectPage(SysErrorCodePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysErrorCodeDO>()
                .eqIfPresent("type", reqVO.getType())
                .likeIfPresent("application_name", reqVO.getApplicationName())
                .eqIfPresent("code", reqVO.getCode())
                .likeIfPresent("message", reqVO.getMessage())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByAsc("application_name", "code"));
    }

    default List<SysErrorCodeDO> selectList(SysErrorCodeExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysErrorCodeDO>()
                .eqIfPresent("type", reqVO.getType())
                .likeIfPresent("application_name", reqVO.getApplicationName())
                .eqIfPresent("code", reqVO.getCode())
                .likeIfPresent("message", reqVO.getMessage())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByAsc("application_name", "code"));
    }

    default List<SysErrorCodeDO> selectListByCodes(Collection<Integer> codes) {
        return selectList(new QueryWrapper<SysErrorCodeDO>().in("code", codes));
    }

    default SysErrorCodeDO selectByCode(Integer code) {
        return selectOne(new QueryWrapper<SysErrorCodeDO>().eq("code", code));
    }

    default List<SysErrorCodeDO> selectListByApplicationNameAndUpdateTimeGt(String applicationName, Date minUpdateTime) {
        return selectList(new QueryWrapperX<SysErrorCodeDO>().eq("application_name", applicationName)
                .gtIfPresent("update_time", minUpdateTime));
    }

}
