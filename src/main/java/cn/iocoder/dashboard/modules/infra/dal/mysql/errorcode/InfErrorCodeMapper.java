package cn.iocoder.dashboard.modules.infra.dal.mysql.errorcode;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodePageDTO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.errorcode.InfErrorCodeDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface InfErrorCodeMapper extends BaseMapper<InfErrorCodeDO> {

    default IPage<InfErrorCodeDO> selectPage(ErrorCodePageDTO pageDTO) {
        return selectPage(new Page<>(pageDTO.getPageNo(), pageDTO.getPageSize()),
                new QueryWrapperX<InfErrorCodeDO>().likeIfPresent("`group`", pageDTO.getGroup())
                        .eqIfPresent("code", pageDTO.getCode()).likeIfPresent("message", pageDTO.getMessage()));
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
