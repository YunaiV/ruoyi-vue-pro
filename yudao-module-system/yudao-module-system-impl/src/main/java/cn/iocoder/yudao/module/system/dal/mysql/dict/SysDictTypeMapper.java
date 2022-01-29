package cn.iocoder.yudao.module.system.dal.mysql.dict;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.type.DictTypeExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.dict.vo.type.DictTypePageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictTypeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDictTypeMapper extends BaseMapperX<DictTypeDO> {

    default PageResult<DictTypeDO> selectPage(DictTypePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<DictTypeDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("`type`", reqVO.getType())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime()));
    }

    default List<DictTypeDO> selectList(DictTypeExportReqVO reqVO) {
        return selectList(new QueryWrapperX<DictTypeDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("`type`", reqVO.getType())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime()));
    }

    default DictTypeDO selectByType(String type) {
        return selectOne(new QueryWrapperX<DictTypeDO>().eq("`type`", type));
    }

    default DictTypeDO selectByName(String name) {
        return selectOne(new QueryWrapperX<DictTypeDO>().eq("name", name));
    }

}
