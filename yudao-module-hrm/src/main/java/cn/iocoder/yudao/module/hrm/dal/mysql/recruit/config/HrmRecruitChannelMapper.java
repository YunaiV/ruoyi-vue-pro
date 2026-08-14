package cn.iocoder.yudao.module.hrm.dal.mysql.recruit.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmRecruitChannelMapper extends BaseMapperX<HrmRecruitChannelDO> {

    default PageResult<HrmRecruitChannelDO> selectPage(HrmRecruitChannelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmRecruitChannelDO>()
                .likeIfPresent(HrmRecruitChannelDO::getName, reqVO.getName())
                .eqIfPresent(HrmRecruitChannelDO::getStatus, reqVO.getStatus())
                .orderByAsc(HrmRecruitChannelDO::getSort)
                .orderByDesc(HrmRecruitChannelDO::getId));
    }

    default List<HrmRecruitChannelDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<HrmRecruitChannelDO>()
                .eq(HrmRecruitChannelDO::getStatus, status)
                .orderByAsc(HrmRecruitChannelDO::getSort)
                .orderByDesc(HrmRecruitChannelDO::getId));
    }

}
