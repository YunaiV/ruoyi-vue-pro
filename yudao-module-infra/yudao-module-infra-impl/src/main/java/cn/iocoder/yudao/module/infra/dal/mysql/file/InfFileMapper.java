package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.module.infra.controller.admin.file.vo.InfFilePageReqVO;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

/**
 * admin 文件操作 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfFileMapper extends BaseMapperX<InfFileDO> {
    default PageResult<InfFileDO> selectPage(InfFilePageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<InfFileDO>()
                .likeIfPresent("id", reqVO.getId())
                .likeIfPresent("type", reqVO.getType())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("create_time"));
    }
}
