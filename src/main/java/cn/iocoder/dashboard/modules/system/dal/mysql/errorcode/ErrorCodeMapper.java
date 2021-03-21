package cn.iocoder.dashboard.modules.system.dal.mysql.errorcode;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.errorcode.dto.ErrorCodePageDTO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.errorcode.ErrorCodeDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface ErrorCodeMapper extends BaseMapper<ErrorCodeDO> {
    default IPage<ErrorCodeDO> selectPage(ErrorCodePageDTO pageDTO) {
        return selectPage(new Page<>(pageDTO.getPageNo(), pageDTO.getPageSize()),
                new QueryWrapperX<ErrorCodeDO>().likeIfPresent("`group`", pageDTO.getGroup())
                        .eqIfPresent("code", pageDTO.getCode()).likeIfPresent("message", pageDTO.getMessage()));
    }

    default List<ErrorCodeDO> selectListByCodes(Collection<Integer> codes) {
        return selectList(new QueryWrapper<ErrorCodeDO>().in("code", codes));
    }

    default ErrorCodeDO selectByCode(Integer code) {
        return selectOne(new QueryWrapper<ErrorCodeDO>().eq("code", code));
    }

    default List<ErrorCodeDO> selectListByGroup(String group, Date minUpdateTime) {
        return selectList(new QueryWrapperX<ErrorCodeDO>().eq("`group`", group)
                .gtIfPresent("update_time", minUpdateTime));
    }
}
