package cn.iocoder.dashboard.modules.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.infra.controller.file.vo.InfFilePageReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.file.InfFileDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfFileMapper extends BaseMapperX<InfFileDO> {

    default Integer selectCountById(String id) {
        return selectCount("id", id);
    }

    default PageResult<InfFileDO> selectPage(InfFilePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<InfFileDO>()
                .likeIfPresent("id", reqVO.getId())
                .likeIfPresent("type", reqVO.getType())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("create_time"));
    }

}
