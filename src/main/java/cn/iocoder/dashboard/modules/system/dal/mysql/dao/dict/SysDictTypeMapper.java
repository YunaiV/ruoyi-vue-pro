package cn.iocoder.dashboard.modules.system.dal.mysql.dao.dict;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypePageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictTypeDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDictTypeMapper extends BaseMapperX<SysDictTypeDO> {

    default PageResult<SysDictTypeDO> selectPage(SysDictTypePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysDictTypeDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("dict_type", reqVO.getType())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

    default List<SysDictTypeDO> selectList(SysDictTypeExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysDictTypeDO>().likeIfPresent("name", reqVO.getName())
                        .likeIfPresent("dict_type", reqVO.getType())
                        .eqIfPresent("status", reqVO.getStatus())
                        .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

    default SysDictTypeDO selectByType(String type) {
        return selectOne(new QueryWrapperX<SysDictTypeDO>().eq("dict_type", type));
    }

    default SysDictTypeDO selectByName(String name) {
        return selectOne(new QueryWrapperX<SysDictTypeDO>().eq("name", name));
    }

    default List<SysDictTypeDO> selectList() {
        return selectList(new QueryWrapper<>());
    }

}
