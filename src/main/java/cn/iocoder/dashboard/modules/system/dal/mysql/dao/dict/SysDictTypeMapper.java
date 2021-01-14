package cn.iocoder.dashboard.modules.system.dal.mysql.dao.dict;

import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.framework.mybatis.core.util.MyBatisUtils;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypeExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.dict.vo.type.SysDictTypePageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dict.SysDictTypeDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictTypeDO> {

    default IPage<SysDictTypeDO> selectList(SysDictTypePageReqVO reqVO) {
        return selectPage(MyBatisUtils.buildPage(reqVO),
                new QueryWrapperX<SysDictTypeDO>().likeIfPresent("name", reqVO.getName())
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
