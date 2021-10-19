package cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.file;

import cn.iocoder.yudao.coreservice.modules.infra.file.vo.InfFilePageReqVO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfFileCoreMapper extends BaseMapperX<InfFileDO> {
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
