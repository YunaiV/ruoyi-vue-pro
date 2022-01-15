package cn.iocoder.yudao.adminserver.modules.bpm.dal.mysql.definition;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.definition.vo.group.BpmUserGroupPageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.definition.BpmUserGroupDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户组 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface BpmUserGroupMapper extends BaseMapperX<BpmUserGroupDO> {

    default PageResult<BpmUserGroupDO> selectPage(BpmUserGroupPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<BpmUserGroupDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default List<BpmUserGroupDO> selectListByStatus(Integer status) {
        return selectList("status", status);
    }

}
